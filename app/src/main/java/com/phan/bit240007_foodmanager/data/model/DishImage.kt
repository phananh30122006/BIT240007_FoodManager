package com.phan.bit240007_foodmanager.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data Model for Dish Image
 * Each dish can have multiple images, with at most 1 marked as thumbnail
 */
data class DishImage(
    @SerializedName("BIT240007_ImageUrl")
    var imageUrl: String = "",

    @SerializedName("BIT240007_IsThumbnail")
    var isThumbnail: Boolean = false
)
