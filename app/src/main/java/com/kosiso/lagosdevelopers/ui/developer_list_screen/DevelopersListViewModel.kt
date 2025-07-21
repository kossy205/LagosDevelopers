package com.kosiso.lagosdevelopers.ui.developer_list_screen

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
class DevelopersListViewModel @Inject constructor(
    val repository: MainRepository
): ViewModel(){

    private val _lagosDevsFlow = MutableStateFlow<PagingData<LagosDeveloper>>(PagingData.empty())
    val lagosDevsFlow: StateFlow<PagingData<LagosDeveloper>> = _lagosDevsFlow


    init {
        getLagosDevs()
    }

    private fun getLagosDevs() {
        viewModelScope.launch {
            repository.getDevelopers()
                .cachedIn(viewModelScope)
                .collectLatest {pagingData->
                _lagosDevsFlow.value = pagingData
            }
        }
    }

    fun addToFavourites(dev: LagosDeveloper){
        viewModelScope.launch {
            val devDetails = repository.getDeveloperDetails(dev.login)
            if (devDetails is DevResponseState.Success) {
                val devDetails = devDetails.data
                val favouriteDev = FavouriteDev(
                    id = devDetails.id,
                    login = devDetails.login,
                    avatarUrl = devDetails.avatarUrl,
                    name = devDetails.name,
                    company = devDetails.company,
                    location = devDetails.location,
                    email = devDetails.email,
                    bio = devDetails.bio,
                    twitterUsername = devDetails.twitterUsername,
                    publicRepos = devDetails.publicRepos,
                    followers = devDetails.followers,
                    following = devDetails.following,
                    createdAt = devDetails.createdAt,
                    updatedAt = devDetails.updatedAt,
                    isFavourite = true
                )
                repository.addToFavorites(favouriteDev)
            }

        }
    }
}