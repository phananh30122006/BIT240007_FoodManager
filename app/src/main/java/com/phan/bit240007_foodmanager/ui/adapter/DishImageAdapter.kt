package com.phan.bit240007_foodmanager.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.phan.bit240007_foodmanager.R
import com.phan.bit240007_foodmanager.data.model.DishImage
import com.phan.bit240007_foodmanager.databinding.ItemDetailImageBinding

class DishImageAdapter(
    private val context: Context,
    private val images: MutableList<DishImage>,
    private val isEditable: Boolean = true,
    private val onThumbnailChanged: ((Int) -> Unit)? = null,
    private val onImageRemoved: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<DishImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemDetailImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    override fun getItemCount(): Int = images.size

    inner class ImageViewHolder(private val binding: ItemDetailImageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DishImage, position: Int) {
            binding.ivItemImage.load(item.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_food_placeholder)
                error(R.drawable.ic_food_placeholder)
            }

            if (item.isThumbnail) {
                binding.tvThumbnailBadge.visibility = View.VISIBLE
                binding.btnSetThumbnail.visibility = View.GONE
                binding.cardImageContainer.setBackgroundResource(R.drawable.bg_thumbnail_border)
            } else {
                binding.tvThumbnailBadge.visibility = View.GONE
                binding.btnSetThumbnail.visibility = if (isEditable) View.VISIBLE else View.GONE
                binding.cardImageContainer.background = null
            }

            binding.btnRemoveImage.visibility = if (isEditable) View.VISIBLE else View.GONE

            binding.btnSetThumbnail.setOnClickListener {
                val currentPos = bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    for (i in images.indices) {
                        images[i].isThumbnail = (i == currentPos)
                    }
                    notifyDataSetChanged()
                    onThumbnailChanged?.invoke(currentPos)
                }
            }

            binding.btnRemoveImage.setOnClickListener {
                val currentPos = bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val wasThumbnail = images[currentPos].isThumbnail
                    images.removeAt(currentPos)
                    if (wasThumbnail && images.isNotEmpty()) {
                        images[0].isThumbnail = true
                    }
                    notifyDataSetChanged()
                    onImageRemoved?.invoke(currentPos)
                }
            }
        }
    }
}
