package com.thedone.finflow_client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thedone.finflow_client.data.local.TokenManager
import com.thedone.finflow_client.ui.auth.LoginScreen
import com.thedone.finflow_client.ui.auth.RegisterScreen
import com.thedone.finflow_client.ui.home.HomeScreen
import com.thedone.finflow_client.ui.theme.FinflowclientTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinflowclientTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // Sayfa geçişlerini yönetecek kontrolcü
                    val navController = rememberNavController()

                    val token by tokenManager.getToken().collectAsState("LOADING")

                    if (token != "LOADING") {

                        LaunchedEffect(token) {
                            if (token == null) {
                                navController.navigate("login") {
                                    popUpTo(0)
                                }
                            }
                        }
                    }

                    // NavHost: Ekranların haritasıdır. startDestination ilk açılacak ekranı belirler.
                    // modifier kısmına innerPadding veriyoruz ki yazılarımız saatin/şarjın altında kalmasın!
                    NavHost(
                        navController = navController,
                        startDestination = if (token != null && token != "LOADING") "home" else "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                },
                                onLoginSuccess = {
                                    // Giriş başarılıysa ana sayfaya (home) git ve geri tuşuyla login'e dönülmesini engelle
                                    navController.navigate("home") {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                },
                                onRegisterSuccess = {
                                    navController.navigate("login")
                                }
                            )
                        }
                        composable("home") {
                            HomeScreen()
                        }
                    }
                }
            }
        }
    }
}

