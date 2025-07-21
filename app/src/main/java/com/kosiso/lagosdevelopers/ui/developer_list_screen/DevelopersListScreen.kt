package com.kosiso.lagosdevelopers.ui.developer_list_screen

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.kosiso.lagosdevelopers.R
import com.kosiso.lagosdevelopers.models.LagosDeveloper
import com.kosiso.lagosdevelopers.ui.theme.BackgroundColor
import com.kosiso.lagosdevelopers.ui.theme.Black
import com.kosiso.lagosdevelopers.ui.theme.Pink
import com.kosiso.lagosdevelopers.ui.theme.Red
import com.kosiso.lagosdevelopers.ui.theme.White
import com.kosiso.lagosdevelopers.ui.theme.onest

@Composable
fun DeveloperListScreen(
    developersListViewModel: DevelopersListViewModel,
    onNavigateToDetailsScreen: (LagosDeveloper) -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 15.dp)
            .padding(bottom = 65.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Lagos Developers",
                style = TextStyle(
                    color = Black,
                    fontFamily = onest,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp
                )
            )

            Spacer(modifier = Modifier.height(15.dp))

            LagosDevsListSection(developersListViewModel, onNavigateToDetailsScreen)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LagosDevsListSection(
    developersListViewModel: DevelopersListViewModel,
    onNavigateToDetailsScreen: (LagosDeveloper) -> Unit){

    val lagosDevsPagingItems = developersListViewModel.lagosDevsFlow.collectAsLazyPagingItems()
    Log.i("lagos dev list screen", lagosDevsPagingItems.toString())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))

    ){
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            items(
                count = lagosDevsPagingItems.itemCount,
                key = lagosDevsPagingItems.itemKey { it.id },
                contentType = lagosDevsPagingItems.itemContentType { "LagosDeveloper" }
            ) { index ->
                lagosDevsPagingItems[index]?.let { developer ->
                    DevItem(
                        dev = developer,
                        onClick = {
                            onNavigateToDetailsScreen(developer)
                                  },
                        onAddToFavourite = {
                            developersListViewModel.addToFavourites(developer)
                        }
                    )
                }
            }


            when (lagosDevsPagingItems.loadState.refresh) {
                is LoadState.Loading -> {
                    item {
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
                }
                is LoadState.Error -> {
                    item {
                        Text("Unable to load developers, enable internet connection and try again.")
                    }
                }
                else -> {}
            }


            when (lagosDevsPagingItems.loadState.append) {
                is LoadState.Loading -> {
                    item {
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
                }
                is LoadState.Error -> {
                    item {
                        Text("Error loading more data")
                    }
                }
                else -> {}
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun DevItem(
    dev: LagosDeveloper,
    onClick:() -> Unit,
    onAddToFavourite:() -> Unit
){

    val context = LocalContext.current
    val devName = if (dev.login.length > 12) {
        dev.login.take(9) + "..."
    } else {
        dev.login
    }
    var showMenu by remember{ mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(White)

    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            AsyncImage(
                model = dev.avatarUrl,
                placeholder = painterResource(id = R.drawable.ic_placeholder),
                error = painterResource(id = R.drawable.ic_placeholder),
                contentDescription = "avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .align(Alignment.TopCenter)
                    .clickable {
                        onClick()
                    },
                contentScale = ContentScale.Crop
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_more_vert),
                contentDescription = "more icon",
                tint = Pink,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .clickable {
                        showMenu = true
                    }
            )
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                DropdownMenuItem(
                    text = { Text("Add To Favourites") },
                    onClick = {
                        onAddToFavourite()
                        showMenu = false
                        Toast.makeText(context, "${dev.login} added to favourites", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Text(
                text = devName,
                style = TextStyle(
                    color = Black,
                    fontFamily = onest,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .clickable {
                        onClick()
                    }
            )
        }
    }
}

