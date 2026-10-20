package com.phan.bit240007_foodmanager.data.model

import com.google.gson.annotations.SerializedName
import com.phan.bit240007_foodmanager.util.Constants

/**
 * Data Model for Dish
 * Mandatory naming prefix {MSSV}_BIT240007_
 */
data class Dish(
    @SerializedName("BIT240007_Id")
    var id: String = "",

    @SerializedName("BIT240007_Name")
    var name: String = "",

    @SerializedName("BIT240007_Price")
    var price: Double = 0.0,

    @SerializedName("BIT240007_PreparationTime")
    var preparationTime: Int = 0,

    @SerializedName("BIT240007_IsAvailable")
    var isAvailable: Boolean = true,

    @SerializedName("BIT240007_Description")
    var description: String = "",

    @SerializedName("BIT240007_DishCategoryId")
    var dishCategoryId: String = "",

    @SerializedName("BIT240007_DishImages")
    var images: MutableList<DishImage> = mutableListOf(),

    // Extra fields for offline & sync support
    @SerializedName("BIT240007_SyncStatus")
    var syncStatus: String = Constants.SYNC_STATUS_SYNCED,

    @SerializedName("BIT240007_UpdatedAt")
    var updatedAt: Long = System.currentTimeMillis()
) {
    // Quick / Regular classification based on <= 15 minutes
    val isQuick: Boolean
        get() = preparationTime <= 15

    // Get thumbnail URL or fallback to first image
    val thumbnailUrl: String
        get() {
            val thumb = images.firstOrNull { it.isThumbnail }
            return thumb?.imageUrl ?: images.firstOrNull()?.imageUrl ?: ""
        }
}
