package com.example.weatherapp.util

sealed class DataState<out R> {
    data class Success<out R>(val data: R) : DataState<R>()
    data class Error(val exception: Exception) : DataState<Nothing>()
    object Loading : DataState<Nothing>()
    object Empty : DataState<Nothing>()
}