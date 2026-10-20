package com.phan.bit240007_foodmanager

import com.phan.bit240007_foodmanager.data.model.Dish
import com.phan.bit240007_foodmanager.data.model.DishImage
import com.phan.bit240007_foodmanager.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DishModelTest {

    @Test
    fun testStudentInfoAndConstants() {
        assertEquals("BIT240007", Constants.MSSV)
        assertEquals("Nguyễn Phan Anh", Constants.STUDENT_NAME)
        assertEquals("BIT240007_FoodManager", Constants.PROJECT_NAME)
        assertEquals("BIT240007_DishCategory", Constants.TABLE_CATEGORY)
        assertEquals("BIT240007_Dish", Constants.TABLE_DISH)
        assertEquals("BIT240007_Name", Constants.COL_DISH_NAME)
    }

    @Test
    fun testQuickVsRegularBadgeLogic() {
        // <= 15 minutes is Quick
        val quickDish1 = Dish(preparationTime = 15)
        assertTrue(quickDish1.isQuick)

        val quickDish2 = Dish(preparationTime = 8)
        assertTrue(quickDish2.isQuick)

        // > 15 minutes is Regular
        val regularDish = Dish(preparationTime = 16)
        assertFalse(regularDish.isQuick)

        val regularDish2 = Dish(preparationTime = 25)
        assertFalse(regularDish2.isQuick)
    }

    @Test
    fun testThumbnailSelection() {
        val dish = Dish(
            images = mutableListOf(
                DishImage("https://example.com/img1.jpg", false),
                DishImage("https://example.com/img2.jpg", true),
                DishImage("https://example.com/img3.jpg", false)
            )
        )
        // Image 2 is marked as thumbnail
        assertEquals("https://example.com/img2.jpg", dish.thumbnailUrl)
    }

    @Test
    fun testFallbackThumbnailSelection() {
        val dish = Dish(
            images = mutableListOf(
                DishImage("https://example.com/fallback.jpg", false)
            )
        )
        // No thumbnail marked, should fallback to first image
        assertEquals("https://example.com/fallback.jpg", dish.thumbnailUrl)
    }
}
