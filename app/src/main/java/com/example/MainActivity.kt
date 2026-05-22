package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppRole
import com.example.ui.CustomerTab
import com.example.ui.MallViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PatelGreenDark
import com.example.ui.theme.PatelYellow

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: MallViewModel = viewModel()
      val isDark by viewModel.isDarkMode.collectAsState()

      MyApplicationTheme(darkTheme = isDark) {
        val currentScreenState by viewModel.currentRole.collectAsState()
        val activeTrackingOrder by viewModel.currentTrackingOrderId.collectAsState()
        val alertMessage by viewModel.notifyMessage.collectAsState()

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            
            // Core screen navigation router
            when (currentScreenState) {
              AppRole.SPLASH -> {
                SplashScreen(
                  viewModel = viewModel,
                  onRoleSelected = { chosenRole ->
                    viewModel.currentRole.value = chosenRole
                  }
                )
              }
              AppRole.LOGIN -> {
                LoginScreen(
                  viewModel = viewModel,
                  onBack = { viewModel.currentRole.value = AppRole.SPLASH }
                )
              }
              AppRole.CUSTOMER -> {
                // Determine if we show the active live map overlay or standard customer tabs
                if (activeTrackingOrder != null) {
                  LiveTrackingScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.currentTrackingOrderId.value = null }
                  )
                } else {
                  CustomerDashboard(
                    viewModel = viewModel,
                    onNavigateToTracking = { orderId ->
                      viewModel.selectOrderForTracking(orderId)
                    },
                    onRoleSelected = {
                      viewModel.currentRole.value = AppRole.SPLASH
                    }
                  )
                }
              }
              AppRole.ADMIN -> {
                AdminPanelScreen(
                  viewModel = viewModel,
                  onBackToSplash = { viewModel.currentRole.value = AppRole.SPLASH }
                )
              }
              AppRole.DELIVERY_BOY -> {
                DeliveryPanelScreen(
                  viewModel = viewModel,
                  onBackToSplash = { viewModel.currentRole.value = AppRole.SPLASH }
                )
              }
            }

            // Centralised animated notification push banner (top anchor)
            AnimatedVisibility(
                visible = alertMessage != null,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = 350)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(durationMillis = 250)
                ) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .fillMaxWidth()
            ) {
              alertMessage?.let { msg ->
                Card(
                  colors = CardDefaults.cardColors(containerColor = PatelGreenDark),
                  shape = RoundedCornerShape(16.dp),
                  elevation = CardDefaults.cardElevation(8.dp),
                  modifier = Modifier
                      .fillMaxWidth()
                      .testTag("notification_banner")
                ) {
                  Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                      Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = Icons.Default.Notifications,
                          contentDescription = "Alert",
                          tint = PatelYellow,
                          modifier = Modifier.size(20.dp)
                        )
                      }
                      Spacer(modifier = Modifier.width(12.dp))
                      Text(
                        text = msg,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 17.sp
                      )
                    }
                    IconButton(
                      onClick = { viewModel.dismissNotification() },
                      modifier = Modifier.size(24.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Alert",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
