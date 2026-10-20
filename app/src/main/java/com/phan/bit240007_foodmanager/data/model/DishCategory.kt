package com.phan.bit240007_foodmanager.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data Model for Dish Category
 * Adheres to BIT240007 database column naming standards
 */
data class DishCategory(
    @SerializedName("BIT240007_Id")
    var id: String = "",

    @SerializedName("BIT240007_Name")
    var name: String = "",

    @SerializedName("BIT240007_Description")
    var description: String = ""
) {
    override fun toString(): String {
        return name
    }
}
