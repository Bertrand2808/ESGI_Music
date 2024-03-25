package com.example.esgimusic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.esgimusic.adapter.CategoryAdapter
import com.example.esgimusic.databinding.ActivityLibraryBinding
import com.example.esgimusic.models.CategoryModel
import com.google.firebase.firestore.FirebaseFirestore

class LibraryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLibraryBinding
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initCategories()
        binding.bottomNavigation.menu.findItem(R.id.navigation_library).isChecked = true
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId) {
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

    private fun initCategories() {
        FirebaseFirestore.getInstance().collection("category")
            .get()
            .addOnSuccessListener { documents ->
                val categories = documents.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categories)
            }
            .addOnFailureListener { exception ->
                Log.e("LibraryActivity", "Error getting categories", exception)
            }
    }

    private fun setupCategoryRecyclerView(categoryList: List<CategoryModel>) {
        categoryAdapter = CategoryAdapter(categoryList)
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }
}
