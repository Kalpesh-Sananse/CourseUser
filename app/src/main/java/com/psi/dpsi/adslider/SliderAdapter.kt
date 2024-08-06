package com.psi.dpsi.adslider

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.psi.dpsi.R
import com.psi.dpsi.databinding.ItemSliderBinding

class SliderAdapter(val context: Context): ListAdapter<SliderModel, SliderAdapter.ImageVH>(DiffUtils) {
    inner class ImageVH(val binding: ItemSliderBinding): RecyclerView.ViewHolder(binding.root)

    object DiffUtils: DiffUtil.ItemCallback<SliderModel>() {
        override fun areItemsTheSame(oldItem: SliderModel, newItem: SliderModel): Boolean {
            return oldItem.sliderId == newItem.sliderId
        }

        override fun areContentsTheSame(oldItem: SliderModel, newItem: SliderModel): Boolean {
           return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVH {
        val binding = ItemSliderBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ImageVH(binding)
    }

    override fun onBindViewHolder(holder: ImageVH, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
                sliderImage.load(item.imageUrl) {
                    placeholder(R.drawable.placeholder)
                    error(R.drawable.placeholder)
                }

            sliderImage.setOnClickListener {
                try {
                    val uri = Uri.parse(item.launchUrl)
                    if (uri != null && uri.scheme != null) {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    } else {
                        Log.d("TAG", "onBindViewHolder: error ")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}