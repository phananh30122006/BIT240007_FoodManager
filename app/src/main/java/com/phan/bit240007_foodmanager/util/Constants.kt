package com.phan.bit240007_foodmanager.util

object Constants {
    const val MSSV = "BIT240007"
    const val STUDENT_NAME = "Nguyễn Phan Anh"
    const val PROJECT_NAME = "BIT240007_FoodManager"

    // Database tables & columns with mandatory prefix {MSSV}_
    const val DB_NAME = "BIT240007_FoodManager.db"
    const val DB_VERSION = 1

    // Category table
    const val TABLE_CATEGORY = "BIT240007_DishCategory"
    const val COL_CAT_ID = "BIT240007_Id"
    const val COL_CAT_NAME = "BIT240007_Name"
    const val COL_CAT_DESC = "BIT240007_Description"

    // Dish table
    const val TABLE_DISH = "BIT240007_Dish"
    const val COL_DISH_ID = "BIT240007_Id"
    const val COL_DISH_NAME = "BIT240007_Name"
    const val COL_DISH_PRICE = "BIT240007_Price"
    const val COL_DISH_PREP_TIME = "BIT240007_PreparationTime"
    const val COL_DISH_IS_AVAILABLE = "BIT240007_IsAvailable"
    const val COL_DISH_DESCRIPTION = "BIT240007_Description"
    const val COL_DISH_CATEGORY_ID = "BIT240007_DishCategoryId"
    const val COL_DISH_IMAGES = "BIT240007_DishImages"
    const val COL_DISH_SYNC_STATUS = "BIT240007_SyncStatus"
    const val COL_DISH_UPDATED_AT = "BIT240007_UpdatedAt"

    // Sync statuses
    const val SYNC_STATUS_SYNCED = "SYNCED"
    const val SYNC_STATUS_PENDING_CREATE = "PENDING_CREATE"
    const val SYNC_STATUS_PENDING_UPDATE = "PENDING_UPDATE"
    const val SYNC_STATUS_PENDING_DELETE = "PENDING_DELETE"

    // Default MockAPI Endpoint (can be configured in app settings)
    const val DEFAULT_MOCKAPI_URL = "https://675ef7a81f7ad2426997a0a2.mockapi.io/api/v1/"
    
    // Preferences
    const val PREFS_NAME = "BIT240007_Prefs"
    const val PREF_KEY_LANG = "key_language"
    const val PREF_KEY_MOCKAPI = "key_mockapi_url"
}
