package com.example.toyproject.retrofit

import android.annotation.SuppressLint
import android.util.Log
import com.example.toyproject.model.Photo
import com.example.toyproject.utils.API
import com.example.toyproject.utils.Constants
import com.example.toyproject.utils.Constants.TAG
import com.example.toyproject.utils.RESPONSE_STATE
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat

class RetrofitManager {
    companion object {
        var instance = RetrofitManager()
    }

    //HttpCall 만들기
    //레트로핏 인터페이스 가져오기
    private val retrofitInterface: RetrofitInterface? =
        RetrofitClient.getClient(API.BASE_URL)?.create(RetrofitInterface::class.java)

    //사진검색 API 호출
    fun searchPhotos(searchTerm: String?, completion: (RESPONSE_STATE, ArrayList<Photo>?) -> Unit) {
        var term = searchTerm.let {
            it
        } ?: ""

        var call = retrofitInterface?.searchPhoto(searchTerm = term).let {
            it
        } ?: return

        call.enqueue(object : retrofit2.Callback<JsonElement> {
            @SuppressLint("SimpleDateFormat")
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                Log.d(
                    TAG,
                    "RetrofitManager - onResponse() called / ::: 응답 성공 / t : ${response.body()}"
                )

                when (response.code()) {
                    200 -> {

                        response.body()?.let {
                            var body = it.asJsonObject
                            var parsedPhotoArrayList = ArrayList<Photo>()

                            var total: Int? = body.get("total").asInt
                            var results: JsonArray = body.getAsJsonArray("results")

                            if (total == 0) {
                                completion(RESPONSE_STATE.NO_COUNT, null)
                            } else {
                                results.forEach { resultItem ->
                                    var resultItemObject = resultItem.asJsonObject
                                    var user = resultItemObject.get("user").asJsonObject

                                    var userName: String = user.get("username").asString
                                    var likesCount = resultItemObject.get("likes").asInt
                                    var thumbnailLink =
                                        resultItemObject.get("urls").asJsonObject.get("thumb").asString
                                    var createAt = resultItemObject.get("created_at").asString

                                    var dateParser = SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss")
                                    var formatter = SimpleDateFormat("yyyy년\nMM월 dd일")

                                    var outputDateString = formatter.format(dateParser.parse(createAt))

                                    var photoItem = Photo(
                                        author = userName,
                                        likesCount = likesCount,
                                        thumbnail = thumbnailLink,
                                        createdAt = outputDateString
                                    )
                                    parsedPhotoArrayList.add(photoItem)
                                }
                                completion(RESPONSE_STATE.SUCCESS, parsedPhotoArrayList)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Log.d(TAG, "RetrofitManager - onFailure() called / ::: 응답 실패 / t : $t")
                completion(RESPONSE_STATE.FAIL, null)
            }
        })
    }

}