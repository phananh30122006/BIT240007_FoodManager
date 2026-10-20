package com.phan.bit240007_foodmanager.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import com.phan.bit240007_foodmanager.data.local.FoodDatabaseHelper
import com.phan.bit240007_foodmanager.data.remote.MockApiService
import com.phan.bit240007_foodmanager.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

class SyncManager private constructor(private val context: Context) {
    val dbHelper = FoodDatabaseHelper(context)
    val apiService = MockApiService(context)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var isSyncing = false
    var isConnected = false
        private set

    private val listeners = mutableListOf<SyncListener>()

    interface SyncListener {
        fun onNetworkStatusChanged(isOnline: Boolean)
        fun onSyncStateChanged(state: SyncState, message: String? = null)
    }

    companion object {
        @Volatile
        private var instance: SyncManager? = null

        fun getInstance(context: Context): SyncManager {
            return instance ?: synchronized(this) {
                instance ?: SyncManager(context.applicationContext).also {
                    instance = it
                    it.initNetworkCallback()
                }
            }
        }
    }

    private fun initNetworkCallback() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                isConnected = true
                mainHandler.post {
                    notifyNetworkChanged(true)
                }
                triggerAutoSync()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                isConnected = false
                mainHandler.post {
                    notifyNetworkChanged(false)
                }
            }
        })
    }

    fun addListener(listener: SyncListener) {
        listeners.add(listener)
        listener.onNetworkStatusChanged(isConnected)
    }

    fun removeListener(listener: SyncListener) {
        listeners.remove(listener)
    }

    private fun notifyNetworkChanged(isOnline: Boolean) {
        listeners.forEach { it.onNetworkStatusChanged(isOnline) }
    }

    private fun notifySyncState(state: SyncState, message: String? = null) {
        mainHandler.post {
            listeners.forEach { it.onSyncStateChanged(state, message) }
        }
    }

    /**
     * Requirement:
     * - Automatic sync when internet becomes available
     * - Consistency preserved, no duplicates
     * - Handles offline created or edited dishes uploaded to MockAPI
     */
    fun triggerAutoSync(onComplete: ((Boolean, String?) -> Unit)? = null) {
        if (isSyncing) {
            onComplete?.let { mainHandler.post { it(false, "Đang trong quá trình đồng bộ") } }
            return
        }

        coroutineScope.launch {
            isSyncing = true
            notifySyncState(SyncState.SYNCING)

            try {
                val pendingDishes = dbHelper.getPendingSyncDishes()
                var lastError: String? = null
                var syncedCount = 0

                for (dish in pendingDishes) {
                    val result = when (dish.syncStatus) {
                        Constants.SYNC_STATUS_PENDING_CREATE -> apiService.createRemoteDish(dish)
                        Constants.SYNC_STATUS_PENDING_UPDATE -> apiService.updateRemoteDish(dish)
                        else -> Result.success(dish)
                    }

                    if (result.isSuccess) {
                        dbHelper.markDishSynced(dish.id)
                        syncedCount++
                    } else {
                        lastError = result.exceptionOrNull()?.message ?: "Lỗi kết nối MockAPI"
                    }
                }

                if (lastError != null) {
                    notifySyncState(SyncState.ERROR, lastError)
                    withContext(Dispatchers.Main) { onComplete?.invoke(false, lastError) }
                } else {
                    val successMsg = if (syncedCount > 0) "Đã đồng bộ $syncedCount món ăn" else "Đã đồng bộ"
                    notifySyncState(SyncState.SUCCESS, successMsg)
                    withContext(Dispatchers.Main) { onComplete?.invoke(true, successMsg) }
                }
            } catch (e: Exception) {
                notifySyncState(SyncState.ERROR, e.localizedMessage)
                withContext(Dispatchers.Main) { onComplete?.invoke(false, e.localizedMessage) }
            } finally {
                isSyncing = false
            }
        }
    }
}
