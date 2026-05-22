package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MallViewModel
import com.example.data.model.OrderEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryPanelScreen(
    viewModel: MallViewModel,
    onBackToSplash: () -> Unit
) {
    val orders by viewModel.allOrders.collectAsState()
    val activeDuty = remember { mutableStateOf(true) }

    // Filter assigned active deliveries
    val assignedDeliveries = remember(orders) {
        orders.filter { it.orderStatus != "Delivered" }
    }
    
    // Delivered deliveries to calculate earnings
    val deliveredCount = remember(orders) {
        orders.filter { it.orderStatus == "Delivered" }.size
    }
    val totalEarnings = deliveredCount * 30.0 // ₹30 incentive per delivery

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DirectionsBike, contentDescription = "Rider Logo", tint = PatelGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PATEL EXPRESS LOGISTICS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dispatcher Gopal Patel",
                                fontSize = 11.sp,
                                color = PatelGreen
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToSplash) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Active Duty Online/Offline Toggle Chip
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (activeDuty.value) PatelGreenLight else Color.LightGray)
                            .clickable { activeDuty.value = !activeDuty.value }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (activeDuty.value) PatelGreen else Color.Gray, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeDuty.value) "ONLINE" else "OFFLINE",
                            color = if (activeDuty.value) PatelGreenDark else Color.DarkGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (viewModel.isDarkMode.collectAsState().value) BackgroundDark else BackgroundLight)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 1. Rider Earnings Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PatelGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("YOUR TODAY'S WALLET", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${totalEarnings.toInt()}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            Text("₹30.00 Delivery Incentive plan active", color = PatelYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Payments, contentDescription = "Paid", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("$deliveredCount Delivered", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Active Tasks Title
            item {
                Text(
                    text = "ASSIGNED DELIVERIES (${assignedDeliveries.size} Active)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 3. Assigned deliveries list
            if (assignedDeliveries.isEmpty() || !activeDuty.value) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Approval, contentDescription = "Alert", tint = PatelGreenLight, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (!activeDuty.value) "You are Offline" else "No pending logistics tasks!",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Take a break, new orders will auto assign.",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(assignedDeliveries) { delivery ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header order details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = delivery.orderId,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = PatelGreen
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PatelYellowLight)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = delivery.orderStatus.uppercase(),
                                        color = Color(0xFFC78103),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Address & Customer Info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = "Cust", tint = PatelGreenDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(delivery.customerName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Addr", tint = Color.Red, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    delivery.deliveryAddress,
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Cash collection notes (UPI, COD, Card info)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BackgroundLight)
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("CASH TO COLLECT:", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = if (delivery.paymentMethod == "Cash on Delivery") "COLLECT ₹${delivery.totalAmount.toInt()}" else "ALREADY PAID ONLINE",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (delivery.paymentMethod == "Cash on Delivery") Color.Red else PatelGreenDark
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (delivery.paymentMethod == "Cash on Delivery") Color.Red.copy(alpha = 0.15f) else PatelGreenLight)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = delivery.paymentMethod,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (delivery.paymentMethod == "Cash on Delivery") Color.Red else PatelGreenDark
                                        )
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            // Interactive GPS coordinate simulator buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Broadcaster
                                OutlinedButton(
                                    onClick = { viewModel.updateDeliveryLocationByPartner(delivery.orderId) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PatelGreen),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.ShareLocation, contentDescription = "GPS", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Broadcast GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Mark delivered complete button
                                Button(
                                    onClick = { viewModel.completeDelivery(delivery.orderId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PatelGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("mark_delivered_btn_${delivery.orderId}")
                                ) {
                                    Icon(imageVector = Icons.Default.DoneOutline, contentDescription = "Confirm", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Fulfill Order", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
