package com.kosiso.lagosdevelopers.ui.favourites_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kosiso.lagosdevelopers.data.repository.MainRepository
import com.kosiso.lagosdevelopers.data.state.DevResponseState
import com.kosiso.lagosdevelopers.models.FavouriteDev
import com.kosiso.lagosdevelopers.models.LagosDeveloper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesListViewModel @Inject constructor(val repository: MainRepository): ViewModel() {

    private val _favouriteDevsFlow = MutableStateFlow<PagingData<FavouriteDev>>(PagingData.empty())
    val favouriteDevsFlow: StateFlow<PagingData<FavouriteDev>> = _favouriteDevsFlow

    init {
        getAllFavouriteDevs()
    }
    fun getAllFavouriteDevs(){
        viewModelScope.launch {
            repository.getAllFavDevs().cachedIn(viewModelScope)
                .collectLatest {pagingData->
                    _favouriteDevsFlow.value = pagingData
            }

        }
    }

    fun clearAllFavourites(){
        viewModelScope.launch {
            repository.clearFavourites()
            getAllFavouriteDevs()
        }
    }

    fun removeFromFavourites(devId: Long){
        viewModelScope.launch {
            repository.removeFavDev(devId)
        }
    }
}