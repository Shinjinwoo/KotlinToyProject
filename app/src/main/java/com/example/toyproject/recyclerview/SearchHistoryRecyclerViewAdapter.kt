package com.example.toyproject.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.toyproject.R
import com.example.toyproject.model.SearchData

class SearchHistoryRecyclerViewAdapter : RecyclerView.Adapter<SearchItemViewHolder>() {

    private var searchHistoryList: ArrayList<SearchData> = ArrayList()

    //뷰홀더가 메모리에 올라갔을때 뷰홀더와 레이아웃을 연결 시킴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {
        var searchItemViewHolder = SearchItemViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.layout_search_item,parent,false)
        )
        return searchItemViewHolder
    }

    override fun getItemCount(): Int {
        return searchHistoryList.size
    }

    override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {

    }

    //외부에서 데이타 어답터 배열을 넣어줌
    fun submitList(searchHistoryList:ArrayList<SearchData>){
        this.searchHistoryList = searchHistoryList
    }

}