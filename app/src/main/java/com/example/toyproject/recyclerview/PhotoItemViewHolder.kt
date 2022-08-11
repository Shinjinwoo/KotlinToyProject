package com.example.toyproject.recyclerview

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.toyproject.R
import com.example.toyproject.application.App
import com.example.toyproject.clickinterface.RecyclerViewClickInterface
import com.example.toyproject.model.Photo
import com.example.toyproject.utils.Constants.TAG
import kotlinx.android.synthetic.main.layout_photo_item.*
import kotlinx.android.synthetic.main.layout_photo_item.view.*

class PhotoItemViewHolder(itemView: View,recyclerViewClickInterface : RecyclerViewClickInterface) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
    // 뷰들을 가져온다
    private var photoImageView = itemView.photo_image
    private var photoCreatedAtText = itemView.created_at_text
    private val photoLikesCountText = itemView.likes_count_text


    private var mRecyclerViewInterface : RecyclerViewClickInterface? = null


    init {
        Log.d(TAG, "PhotoItemViewHolder - init() called")

        itemView.setOnClickListener(this)
        this.mRecyclerViewInterface = recyclerViewClickInterface
    }

    // 데이터와 뷰 바인딩
    fun bindWithView(photoItem : Photo) {

        photoCreatedAtText.text = photoItem.createdAt
        photoLikesCountText.text = photoItem.likesCount.toString()

        // 글라이드 사용



        Glide.with(App.instance!!)
            .load(photoItem.thumbnail)
            .placeholder(R.drawable.ic_baseline_insert_photo_24)
            .into(photoImageView);

    }

    override fun onClick(p0: View?) {
        Log.d(TAG,"MyViewHolder - onClick() called")
        this.mRecyclerViewInterface?.onItemClicked(adapterPosition)
    }

}