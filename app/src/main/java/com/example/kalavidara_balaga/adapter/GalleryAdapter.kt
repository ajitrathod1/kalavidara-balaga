package com.example.kalavidara_balaga.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kalavidara_balaga.R
import com.example.kalavidara_balaga.databinding.ItemGalleryPhotoBinding

class GalleryAdapter(private val images: List<String>) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    class GalleryViewHolder(val binding: ItemGalleryPhotoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = ItemGalleryPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val imageUrl = images[position]
        
        if (imageUrl.startsWith("res/")) {
            val resPath = imageUrl.substringAfter("res/")
            val resourceId = holder.itemView.context.resources.getIdentifier(
                resPath, null, holder.itemView.context.packageName
            )
            if (resourceId != 0) {
                holder.binding.ivGalleryPhoto.setImageResource(resourceId)
            } else {
                holder.binding.ivGalleryPhoto.setImageResource(R.drawable.placeholder_troupe)
            }
        } else {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_troupe)
                .into(holder.binding.ivGalleryPhoto)
        }
    }

    override fun getItemCount(): Int = images.size
}
