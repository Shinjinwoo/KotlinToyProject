package com.example.toyproject.recyclerview

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.toyproject.model.SearchData
import com.example.toyproject.utils.Constants.TAG
import kotlinx.android.synthetic.main.layout_search_item.view.*

class SearchItemViewHolder(itemView: View)
                        : RecyclerView.ViewHolder(itemView),
                            View.OnClickListener{

    // XML 뷰 가져오기
    private var searchTermTextView = itemView.search_term_text
    private var whenSearchedTextView = itemView.when_searched_text
    private var deleteSearchBtn = itemView.delete_search_btn
    private var constraintSearchItem = itemView.constraint_search_item


    init {
        deleteSearchBtn.setOnClickListener(this)
        constraintSearchItem.setOnClickListener(this)
    }

    // 데이터와 뷰를 묶는 행위
    fun bindWithView(searchItem:SearchData) {
        Log.d(TAG,"SearchItemViewHolder - bindWithView() called")
        whenSearchedTextView.text = searchItem.timestamp
        searchTermTextView.text = searchItem.term


    }

    override fun onClick(view: View?) {
        Log.d(TAG,"SearchItemViewHolder - onClick() called")

        when(view){
            deleteSearchBtn -> {
                Log.d(TAG,"SearchItemViewHolder - 삭제버튼 클릭")
            }
            constraintSearchItem -> {
                Log.d(TAG,"SearchItemViewHolder -  검색 아이템 클릭")
            }
            whenSearchedTextView -> {

            }
        }
    }
}