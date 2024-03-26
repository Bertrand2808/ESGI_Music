package com.example.esgimusic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.esgimusic.adapter.CategoryAdapter
import com.example.esgimusic.databinding.ActivityMainBinding
import com.example.esgimusic.models.CategoryModel
import com.example.esgimusic.models.SongsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var categoryAdapter: CategoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getCategories()
        setupSections("section_1", binding.section1MainLayout, binding.section1Title, binding.section1RecyclerView)
        setupSections("section_2", binding.section2MainLayout, binding.section2Title, binding.section2RecyclerView)
        setupSections("section_3", binding.section3MainLayout, binding.section3Title, binding.section3RecyclerView)
        setupMostlyPlayedSection("mostly_played", binding.mostlyPlayedMainLayout, binding.mostlyPlayedTitle, binding.mostlyPlayedRecyclerView)

        binding.optionBtn.setOnClickListener {
            showPopupMenu()
        }
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navigation_home -> {
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

    fun showPopupMenu() {
        val popupMenu = PopupMenu(this, binding.optionBtn)
        val inflator = popupMenu.menuInflater
        inflator.inflate(R.menu.option_menu, popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.logout -> {
                    logout()
                    true
                }
            }
            false
        }
    }

    fun logout() {
        MyExoplayer.getInstance()?.release()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    override fun onResume() {
        super.onResume()
        showPlayerView()
    }
    fun showPlayerView(){
        binding.playerView.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
        }
        MyExoplayer.getCurrentSong()?.let {
            binding.playerView.visibility = View.VISIBLE
            binding.songTitleTextView.text = "Now playing : " + it.title
            Glide.with(binding.songCoverImageView.context).load(it.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                ).into(binding.songCoverImageView)
        }?: run {
            binding.playerView.visibility = View.GONE
        }
    }
    fun getCategories(){
        FirebaseFirestore.getInstance().collection("category")
            .get().addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
                Log.wtf("wtf", "categoryList : $categoryList")
            }.addOnFailureListener {
                Log.wtf("wtf", "Error getting documents.", it)
            }
    }

    fun setupCategoryRecyclerView(categoryList: List<CategoryModel>){
        categoryAdapter = CategoryAdapter(categoryList)
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }

    // Sections
    fun setupSections(id: String, mainLayout: RelativeLayout, titleView: TextView, recyclerView: RecyclerView) {
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                val section = it.toObject(CategoryModel::class.java)
                section?.apply {
                    mainLayout.visibility = View.VISIBLE
                    titleView.text = name
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                    recyclerView.adapter = SectionSongListAdapter(songs)
                    mainLayout.setOnClickListener {
                        SongsListActivity.category = section
                        startActivity(Intent(this@MainActivity, SongsListActivity::class.java))
                    }
                }
            }
    }

    fun setupMostlyPlayedSection(id: String, mainLayout: RelativeLayout, titleView: TextView, recyclerView: RecyclerView) {
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                // get most played songs
                FirebaseFirestore.getInstance().collection("songs")
                    .orderBy("count", Query.Direction.DESCENDING)
                    .limit(5)
                    .get().addOnSuccessListener {songListSnapshot ->
                        val songsModel = songListSnapshot.toObjects<SongsModel>()
                        val songsIdList = songsModel.map {
                            it.id
                        }.toList()
                        val section = it.toObject(CategoryModel::class.java)
                        section?.apply {
                            section.songs = songsIdList
                            mainLayout.visibility = View.VISIBLE
                            titleView.text = name
                            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                            recyclerView.adapter = SectionSongListAdapter(songs)
                            mainLayout.setOnClickListener {
                                SongsListActivity.category = section
                                startActivity(Intent(this@MainActivity, SongsListActivity::class.java))
                            }
                        }
                    }
            }
    }
}