package com.example.toyproject.retrofit

import android.util.Log
import com.example.toyproject.utils.API
import com.example.toyproject.utils.Constants
import com.example.toyproject.utils.Constants.TAG
import com.example.toyproject.utils.RESPONSE_STATE
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit

class RetrofitManager {
    companion object {
        var instance = RetrofitManager()
    }

    //HttpCall 만들기
    //레트로핏 인터페이스 가져오기
    private val retrofitInterface: RetrofitInterface? =
        RetrofitClient.getClient(API.BASE_URL)?.create(RetrofitInterface::class.java)

    //사진검색 API 호출
    fun searchPhotos(searchTerm: String?, completion: (RESPONSE_STATE,String) -> Unit) {
        var term = searchTerm.let {
            it
        } ?: ""

        var call = retrofitInterface?.searchPhoto(searchTerm = term).let {
            it
        }?:return

        call.enqueue(object:retrofit2.Callback<JsonElement>{
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                Log.d(TAG,"RetrofitManager - onResponse() called / ::: 응답 성공 / t : ${response.body()}" )
                completion(RESPONSE_STATE.SUCCESS,response.body().toString())
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Log.d(TAG,"RetrofitManager - onFailure() called / ::: 응답 실패 / t : $t")
                completion(RESPONSE_STATE.FAIL,t.toString())
            }
        })
    }

}