package com.psi.dpsi.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.psi.dpsi.R
import com.psi.dpsi.databinding.ItemContentBinding
import com.psi.dpsi.model.CourseContentModel
import com.psi.dpsi.utils.Utils.gone
import com.psi.dpsi.utils.Utils.visible


class ContentAdapter(private val context: Context, private val onClick: OnItemClickListener) : ListAdapter<CourseContentModel, ContentAdapter.CategoryVH>(DiffUtils) {
    inner class CategoryVH(val binding: ItemContentBinding) : RecyclerView.ViewHolder(binding.root)

    object DiffUtils : DiffUtil.ItemCallback<CourseContentModel>() {
        override fun areItemsTheSame(oldItem: CourseContentModel, newItem: CourseContentModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CourseContentModel, newItem: CourseContentModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val binding = ItemContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryVH(binding)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val item = getItem(position)

        holder.binding.apply {

            ivServiceImage.load(item.thumbnail) {
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }

            tvTitle.text = "${position + 1}. ${ item.videoTitle }"

            if(item.notesUrl.isNotEmpty()) btDownloadNotes.visible() else btDownloadNotes.gone()

            btDownloadNotes.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(item.notesUrl)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    print(e.stackTrace)
                }
            }

            btUpdateStatus.setOnClickListener {
                onClick.onItemClick(item, position)
            }

            holder.itemView.setOnClickListener {
                onClick.onItemClick(item, position)
            }



        }


    }


    interface OnItemClickListener {
        fun onItemClick(contentModel: CourseContentModel, index: Int)

    }


}







