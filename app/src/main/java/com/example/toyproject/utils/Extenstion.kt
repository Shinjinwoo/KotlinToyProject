package com.example.toyproject.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

//스트링에 대한 익스텐션 제이슨 배열인 아닌지

fun String?.isJsonObject():Boolean {
    if(this?.startsWith("{") == true && this.endsWith("}")){
        return true
    } else {
        return false
    }
}

fun String?.isJsonArray():Boolean {
    if(this?.startsWith("[") == true && this.endsWith("]")){
        return true
    }else {
        return false
    }
}


//에딧 텍스트에 대한 익스텐션
fun EditText.onMyTextChanged(completion : (Editable?) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(editable: Editable?) {
            completion(editable)
        }
    })
}