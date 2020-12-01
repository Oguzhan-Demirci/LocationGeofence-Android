package com.demo.locationgeo.data.listener

interface ResultListener<T> {
    fun onSuccess(result: T? = null)
    fun onFailure(message: String, exception: Exception? = null)
}