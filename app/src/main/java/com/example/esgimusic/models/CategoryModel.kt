package com.example.esgimusic.models

data class CategoryModel(
    val name: String,
    val coverUrl: String,
    val songs: List<String>
){
    constructor() : this("", "", listOf())
}
