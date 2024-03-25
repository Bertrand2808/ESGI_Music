package com.example.esgimusic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.esgimusic.adapter.SongsListAdapter
import com.example.esgimusic.databinding.ActivitySongsListBinding
import com.example.esgimusic.models.CategoryModel

class SongsListActivity : AppCompatActivity() {
    companion object {
        lateinit var category : CategoryModel
    }
    lateinit var binding: ActivitySongsListBinding
    lateinit var songsListAdapter: SongsListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.nameTextView.text = category.name
        Glide.with(binding.coverImageView).load(category.coverUrl)
            .apply(
                RequestOptions().transform(RoundedCorners(32))
            )
            .into(binding.coverImageView)
        setupSongsListRecyclerView()

        binding.bottomNavigation.menu.findItem(R.id.navigation_library).isChecked = true
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_library -> {
                    val intent = Intent(this, LibraryActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_record -> {
                    val intent = Intent(this, RecordActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    fun setupSongsListRecyclerView(){
        songsListAdapter = SongsListAdapter(category.songs)
        binding.songsListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.songsListRecyclerView.adapter = songsListAdapter
    }
}