package com.kosiso.lagosdevelopers.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kosiso.lagosdevelopers.R
import com.kosiso.lagosdevelopers.models.LagosDeveloper
import com.kosiso.lagosdevelopers.ui.developer_details_screen.DeveloperDetailsScreen
import com.kosiso.lagosdevelopers.ui.developer_details_screen.DeveloperDetailsViewModel
import com.kosiso.lagosdevelopers.ui.developer_list_screen.DeveloperListScreen
import com.kosiso.lagosdevelopers.ui.developer_list_screen.DevelopersListViewModel
import com.kosiso.lagosdevelopers.ui.favourites_screen.FavouritesListViewModel
import com.kosiso.lagosdevelopers.ui.favourites_screen.FavouritesScreen
import com.kosiso.lagosdevelopers.ui.navigation.BottomNavItem
import com.kosiso.lagosdevelopers.ui.navigation.MainAppNav
import com.kosiso.lagosdevelopers.ui.navigation.RootNav
import com.kosiso.lagosdevelopers.ui.theme.Black
import com.kosiso.lagosdevelopers.ui.theme.LagosDevelopersTheme
import com.kosiso.lagosdevelopers.ui.theme.Pink
import com.kosiso.lagosdevelopers.ui.theme.White
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val developersListViewModel by viewModels<DevelopersListViewModel>()
    val favouritesListViewModel by viewModels<FavouritesListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LagosDevelopersTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding ->
                    val rootNavController = rememberNavController()
                    RootNavigation(rootNavController, developersListViewModel)
                }
            }
        }
    }


    @Composable
    fun RootNavigation(rootNavController: NavHostController, developerListViewModel: DevelopersListViewModel){
        NavHost(
            navController = rootNavController,
            startDestination = RootNav.MAIN_APP.route
        ) {

            composable(RootNav.MAIN_APP.route) {
                MainApp(rootNavController, developerListViewModel)
            }

            composable(
                route = RootNav.DEVELOPER_DETAILS.route

            ) {backStackEntry->
                val developer = remember {
                    rootNavController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<LagosDeveloper>("developer")
                }
                val developerDetailsViewModel: DeveloperDetailsViewModel = hiltViewModel()
                BackHandler {
                    rootNavController.popBackStack()
                }
                DeveloperDetailsScreen(
                    developer = developer,
                    developerDetailsViewModel = developerDetailsViewModel,
                    onBackClick = { rootNavController.popBackStack() }
                )
            }

        }
    }


    @Composable
    fun MainApp(rootNavController: NavHostController, developersListViewModel: DevelopersListViewModel){

        val mainAppNavController = rememberNavController()

        val bottomNavItems = listOf<BottomNavItem>(
            BottomNavItem(
                id = UUID.randomUUID().toString(),
                name = "Developers",
                route = MainAppNav.DEVELOPERS.route,
                icon = R.drawable.ic_list
            ),
            BottomNavItem(
                id = UUID.randomUUID().toString(),
                name = "Favourites",
                route = MainAppNav.FAVOURITES.route,
                icon = R.drawable.ic_love_filled
            )
        )

        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    navItems = bottomNavItems,
                    navController = mainAppNavController,
                    onItemClick = {
                        if (mainAppNavController.currentDestination?.route != it.route) {
                            mainAppNavController.navigate(it.route) {
                                popUpTo(MainAppNav.FAVOURITES.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        ){
            Navigation(
                mainAppNavController= mainAppNavController,
                rootNavController = rootNavController,
                developersListViewModel
            )
        }
    }


    @Composable
    fun Navigation(
        mainAppNavController: NavHostController,
        rootNavController: NavHostController,
        developersListViewModel: DevelopersListViewModel){

        NavHost(navController = mainAppNavController, startDestination = MainAppNav.DEVELOPERS.route){
            composable(MainAppNav.DEVELOPERS.route){
                DeveloperListScreen(
                    developersListViewModel = developersListViewModel,
                    onNavigateToDetailsScreen = {developer ->
                        rootNavController.currentBackStackEntry?.savedStateHandle?.set(
                            key = "developer",
                            value = developer
                        )
                        rootNavController.navigate(RootNav.DEVELOPER_DETAILS.route)
                    }
                )
            }
            composable(MainAppNav.FAVOURITES.route){
                FavouritesScreen(
                    favouritesListViewModel = favouritesListViewModel,
                    onNavigateToDetailsScreen = {developer ->
                        rootNavController.currentBackStackEntry?.savedStateHandle?.set(
                            key = "developer",
                            value = developer
                        )
                        rootNavController.navigate(RootNav.DEVELOPER_DETAILS.route)

                    }
                )
            }
        }
    }

    @Composable
    fun BottomNavigationBar(
        navItems: List<BottomNavItem>,
        navController: NavController,
        modifier: Modifier = Modifier,
        onItemClick: (BottomNavItem) -> Unit
    ){
        val backStackEntry = navController.currentBackStackEntryAsState()
        NavigationBar(
            modifier = modifier.height(60.dp),
            containerColor = White,
            tonalElevation = 5.dp
        ) {
            navItems.forEach{navItem ->

                val selected = navItem.route == backStackEntry.value?.destination?.route
                NavigationBarItem(
                    modifier = Modifier.padding(top = 5.dp),
                    selected = selected,
                    onClick = { onItemClick(navItem) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = White,
                        unselectedIconColor = Black.copy(alpha = 0.8f),
                        indicatorColor = Pink
                    ),
                    icon = {
                        BottomNavIconStyle(
                            navItem,
                            selected
                        )
                    },
                    alwaysShowLabel = true,
                    label = {
                        Text(
                            text = navItem.name,
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp
                        )
                    }
                )
            }
        }
    }

    @Composable
    fun BottomNavIconStyle(
        navItem: BottomNavItem,
        selected: Boolean
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Box{
                Icon(
                    painter = painterResource(navItem.icon),
                    contentDescription = navItem.name,
                    modifier = Modifier.size(24.dp)
                )
            }
            if(selected){

            }
        }
    }
}

