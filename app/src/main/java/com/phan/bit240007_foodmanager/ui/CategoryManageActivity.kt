package com.phan.bit240007_foodmanager.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.phan.bit240007_foodmanager.R
import com.phan.bit240007_foodmanager.data.local.FoodDatabaseHelper
import com.phan.bit240007_foodmanager.data.model.DishCategory
import com.phan.bit240007_foodmanager.databinding.ActivityCategoryManageBinding
import com.phan.bit240007_foodmanager.ui.adapter.CategoryAdapter

class CategoryManageActivity : BaseActivity() {

    private lateinit var binding: ActivityCategoryManageBinding
    private lateinit var dbHelper: FoodDatabaseHelper
    private lateinit var adapter: CategoryAdapter
    private val categoriesList = mutableListOf<DishCategory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = FoodDatabaseHelper(this)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadCategories()
    }

    private fun setupToolbar() {
        binding.toolbarCategory.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(categoriesList) { category ->
            confirmDeleteCategory(category)
        }
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAddCategorySubmit.setOnClickListener {
            val name = binding.etCatName.text?.toString()?.trim() ?: ""
            val desc = binding.etCatDesc.text?.toString()?.trim() ?: ""

            if (name.isBlank()) {
                binding.tilCatName.error = getString(R.string.err_name_empty)
                return@setOnClickListener
            }
            binding.tilCatName.error = null

            val newCategory = DishCategory(name = name, description = desc)
            val success = dbHelper.insertCategory(newCategory)
            if (success) {
                Toast.makeText(this, getString(R.string.msg_category_saved), Toast.LENGTH_SHORT).show()
                binding.etCatName.text?.clear()
                binding.etCatDesc.text?.clear()
                loadCategories()
            }
        }
    }

    private fun loadCategories() {
        val list = dbHelper.getAllCategories()
        categoriesList.clear()
        categoriesList.addAll(list)
        adapter.notifyDataSetChanged()

        binding.tvEmptyCategories.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun confirmDeleteCategory(category: DishCategory) {
        // Requirement 4.4: Không cho xóa loại món đang được sử dụng
        if (!dbHelper.canDeleteCategory(category.id)) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_title))
                .setMessage(getString(R.string.err_cannot_delete_cat_in_use))
                .setPositiveButton(getString(android.R.string.ok), null)
                .show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_delete_title))
            .setMessage(getString(R.string.confirm_delete_cat_msg, category.name))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                val deleted = dbHelper.deleteCategory(category.id)
                if (deleted) {
                    Toast.makeText(this, getString(R.string.msg_category_deleted), Toast.LENGTH_SHORT).show()
                    loadCategories()
                }
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}
