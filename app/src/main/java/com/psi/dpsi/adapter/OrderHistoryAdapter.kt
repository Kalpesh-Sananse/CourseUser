package com.psi.dpsi.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.psi.dpsi.R
import com.psi.dpsi.activities.EnrollCourseActivity
import com.psi.dpsi.activities.NotesDownloadActivity
import com.psi.dpsi.databinding.ItemOrderHistoryBinding
import com.psi.dpsi.model.CartModel
import com.psi.dpsi.utils.Constants
import com.psi.dpsi.utils.Utils.gone


class OrderHistoryAdapter(val context: Context) : ListAdapter<CartModel, OrderHistoryAdapter.CategoryVH>(DiffUtils) {
    inner class CategoryVH(val binding: ItemOrderHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    object DiffUtils : DiffUtil.ItemCallback<CartModel>() {
        override fun areItemsTheSame(oldItem: CartModel, newItem: CartModel): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartModel, newItem: CartModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val binding = ItemOrderHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryVH(binding)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val item = getItem(position)

        holder.binding.apply {

            ivServiceImage.load(item.productImage) {
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }


            tvTitle.text = item.title
            tvOrderDate.text =  "Order: ${item.orderDate}"
            tvDeliveredDate.text =  "${item.type}"
            tvDeliveredDate.gone()
            tvQty.text = "Qty : ${item.qty}"
            tvAction.text = if(item.type == Constants.NOTES) "Download" else "Course"
            tvPrice.text = "₹${item.price.toInt() * item.qty.toInt()}"
            Log.d("TAGsss", "onBindViewHolder: ${item.type}")

            tvAction.setOnClickListener {
                if(item.type == Constants.NOTES) {
                    val intent = Intent(context, NotesDownloadActivity::class.java)
                    intent.putExtra(Constants.NOTES_REF, item.productId)
                    context.startActivity(intent)
                } else if(item.type == Constants.COURSE) {
                    val intent = Intent(context, EnrollCourseActivity::class.java)
                    intent.putExtra(Constants.COURSE_REF, item.productId)
                    context.startActivity(intent)
                }
            }

        }

    }






}







