package com.example.toyproject.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.toyproject.R
import com.example.toyproject.application.App
import com.example.toyproject.model.Photo
import kotlinx.android.synthetic.main.layout_photo_item.view.*

class PhotoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    // 뷰들을 가져온다
    private var photoImageView = itemView.photo_image
    private var photoCreatedAtText = itemView.created_at_text
    private val photoLikesCountText = itemView.likes_count_text

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

}