package com.example.toyproject.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.toyproject.activities.PhotoCollectionActivity
import com.example.toyproject.R
import com.example.toyproject.application.App
import com.example.toyproject.clickinterface.RecyclerViewClickInterface
import com.example.toyproject.model.Photo

class PhotoGridRecyclerViewAdapter(recyclerViewClickInterface: RecyclerViewClickInterface)  : RecyclerView.Adapter<PhotoItemViewHolder>() {

    private var photoList = ArrayList<Photo>()

    var mPhotoCollectionActivity: PhotoCollectionActivity? = null;
    var mRecyclerViewClickInterface : RecyclerViewClickInterface? = null


    init {
        this.mRecyclerViewClickInterface = recyclerViewClickInterface
    }

    //뷰홀더와 레이아웃 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoItemViewHolder {

        var photoItemViewHolder = PhotoItemViewHolder(LayoutInflater
                                                                .from(App.instance)
                                                                .inflate(R.layout.layout_photo_item,parent,false),this.mRecyclerViewClickInterface!!)

        mPhotoCollectionActivity = PhotoCollectionActivity.instance

        return photoItemViewHolder
    }

    //보여줄 목록의 갯수
    override fun getItemCount(): Int {
        return this.photoList.size
    }

    // 뷰가 묶였을대 데이터를 뷰홀더에 넘겨준다.
    override fun onBindViewHolder(holder: PhotoItemViewHolder, position: Int) {
        holder.bindWithView(this.photoList[position])
    }

    // 외부에서 어답터에 데이터 배열을 넘겨준다.
    fun submitList(photoList:ArrayList<Photo>){
        this.photoList = photoList
    }




}