package com.example.esgimusic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.example.esgimusic.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    lateinit var binding: ActivityPlayerBinding
    lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        MyExoplayer.getCurrentSong()?.apply {
            binding.songTitleTextView.text = title
            binding.songSubtitleTextView.text  = subtitle
            Glide.with(binding.songCoverImageView).load(coverUrl)
                .circleCrop()
                .into(binding.songCoverImageView)
            exoPlayer = MyExoplayer.getInstance()!!
            binding.playerView.player = exoPlayer

        }

    }
}