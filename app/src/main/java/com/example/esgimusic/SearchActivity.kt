package com.example.esgimusic

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.esgimusic.adapter.SearchResultsAdapter
import com.example.esgimusic.databinding.ActivitySearchBinding
import com.example.esgimusic.models.SongsModel
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private var searchResultsAdapter: SearchResultsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        binding.bottomNavigation.menu.findItem(R.id.navigation_search).isChecked = true
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
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

    private fun setupRecyclerView() {
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchResultsAdapter = SearchResultsAdapter(listOf())
        binding.searchResultsRecyclerView.adapter = searchResultsAdapter
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    private fun performSearch(query: String) {
        FirebaseFirestore.getInstance().collection("songs")
            .get()
            .addOnSuccessListener { documents ->
                val songs = documents.toObjects(SongsModel::class.java)
                // Filtre les chansons si la requête n'est pas vide, sinon affiche toutes les chansons
                val filteredSongs = if (query.isNotEmpty()) {
                    songs.filter { it.title.contains(query, ignoreCase = true) }
                } else {
                    songs
                }
                searchResultsAdapter?.updateSongsList(filteredSongs) { isEmpty ->
                    binding.noResultsTextView.visibility = if (isEmpty) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("SearchActivity", "Erreur lors de la récupération des documents", exception)
            }
    }


}
