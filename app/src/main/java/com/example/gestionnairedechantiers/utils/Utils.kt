package com.example.gestionnairedechantiers.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

fun hideKeyboard(activity: Activity) {
    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    // Check if no view has focus
    val currentFocusedView = activity.currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }


}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

data class Resource2<out T>(val data: T? = null, val message: String? = null) {

    companion object {

        fun <T> success(data: T?): Resource2<T> {
            return Resource2( data, null)
        }

        fun <T> error(msg: String, data: T?): Resource2<T> {
            return Resource2( data, msg)
        }

        fun <T> loading(data: T?): Resource2<T> {
            return Resource2( data, null)
        }

    }

}

data class State(val status: Status, val message: String? = null) {

    companion object {

        fun success(): State {
            return State(Status.SUCCESS)
        }

        fun error(msg: String): State {
            return State(Status.ERROR, msg)
        }

        fun loading(): State {
            return State(Status.LOADING)
        }

    }

}
