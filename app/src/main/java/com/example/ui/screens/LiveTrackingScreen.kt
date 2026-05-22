package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MallViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTrackingScreen(
    viewModel: MallViewModel,
    onBack: () -> Unit
) {
    val isEnglish by viewModel.isLanguageEnglish.collectAsState()
    val orderId by viewModel.currentTrackingOrderId.collectAsState()
    val orderState by viewModel.trackingOrderState.collectAsState()
    val ctx = LocalContext.current

    // Coordinates path interpolation for drawing the animate marker on map
    val progress = when (orderState?.orderStatus) {
        "Order received" -> 0.05f
        "Packing" -> 0.15f
        "Out for delivery" -> 0.45f
        "Delivered" -> 1.0f
        else -> 0.65f // out for delivery transit steps
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = viewModel.getLabel("Live Carrier Routing", "લાઈવ ઓર્ડર ટ્રેકિંગ"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = orderId ?: "Order Details",
                            fontSize = 12.sp,
                            color = PatelGreen
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (viewModel.isDarkMode.collectAsState().value) BackgroundDark else BackgroundLight)
        ) {
            
            // 1. Live Vector Map Simulator Canvas Box
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        2.dp,
                        if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight,
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("gps_tracking_map_area")
            ) {
                // Interactive Road/Street Grid Canvas representing Kotda Jadodar
                val isDark = viewModel.isDarkMode.collectAsState().value
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Background land gradient color
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = if (isDark) {
                                listOf(Color(0xFF101B13), Color(0xFF040A06))
                            } else {
                                listOf(Color(0xFFE8F1EB), Color(0xFFF3F7F4))
                            }
                        )
                    )

                    // Draw grid layout representing streets
                    val streetColor = if (isDark) Color(0xFF1B2B20) else Color(0xFFDFE6E1)
                    val streetStroke = Stroke(width = 6f)

                    // Parallel block horizontal roads
                    for (i in 1..4) {
                        val y = height * (i / 5f)
                        drawLine(color = streetColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 10f)
                    }
                    // Vertical cross grid lines
                    for (j in 1..4) {
                        val x = width * (j / 5f)
                        drawLine(color = streetColor, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 10f)
                    }

                    // Draw organic river curve looping Gujarat landscapes
                    val riverColor = Color(0xFF2B80B7).copy(alpha = 0.25f)
                    val riverStroke = Stroke(width = 16f)
                    drawArc(
                        color = riverColor,
                        startAngle = 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(-100f, height * 0.4f),
                        size = Size(width * 1.2f, height * 0.5f),
                        style = riverStroke
                    )

                    // DRAW TRANSIT PATH CONNECTOR FROM SOURCE HUB TO CUSTOMER HOME
                    val pathColor = PatelGreen.copy(alpha = 0.7f)
                    val sourceX = width * 0.2f
                    val sourceY = height * 0.8f
                    val destX = width * 0.8f
                    val destY = height * 0.2f

                    // Path lines
                    drawLine(
                        color = pathColor,
                        start = Offset(sourceX, sourceY),
                        end = Offset(sourceX, destY),
                        strokeWidth = 6f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )
                    drawLine(
                        color = pathColor,
                        start = Offset(sourceX, destY),
                        end = Offset(destX, destY),
                        strokeWidth = 6f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )

                    // Draw Patel Mall Warehouse Base Icon Badge
                    drawCircle(color = PatelGreen, radius = 24f, center = Offset(sourceX, sourceY))
                    drawCircle(color = Color.White, radius = 10f, center = Offset(sourceX, sourceY))

                    // Draw Destination House Icon Circle
                    drawCircle(color = PatelYellow, radius = 24f, center = Offset(destX, destY))
                    drawCircle(color = Color.White, radius = 10f, center = Offset(destX, destY))

                    // Draw RIDER POSITION BASED ON ANIMATION/PROGRESS STEPS
                    // Path goes from (sourceX, sourceY) -> (sourceX, destY) -> (destX, destY)
                    val currentX: Float
                    val currentY: Float
                    if (progress <= 0.5f) {
                        val subProgress = progress / 0.5f
                        currentX = sourceX
                        currentY = sourceY - (sourceY - destY) * subProgress
                    } else {
                        val subProgress = (progress - 0.5f) / 0.5f
                        currentX = sourceX + (destX - sourceX) * subProgress
                        currentY = destY
                    }

                    // Draw pulsating beacon glow effect under rider
                    drawCircle(
                        color = PatelYellow.copy(alpha = 0.35f),
                        radius = 45f,
                        center = Offset(currentX, currentY)
                    )
                    drawCircle(
                        color = PatelGreenDark,
                        radius = 20f,
                        center = Offset(currentX, currentY)
                    )
                }

                // Landmark floating labels on GIS map
                Text(
                    text = viewModel.getLabel("Patel Mall Hub", "પટેલ મોલ આઉટલેટ"),
                    fontSize = 11.sp,
                    color = PatelGreenDark,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 10.dp, y = (-54).dp)
                        .background(Color.White, RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Text(
                    text = viewModel.getLabel("Your Address", "તમારું સરનામું"),
                    fontSize = 11.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-30).dp, y = 54.dp)
                        .background(PatelYellow, RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                // Current GPS live coordinate telemetry tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(6.dp)
                ) {
                    Text(
                        text = "GPS: ${orderState?.currentLat?.toString()?.take(7)}, ${orderState?.currentLng?.toString()?.take(7)}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. Rider Details and delivery ETA card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // ETA Indicator Top line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val etaText = when (orderState?.orderStatus) {
                                "Order received" -> "15 mins"
                                "Packing" -> "12 mins"
                                "Out for delivery" -> "5 mins"
                                "Delivered" -> "Delivered"
                                else -> "8 mins"
                            }
                            Text(
                                text = viewModel.getLabel("Estimated Arrival Time", "અંદાજીત પહોંચવાનો સમય"),
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Text(
                                text = etaText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = PatelGreen
                            )
                        }

                        // Instant Status Ticker Info Bubble
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PatelGreenLight)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = orderState?.orderStatus?.uppercase() ?: "PENDING",
                                color = PatelGreenDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Divider()

                    // Assigned Rider description
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rider avatar circle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(PatelYellowLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBike,
                                contentDescription = "Rider Details",
                                tint = PatelGreenDark
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = orderState?.deliveryBoyName ?: "Gopal Patel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = viewModel.getLabel("Patel Mall Express Rider", "પટેલ મોલ એક્સપ્રેસ ડિલિવરી બોય"),
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        // Support dial links (+919427126911)
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919427126911"))
                                ctx.startActivity(intent)
                            },
                            modifier = Modifier
                                .background(PatelGreen, CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call rider",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Delivery warning instructions footer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PatelYellowLight)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Safe Checkout",
                                tint = Color(0xFFC28100),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = viewModel.getLabel(
                                    "No Contact Delivery Active. Cashless payment preference.",
                                    "સુરક્ષિત સંપર્ક રહિત વિતરણ સક્રિય. કેશલેસ ચુકવણી પસંદગી."
                                ),
                                fontSize = 10.sp,
                                color = Color(0xFF7A5800),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
