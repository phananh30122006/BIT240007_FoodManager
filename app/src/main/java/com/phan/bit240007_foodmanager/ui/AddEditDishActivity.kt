package com.phan.bit240007_foodmanager.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.phan.bit240007_foodmanager.R
import com.phan.bit240007_foodmanager.data.local.FoodDatabaseHelper
import com.phan.bit240007_foodmanager.data.model.Dish
import com.phan.bit240007_foodmanager.data.model.DishCategory
import com.phan.bit240007_foodmanager.data.model.DishImage
import com.phan.bit240007_foodmanager.data.sync.SyncManager
import com.phan.bit240007_foodmanager.databinding.ActivityAddEditDishBinding
import com.phan.bit240007_foodmanager.ui.adapter.DishImageAdapter

class AddEditDishActivity : BaseActivity() {

    companion object {
        const val EXTRA_DISH_ID = "extra_dish_id"
    }

    private lateinit var binding: ActivityAddEditDishBinding
    private lateinit var dbHelper: FoodDatabaseHelper
    private var editingDish: Dish? = null
    private var categories = listOf<DishCategory>()
    private val imagesList = mutableListOf<DishImage>()
    private lateinit var imageAdapter: DishImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditDishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = FoodDatabaseHelper(this)

        val dishId = intent.getStringExtra(EXTRA_DISH_ID)
        if (!dishId.isNullOrBlank()) {
            editingDish = dbHelper.getDishById(dishId)
        }

        setupToolbar()
        setupCategoriesSpinner()
        setupImagesRecyclerView()
        setupFormListeners()

        if (editingDish != null) {
            populateForm(editingDish!!)
        }
    }

    private fun setupToolbar() {
        val titleRes = if (editingDish == null) R.string.title_add_dish else R.string.title_edit_dish
        binding.toolbarAddEdit.setTitle(titleRes)
        binding.toolbarAddEdit.setNavigationOnClickListener { finish() }
    }

    private fun setupCategoriesSpinner() {
        categories = dbHelper.getAllCategories()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories.map { it.name })
        binding.spFormCategory.adapter = adapter
    }

    private fun setupImagesRecyclerView() {
        imageAdapter = DishImageAdapter(
            context = this,
            images = imagesList,
            isEditable = true,
            onThumbnailChanged = {
                // thumbnail changed internally in adapter
            },
            onImageRemoved = {
                // image removed internally
            }
        )
        binding.rvFormImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFormImages.adapter = imageAdapter
    }

    private fun setupFormListeners() {
        binding.btnFormAddImage.setOnClickListener {
            val url = binding.etFormImageUrl.text?.toString()?.trim() ?: ""
            if (url.isBlank()) {
                Toast.makeText(this, getString(R.string.err_image_url_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isFirst = imagesList.isEmpty()
            imagesList.add(DishImage(imageUrl = url, isThumbnail = isFirst))
            imageAdapter.notifyItemInserted(imagesList.size - 1)
            binding.etFormImageUrl.text?.clear()
        }

        binding.btnFormCancel.setOnClickListener {
            finish()
        }

        binding.btnFormSave.setOnClickListener {
            saveDish()
        }
    }

    private fun populateForm(dish: Dish) {
        binding.etFormDishName.setText(dish.name)
        binding.etFormPrice.setText(dish.price.toString())
        binding.etFormPrepTime.setText(dish.preparationTime.toString())
        binding.switchFormAvailable.isChecked = dish.isAvailable
        binding.etFormDescription.setText(dish.description)

        // Select category
        val catIndex = categories.indexOfFirst { it.id == dish.dishCategoryId }
        if (catIndex >= 0) {
            binding.spFormCategory.setSelection(catIndex)
        }

        // Images
        imagesList.clear()
        imagesList.addAll(dish.images.map { it.copy() })
        imageAdapter.notifyDataSetChanged()
    }

    private fun saveDish() {
        var isValid = true

        val name = binding.etFormDishName.text?.toString()?.trim() ?: ""
        if (name.isBlank()) {
            binding.tilDishName.error = getString(R.string.err_name_empty)
            isValid = false
        } else {
            binding.tilDishName.error = null
        }

        val priceStr = binding.etFormPrice.text?.toString()?.trim() ?: ""
        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            binding.tilDishPrice.error = getString(R.string.err_price_invalid)
            isValid = false
        } else {
            binding.tilDishPrice.error = null
        }

        val prepTimeStr = binding.etFormPrepTime.text?.toString()?.trim() ?: ""
        val prepTime = prepTimeStr.toIntOrNull()
        if (prepTime == null || prepTime <= 0) {
            binding.tilDishPrepTime.error = getString(R.string.err_prep_time_invalid)
            isValid = false
        } else {
            binding.tilDishPrepTime.error = null
        }

        val selectedCatPos = binding.spFormCategory.selectedItemPosition
        if (selectedCatPos < 0 || selectedCatPos >= categories.size) {
            binding.tvCategoryError.text = getString(R.string.err_category_invalid)
            binding.tvCategoryError.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.tvCategoryError.visibility = View.GONE
        }

        if (!isValid) return

        val selectedCategory = categories[selectedCatPos]

        // Requirement 3: Tên món không trùng trong cùng một loại
        val isDuplicate = dbHelper.isDishNameDuplicate(name, selectedCategory.id, editingDish?.id)
        if (isDuplicate) {
            binding.tilDishName.error = getString(R.string.err_duplicate_name)
            return
        }

        // Ensure at most 1 thumbnail
        if (imagesList.isNotEmpty()) {
            val hasThumbnail = imagesList.any { it.isThumbnail }
            if (!hasThumbnail) {
                imagesList[0].isThumbnail = true
            }
        }

        val isAvailable = binding.switchFormAvailable.isChecked
        val desc = binding.etFormDescription.text?.toString()?.trim() ?: ""

        val success: Boolean
        if (editingDish == null) {
            val newDish = Dish(
                name = name,
                price = price!!,
                preparationTime = prepTime!!,
                isAvailable = isAvailable,
                description = desc,
                dishCategoryId = selectedCategory.id,
                images = imagesList
            )
            success = dbHelper.insertDish(newDish)
        } else {
            val updated = editingDish!!.copy(
                name = name,
                price = price!!,
                preparationTime = prepTime!!,
                isAvailable = isAvailable,
                description = desc,
                dishCategoryId = selectedCategory.id,
                images = imagesList
            )
            success = dbHelper.updateDish(updated)
        }

        if (success) {
            Toast.makeText(this, getString(R.string.msg_dish_saved), Toast.LENGTH_SHORT).show()
            // Trigger auto sync
            SyncManager.getInstance(this).triggerAutoSync()
            setResult(RESULT_OK)
            finish()
        }
    }
}
