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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thedone.finflow_client.ui.auth.LoginScreen
import com.thedone.finflow_client.ui.auth.RegisterScreen
import com.thedone.finflow_client.ui.theme.FinflowclientTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinflowclientTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // Sayfa geçişlerini yönetecek kontrolcü
                    val navController = rememberNavController()

                    // NavHost: Ekranların haritasıdır. startDestination ilk açılacak ekranı belirler.
                    // modifier kısmına innerPadding veriyoruz ki yazılarımız saatin/şarjın altında kalmasın!
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        // 1. GİRİŞ EKRANI
                        composable("login") {
                            LoginScreen(
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                },
                                onLoginSuccess = {
                                    // Giriş başarılıysa ana sayfaya (home) git ve geri tuşuyla login'e dönülmesini engelle
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. KAYIT EKRANI
                        composable("register") {
                            RegisterScreen(
                                onNavigateToLogin = {
                                    navController.popBackStack() // Geri tuşuna basmış gibi Login'e dön
                                },
                                onRegisterSuccess = {
                                    // Kayıt olunca da login ekranına yönlendir
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 3. ANA SAYFA (Şimdilik boş, burayı sonra çizeceğiz)
                        composable("home") {
                            // Cüzdan ekranı buraya gelecek
                            Text(text = "FinFlow Cüzdanına Hoşgeldin!")
                        }
                    }
                }
            }
        }
    }
}

