package com.example.esgimusic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.esgimusic.databinding.ActivityMainBinding

class PlayerActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}