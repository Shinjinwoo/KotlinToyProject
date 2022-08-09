package com.example.toyproject

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.toyproject.model.Photo
import com.example.toyproject.utils.Constants.TAG

class PhotoCollectionActivity :AppCompatActivity(){

    var photoList = ArrayList<Photo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var bundle = intent.getBundleExtra("photo_array_list")
        var searchTerm = intent.getStringExtra("search_term")

        photoList = bundle?.getSerializable("array_bundle") as ArrayList<Photo>

        Log.d(TAG,"PhotoCollectionActivity - onCreate Called :::: searchTerm : $searchTerm, photoArrayList : ${photoList.count()}")



        setContentView(R.layout.activity_photo_collection)
    }
}