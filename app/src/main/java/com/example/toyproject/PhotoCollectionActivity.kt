package com.example.toyproject

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.GridLayoutManager
import com.example.toyproject.application.App
import com.example.toyproject.clickinterface.RecyclerViewClickInterface
import com.example.toyproject.model.Photo
import com.example.toyproject.recyclerview.PhotoGridRecyclerViewAdapter
import com.example.toyproject.retrofit.RetrofitManager
import com.example.toyproject.utils.Constants.TAG
import com.example.toyproject.utils.RESPONSE_STATE
import kotlinx.android.synthetic.main.activity_photo_collection.*
import kotlinx.android.synthetic.main.layout_photo_item.*

class PhotoCollectionActivity : AppCompatActivity(),
    RecyclerViewClickInterface,
    SearchView.OnQueryTextListener,
    CompoundButton.OnCheckedChangeListener,
    View.OnClickListener
{

    //데이터
    var photoList = ArrayList<Photo>()

    //어답터
    private lateinit var photoGridRecyclerViewAdapter: PhotoGridRecyclerViewAdapter
    lateinit var getResultText: ActivityResultLauncher<Intent>

    private lateinit var mSearchView: SearchView
    private lateinit var mSearchViewEditText: EditText

    //이 Activity에 대한 컨텍스트
    companion object {
        var instance: PhotoCollectionActivity? = null
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_collection)


        search_history_switch.setOnCheckedChangeListener(this)
        search_history_mode_switch_label.setOnClickListener(this)
        clear_search_history_btn.setOnClickListener(this)

        var bundle = intent.getBundleExtra("array_bundle")
        var searchTerm = intent.getStringExtra("search_term")

        photoList = bundle?.getSerializable("photo_array_list") as ArrayList<Photo>

        Log.d(
            TAG,
            "PhotoCollectionActivity - onCreate Called :::: searchTerm : $searchTerm, photoArrayList : ${photoList.count()}"
        )


        top_app_bar.title = "현재검색어 : $searchTerm"

        setSupportActionBar(top_app_bar)

        this.photoGridRecyclerViewAdapter = PhotoGridRecyclerViewAdapter(this)
        this.photoGridRecyclerViewAdapter.submitList(photoList)

        /**
         * @param1 Context
         * @param2 Span 줄 수
         * @param3 Vertical,Horizon
         * @param4 데이터 출력방향,
         */
        my_photo_recyclerview.layoutManager =
            GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        my_photo_recyclerview.adapter = this.photoGridRecyclerViewAdapter

        getResultText =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val dataUri = result.data?.data

                    Log.d(TAG, "PhotoCollectionActivity - onCreate Called :: getData:${dataUri.toString()}"
                    )
                }
            }
    }

    override fun onItemClicked(position: Int) {
        startDefaultGalleryApp()
    }


    private fun startDefaultGalleryApp() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        getResultText.launch(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        Log.d(TAG, "PhotoCollectionActivity - onCreateOptionsMenu Called")

        var inflater = menuInflater

        inflater.inflate(R.menu.top_app_bar_menu, menu)

        var searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        this.mSearchView = menu?.findItem(R.id.search_menu_item)?.actionView as SearchView
        this.mSearchView.apply {

            this.queryHint = "검색어를 입력 해주세요."
            this.setOnQueryTextListener(this@PhotoCollectionActivity)
            this.setOnQueryTextFocusChangeListener { _, hasExpended ->
                when (hasExpended) {
                    true -> {
                        search_history_view.visibility = View.VISIBLE
                        Log.d(TAG, "서치뷰 열림 ")
                    }
                    false -> {
                        search_history_view.visibility = View.INVISIBLE
                        Log.d(TAG, "서치뷰 닫힘")
                    }
                }
            }

            mSearchViewEditText = this.findViewById(androidx.appcompat.R.id.search_src_text)
        }

        this.mSearchViewEditText.apply {
            this.filters = arrayOf(InputFilter.LengthFilter(12))
            this.setTextColor(Color.WHITE)
            this.setHintTextColor(Color.WHITE)
        }



        return true
    }

    //서치뷰 검색 이벤트
    //검색 버튼 클릭이 되었을때
    override fun onQueryTextSubmit(query: String?): Boolean {
        Log.d(TAG, "PhotoCollectionActivity - onQueryTextSubmit Called :: query : $query")
        if (!query.isNullOrEmpty()) {
            this.top_app_bar.title = "현재검색어 : $query"

            searchPhotoFunction(query)
        }
        this.mSearchView.setQuery("",false)
        this.mSearchView.clearFocus()
        this.top_app_bar.collapseActionView()

        return true
    }

    //
    override fun onQueryTextChange(newText: String?): Boolean {

        var userInputText = newText ?.let {
            it
        }?: ""

        if (userInputText.count() == 12) {
            Toast.makeText(this,"검색어는 12자 까지만 입력 가능 합니다.",Toast.LENGTH_SHORT).show()
        }

        Log.d(TAG, "PhotoCollectionActivity - onQueryTextChange Called :: newText : $newText")
        return true
    }

    override fun onCheckedChanged(switchButtonView: CompoundButton?, isChecked: Boolean) {
        when (switchButtonView) {
            search_history_switch -> {
                if (isChecked == true) {
                    Log.d(TAG, "검색어 저장기능 온")
                } else {
                    Log.d(TAG,"검색어 저장기능 오프")
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when(view) {
            clear_search_history_btn-> {
                Log.d(TAG,"검색기능 삭제 버튼 클릭")
            }
        }
    }


    private fun searchPhotoFunction(userSearchInput: String ) {
        RetrofitManager.instance.searchPhotos(searchTerm = userSearchInput , completion = {
                responseState,responseDataArrayList ->
            when(responseState){
                RESPONSE_STATE.SUCCESS -> {
                    Log.d(TAG,"MainActivity - 서버 리스폰스 성공 : $responseDataArrayList?.size")
                    var intent = Intent(this,PhotoCollectionActivity::class.java)
                    var bundle = Bundle()

                    bundle.putSerializable("photo_array_list",responseDataArrayList)


                    intent.putExtra("array_bundle",bundle)
                    intent.putExtra("search_term",userSearchInput)

                    startActivity(intent)
                    finish()

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


    override fun onDestroy() {
        
        Log.d(TAG,"PhotoCollectionActivity - onDestroy Called")
        super.onDestroy()
    }
}