package com.phan.bit240007_foodmanager.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.phan.bit240007_foodmanager.data.model.DishCategory
import com.phan.bit240007_foodmanager.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val categories: MutableList<DishCategory>,
    private val onDeleteClick: (DishCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    fun submitList(newList: List<DishCategory>) {
        categories.clear()
        categories.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: DishCategory) {
            binding.tvCategoryItemName.text = category.name
            binding.tvCategoryItemDesc.text = if (category.description.isNotBlank()) category.description else "No description"
            binding.btnDeleteCategory.setOnClickListener { onDeleteClick(category) }
        }
    }
}
