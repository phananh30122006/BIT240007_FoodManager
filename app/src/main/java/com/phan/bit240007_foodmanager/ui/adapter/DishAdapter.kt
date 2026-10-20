package com.phan.bit240007_foodmanager.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.phan.bit240007_foodmanager.R
import com.phan.bit240007_foodmanager.data.model.Dish
import com.phan.bit240007_foodmanager.data.model.DishCategory
import com.phan.bit240007_foodmanager.databinding.ItemDishBinding
import com.phan.bit240007_foodmanager.util.Constants
import java.text.DecimalFormat

class DishAdapter(
    private val context: Context,
    private val categoriesMap: Map<String, DishCategory>,
    private val onItemClick: (Dish) -> Unit,
    private val onEditClick: (Dish) -> Unit,
    private val onDeleteClick: (Dish) -> Unit
) : RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    private val dishes = mutableListOf<Dish>()
    private val currencyFormatter = DecimalFormat("#,###")

    fun submitList(newList: List<Dish>) {
        dishes.clear()
        dishes.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val binding = ItemDishBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DishViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        holder.bind(dishes[position])
    }

    override fun getItemCount(): Int = dishes.size

    inner class DishViewHolder(private val binding: ItemDishBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dish: Dish) {
            binding.tvDishName.text = dish.name
            binding.tvDishCategory.text = categoriesMap[dish.dishCategoryId]?.name ?: dish.dishCategoryId
            binding.tvDishPrice.text = "${currencyFormatter.format(dish.price)} VND"
            binding.tvPrepTimeMinutes.text = "${dish.preparationTime} ${context.getString(R.string.dish_prep_time)}"

            // Quick vs Regular Badge (<= 15 min)
            if (dish.isQuick) {
                binding.tvPrepTimeBadge.text = context.getString(R.string.badge_quick)
                binding.tvPrepTimeBadge.setBackgroundResource(R.drawable.bg_badge_quick)
                binding.tvPrepTimeBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_quick_text))
            } else {
                binding.tvPrepTimeBadge.text = context.getString(R.string.badge_regular)
                binding.tvPrepTimeBadge.setBackgroundResource(R.drawable.bg_badge_regular)
                binding.tvPrepTimeBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_regular_text))
            }

            // Available vs Unavailable Badge
            if (dish.isAvailable) {
                binding.tvAvailabilityBadge.text = context.getString(R.string.status_available)
                binding.tvAvailabilityBadge.setBackgroundResource(R.drawable.bg_status_available)
                binding.tvAvailabilityBadge.setTextColor(ContextCompat.getColor(context, R.color.status_available_text))
            } else {
                binding.tvAvailabilityBadge.text = context.getString(R.string.status_unavailable)
                binding.tvAvailabilityBadge.setBackgroundResource(R.drawable.bg_status_unavailable)
                binding.tvAvailabilityBadge.setTextColor(ContextCompat.getColor(context, R.color.status_unavailable_text))
            }

            // Sync Badge
            if (dish.syncStatus == Constants.SYNC_STATUS_SYNCED) {
                binding.tvSyncBadge.text = "✓ ${context.getString(R.string.status_synced)}"
                binding.tvSyncBadge.setTextColor(ContextCompat.getColor(context, R.color.status_online))
            } else {
                binding.tvSyncBadge.text = "⏳ ${context.getString(R.string.status_pending_sync)}"
                binding.tvSyncBadge.setTextColor(ContextCompat.getColor(context, R.color.accent))
            }

            // Image Thumbnail
            val thumbUrl = dish.thumbnailUrl
            if (thumbUrl.isNotBlank()) {
                binding.imgDishThumbnail.load(thumbUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_food_placeholder)
                    error(R.drawable.ic_food_placeholder)
                }
            } else {
                binding.imgDishThumbnail.setImageResource(R.drawable.ic_food_placeholder)
            }

            // Click Handlers
            binding.root.setOnClickListener { onItemClick(dish) }
            binding.btnEditDish.setOnClickListener { onEditClick(dish) }
            binding.btnDeleteDish.setOnClickListener { onDeleteClick(dish) }
        }
    }
}
