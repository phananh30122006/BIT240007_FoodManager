package com.phan.bit240007_foodmanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.phan.bit240007_foodmanager.R
import com.phan.bit240007_foodmanager.data.local.FoodDatabaseHelper
import com.phan.bit240007_foodmanager.data.model.Dish
import com.phan.bit240007_foodmanager.data.model.DishImage
import com.phan.bit240007_foodmanager.data.sync.SyncManager
import com.phan.bit240007_foodmanager.databinding.ActivityDishDetailBinding
import com.phan.bit240007_foodmanager.ui.adapter.DishImageAdapter
import java.text.DecimalFormat

class DishDetailActivity : BaseActivity() {

    companion object {
        const val EXTRA_DISH_ID = "extra_dish_id"
    }

    private lateinit var binding: ActivityDishDetailBinding
    private lateinit var dbHelper: FoodDatabaseHelper
    private var currentDishId: String = ""
    private var dish: Dish? = null
    private val imagesList = mutableListOf<DishImage>()
    private lateinit var imageAdapter: DishImageAdapter
    private val currencyFormatter = DecimalFormat("#,###")

    private val editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            loadDishData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDishDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = FoodDatabaseHelper(this)
        currentDishId = intent.getStringExtra(EXTRA_DISH_ID) ?: ""

        setupToolbar()
        setupImagesRecyclerView()
        setupListeners()
        loadDishData()
    }

    private fun setupToolbar() {
        binding.toolbarDetail.setNavigationOnClickListener { finish() }
    }

    private fun setupImagesRecyclerView() {
        imageAdapter = DishImageAdapter(
            context = this,
            images = imagesList,
            isEditable = true,
            onThumbnailChanged = {
                // Update dish with the newly selected thumbnail
                dish?.images = imagesList
                dish?.let {
                    dbHelper.updateDish(it)
                    updateMainThumbnail(it)
                }
            },
            onImageRemoved = {
                dish?.images = imagesList
                dish?.let {
                    dbHelper.updateDish(it)
                    updateMainThumbnail(it)
                }
            }
        )
        binding.rvDetailImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvDetailImages.adapter = imageAdapter
    }

    private fun setupListeners() {
        // Requirement 4.3: Thêm ảnh bằng ImageUrl ngay ở màn hình chi tiết
        binding.btnDetailAddImage.setOnClickListener {
            val url = binding.etDetailAddImageUrl.text?.toString()?.trim() ?: ""
            if (url.isBlank()) {
                Toast.makeText(this, getString(R.string.err_image_url_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isFirst = imagesList.isEmpty()
            imagesList.add(DishImage(imageUrl = url, isThumbnail = isFirst))
            imageAdapter.notifyItemInserted(imagesList.size - 1)
            binding.etDetailAddImageUrl.text?.clear()

            dish?.images = imagesList
            dish?.let {
                dbHelper.updateDish(it)
                updateMainThumbnail(it)
                SyncManager.getInstance(this).triggerAutoSync()
            }
        }

        binding.btnDetailEdit.setOnClickListener {
            val intent = Intent(this, AddEditDishActivity::class.java).apply {
                putExtra(AddEditDishActivity.EXTRA_DISH_ID, currentDishId)
            }
            editLauncher.launch(intent)
        }

        binding.btnDetailDelete.setOnClickListener {
            confirmDelete()
        }
    }

    private fun loadDishData() {
        dish = dbHelper.getDishById(currentDishId)
        if (dish == null) {
            Toast.makeText(this, getString(R.string.err_dish_not_found), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val d = dish!!
        binding.tvDetailDishName.text = d.name
        val category = dbHelper.getCategoryById(d.dishCategoryId)
        binding.tvDetailCategory.text = category?.name ?: d.dishCategoryId
        binding.tvDetailPrice.text = "${currencyFormatter.format(d.price)} VND"
        binding.tvDetailPrepTime.text = "${getString(R.string.dish_prep_time)}: ${d.preparationTime} ${getString(R.string.badge_quick).lowercase()}"
        binding.tvDetailDescription.text = if (d.description.isNotBlank()) d.description else getString(R.string.empty_dishes)

        // Quick vs Regular Badge
        if (d.isQuick) {
            binding.tvDetailPrepBadge.text = getString(R.string.badge_quick)
            binding.tvDetailPrepBadge.setBackgroundResource(R.drawable.bg_badge_quick)
            binding.tvDetailPrepBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_quick_text))
        } else {
            binding.tvDetailPrepBadge.text = getString(R.string.badge_regular)
            binding.tvDetailPrepBadge.setBackgroundResource(R.drawable.bg_badge_regular)
            binding.tvDetailPrepBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_regular_text))
        }

        // Available vs Unavailable Badge
        if (d.isAvailable) {
            binding.tvDetailStatusBadge.text = getString(R.string.status_available)
            binding.tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_status_available)
            binding.tvDetailStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_available_text))
        } else {
            binding.tvDetailStatusBadge.text = getString(R.string.status_unavailable)
            binding.tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_status_unavailable)
            binding.tvDetailStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_unavailable_text))
        }

        // Images list
        imagesList.clear()
        imagesList.addAll(d.images)
        imageAdapter.notifyDataSetChanged()

        updateMainThumbnail(d)
    }

    private fun updateMainThumbnail(d: Dish) {
        val thumbUrl = d.thumbnailUrl
        if (thumbUrl.isNotBlank()) {
            binding.ivDetailMainImage.load(thumbUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_food_placeholder)
                error(R.drawable.ic_food_placeholder)
            }
        } else {
            binding.ivDetailMainImage.setImageResource(R.drawable.ic_food_placeholder)
        }
    }

    private fun confirmDelete() {
        dish?.let { d ->
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_title))
                .setMessage(getString(R.string.confirm_delete_dish_msg, d.name))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    val deleted = dbHelper.deleteDish(d.id)
                    if (deleted) {
                        Toast.makeText(this, getString(R.string.msg_dish_deleted), Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }
    }
}
