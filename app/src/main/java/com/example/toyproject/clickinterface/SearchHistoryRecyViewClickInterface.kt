package com.example.toyproject.clickinterface

import java.text.FieldPosition

interface SearchHistoryRecyViewClickInterface {

    //검색 아이템 삭제 버튼 클릭
    fun onSearchItemDeleteBtnClick(position: Int)

    //검색 버튼 클릭
    fun onSearchItemClick(position: Int)
}