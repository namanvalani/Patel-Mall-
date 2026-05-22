package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MallViewModel
import com.example.data.model.ProductEntity
import com.example.data.model.OrderEntity
import com.example.ui.theme.*

enum class AdminTab {
    DASHBOARD, PRODUCTS, ORDERS, CONFIG
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: MallViewModel,
    onBackToSplash: () -> Unit
) {
    var activeTab by remember { mutableStateOf(AdminTab.DASHBOARD) }
    val lowStockCount by viewModel.lowStockProducts.collectAsState()
    val orders by viewModel.allOrders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Owner Panel", tint = PatelGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PATEL MALL OWNER HUB",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = viewModel.getLabel("Administrative Head Office", "વહીવટી હેડ ઓફિસ"),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                    // Quick stats notification for low stock!
                    if (lowStockCount.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = Color.Red) {
                                    Text(lowStockCount.size.toString(), color = Color.White)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alerts",
                                tint = Color.Red
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Admin bottom selector
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val menus = listOf(
                    Triple(AdminTab.DASHBOARD, Icons.Default.Analytics, "Analytics"),
                    Triple(AdminTab.PRODUCTS, Icons.Default.FormatListBulleted, "Products"),
                    Triple(AdminTab.ORDERS, Icons.Default.Assignment, "Orders"),
                    Triple(AdminTab.CONFIG, Icons.Default.Tune, "Configs")
                )

                menus.forEach { (tab, icon, title) ->
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PatelGreen,
                            indicatorColor = PatelGreenLight,
                            selectedTextColor = PatelGreen
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (viewModel.isDarkMode.collectAsState().value) BackgroundDark else BackgroundLight)
        ) {
            when (activeTab) {
                AdminTab.DASHBOARD -> AdminDashboardContent(viewModel)
                AdminTab.PRODUCTS -> AdminProductsContent(viewModel)
                AdminTab.ORDERS -> AdminOrdersContent(viewModel)
                AdminTab.CONFIG -> AdminConfigContent(viewModel)
            }
        }
    }
}

// ------ ADMIN SUB-TABS VIEWS ------

@Composable
fun AdminDashboardContent(viewModel: MallViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val lowStockList by viewModel.lowStockProducts.collectAsState()

    val totalRevenue = orders.filter { it.orderStatus == "Delivered" }.sumOf { it.totalAmount }
    val activeOrdersCount = orders.filter { it.orderStatus != "Delivered" }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and location
        item {
            Text(
                text = viewModel.getLabel("Executive Business Summary", "બિઝનેસ નાણાકીય અહેવાલ"),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Key statistical metrics row widgets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sales volume card
                Card(
                    colors = CardDefaults.cardColors(containerColor = PatelGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(imageVector = Icons.Default.CurrencyRupee, contentDescription = "Revenue", tint = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("TOTAL REVENUE", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("₹${totalRevenue.toInt()}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Gross orders
                Card(
                    colors = CardDefaults.cardColors(containerColor = PatelYellow),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Orders", tint = TextDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("FULFILLED ORDERS", color = TextDark.copy(alpha = 0.75f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${orders.size} orders", color = TextDark, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Active delivery transit logs progress status callout card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PatelGreenLight, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.DeliveryDining, contentDescription = "Active Deliveries", tint = PatelGreen)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("ACTIVE DELIVERIES", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text("$activeOrdersCount Shipments on Way", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (activeOrdersCount > 0) PatelYellow else Color.LightGray)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(if (activeOrdersCount > 0) "LIVE" else "IDLE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = TextDark)
                    }
                }
            }
        }

        // Low stock alerts critical attention zone!
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = viewModel.getLabel("Critical Low Stock Alerts", "મર્યાદિત સ્ટોક ચેતવણી"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.Warning, contentDescription = "Warn", tint = Color.Red, modifier = Modifier.size(16.dp))
            }
        }

        if (lowStockList.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PatelGreenLight.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ All inventory stocks are in safe ranges. No low stock alerts.",
                        color = PatelGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(lowStockList) { prd ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.Red.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(prd.nameEn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Current Stock: ${prd.stock} units remaining", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = { viewModel.restockProduct(prd.id, 50) },
                            colors = ButtonDefaults.buttonColors(containerColor = PatelGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("+50 units", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductsContent(viewModel: MallViewModel) {
    val products by viewModel.allProducts.collectAsState()

    val nameEn by viewModel.newProductNameEn.collectAsState()
    val nameGu by viewModel.newProductNameGu.collectAsState()
    val catEn by viewModel.newProductCategoryEn.collectAsState()
    val price by viewModel.newProductPrice.collectAsState()
    val discount by viewModel.newProductDiscount.collectAsState()
    val stock by viewModel.newProductStock.collectAsState()
    val unit by viewModel.newProductVariantUnit.collectAsState()
    val size by viewModel.newProductVariantSize.collectAsState()

    var expandedCategoryMenu by remember { mutableStateOf(false) }
    var expandedUnitMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form structure: Add new product
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ADD NEW GROCERY PRODUCT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = PatelGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { viewModel.newProductNameEn.value = it },
                        label = { Text("Product Name (English)") },
                        placeholder = { Text("e.g. Milk Packet") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_product_en_name")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nameGu,
                        onValueChange = { viewModel.newProductNameGu.value = it },
                        label = { Text("Product Name (Gujarati)") },
                        placeholder = { Text("દા.ત. અમૂલ દૂધ") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_product_gu_name")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row of parameters
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { viewModel.newProductPrice.value = it },
                            label = { Text("Price (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1.2f).testTag("add_product_price")
                        )

                        OutlinedTextField(
                            value = discount,
                            onValueChange = { viewModel.newProductDiscount.value = it },
                            label = { Text("Discount %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { viewModel.newProductStock.value = it },
                            label = { Text("Initial Stock") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Variant size (e.g., 500, 1)
                        OutlinedTextField(
                            value = size,
                            onValueChange = { viewModel.newProductVariantSize.value = it },
                            label = { Text("Size") },
                            placeholder = { Text("e.g. 500") },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category switcher dropdown
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedButton(
                                onClick = { expandedCategoryMenu = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(catEn, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = expandedCategoryMenu,
                                onDismissRequest = { expandedCategoryMenu = false }
                            ) {
                                val categories = listOf(
                                    "Vegetables & Fruits",
                                    "Milk & Dairy",
                                    "Atta, Rice & Groceries",
                                    "Snacks, Drinks & Sweets"
                                )
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.newProductCategoryEn.value = cat
                                            expandedCategoryMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Unit switcher dropdown
                        Box(modifier = Modifier.weight(0.8f)) {
                            OutlinedButton(
                                onClick = { expandedUnitMenu = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(unit.uppercase(), fontSize = 12.sp)
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = expandedUnitMenu,
                                onDismissRequest = { expandedUnitMenu = false }
                            ) {
                                val units = listOf("kg", "gram", "liter", "packet", "pieces")
                                units.forEach { un ->
                                    DropdownMenuItem(
                                        text = { Text(un.uppercase()) },
                                        onClick = {
                                            viewModel.newProductVariantUnit.value = un
                                            expandedUnitMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.saveProductFromAdmin() },
                        colors = ButtonDefaults.buttonColors(containerColor = PatelGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_product_admin_submit")
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Upload", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SAVE PRODUCT TO STORAGE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // List displaying catalog items
        item {
            Text(
                text = "CATALOG MANAGER (${products.size} Items)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(products) { prd ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(prd.nameEn, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${prd.categoryEn} | Stock: ${prd.stock} remaining", fontSize = 11.sp, color = TextMuted)
                        Text("MRP: ₹${prd.price.toInt()} | Variant: ${prd.variantSize} ${prd.variantUnit.uppercase()}", fontSize = 11.sp, color = PatelGreen)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Quick Stock Incrementor
                        IconButton(
                            onClick = { viewModel.restockProduct(prd.id, 10) },
                            modifier = Modifier
                                .background(PatelYellowLight, CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add More Stream", tint = Color(0xFFC78300), modifier = Modifier.size(16.dp))
                        }

                        // Remove / Delete Product control
                        IconButton(
                            onClick = { viewModel.deleteProduct(prd.id) },
                            modifier = Modifier
                                .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrdersContent(viewModel: MallViewModel) {
    val orders by viewModel.allOrders.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "INCOMING CUSTOMER ORDERS (${orders.size} Total)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (orders.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No customer orders placed yet.", color = TextMuted, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            items(orders) { order ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(order.orderId, fontWeight = FontWeight.Bold, color = PatelGreen, fontSize = 15.sp)
                            Text("Total: ₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Black, color = PatelGreenDark)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("User Name: ${order.customerName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Address: ${order.deliveryAddress}", fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PatelGreenLight.copy(alpha = 0.4f))
                                .padding(8.dp)
                        ) {
                            Text(order.itemsSummary, fontSize = 11.sp, color = PatelGreenDark)
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Admin status change slider buttons
                        Text("UPDATE LOGISTICS STATUS:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val statuses = listOf("Packing", "Out for delivery", "Delivered")
                            statuses.forEach { st ->
                                val active = order.orderStatus == st
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) PatelGreen else Color.LightGray.copy(alpha = 0.5f))
                                        .clickable { viewModel.updateOrderStateFromAdmin(order.orderId, st) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        st,
                                        color = if (active) Color.White else TextDark,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
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

@Composable
fun AdminConfigContent(viewModel: MallViewModel) {
    val defaultAddressList by viewModel.savedAddresses.collectAsState()
    val isEnglish by viewModel.isLanguageEnglish.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "GLOBAL OFFICE CONTROL SETTINGS",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Test push triggers
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TRIGGER BROADCAST NOTIFICATIONS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Simulate system wide alerts transmitting to customer modules instantly.", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.completeDelivery("ALL") // trigger internal notification updates
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PatelYellow),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Rain Alert: Delayed +5m", color = TextDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.applyCoupon("PATEL15")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PatelGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Festive 15% OFF Coupon", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Owner branding details
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PATEL MALL REGISTERED DETAILS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Service Hub: Kotda Jadodar, Gujarat, India", fontSize = 12.sp)
                    Text("Primary Contact: +91 94271 26911", fontSize = 12.sp, color = PatelGreen)
                    Text("Licences: GSTIN Registered (5% Fresh Commodities slab)", fontSize = 12.sp)
                    Text("Tech Setup: Room SQLite Offline Database Sync v1.0", fontSize = 12.sp)
                }
            }
        }
    }
}
