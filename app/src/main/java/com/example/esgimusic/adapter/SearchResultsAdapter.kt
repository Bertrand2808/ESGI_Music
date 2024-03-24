package com.example.esgimusic.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.esgimusic.MyExoplayer
import com.example.esgimusic.PlayerActivity
import com.example.esgimusic.databinding.SongListItemRecyclerRowBinding
import com.example.esgimusic.models.SongsModel

class SearchResultsAdapter(private var songsList: List<SongsModel>) :
    RecyclerView.Adapter<SearchResultsAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding: SongListItemRecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(song: SongsModel) {
            binding.songTitleTextView.text = song.title
            binding.songSubtitleTextView.text = song.subtitle
            Glide.with(binding.songCoverImageView.context).load(song.coverUrl)
                .apply(RequestOptions().transform(RoundedCorners(32)))
                .into(binding.songCoverImageView)

            binding.root.setOnClickListener {
                MyExoplayer.startPlaying(binding.root.context, song)
                it.context.startActivity(Intent(it.context, PlayerActivity::class.java))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = SongListItemRecyclerRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(songsList[position])
    }

    override fun getItemCount() = songsList.size

    fun updateSongsList(newSongsList: List<SongsModel>, onResultEmpty: (Boolean) -> Unit) {
        this.songsList = newSongsList
        notifyDataSetChanged()
        onResultEmpty(newSongsList.isEmpty())
    }
}
