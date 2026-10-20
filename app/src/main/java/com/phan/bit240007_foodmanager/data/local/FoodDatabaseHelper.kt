package com.phan.bit240007_foodmanager.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.phan.bit240007_foodmanager.data.model.Dish
import com.phan.bit240007_foodmanager.data.model.DishCategory
import com.phan.bit240007_foodmanager.data.model.DishImage
import com.phan.bit240007_foodmanager.util.Constants
import java.util.UUID

class FoodDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    Constants.DB_NAME,
    null,
    Constants.DB_VERSION
) {
    private val gson = Gson()

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Category Table with prefix BIT240007_
        val createCategoryTable = """
            CREATE TABLE ${Constants.TABLE_CATEGORY} (
                ${Constants.COL_CAT_ID} TEXT PRIMARY KEY,
                ${Constants.COL_CAT_NAME} TEXT NOT NULL,
                ${Constants.COL_CAT_DESC} TEXT
            );
        """.trimIndent()
        db.execSQL(createCategoryTable)

        // Create Dish Table with prefix BIT240007_
        val createDishTable = """
            CREATE TABLE ${Constants.TABLE_DISH} (
                ${Constants.COL_DISH_ID} TEXT PRIMARY KEY,
                ${Constants.COL_DISH_NAME} TEXT NOT NULL,
                ${Constants.COL_DISH_PRICE} REAL NOT NULL,
                ${Constants.COL_DISH_PREP_TIME} INTEGER NOT NULL,
                ${Constants.COL_DISH_IS_AVAILABLE} INTEGER NOT NULL,
                ${Constants.COL_DISH_DESCRIPTION} TEXT,
                ${Constants.COL_DISH_CATEGORY_ID} TEXT NOT NULL,
                ${Constants.COL_DISH_IMAGES} TEXT NOT NULL,
                ${Constants.COL_DISH_SYNC_STATUS} TEXT NOT NULL,
                ${Constants.COL_DISH_UPDATED_AT} INTEGER NOT NULL,
                FOREIGN KEY (${Constants.COL_DISH_CATEGORY_ID}) REFERENCES ${Constants.TABLE_CATEGORY}(${Constants.COL_CAT_ID})
            );
        """.trimIndent()
        db.execSQL(createDishTable)

        // Seed initial data (>= 3 categories, >= 5 dishes)
        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${Constants.TABLE_DISH}")
        db.execSQL("DROP TABLE IF EXISTS ${Constants.TABLE_CATEGORY}")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        // 1. Categories
        val cat1 = DishCategory("cat_appetizer", "Khai vị (Appetizers)", "Các món khai vị nhẹ nhàng, kích thích vị giác")
        val cat2 = DishCategory("cat_main", "Món chính (Main Courses)", "Các món ăn chính đậm đà hương vị truyền thống")
        val cat3 = DishCategory("cat_dessert", "Tráng miệng & Đồ uống (Desserts & Drinks)", "Đồ uống và món tráng miệng ngọt ngào")

        insertCategoryInternal(db, cat1)
        insertCategoryInternal(db, cat2)
        insertCategoryInternal(db, cat3)

        // 2. Dishes
        // Dish 1: Phở Bò Hà Nội (Quick: 12 mins <= 15)
        val dish1 = Dish(
            id = "dish_pho_bo",
            name = "Phở Bò Tái Lăn Hà Nội",
            price = 55000.0,
            preparationTime = 12,
            isAvailable = true,
            description = "Bánh phở tươi mềm, thịt bò xào lăn thơm lừng cùng nước dùng ninh xương đậm đà thảo quả quế hồi.",
            dishCategoryId = cat2.id,
            images = mutableListOf(
                DishImage("https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=600", true),
                DishImage("https://images.unsplash.com/photo-1594041680534-e8c8cdebd659?w=600", false)
            ),
            syncStatus = Constants.SYNC_STATUS_SYNCED
        )

        // Dish 2: Bún Chả Hà Nội (Regular: 20 mins > 15)
        val dish2 = Dish(
            id = "dish_bun_cha",
            name = "Bún Chả Nem Hà Nội",
            price = 60000.0,
            preparationTime = 20,
            isAvailable = true,
            description = "Thịt ba chỉ nướng than hoa thơm nức mũi, chả viên nướng mềm ngọt ăn kèm bún sợi và nước mắm chua ngọt.",
            dishCategoryId = cat2.id,
            images = mutableListOf(
                DishImage("https://images.unsplash.com/photo-1626804475297-41608ea09aeb?w=600", true),
                DishImage("https://images.unsplash.com/photo-1559847844-5315695dadae?w=600", false)
            ),
            syncStatus = Constants.SYNC_STATUS_SYNCED
        )

        // Dish 3: Gỏi Cuốn Tôm Thịt (Quick: 10 mins <= 15)
        val dish3 = Dish(
            id = "dish_goi_cuon",
            name = "Gỏi Cuốn Tôm Thịt",
            price = 35000.0,
            preparationTime = 10,
            isAvailable = true,
            description = "Tôm luộc tươi, thịt ba chỉ, bún tươi và rau thơm cuộn trong bánh tráng, chấm tương đen đậu phộng béo bùi.",
            dishCategoryId = cat1.id,
            images = mutableListOf(
                DishImage("https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?w=600", true),
                DishImage("https://images.unsplash.com/photo-1544025162-d76694265947?w=600", false)
            ),
            syncStatus = Constants.SYNC_STATUS_SYNCED
        )

        // Dish 4: Nem Rán Truyền Thống (Regular: 25 mins > 15, isAvailable = false to test available filter)
        val dish4 = Dish(
            id = "dish_nem_ran",
            name = "Nem Rán Truyền Thống (Chả Giò)",
            price = 45000.0,
            preparationTime = 25,
            isAvailable = false,
            description = "Nem rán vỏ giòn rụm, nhân thịt nạc vai, mộc nhĩ, miến dong thơm ngon chuẩn vị gia truyền.",
            dishCategoryId = cat1.id,
            images = mutableListOf(
                DishImage("https://images.unsplash.com/photo-1541518763669-27fef04b14ea?w=600", true)
            ),
            syncStatus = Constants.SYNC_STATUS_SYNCED
        )

        // Dish 5: Cà Phê Trứng (Quick: 8 mins <= 15)
        val dish5 = Dish(
            id = "dish_cafe_trung",
            name = "Cà Phê Trứng Giảng",
            price = 40000.0,
            preparationTime = 8,
            isAvailable = true,
            description = "Lớp kem trứng béo ngậy được đánh bông sánh mịn phủ lên trên cà phê Robusta đen đậm đặc nóng hổi.",
            dishCategoryId = cat3.id,
            images = mutableListOf(
                DishImage("https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600", true),
                DishImage("https://images.unsplash.com/photo-1517256064527-09c73fc73e38?w=600", false)
            ),
            syncStatus = Constants.SYNC_STATUS_SYNCED
        )

        insertDishInternal(db, dish1)
        insertDishInternal(db, dish2)
        insertDishInternal(db, dish3)
        insertDishInternal(db, dish4)
        insertDishInternal(db, dish5)
    }

    private fun insertCategoryInternal(db: SQLiteDatabase, category: DishCategory): Long {
        val cv = ContentValues().apply {
            put(Constants.COL_CAT_ID, category.id)
            put(Constants.COL_CAT_NAME, category.name)
            put(Constants.COL_CAT_DESC, category.description)
        }
        return db.insert(Constants.TABLE_CATEGORY, null, cv)
    }

    private fun insertDishInternal(db: SQLiteDatabase, dish: Dish): Long {
        val cv = ContentValues().apply {
            put(Constants.COL_DISH_ID, dish.id)
            put(Constants.COL_DISH_NAME, dish.name)
            put(Constants.COL_DISH_PRICE, dish.price)
            put(Constants.COL_DISH_PREP_TIME, dish.preparationTime)
            put(Constants.COL_DISH_IS_AVAILABLE, if (dish.isAvailable) 1 else 0)
            put(Constants.COL_DISH_DESCRIPTION, dish.description)
            put(Constants.COL_DISH_CATEGORY_ID, dish.dishCategoryId)
            put(Constants.COL_DISH_IMAGES, gson.toJson(dish.images))
            put(Constants.COL_DISH_SYNC_STATUS, dish.syncStatus)
            put(Constants.COL_DISH_UPDATED_AT, dish.updatedAt)
        }
        return db.insert(Constants.TABLE_DISH, null, cv)
    }

    // ==========================================
    // CATEGORY OPERATIONS
    // ==========================================

    fun getAllCategories(): List<DishCategory> {
        val list = mutableListOf<DishCategory>()
        val db = readableDatabase
        val cursor = db.query(
            Constants.TABLE_CATEGORY,
            null,
            null,
            null,
            null,
            null,
            "${Constants.COL_CAT_NAME} ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToCategory(it))
            }
        }
        return list
    }

    fun getCategoryById(id: String): DishCategory? {
        val db = readableDatabase
        val cursor = db.query(
            Constants.TABLE_CATEGORY,
            null,
            "${Constants.COL_CAT_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToCategory(it)
            }
        }
        return null
    }

    fun insertCategory(category: DishCategory): Boolean {
        if (category.id.isBlank()) {
            category.id = "cat_" + UUID.randomUUID().toString().take(8)
        }
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(Constants.COL_CAT_ID, category.id)
            put(Constants.COL_CAT_NAME, category.name)
            put(Constants.COL_CAT_DESC, category.description)
        }
        return db.insert(Constants.TABLE_CATEGORY, null, cv) != -1L
    }

    /**
     * Requirement: Cannot delete a category that is currently being used by any dish!
     */
    fun canDeleteCategory(categoryId: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${Constants.TABLE_DISH} WHERE ${Constants.COL_DISH_CATEGORY_ID} = ?",
            arrayOf(categoryId)
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0) == 0
            }
        }
        return true
    }

    fun deleteCategory(categoryId: String): Boolean {
        if (!canDeleteCategory(categoryId)) {
            return false
        }
        val db = writableDatabase
        return db.delete(
            Constants.TABLE_CATEGORY,
            "${Constants.COL_CAT_ID} = ?",
            arrayOf(categoryId)
        ) > 0
    }

    // ==========================================
    // DISH OPERATIONS & VALIDATION
    // ==========================================

    /**
     * Requirement: Dish name must not duplicate within the same category
     */
    fun isDishNameDuplicate(name: String, categoryId: String, excludeDishId: String? = null): Boolean {
        val db = readableDatabase
        val query: String
        val args: Array<String>
        if (excludeDishId.isNullOrBlank()) {
            query = "SELECT COUNT(*) FROM ${Constants.TABLE_DISH} WHERE LOWER(${Constants.COL_DISH_NAME}) = LOWER(?) AND ${Constants.COL_DISH_CATEGORY_ID} = ?"
            args = arrayOf(name.trim(), categoryId)
        } else {
            query = "SELECT COUNT(*) FROM ${Constants.TABLE_DISH} WHERE LOWER(${Constants.COL_DISH_NAME}) = LOWER(?) AND ${Constants.COL_DISH_CATEGORY_ID} = ? AND ${Constants.COL_DISH_ID} != ?"
            args = arrayOf(name.trim(), categoryId, excludeDishId)
        }
        val cursor = db.rawQuery(query, args)
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0) > 0
            }
        }
        return false
    }

    fun getAllDishes(): List<Dish> {
        return searchAndFilterDishes(null, null, false, null, null, null)
    }

    /**
     * Requirement:
     * - Search by substring of dish name
     * - Filter by category, available status, price range
     * - Sort by price asc/desc, preparation time asc
     * - All conditions can be coordinated simultaneously
     */
    fun searchAndFilterDishes(
        keyword: String?,
        categoryId: String?,
        onlyAvailable: Boolean,
        minPrice: Double?,
        maxPrice: Double?,
        sortOrder: String?
    ): List<Dish> {
        val list = mutableListOf<Dish>()
        val db = readableDatabase

        val selectionList = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        if (!keyword.isNullOrBlank()) {
            selectionList.add("${Constants.COL_DISH_NAME} LIKE ?")
            selectionArgs.add("%${keyword.trim()}%")
        }

        if (!categoryId.isNullOrBlank() && categoryId != "ALL") {
            selectionList.add("${Constants.COL_DISH_CATEGORY_ID} = ?")
            selectionArgs.add(categoryId)
        }

        if (onlyAvailable) {
            selectionList.add("${Constants.COL_DISH_IS_AVAILABLE} = 1")
        }

        if (minPrice != null && minPrice > 0) {
            selectionList.add("${Constants.COL_DISH_PRICE} >= ?")
            selectionArgs.add(minPrice.toString())
        }

        if (maxPrice != null && maxPrice > 0) {
            selectionList.add("${Constants.COL_DISH_PRICE} <= ?")
            selectionArgs.add(maxPrice.toString())
        }

        val whereClause = if (selectionList.isNotEmpty()) selectionList.joinToString(" AND ") else null
        val whereArgs = if (selectionArgs.isNotEmpty()) selectionArgs.toTypedArray() else null

        val orderBy = when (sortOrder) {
            "PRICE_ASC" -> "${Constants.COL_DISH_PRICE} ASC"
            "PRICE_DESC" -> "${Constants.COL_DISH_PRICE} DESC"
            "PREP_TIME_ASC" -> "${Constants.COL_DISH_PREP_TIME} ASC"
            else -> "${Constants.COL_DISH_UPDATED_AT} DESC"
        }

        val cursor = db.query(
            Constants.TABLE_DISH,
            null,
            whereClause,
            whereArgs,
            null,
            null,
            orderBy
        )

        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToDish(it))
            }
        }
        return list
    }

    fun getDishById(id: String): Dish? {
        val db = readableDatabase
        val cursor = db.query(
            Constants.TABLE_DISH,
            null,
            "${Constants.COL_DISH_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToDish(it)
            }
        }
        return null
    }

    fun insertDish(dish: Dish, syncStatus: String = Constants.SYNC_STATUS_PENDING_CREATE): Boolean {
        if (dish.id.isBlank()) {
            dish.id = "dish_" + UUID.randomUUID().toString().take(8)
        }
        dish.syncStatus = syncStatus
        dish.updatedAt = System.currentTimeMillis()

        val db = writableDatabase
        val cv = ContentValues().apply {
            put(Constants.COL_DISH_ID, dish.id)
            put(Constants.COL_DISH_NAME, dish.name)
            put(Constants.COL_DISH_PRICE, dish.price)
            put(Constants.COL_DISH_PREP_TIME, dish.preparationTime)
            put(Constants.COL_DISH_IS_AVAILABLE, if (dish.isAvailable) 1 else 0)
            put(Constants.COL_DISH_DESCRIPTION, dish.description)
            put(Constants.COL_DISH_CATEGORY_ID, dish.dishCategoryId)
            put(Constants.COL_DISH_IMAGES, gson.toJson(dish.images))
            put(Constants.COL_DISH_SYNC_STATUS, dish.syncStatus)
            put(Constants.COL_DISH_UPDATED_AT, dish.updatedAt)
        }
        return db.insert(Constants.TABLE_DISH, null, cv) != -1L
    }

    fun updateDish(dish: Dish, syncStatus: String = Constants.SYNC_STATUS_PENDING_UPDATE): Boolean {
        dish.syncStatus = syncStatus
        dish.updatedAt = System.currentTimeMillis()

        val db = writableDatabase
        val cv = ContentValues().apply {
            put(Constants.COL_DISH_NAME, dish.name)
            put(Constants.COL_DISH_PRICE, dish.price)
            put(Constants.COL_DISH_PREP_TIME, dish.preparationTime)
            put(Constants.COL_DISH_IS_AVAILABLE, if (dish.isAvailable) 1 else 0)
            put(Constants.COL_DISH_DESCRIPTION, dish.description)
            put(Constants.COL_DISH_CATEGORY_ID, dish.dishCategoryId)
            put(Constants.COL_DISH_IMAGES, gson.toJson(dish.images))
            put(Constants.COL_DISH_SYNC_STATUS, dish.syncStatus)
            put(Constants.COL_DISH_UPDATED_AT, dish.updatedAt)
        }
        return db.update(
            Constants.TABLE_DISH,
            cv,
            "${Constants.COL_DISH_ID} = ?",
            arrayOf(dish.id)
        ) > 0
    }

    fun deleteDish(id: String): Boolean {
        val db = writableDatabase
        return db.delete(
            Constants.TABLE_DISH,
            "${Constants.COL_DISH_ID} = ?",
            arrayOf(id)
        ) > 0
    }

    fun getPendingSyncDishes(): List<Dish> {
        val list = mutableListOf<Dish>()
        val db = readableDatabase
        val cursor = db.query(
            Constants.TABLE_DISH,
            null,
            "${Constants.COL_DISH_SYNC_STATUS} != ?",
            arrayOf(Constants.SYNC_STATUS_SYNCED),
            null,
            null,
            "${Constants.COL_DISH_UPDATED_AT} ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToDish(it))
            }
        }
        return list
    }

    fun markDishSynced(dishId: String) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(Constants.COL_DISH_SYNC_STATUS, Constants.SYNC_STATUS_SYNCED)
        }
        db.update(
            Constants.TABLE_DISH,
            cv,
            "${Constants.COL_DISH_ID} = ?",
            arrayOf(dishId)
        )
    }

    private fun cursorToCategory(cursor: Cursor): DishCategory {
        return DishCategory(
            id = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_CAT_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_CAT_NAME)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_CAT_DESC)) ?: ""
        )
    }

    private fun cursorToDish(cursor: Cursor): Dish {
        val imagesJson = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_DISH_IMAGES))
        val type = object : TypeToken<MutableList<DishImage>>() {}.type
        val images: MutableList<DishImage> = try {
            gson.fromJson(imagesJson, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }

        return Dish(
            id = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_DISH_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_DISH_NAME)),
            price = cursor.getDouble(cursor.getColumnIndexOrThrow(Constants.COL_DISH_PRICE)),
            preparationTime = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COL_DISH_PREP_TIME)),
            isAvailable = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COL_DISH_IS_AVAILABLE)) == 1,
            description = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_DISH_DESCRIPTION)) ?: "",
            dishCategoryId = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_DISH_CATEGORY_ID)),
            images = images,
            syncStatus = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_DISH_SYNC_STATUS)) ?: Constants.SYNC_STATUS_SYNCED,
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COL_DISH_UPDATED_AT))
        )
    }
}
