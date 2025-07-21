package com.kosiso.lagosdevelopers.data.state

sealed class DevResponseState<out T> {
    object Loading : DevResponseState<Nothing>()
    data class Success<out T>(val data: T) : DevResponseState<T>()
    data class Error(val message: String) : DevResponseState<Nothing>()
}
