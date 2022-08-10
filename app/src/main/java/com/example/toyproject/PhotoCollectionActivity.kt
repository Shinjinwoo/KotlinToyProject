package com.example.toyproject

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.toyproject.model.Photo
import com.example.toyproject.recyclerview.PhotoGridRecyclerViewAdapter
import com.example.toyproject.utils.Constants.TAG
import kotlinx.android.synthetic.main.activity_photo_collection.*

class PhotoCollectionActivity :AppCompatActivity(){

    //데이터
    var photoList = ArrayList<Photo>()

    //어답터
    private lateinit var photoGridRecyclerViewAdapter: PhotoGridRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_collection)


        var bundle = intent.getBundleExtra("array_bundle")
        var searchTerm = intent.getStringExtra("search_term")

        photoList = bundle?.getSerializable("photo_array_list") as ArrayList<Photo>

        Log.d(TAG,"PhotoCollectionActivity - onCreate Called :::: searchTerm : $searchTerm, photoArrayList : ${photoList.count()}")


        top_app_bar.title = "현재검색어 : " + searchTerm

        this.photoGridRecyclerViewAdapter = PhotoGridRecyclerViewAdapter()
        this.photoGridRecyclerViewAdapter.submitList(photoList)

        /**
         * @param1 Context
         * @param2 Span 줄 수
         * @param3 Vertical,Horizon
         * @param4 데이터 출력방향,
         */
        my_photo_recyclerview.layoutManager = GridLayoutManager(this,2, GridLayoutManager.VERTICAL,false)
        my_photo_recyclerview.adapter = this.photoGridRecyclerViewAdapter

    }

}