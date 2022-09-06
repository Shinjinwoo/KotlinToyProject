package com.example.toyproject.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.toyproject.R
import com.example.toyproject.clickinterface.RecyclerViewClickInterface
import com.example.toyproject.clickinterface.SearchHistoryRecyViewClickInterface
import com.example.toyproject.model.Photo
import com.example.toyproject.model.SearchData
import com.example.toyproject.recyclerview.PhotoGridRecyclerViewAdapter
import com.example.toyproject.recyclerview.SearchHistoryRecyclerViewAdapter
import com.example.toyproject.retrofit.RetrofitManager
import com.example.toyproject.utils.Constants.TAG
import com.example.toyproject.utils.RESPONSE_STATE
import com.example.toyproject.utils.SharedPreferenceManager
import com.example.toyproject.utils.toSimpleString
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_photo_collection.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class PhotoCollectionActivity : AppCompatActivity(),
    RecyclerViewClickInterface,
    SearchHistoryRecyViewClickInterface,
    SearchView.OnQueryTextListener,
    CompoundButton.OnCheckedChangeListener,
    View.OnClickListener {

    //데이터
    var photoList = ArrayList<Photo>()

    //어답터
    private lateinit var photoGridRecyclerViewAdapter: PhotoGridRecyclerViewAdapter
    private lateinit var searchHistoryRecyclerViewAdapter: SearchHistoryRecyclerViewAdapter
    lateinit var getResultText: ActivityResultLauncher<Intent>

    private lateinit var mSearchView: SearchView
    private lateinit var mSearchViewEditText: EditText

    //검색 기록 배열
    private var searchHistoryList = ArrayList<SearchData>()


    //RxCompositeDisposable
    //옵저버를 통합해서 제거하기 위한 CompositeDisposable
    private var mCompositeDisposable = CompositeDisposable()

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

        setSupportActionBar(top_app_bar)


        search_history_switch.isChecked = SharedPreferenceManager.checkSearchHistoryMode()

        var bundle = intent.getBundleExtra("array_bundle")
        var searchTerm = intent.getStringExtra("search_term")



        photoList = bundle?.getSerializable("photo_array_list") as ArrayList<Photo>


        //저장된 검색 기록 가져오기
        this.searchHistoryList =
            SharedPreferenceManager.loadSearchHistoryList() as ArrayList<SearchData> /* = java.util.ArrayList<com.example.toyproject.model.SearchData> */
        this.searchHistoryList.forEach {
            Log.d(TAG, "저장된 검색 기록 - it.term : ${it.term}, it.timestamp : ${it.timestamp}")
        }
        handleSearchViewUI()

        // 검색 리사이클러 뷰 세팅
        this.searchHistoryRecyclerViewSet(this.searchHistoryList)

        // 사진 리사이클러 뷰 세팅
        this.photoSearchRecyclerViewSet(this.photoList)

        if (searchTerm != null) {
            if (searchTerm.isNotEmpty()) {
                var term = searchTerm?.let {
                    it
                } ?: ""
                this.insertSearchTermHistory(term)
            }
        }

        Log.d(
            TAG,
            "PhotoCollectionActivity - onCreate Called :::: searchTerm : $searchTerm, photoArrayList : ${photoList.count()}"
        )
        top_app_bar.title = "현재검색어 : $searchTerm"





        getResultText =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val dataUri = result.data?.data

                    Log.d(
                        TAG,
                        "PhotoCollectionActivity - onCreate Called :: getData:${dataUri.toString()}"
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

        this.mSearchView = menu?.findItem(R.id.search_menu_item)?.actionView as SearchView
        this.mSearchView.apply {

            this.queryHint = "검색어를 입력 해주세요."
            this.setOnQueryTextListener(this@PhotoCollectionActivity)
            this.setOnQueryTextFocusChangeListener { _, hasExpended ->
                when (hasExpended) {
                    true -> {
                        //search_history_view.visibility = View.VISIBLE

                        handleSearchViewUI()
                        Log.d(TAG, "서치뷰 열림 ")
                    }
                    false -> {
                        search_history_view.visibility = View.INVISIBLE
                        Log.d(TAG, "서치뷰 닫힘")
                    }
                }
            }

            mSearchViewEditText = this.findViewById(androidx.appcompat.R.id.search_src_text)
            // 써치뷰 에딧텍스트 옵저버 생성
            val editTextChangeObservable = mSearchViewEditText.textChanges()



            var searchEditTextSubscription: Disposable =
                // 옵저버블에 오퍼레이터를 추가
                editTextChangeObservable
                    .debounce(800, TimeUnit.MILLISECONDS)
                    // 글자가 입력되고 나서 0.8초에 onNext 이벤트로 데이터 흘려보내기
                    .subscribeOn(Schedulers.io())
                    //구독을 통해 이벤트 응답 받기
                    .subscribeBy(
                        onNext = {
                            Log.d("RX", "onNext : $it")
                            //들어온 이벤트 데이터로 api 호출
                            if (it.isNotEmpty()) {
                                searchPhotoFunction(it.toString())
                            }
                        },
                        onComplete = {
                            Log.d("RX", "onComplete")
                        },
                        onError = {
                            Log.d("RX", "onError : $it")
                        }
                    )

            //살아있는 옵저버블을 compositeDisposable 추가
            mCompositeDisposable.add(searchEditTextSubscription)
            //디스포서블에 대한 추가적인 학습이 필요해 보임.

            // 그리고 저는 민지가 참 좋아요 ㅎㅎㅎ
            // todo 민지랑 여행가기

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
        //코틀린 문법
        Log.d(TAG, "PhotoCollectionActivity - onQueryTextSubmit Called :: query : $query")
        if (!query.isNullOrEmpty()) {
            this.top_app_bar.title = "현재검색어 : $query"

            this.insertSearchTermHistory(query)
            this.searchPhotoFunction(query)
        }
        this.mSearchView.setQuery("", false)
        this.mSearchView.clearFocus()

        return true
    }

    //
    override fun onQueryTextChange(newText: String?): Boolean {

        var userInputText = newText?.let {
            it
        } ?: ""

        if (userInputText.count() == 12) {
            Toast.makeText(this, "검색어는 12자 까지만 입력 가능 합니다.", Toast.LENGTH_SHORT).show()
        }

        if (userInputText.length in 1..12) {
            //계속 API를 호출하게 됨
            //searchPhotoFunction(userInputText)
        }

        Log.d(TAG, "PhotoCollectionActivity - onQueryTextChange Called :: newText : $newText")
        return true
    }

    override fun onCheckedChanged(switchButtonView: CompoundButton?, isChecked: Boolean) {
        when (switchButtonView) {
            search_history_switch -> {
                if (isChecked == true) {
                    Log.d(TAG, "검색어 저장기능 온")
                    SharedPreferenceManager.setSearchHistoryMode(isActivate = true)
                } else {
                    Log.d(TAG, "검색어 저장기능 오프")
                    SharedPreferenceManager.setSearchHistoryMode(isActivate = false)

                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            clear_search_history_btn -> {
                Log.d(TAG, "검색기능 삭제 버튼 클릭")
                SharedPreferenceManager.clearSearchHistoryList()
                this.searchHistoryList.clear()
                this.mSearchView.clearFocus()
            }
        }
    }


    private fun searchPhotoFunction(userSearchInput: String) {
        RetrofitManager.instance.searchPhotos(
            searchTerm = userSearchInput,
            completion = { responseState, responseDataArrayList ->
                when (responseState) {
                    RESPONSE_STATE.SUCCESS -> {
                        Log.d(
                            TAG,
                            "PhotoCollectionActivity - 서버 리스폰스 성공 : $responseDataArrayList?.size"
                        // completion에 대한 아주 정확한 이해가 필요.

                        )


                        if (responseDataArrayList != null) {
                            this.photoList.clear()
                            this.photoList = responseDataArrayList
                            this.photoGridRecyclerViewAdapter.submitList(this.photoList)
                            this.photoGridRecyclerViewAdapter.notifyDataSetChanged()
                        }

                    }
                    RESPONSE_STATE.FAIL -> {
                        Toast.makeText(this, "서버 리스폰스 에러 입니다.", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "PhotoCollectionActivity - 서버 리스폰스 실패 : $responseDataArrayList")
                    }
                    RESPONSE_STATE.NO_COUNT -> {
                        Toast.makeText(this, "검색결과가 없습니다.", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "PhotoCollectionActivity - 검색결과가 없습니다. : $responseDataArrayList")
                    }
                }
            })
    }


    override fun onDestroy() {

        Log.d(TAG, "PhotoCollectionActivity - onDestroy Called")
        this.mCompositeDisposable.clear()
        super.onDestroy()
    }


    // 검색 기록 리사이클러 뷰 셋팅
    private fun searchHistoryRecyclerViewSet(searchHistoryList: ArrayList<SearchData>) {

        this.searchHistoryRecyclerViewAdapter = SearchHistoryRecyclerViewAdapter(this)
        this.searchHistoryRecyclerViewAdapter.submitList(searchHistoryList)

        //리버스를 하게 되면 최신 데이터가 위로 올라가게 됨 오름차순
        var linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true

        search_history_recycler_view.apply {
            layoutManager = linearLayoutManager
            this.scrollToPosition(searchHistoryRecyclerViewAdapter.itemCount - 1)
            adapter = searchHistoryRecyclerViewAdapter
        }
    }

    private fun photoSearchRecyclerViewSet(photoList: ArrayList<Photo>) {

        Log.d(TAG, "PhotoCollectionActivity - photoSearchRecyclerViewSet() called")
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
    }

    override fun onSearchItemDeleteBtnClick(position: Int) {
        //해당 포지션의 아이탬 삭제
        Log.d(TAG, "PhotoCollectionActivity - Position : $position")

        this.searchHistoryList.removeAt(position)

        SharedPreferenceManager.saveSearchHistoryList(this.searchHistoryList)
        //데이터가 변경됨을 알려줌
        this.searchHistoryRecyclerViewAdapter.notifyDataSetChanged()
        handleSearchViewUI()
    }

    override fun onSearchItemClick(position: Int) {
        //해당 포지션의 검색어로 API 호출
        Log.d(TAG, "PhotoCollectionActivity - onSearchItemClick() called")
        var queryString = this.searchHistoryList[position].term
        searchPhotoFunction(queryString)

        top_app_bar.title = queryString
        this.insertSearchTermHistory(searchTerm = queryString)
        //top_app_bar.collapseActionView()

        //this.mSearchView.setQuery("", false)
        this.mSearchView.clearFocus()

    }


    private fun handleSearchViewUI() {
        Log.d(
            TAG,
            "PhotoCollectionActivity - handleSearchViewUI() called / size : ${this.searchHistoryList.size}"
        )

        if (this.searchHistoryList.size > 0) {
            search_history_recycler_view.visibility = View.VISIBLE
            search_history_label.visibility = View.VISIBLE
            clear_search_history_btn.visibility = View.VISIBLE
        } else {
            search_history_recycler_view.visibility = View.INVISIBLE
            search_history_label.visibility = View.INVISIBLE
            clear_search_history_btn.visibility = View.INVISIBLE
        }
    }

    //검색어 저장 및 중복체크 필터링
    private fun insertSearchTermHistory(searchTerm: String) {
        if (SharedPreferenceManager.checkSearchHistoryMode() == true) {
//            var newSearchData = SearchData(term = searchTerm, timestamp = Date().toSimpleString())
            var indexListToRemove = ArrayList<Int>()

            this.searchHistoryList.forEachIndexed { index, searchDataItem ->
                Log.d(TAG, "insertSearchTermHistory(searchTerm: String) called index : $index")
                if (searchDataItem.term == searchTerm) {
                    indexListToRemove.add(index)
                }
            }
            indexListToRemove.forEach {
                this.searchHistoryList.removeAt(it)
            }

            //새 아이탬 넣기
            var newSearchData = SearchData(term = searchTerm, timestamp = Date().toSimpleString())
            this.searchHistoryList.add(newSearchData)

            //기존 데이터에 덮어쓰기
            SharedPreferenceManager.saveSearchHistoryList(this.searchHistoryList)
            this.searchHistoryRecyclerViewAdapter.notifyDataSetChanged()
        }
    }


}