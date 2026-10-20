package com.phan.bit240007_foodmanager.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.phan.bit240007_foodmanager.R
import com.phan.bit240007_foodmanager.data.local.FoodDatabaseHelper
import com.phan.bit240007_foodmanager.data.model.Dish
import com.phan.bit240007_foodmanager.data.model.DishCategory
import com.phan.bit240007_foodmanager.data.sync.SyncManager
import com.phan.bit240007_foodmanager.data.sync.SyncState
import com.phan.bit240007_foodmanager.databinding.ActivityMainBinding
import com.phan.bit240007_foodmanager.ui.adapter.DishAdapter
import com.phan.bit240007_foodmanager.util.Constants
import com.phan.bit240007_foodmanager.util.LocaleHelper

class MainActivity : BaseActivity(), SyncManager.SyncListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: FoodDatabaseHelper
    private lateinit var syncManager: SyncManager
    private lateinit var dishAdapter: DishAdapter

    private var categories = listOf<DishCategory>()
    private var categoriesMap = mutableMapOf<String, DishCategory>()

    // State preservation for search/filter/sort (Req 4.2: Giữ các điều kiện đã chọn khi quay về)
    private var currentKeyword = ""
    private var currentCategoryId: String? = null
    private var currentOnlyAvailable = false
    private var currentMinPrice: Double? = null
    private var currentMaxPrice: Double? = null
    private var currentSortOrder: String = "DEFAULT"

    private val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Reload categories & dishes after returning from Add/Edit/Detail/Categories
        loadCategories()
        loadDishes()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = FoodDatabaseHelper(this)
        syncManager = SyncManager.getInstance(this)

        setupToolbarAndLanguage()
        setupSyncBanner()
        setupRecyclerView()
        setupFiltersAndSearch()
        setupFab()

        loadCategories()
        loadDishes()
    }

    override fun onStart() {
        super.onStart()
        syncManager.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        syncManager.removeListener(this)
    }

    override fun onResume() {
        super.onResume()
        // Ensure filters and data remain consistent when returning
        loadCategories()
        loadDishes()
    }

    private fun setupToolbarAndLanguage() {
        // Multi-language Toggle (Req 4.5)
        val currentLang = LocaleHelper.getLanguage(this)
        binding.btnLanguageToggle.text = if (currentLang == LocaleHelper.LANG_VI) "English" else "Tiếng Việt"

        binding.btnLanguageToggle.setOnClickListener {
            val newLang = if (currentLang == LocaleHelper.LANG_VI) LocaleHelper.LANG_EN else LocaleHelper.LANG_VI
            LocaleHelper.setLocale(this, newLang)
            // Recreate activity to apply language changes across UI
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }

        binding.btnNavCategories.setOnClickListener {
            val intent = Intent(this, CategoryManageActivity::class.java)
            activityLauncher.launch(intent)
        }
    }

    private fun setupSyncBanner() {
        binding.tvConfigureMockApi.setOnClickListener {
            showMockApiConfigDialog()
        }
    }

    private fun setupRecyclerView() {
        dishAdapter = DishAdapter(
            context = this,
            categoriesMap = categoriesMap,
            onItemClick = { dish ->
                val intent = Intent(this, DishDetailActivity::class.java).apply {
                    putExtra(DishDetailActivity.EXTRA_DISH_ID, dish.id)
                }
                activityLauncher.launch(intent)
            },
            onEditClick = { dish ->
                val intent = Intent(this, AddEditDishActivity::class.java).apply {
                    putExtra(AddEditDishActivity.EXTRA_DISH_ID, dish.id)
                }
                activityLauncher.launch(intent)
            },
            onDeleteClick = { dish ->
                confirmDeleteDish(dish)
            }
        )
        binding.rvDishes.layoutManager = LinearLayoutManager(this)
        binding.rvDishes.adapter = dishAdapter
    }

    private fun setupFiltersAndSearch() {
        // Search text watcher
        binding.etSearchDish.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentKeyword = s?.toString()?.trim() ?: ""
                loadDishes()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Switch only available
        binding.switchOnlyAvailable.setOnCheckedChangeListener { _, isChecked ->
            currentOnlyAvailable = isChecked
            loadDishes()
        }

        // Price range inputs
        val priceWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentMinPrice = binding.etMinPrice.text?.toString()?.toDoubleOrNull()
                currentMaxPrice = binding.etMaxPrice.text?.toString()?.toDoubleOrNull()

                // Validate range: min <= max
                if (currentMinPrice != null && currentMaxPrice != null && currentMinPrice!! > currentMaxPrice!!) {
                    Toast.makeText(this@MainActivity, getString(R.string.err_price_range_invalid), Toast.LENGTH_SHORT).show()
                } else {
                    loadDishes()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etMinPrice.addTextChangedListener(priceWatcher)
        binding.etMaxPrice.addTextChangedListener(priceWatcher)

        // Sort spinner
        val sortOptions = listOf(
            getString(R.string.sort_default),
            getString(R.string.sort_price_asc),
            getString(R.string.sort_price_desc),
            getString(R.string.sort_prep_asc)
        )
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sortOptions)
        binding.spSortOrder.adapter = sortAdapter

        binding.spSortOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSortOrder = when (position) {
                    1 -> "PRICE_ASC"
                    2 -> "PRICE_DESC"
                    3 -> "PREP_TIME_ASC"
                    else -> "DEFAULT"
                }
                loadDishes()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Reset filter
        binding.btnResetFilter.setOnClickListener {
            binding.etSearchDish.text?.clear()
            binding.spCategoryFilter.setSelection(0)
            binding.switchOnlyAvailable.isChecked = false
            binding.etMinPrice.text?.clear()
            binding.etMaxPrice.text?.clear()
            binding.spSortOrder.setSelection(0)
            currentKeyword = ""
            currentCategoryId = null
            currentOnlyAvailable = false
            currentMinPrice = null
            currentMaxPrice = null
            currentSortOrder = "DEFAULT"
            loadDishes()
        }
    }

    private fun setupFab() {
        binding.fabAddDish.setOnClickListener {
            val intent = Intent(this, AddEditDishActivity::class.java)
            activityLauncher.launch(intent)
        }
    }

    private fun loadCategories() {
        categories = dbHelper.getAllCategories()
        categoriesMap.clear()
        categories.forEach { categoriesMap[it.id] = it }

        val categorySpinnerList = mutableListOf(getString(R.string.filter_all_categories))
        categorySpinnerList.addAll(categories.map { it.name })

        val catAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorySpinnerList)
        binding.spCategoryFilter.adapter = catAdapter

        // Re-apply selected category if state was kept
        val prevSelectionIndex = if (currentCategoryId != null) {
            val idx = categories.indexOfFirst { it.id == currentCategoryId }
            if (idx >= 0) idx + 1 else 0
        } else 0
        binding.spCategoryFilter.setSelection(prevSelectionIndex)

        binding.spCategoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentCategoryId = if (position == 0) null else categories[position - 1].id
                loadDishes()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadDishes() {
        val dishes = dbHelper.searchAndFilterDishes(
            keyword = currentKeyword,
            categoryId = currentCategoryId,
            onlyAvailable = currentOnlyAvailable,
            minPrice = currentMinPrice,
            maxPrice = currentMaxPrice,
            sortOrder = currentSortOrder
        )

        dishAdapter.submitList(dishes)
        binding.tvEmptyDishes.visibility = if (dishes.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun confirmDeleteDish(dish: Dish) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_delete_title))
            .setMessage(getString(R.string.confirm_delete_dish_msg, dish.name))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                val deleted = dbHelper.deleteDish(dish.id)
                if (deleted) {
                    Toast.makeText(this, getString(R.string.msg_dish_deleted), Toast.LENGTH_SHORT).show()
                    loadDishes()
                }
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun showMockApiConfigDialog() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val currentUrl = prefs.getString(Constants.PREF_KEY_MOCKAPI, Constants.DEFAULT_MOCKAPI_URL)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val tvInfo = android.widget.TextView(this).apply {
            text = "Nhập URL MockAPI (Project URL hoặc Resource URL):\nVí dụ: https://67xxx.mockapi.io/api/v1"
            textSize = 13f
        }
        layout.addView(tvInfo)

        val input = android.widget.EditText(this).apply {
            setText(currentUrl)
            setSelection(text.length)
            hint = "https://xxxx.mockapi.io/api/v1/"
        }
        layout.addView(input)

        val tvEndpointPreview = android.widget.TextView(this).apply {
            text = "Endpoint đích: ${syncManager.apiService.getResourceUrl()}"
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_secondary))
            setPadding(0, 10, 0, 10)
        }
        layout.addView(tvEndpointPreview)

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val temp = s?.toString()?.trim() ?: ""
                var clean = temp
                while (clean.endsWith("/")) clean = clean.substring(0, clean.length - 1)
                val target = if (clean.lowercase().contains("dish")) clean else "$clean/${Constants.TABLE_DISH}"
                tvEndpointPreview.text = "Endpoint đích: $target"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.mockapi_settings))
            .setView(layout)
            .setPositiveButton(getString(R.string.btn_save)) { _, _ ->
                val newUrl = input.text.toString().trim()
                if (newUrl.isNotBlank()) {
                    prefs.edit().putString(Constants.PREF_KEY_MOCKAPI, newUrl).apply()
                    Toast.makeText(this, "Đã lưu MockAPI URL", Toast.LENGTH_SHORT).show()
                    testAndSync(newUrl)
                }
            }
            .setNeutralButton("Kiểm tra (Test)") { _, _ ->
                val newUrl = input.text.toString().trim()
                if (newUrl.isNotBlank()) {
                    prefs.edit().putString(Constants.PREF_KEY_MOCKAPI, newUrl).apply()
                }
                testConnectionOnly()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun testConnectionOnly() {
        lifecycleScope.launch {
            binding.tvSyncStatus.text = "Đang kiểm tra kết nối MockAPI..."
            val result = syncManager.apiService.testConnection()
            if (result.isSuccess) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Kết Nối Thành Công! (OK)")
                    .setMessage("Đã kết nối thành công tới MockAPI:\n${syncManager.apiService.getResourceUrl()}\n\nBạn có muốn tải toàn bộ món ăn hiện có lên MockAPI ngay không?")
                    .setPositiveButton("Tải lên tất cả (Upload All)") { _, _ ->
                        uploadAllDishesToMockApi()
                    }
                    .setNegativeButton("Đóng", null)
                    .show()
                binding.tvSyncStatus.text = "${getString(R.string.status_online)} - Đã kết nối MockAPI"
            } else {
                val err = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Không Thể Kết Nối MockAPI")
                    .setMessage("Chi tiết lỗi:\n$err\n\nCách khắc phục:\n1. Kiểm tra bạn đã tạo Resource '${Constants.TABLE_DISH}' trên web mockapi.io chưa.\n2. Kiểm tra thiết bị có mạng Internet chưa.\n3. Đảm bảo đường link có dạng: https://xxxx.mockapi.io/api/v1")
                    .setPositiveButton("Đã hiểu", null)
                    .show()
                binding.tvSyncStatus.text = "Lỗi MockAPI: $err"
            }
        }
    }

    private fun testAndSync(url: String) {
        lifecycleScope.launch {
            val result = syncManager.apiService.testConnection()
            if (result.isSuccess) {
                syncManager.triggerAutoSync { success, msg ->
                    loadDishes()
                    Toast.makeText(this@MainActivity, msg ?: "Đã đồng bộ", Toast.LENGTH_SHORT).show()
                }
            } else {
                testConnectionOnly()
            }
        }
    }

    private fun uploadAllDishesToMockApi() {
        lifecycleScope.launch {
            binding.tvSyncStatus.text = "Đang tải dữ liệu lên MockAPI..."
            val allDishes = dbHelper.getAllDishes()
            var count = 0
            for (dish in allDishes) {
                val res = syncManager.apiService.createRemoteDish(dish)
                if (res.isSuccess) {
                    dbHelper.markDishSynced(dish.id)
                    count++
                }
            }
            loadDishes()
            Toast.makeText(this@MainActivity, "Đã tải lên $count món ăn lên MockAPI!", Toast.LENGTH_LONG).show()
            binding.tvSyncStatus.text = "${getString(R.string.status_online)} - Đã đồng bộ $count món"
        }
    }

    // ==========================================
    // SYNC LISTENER CALLBACKS (Req 5)
    // ==========================================

    override fun onNetworkStatusChanged(isOnline: Boolean) {
        runOnUiThread {
            if (isOnline) {
                binding.layoutSyncStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_online))
                binding.tvSyncStatus.text = getString(R.string.status_online)
            } else {
                binding.layoutSyncStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_offline))
                binding.tvSyncStatus.text = getString(R.string.status_offline)
            }
        }
    }

    override fun onSyncStateChanged(state: SyncState, message: String?) {
        runOnUiThread {
            when (state) {
                SyncState.SYNCING -> {
                    binding.tvSyncStatus.text = getString(R.string.status_syncing)
                }
                SyncState.SUCCESS -> {
                    binding.tvSyncStatus.text = "${getString(R.string.status_online)} - ${getString(R.string.status_synced)}"
                    loadDishes()
                }
                SyncState.ERROR -> {
                    binding.tvSyncStatus.text = "${getString(R.string.status_online)} (${message ?: "Sync error"})"
                }
                SyncState.IDLE -> {}
            }
        }
    }
}
