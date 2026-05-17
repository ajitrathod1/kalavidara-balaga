package com.example.kalavidara_balaga.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kalavidara_balaga.R
import com.example.kalavidara_balaga.databinding.ItemTroupeBinding
import com.example.kalavidara_balaga.model.Artist

class ArtistAdapter(
    private val artists: List<Artist>,
    private val onArtistClick: (Artist) -> Unit
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    class ArtistViewHolder(val binding: ItemTroupeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemTroupeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]
        holder.binding.tvTroupeName.text = artist.name
        holder.binding.tvTroupeLocation.text = if (artist.district.isNotEmpty()) artist.district else artist.location
        holder.binding.tvArtForm.text = artist.artForm
        holder.binding.tvRating.text = "4.8" 
        
        // Load Image
        if (!artist.imageUrl.isNullOrBlank()) {
            if (artist.imageUrl.startsWith("res/")) {
                val resPath = artist.imageUrl.substringAfter("res/") 
                val resourceId = holder.itemView.context.resources.getIdentifier(
                    resPath, null, holder.itemView.context.packageName
                )
                if (resourceId != 0) {
                    holder.binding.ivTroupeImage.setImageResource(resourceId)
                } else {
                    holder.binding.ivTroupeImage.setImageResource(R.drawable.placeholder_troupe)
                }
            } else {
                Glide.with(holder.itemView.context)
                    .load(artist.imageUrl)
                    .placeholder(R.drawable.placeholder_troupe)
                    .into(holder.binding.ivTroupeImage)
            }
        } else {
            holder.binding.ivTroupeImage.setImageResource(R.drawable.placeholder_troupe)
        }

        holder.itemView.setOnClickListener {
            onArtistClick(artist)
        }
    }

    override fun getItemCount() = artists.size
}