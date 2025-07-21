package com.kosiso.lagosdevelopers.ui.developer_details_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kosiso.lagosdevelopers.data.repository.MainRepository
import com.kosiso.lagosdevelopers.data.state.DevResponseState
import com.kosiso.lagosdevelopers.models.FavouriteDev
import com.kosiso.lagosdevelopers.models.LagosDeveloper
import com.kosiso.lagosdevelopers.ui.favourites_screen.FavouritesScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeveloperDetailsViewModel @Inject constructor(val repository: MainRepository) : ViewModel() {

    private val _isFavourite = MutableStateFlow(false)
    val isFavourite: StateFlow<Boolean> = _isFavourite

    private val _insertOrRemoveFromFavState = MutableStateFlow<DevResponseState<String>>(DevResponseState.Loading)
    val insertOrRemoveFromFavState: StateFlow<DevResponseState<String>> = _insertOrRemoveFromFavState

    private val _developerState = MutableStateFlow<DevResponseState<FavouriteDev>>(DevResponseState.Loading)
    val developerState: StateFlow<DevResponseState<FavouriteDev>> = _developerState


    fun getDeveloperDetails(dev: LagosDeveloper){
        viewModelScope.launch {
            val devFromLocalDb = repository.getFavDevById(dev.id)

            if(devFromLocalDb != null){
                val favouriteDev = FavouriteDev(
                    id = devFromLocalDb.id,
                    login = devFromLocalDb.login,
                    avatarUrl = devFromLocalDb.avatarUrl,
                    name = devFromLocalDb.name,
                    company = devFromLocalDb.company,
                    location = devFromLocalDb.location,
                    email = devFromLocalDb.email,
                    bio = devFromLocalDb.bio,
                    twitterUsername = devFromLocalDb.twitterUsername,
                    publicRepos = devFromLocalDb.publicRepos,
                    followers = devFromLocalDb.followers,
                    following = devFromLocalDb.following,
                    createdAt = devFromLocalDb.createdAt,
                    updatedAt = devFromLocalDb.updatedAt,
                    isFavourite = true
                )
                _developerState.value = DevResponseState.Success(favouriteDev)
                _isFavourite.value = true
            }else{
                val devFromApiState = repository.getDeveloperDetails(dev.login)
                if(devFromApiState is DevResponseState.Success){
                    val devFromApi = devFromApiState.data
                    val favouriteDev = FavouriteDev(
                        id = devFromApi.id,
                        login = devFromApi.login,
                        avatarUrl = devFromApi.avatarUrl,
                        name = devFromApi.name,
                        company = devFromApi.company,
                        location = devFromApi.location,
                        email = devFromApi.email,
                        bio = devFromApi.bio,
                        twitterUsername = devFromApi.twitterUsername,
                        publicRepos = devFromApi.publicRepos,
                        followers = devFromApi.followers,
                        following = devFromApi.following,
                        createdAt = devFromApi.createdAt,
                        updatedAt = devFromApi.updatedAt,
                        isFavourite = false
                    )
                    _developerState.value = DevResponseState.Success(favouriteDev)
                    _isFavourite.value = false
                }else{
                    _developerState.value = DevResponseState.Error("Error fetching developer details")
                }
            }
        }
    }

    fun removeFromFavourites(dev: LagosDeveloper){
        viewModelScope.launch {
            repository.removeFavDev(dev.id)
            _insertOrRemoveFromFavState.value = DevResponseState.Success("Removed from favourites")
        }
    }

    fun insertIntoFavourites(dev: LagosDeveloper){
        viewModelScope.launch {
            val developerState = _developerState.value
            if(developerState is DevResponseState.Success){
                val developer = developerState.data
                repository.addToFavorites(developer)
                _insertOrRemoveFromFavState.value = DevResponseState.Success("Added to favourites")
            }
        }
    }

    fun setIsFavourite(isFavourite: Boolean){
        _isFavourite.value = isFavourite
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("DeveloperDetailsViewModel", "ViewModel cleared")
    }

}