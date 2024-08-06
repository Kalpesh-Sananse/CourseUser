package com.psi.dpsi.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.psi.dpsi.model.CourseModel
import com.psi.dpsi.R
import com.psi.dpsi.databinding.ItemCourseBinding
import com.psi.dpsi.utils.Utils
import com.psi.dpsi.utils.Utils.gone
import com.psi.dpsi.utils.Utils.visible


class CourseAdapter(private val onClick: OnItemClickListener): ListAdapter<CourseModel, CourseAdapter.CategoryVH>(DiffUtils) {
    inner class CategoryVH(val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root)

    object DiffUtils : DiffUtil.ItemCallback<CourseModel>() {
        override fun areItemsTheSame(oldItem: CourseModel, newItem: CourseModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CourseModel, newItem: CourseModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryVH(binding)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val item = getItem(position)

        holder.binding.apply {

            ivCategory.load(item.image) {
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }

            tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val offerPrice = item.offerPrice.toIntOrNull() ?: 0
            val originalPrice = item.originalPrice.toIntOrNull() ?: 0
            val dc = Utils.calculateDiscount(offerPrice, originalPrice)
            tvTitle.text = item.courseName
            tvRatings.text = item.rating
            tvOfferPrice.text = if(item.offerPrice == "0") "Free" else "₹${ item.offerPrice }"
            tvOriginalPrice.text = "₹${ item.originalPrice }"
            tvDiscount.text = "${dc.toInt()}% Off"
            if(item.offerPrice == "0") tvDiscount.gone() else tvDiscount.visible()


            holder.itemView.setOnClickListener {
                onClick.onItemClick(item)
            }


        }


    }

    interface OnItemClickListener {
        fun onItemClick(courseModel: CourseModel)
    }


}







