package com.example.toyproject.retrofit

import android.util.Log
import com.example.toyproject.utils.API
import com.example.toyproject.utils.Constants.TAG
import com.example.toyproject.utils.isJsonArray
import com.example.toyproject.utils.isJsonObject
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.concurrent.TimeUnit

//싱글턴
object RetrofitClient {
    // 레트로핏 클라이언트 선언

    private var retrofitClient: Retrofit? = null

    //레트로핏 클라이언트 가져오기

    fun getClient(baseUrl: String): Retrofit? {
        //okHttp 인스턴스 생성
        var okHttpClient = OkHttpClient.Builder()
        //로깅 인터셉터 추가
        var loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {

                when {
                    message.isJsonObject() -> {
                        Log.d(TAG, JSONObject(message).toString(4))
                    }
                    message.isJsonArray() -> {
                        Log.d(TAG, "RetrofitClient - log() called")
                    }
                    else -> {
                        try {
                            Log.d(TAG, JSONObject(message).toString(4))
                        } catch (e: Exception) {
                            Log.d(TAG, e.toString())
                        }
                    }
                }
            }
        })

        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        //인터셉터 okHttpClient에 추가
        okHttpClient.addInterceptor(loggingInterceptor)


        //기본 파라매터 인터셉터 설정
        var baseParameterInterceptor: Interceptor = (object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                Log.d(TAG, "RetrofitClient - intercept() called")
                //오리지날 리퀘스트
                var originalRequest: Request = chain.request()

                //쿼리 파라미터 추가하기
                var addClientId: HttpUrl =
                    originalRequest.url.newBuilder().addQueryParameter("client_id", API.CLIENT_ID)
                        .build()

                var resultRequest = originalRequest
                    .newBuilder()
                    .url(addClientId)
                    .method(originalRequest.method, originalRequest.body)
                    .build()

                return chain.proceed(resultRequest)
            }
        })

        okHttpClient.addInterceptor(baseParameterInterceptor)

        okHttpClient.connectTimeout(10,TimeUnit.SECONDS)
        okHttpClient.readTimeout(10,TimeUnit.SECONDS)
        okHttpClient.writeTimeout(10,TimeUnit.SECONDS)
        okHttpClient.retryOnConnectionFailure(true)


        Log.d(TAG, "RetrofitClient - getClient() called ")
        if (retrofitClient == null) {
            //빌더 패턴
            retrofitClient = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                //로깅 인터셉터를 레트로핏 클라이언트에 탑재
                .client(okHttpClient.build())
                .build()
        }
        return retrofitClient
    }
}