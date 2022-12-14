package com.example.toyproject.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.toyproject.R
import com.example.toyproject.retrofit.RetrofitManager
import com.example.toyproject.utils.Constants.TAG
import com.example.toyproject.utils.RESPONSE_STATE
import com.example.toyproject.utils.SEARCH_TYPE
import com.example.toyproject.utils.onMyTextChanged
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_button_search.*


class MainActivity : AppCompatActivity() {

    private var currentSearchType : SEARCH_TYPE = SEARCH_TYPE.PHOTO

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG,"MainActivity - onCreate Called")


        // 라디오 그룹 가져오기
        search_term_radio_group.setOnCheckedChangeListener{_,checkedId->
            when(checkedId) {
                R.id.photo_search_radio_btn ->{
                    Log.d(TAG,"사진검색버튼 클릭")
                    search_term_text_layout.hint = "사진검색"
                    search_term_text_layout.startIconDrawable = resources.getDrawable(R.drawable.ic_baseline_photo_library_24,resources.newTheme())
                    this.currentSearchType = SEARCH_TYPE.PHOTO
                }
                R.id.user_search_radio_btn -> {
                    Log.d(TAG,"사용자검색 클릭")
                    search_term_text_layout.hint = "사용자 검색"
                    search_term_text_layout.startIconDrawable = resources.getDrawable(R.drawable.ic_user,resources.newTheme())
                    this.currentSearchType = SEARCH_TYPE.USER
                }
            }
            Log.d(TAG,"MainActivity - setOnCheckedChangeListener / currentSearchType : $currentSearchType")
        }

        //텍스트가 변경이 되었을때
        search_term_edit_text.onMyTextChanged {
            // 입력된 글자 하나 이상일시
            if(it.toString().count() > 0 ) {
                // 검색 버튼 보여주기
                frame_search.visibility = View.VISIBLE
                // 스크롤뷰 올리기.
                main_scrollview.scrollTo(0,200)
                search_term_text_layout.helperText = " "
            } else {
                frame_search.visibility = View.INVISIBLE
            }

            if ( it.toString().count() == 12 ){
                Log.d(TAG,"MainActivity - 글자수 12자 이상 입력")
                Toast.makeText(this,"검색어는 12자 이상 입력 하실 수 없습니다.",Toast.LENGTH_SHORT).show()
            }
        }

        // 검색 버튼 클릭시
        btn_search.setOnClickListener{
            Log.d(TAG,"MainActivity - 검색 버튼이 클릭됨 / currentSearchType : $currentSearchType")

            var userSearchInput = search_term_edit_text.text.toString()
            //검색 API 호출

            searchPhotoFunction(userSearchInput)

            this.handleSearchButtonUi()
        }
    }


    private fun handleSearchButtonUi() {
        btn_search_progressbar.visibility = View.VISIBLE
        btn_search.text = ""

        Handler().postDelayed({
            btn_search_progressbar.visibility = View.INVISIBLE
            btn_search.text = "검색"
        },1000)
    }

    private fun searchPhotoFunction(userSearchInput :String) {
        RetrofitManager.instance.searchPhotos(searchTerm = userSearchInput , completion = {
                responseState,responseDataArrayList ->
            when(responseState){
                RESPONSE_STATE.SUCCESS -> {
                    Log.d(TAG,"MainActivity - 서버 리스폰스 성공 : $responseDataArrayList?.size")
                    var intent = Intent(this, PhotoCollectionActivity::class.java)
                    var bundle = Bundle()

                    bundle.putSerializable("photo_array_list",responseDataArrayList)


                    intent.putExtra("array_bundle",bundle)
                    intent.putExtra("search_term",userSearchInput)

                    startActivity(intent)
                }
                RESPONSE_STATE.FAIL -> {
                    Toast.makeText(this,"서버 리스폰스 에러 입니다.",Toast.LENGTH_SHORT).show()
                    Log.d(TAG,"MainActivity - 서버 리스폰스 실패 : $responseDataArrayList")
                }
                RESPONSE_STATE.NO_COUNT -> {
                    Toast.makeText(this,"검색결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    Log.d(TAG,"MainActivity - 검색결과가 없습니다. : $responseDataArrayList")
                }
            }
        })
    }
}