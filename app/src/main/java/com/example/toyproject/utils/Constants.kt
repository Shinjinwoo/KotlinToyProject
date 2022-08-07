package com.example.toyproject.utils

object Constants {
    const val TAG = "로그"
}

enum class SEARCH_TYPE {
    PHOTO,
    USER
}

enum class RESPONSE_STATE {
    SUCCESS,
    FAIL,
    NO_COUNT
}

object API {
    const val BASE_URL = "https://api.unsplash.com"
    const val CLIENT_ID = "MKRi12N0beJLCLuVHYznx5PcMwkKpt3DCbTPl6-HwSQ"
    const val SEARCH_PHOTO = "/search/photos"
    const val SEARCH_USERS = "search/users"
}

