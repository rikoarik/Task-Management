package com.taskmanagement.presentation.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.taskmanagement.data.model.FileData
import com.taskmanagement.databinding.ItemMediaBinding

class GalleryAdapter(
    private val onItemClick: (url: String, name: String) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    private val mediaItems = mutableListOf<FileData>()

    inner class GalleryViewHolder(private val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(fileData: FileData) {
            val uri = Uri.parse(fileData.url)
            val mimeType = itemView.context.contentResolver.getType(uri)

            if (mimeType != null && mimeType.startsWith("video")) {
                binding.playIcon.visibility = View.VISIBLE
                binding.imageViewThumbnail.alpha = 0.7f
            } else {
                binding.playIcon.visibility = View.GONE
                binding.imageViewThumbnail.alpha = 1.0f
            }

            Glide.with(itemView.context)
                .load(uri)
                .into(binding.imageViewThumbnail)

            itemView.setOnClickListener {
                onItemClick(fileData.url, fileData.name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(mediaItems[position])
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun addMediaItems(newItems: List<FileData>) {
        mediaItems.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setMediaItems(newItems: List<FileData>) {
        mediaItems.clear()
        mediaItems.addAll(newItems)
        notifyDataSetChanged()
    }
}
