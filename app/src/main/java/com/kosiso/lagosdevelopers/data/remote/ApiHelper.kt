package com.kosiso.lagosdevelopers.data.remote

import android.util.Log
import com.kosiso.lagosdevelopers.data.state.DevResponseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ApiHelper {

    suspend fun <T> safeApiCall(apiCall: suspend () -> DevResponseState<T>): DevResponseState<T> {
        return try {
            withContext(Dispatchers.IO) {
                apiCall()
            }
        } catch (e: HttpException) {
            Log.e("HttpException error", e.message, e)
            DevResponseState.Error("http error: ${e.message}")
        } catch (e: IOException) {
            Log.e("IOException error", "IOException error", e)
            DevResponseState.Error(e.message.toString())
        } catch (e: Exception) {
            Log.e("api Exception error", e.message, e)
            DevResponseState.Error("Unexpected error: ${e.message}")
        }
    }

}