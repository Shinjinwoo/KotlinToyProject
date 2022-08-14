package com.example.toyproject.utils

import android.content.Context
import android.util.Log
import com.example.toyproject.application.App
import com.example.toyproject.model.SearchData
import com.example.toyproject.utils.Constants.TAG
import com.google.gson.Gson

//싱글턴 패턴
object SharedPreferenceManager {
    private const val SHARED_SEARCH_HISTORY="shared_search_history"
    private const val KEY_SEARCH_HISTORY = "key_search_history"

    private const val SHARED_SEARCH_HISTORY_MODE = "shared_search_history_mode"
    private const val KEY_SEARCH_HISTORY_MODE = "key_search_history_mode"
    //검색 저장 플래그 설정

    fun setSearchHistoryMode(isActivate:Boolean){
        Log.d(TAG,"SharedPreferenceManager - setSearchHistoryMode() called")

        var shared = App.instance.getSharedPreferences(SHARED_SEARCH_HISTORY_MODE, Context.MODE_PRIVATE)
        //쉐어드 에디터 가져오기
        var editor = shared.edit()

        editor.putBoolean(KEY_SEARCH_HISTORY_MODE,isActivate)
        editor.apply()
    }

    //검색어 저장모드 가져오기
    fun checkSearchHistoryMode() : Boolean {

        var shared = App.instance.getSharedPreferences(SHARED_SEARCH_HISTORY_MODE, Context.MODE_PRIVATE)
        //쉐어드 에디터 가져오기
        var isSearchHistoryMode = shared.getBoolean(KEY_SEARCH_HISTORY_MODE,true )!!


        return isSearchHistoryMode
    }

    //검색 목록 지우기
    fun clearSearchHistoryList() {
        Log.d(TAG,"SharedPreferenceManager - clearSearchHistoryList() called")

        var shared = App.instance.getSharedPreferences(SHARED_SEARCH_HISTORY, Context.MODE_PRIVATE)
        //쉐어드 에디터 가져오기
        var editor = shared.edit()

        //해당 데이터 지우기
        editor.clear()

        //변경사항 적용
        editor.apply()
    }


    //검색 목록 저장. 객체 배열을 Gson-> 통해 이용해 문자열로 변환
    fun saveSearchHistoryList(searchHistoryList : MutableList<SearchData>) {
        Log.d(TAG,"SharedPreferenceManager - storeSearchHistoryList() called")
        // 매개변수로 들어온 배열 -> 문자열로 변환
        var searchHistoryList : String = Gson().toJson(searchHistoryList)
        Log.d(TAG,"searchHistoryList - $searchHistoryList")

        //쉐어드 프리페어런스 가져오기
        var shared = App.instance.getSharedPreferences(SHARED_SEARCH_HISTORY, Context.MODE_PRIVATE)
        //쉐어드 에디터 가져오기
        var editor = shared.edit()

        editor.putString(KEY_SEARCH_HISTORY,searchHistoryList)
        editor.apply()

    }

    //검색 목록 가져오기
    fun loadSearchHistoryList() : MutableList<SearchData> {

        var shared = App.instance.getSharedPreferences(SHARED_SEARCH_HISTORY, Context.MODE_PRIVATE)
        //쉐어드 에디터 가져오기
        var saveSearchHistoryListString = shared.getString(KEY_SEARCH_HISTORY,"" )!!

        var savedSearchHistoryList = ArrayList<SearchData>()

        if (savedSearchHistoryList.isNotEmpty()) {

            // 저장된 문자열 -> 객체 배열로 Converting
            savedSearchHistoryList = Gson()
                .fromJson(saveSearchHistoryListString,Array<SearchData>::class.java)
                .toMutableList() as ArrayList<SearchData> /* = java.util.ArrayList<com.example.toyproject.model.SearchData> */
        }
        return savedSearchHistoryList
    }


}