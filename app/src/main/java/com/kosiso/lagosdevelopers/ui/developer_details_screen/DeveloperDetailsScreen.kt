package com.kosiso.lagosdevelopers.ui.developer_details_screen

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.widget.ConstraintLayout
import coil.compose.AsyncImage
import com.kosiso.lagosdevelopers.R
import com.kosiso.lagosdevelopers.data.state.DevResponseState
import com.kosiso.lagosdevelopers.models.FavouriteDev
import com.kosiso.lagosdevelopers.models.LagosDeveloper
import com.kosiso.lagosdevelopers.ui.Constants.ADDED_TO_FAVOURITES
import com.kosiso.lagosdevelopers.ui.Constants.AVATAR
import com.kosiso.lagosdevelopers.ui.Constants.BIO
import com.kosiso.lagosdevelopers.ui.Constants.CHECK_INTERNET_CONNECTION
import com.kosiso.lagosdevelopers.ui.Constants.COMPANY
import com.kosiso.lagosdevelopers.ui.Constants.DATE_CREATED
import com.kosiso.lagosdevelopers.ui.Constants.EMAIL
import com.kosiso.lagosdevelopers.ui.Constants.FOLLOWERS
import com.kosiso.lagosdevelopers.ui.Constants.FOLLOWING
import com.kosiso.lagosdevelopers.ui.Constants.INVALID_DATE
import com.kosiso.lagosdevelopers.ui.Constants.LAST_UPDATED
import com.kosiso.lagosdevelopers.ui.Constants.LOCATION
import com.kosiso.lagosdevelopers.ui.Constants.NAME
import com.kosiso.lagosdevelopers.ui.Constants.REMOVED_FROM_FAVOURITES
import com.kosiso.lagosdevelopers.ui.Constants.REPOSITORIES
import com.kosiso.lagosdevelopers.ui.Constants.TWITTER
import com.kosiso.lagosdevelopers.ui.theme.BackgroundColor
import com.kosiso.lagosdevelopers.ui.theme.Black
import com.kosiso.lagosdevelopers.ui.theme.Pink
import com.kosiso.lagosdevelopers.ui.theme.Red
import com.kosiso.lagosdevelopers.ui.theme.White
import com.kosiso.lagosdevelopers.ui.theme.onest
import kotlinx.coroutines.flow.StateFlow
import java.text.NumberFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeveloperDetailsScreen(
    developer: LagosDeveloper?,
    developerDetailsViewModel: DeveloperDetailsViewModel,
    onBackClick: () -> Unit
) {
    val developerState = developerDetailsViewModel.developerState

    BackHandler {
        onBackClick()
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 15.dp)
            .padding(bottom = 20.dp)
    ) {
        val (topSection, detailsSection) = createRefs()

        TopSection(
            dev = developer!!,
            developerDetailsViewModel = developerDetailsViewModel,
            developerState = developerState,
            modifier = Modifier
                .constrainAs(topSection) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        DeveloperDetailsSection(
            developerDetailsViewModel = developerDetailsViewModel,
            developerState = developerState,
            dev = developer,
            modifier = Modifier
                .constrainAs(detailsSection) {
                    top.linkTo(topSection.bottom, margin = 10.dp)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    height = Dimension.fillToConstraints
                }
        )
    }
}

@Composable
private fun TopSection(
    dev: LagosDeveloper,
    developerState: StateFlow<DevResponseState<FavouriteDev>>,
    developerDetailsViewModel: DeveloperDetailsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val developerState = developerState.collectAsState().value
    val isFavourite = developerDetailsViewModel.isFavourite.collectAsState().value
    val noInternetState = developerState is DevResponseState.NoInternet
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dev.login,
            style = TextStyle(
                color = Black,
                fontFamily = onest,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
        )

        if(!noInternetState){
            if(isFavourite){
                Icon(
                    painter = painterResource(id = R.drawable.ic_love_filled),
                    contentDescription = "",
                    tint = Red,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable {
                            developerDetailsViewModel.apply {
                                removeFromFavourites(dev)
                                setIsFavourite(!isFavourite)
                                Toast.makeText(context, REMOVED_FROM_FAVOURITES, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                )
            }else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_love_outlined),
                    contentDescription = "",
                    tint = Red,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable {
                            developerDetailsViewModel.apply {
                                insertIntoFavourites(dev)
                                setIsFavourite(!isFavourite)
                                Toast.makeText(context, ADDED_TO_FAVOURITES, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                )
            }
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DeveloperDetailsSection(
    developerDetailsViewModel: DeveloperDetailsViewModel,
    developerState: StateFlow<DevResponseState<FavouriteDev>>,
    dev: LagosDeveloper?,
    modifier: Modifier = Modifier
) {
    developerDetailsViewModel.getDeveloperDetails(dev!!)
    val developerState = developerState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            when (val result = developerState.value) {
                DevResponseState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                        ){
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(30.dp),
                            color = Pink,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
                is DevResponseState.Error -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                    ){
                        Text(
                            text = "${ result.message }, " + CHECK_INTERNET_CONNECTION,
                            style = TextStyle(
                                color = Black.copy(alpha = 0.5f),
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
                is DevResponseState.NoInternet -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                    ){
                        Text(
                            text = result.message,
                            style = TextStyle(
                                color = Black.copy(alpha = 0.5f),
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
                is DevResponseState.Success -> {
                    val developer = result.data
                    AsyncImage(
                        model = developer.avatarUrl,
                        placeholder = painterResource(id = R.drawable.ic_placeholder),
                        error = painterResource(id = R.drawable.ic_placeholder),
                        contentDescription = AVATAR,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Row {
                        Text(
                            text = NAME,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = developer.name!!,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )

                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        Text(
                            text = EMAIL,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = developer.email!!,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )

                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        Text(
                            text = BIO,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = developer.bio!!,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )

                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        Text(
                            text = COMPANY,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = developer.company!!,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        Text(
                            text = LOCATION,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = developer.location!!,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        Text(
                            text = TWITTER,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = developer.twitterUsername!!,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        Text(
                            text = REPOSITORIES,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${ developer.publicRepos }",
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        Text(
                            text = FOLLOWERS,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${ developer.followers }",
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        Text(
                            text = FOLLOWING,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${ developer.following }",
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        Text(
                            text = DATE_CREATED,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = date(developer.createdAt),
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        Text(
                            text = LAST_UPDATED,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = date(developer.updatedAt),
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun date(isoDate: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(isoDate)
        zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    } catch (e: Exception) {
        Log.e("convert Iso To Date String", "Failed to parse date: $isoDate", e)
        INVALID_DATE
    }
}

