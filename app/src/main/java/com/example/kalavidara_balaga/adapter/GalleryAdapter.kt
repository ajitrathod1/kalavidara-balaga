package com.example.kalavidara_balaga.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kalavidara_balaga.R
import com.example.kalavidara_balaga.databinding.ItemGalleryBinding

class GalleryAdapter(private val mediaList: List<Pair<String, Boolean>>) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    class GalleryViewHolder(val binding: ItemGalleryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val (url, isVideo) = mediaList[position]
        val context = holder.itemView.context
        
        // Handle local resources
        if (url.startsWith("res/")) {
            val resPath = url.substringAfter("res/")
            val resourceId = context.resources.getIdentifier(resPath, null, context.packageName)
            if (resourceId != 0) {
                holder.binding.ivGalleryImage.setImageResource(resourceId)
            } else {
                holder.binding.ivGalleryImage.setImageResource(R.drawable.placeholder_troupe)
            }
        } else {
            Glide.with(context)
                .load(url)
                .placeholder(R.drawable.placeholder_troupe)
                .into(holder.binding.ivGalleryImage)
        }
        
        if (isVideo) {
            holder.binding.ivPlayIcon.visibility = View.VISIBLE
        } else {
            holder.binding.ivPlayIcon.visibility = View.GONE
        }
        
        // In staggered grid, different heights look better
        val params = holder.binding.ivGalleryImage.layoutParams
        params.height = if (position % 3 == 0) 600 else if (position % 2 == 0) 800 else 700
        holder.binding.ivGalleryImage.layoutParams = params
        
        holder.itemView.setOnClickListener {
            if (isVideo) {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                intent.setDataAndType(android.net.Uri.parse(url), "video/*")
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = mediaList.size
}