package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CartItem
import com.example.ui.CustomerTab
import com.example.ui.MallViewModel
import com.example.ui.data.DummyCategory
import com.example.data.model.ProductEntity
import com.example.data.model.OrderEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboard(
    viewModel: MallViewModel,
    onNavigateToTracking: (String) -> Unit,
    onRoleSelected: () -> Unit // Reset back to role selection
) {
    val isEnglish by viewModel.isLanguageEnglish.collectAsState()
    val activeTab by viewModel.customerTab.collectAsState()
    val cart by viewModel.cartState.collectAsState()
    val totalQty = cart.values.sum()

    val currentBg = if (viewModel.isDarkMode.collectAsState().value) BackgroundDark else BackgroundLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable { onRoleSelected() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalMall,
                            contentDescription = "Mall Logo",
                            tint = PatelGreen
                        )
                        Column {
                            Text(
                                text = "PATEL MALL",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PatelGreen
                            )
                            Text(
                                text = viewModel.getLabel("Quick Delivery Dashboard", "ઝડપી ઘરપહોંચ સેવા"),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    // Dark Mode Toggle
                    IconButton(
                        onClick = { viewModel.isDarkMode.value = !viewModel.isDarkMode.value }
                    ) {
                        Icon(
                            imageVector = if (viewModel.isDarkMode.collectAsState().value) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Dark Mode Toggle",
                            tint = PatelGreen
                        )
                    }

                    // Quick Switch Language Icon
                    IconButton(
                        onClick = { viewModel.isLanguageEnglish.value = !viewModel.isLanguageEnglish.value }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language toggle",
                            tint = PatelGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Tab items
                val tabs = listOf(
                    Triple(CustomerTab.HOME, Icons.Default.Home, viewModel.getLabel("Home", "ઘર")),
                    Triple(CustomerTab.CATEGORIES, Icons.Default.Category, viewModel.getLabel("Categories", "શ્રેણીઓ")),
                    Triple(CustomerTab.CART, Icons.Default.ShoppingCart, viewModel.getLabel("Cart (${totalQty})", "કાર્ટ (${totalQty})")),
                    Triple(CustomerTab.ORDERS, Icons.Default.ReceiptLong, viewModel.getLabel("Orders", "ઓર્ડર્સ")),
                    Triple(CustomerTab.PROFILE, Icons.Default.Person, viewModel.getLabel("Profile", "પ્રોફાઇલ"))
                )

                tabs.forEach { (tab, icon, title) ->
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { viewModel.customerTab.value = tab },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (tab == CustomerTab.CART && totalQty > 0) {
                                        Badge(containerColor = PatelYellow) {
                                            Text(totalQty.toString(), color = TextDark)
                                        }
                                    }
                                }
                            ) {
                                Icon(icon, contentDescription = title)
                            }
                        },
                        label = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PatelGreen,
                            selectedTextColor = PatelGreen,
                            indicatorColor = PatelGreenLight
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(currentBg)
                .padding(innerPadding)
        ) {
            when (activeTab) {
                CustomerTab.HOME -> HomeContent(viewModel)
                CustomerTab.CATEGORIES -> CategoriesContent(viewModel)
                CustomerTab.CART -> CartContent(viewModel, onNavigateToTracking)
                CustomerTab.ORDERS -> OrdersContent(viewModel, onNavigateToTracking)
                CustomerTab.PROFILE -> ProfileContent(viewModel, onRoleSelected)
            }
        }
    }
}

// ------ CUSTOMER SPLIT SUB-TABS VIEWS ------

@Composable
fun HomeContent(viewModel: MallViewModel) {
    val isEnglish by viewModel.isLanguageEnglish.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val searchKey by viewModel.searchKeyword.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()
    val activeBanner by viewModel.activeBannerIndex.collectAsState()
    val voiceActive by viewModel.voiceSearchActive.collectAsState()

    // Filter products based on search keyword + category choice
    val filteredProducts = remember(products, searchKey, selectedCat) {
        products.filter { prd ->
            val matchesCategory = selectedCat == null || prd.categoryEn == selectedCat
            val matchesSearch = searchKey.isBlank() ||
                    prd.nameEn.lowercase().contains(searchKey.lowercase()) ||
                    prd.nameGu.lowercase().contains(searchKey.lowercase()) ||
                    prd.categoryEn.lowercase().contains(searchKey.lowercase())
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step 1: Address header & Location Title
        item {
            AddressHeaderSection(viewModel)
        }

        // Step 2: Sticky quick search bar with voice search triggered capability
        item {
            SearchAndVoiceSection(viewModel)
        }

        if (searchKey.isBlank() && selectedCat == null) {
            // Step 3: High conversion Promos/Banners (rotates atomically)
            item {
                FeaturedPromoSection(viewModel, activeBanner)
            }

            // Step 4: Category chips layout
            item {
                CategoryChipsSection(viewModel)
            }

            // Step 5: Daily essentials
            item {
                Text(
                    text = viewModel.getLabel("Daily Essentials", "રોજિંદી જરૂરીયાતો"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                val dailyEssentialsPrds = products.filter { it.isDailyEssential }
                HorizontalProductsSlider(dailyEssentialsPrds, viewModel)
            }

            // Step 6: Recently ordered items / Discount Sections
            item {
                Text(
                    text = viewModel.getLabel("Recently Ordered & Discount Offers", "તાજેતરમાં મંગાવેલ અને ડિસ્કાઉન્ટ સ્કીમ"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                val discountsPrds = products.filter { it.discountPercent > 0 || it.isRecentlyOrdered }
                HorizontalProductsSlider(discountsPrds, viewModel)
            }
        }

        // Step 7: Main grid header & products
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCat != null) {
                        viewModel.getLabel(selectedCat ?: "", selectedCat ?: "")
                    } else if (searchKey.isNotBlank()) {
                        viewModel.getLabel("Search results for '$searchKey'", "'$searchKey' ના પરિણામો")
                    } else {
                        viewModel.getLabel("All Products Marketplace", "બધા જ પ્રોડક્ટ કેટલોગ")
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                if (selectedCat != null || searchKey.isNotBlank()) {
                    TextButton(onClick = {
                        viewModel.selectedCategory.value = null
                        viewModel.searchKeyword.value = ""
                    }) {
                        Text(viewModel.getLabel("Clear Filters", "ફિલ્ટર ખાલી કરો"), color = PatelGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // List item displaying all or filtered products
        if (filteredProducts.isEmpty()) {
            item {
                EmptyStateCard(
                    viewModel.getLabel("No grocery products found today.", "આ શ્રેણીમાં કોઈ પ્રોડક્ટ મળ્યા નથી."),
                    Icons.Default.HourglassEmpty
                )
            }
        } else {
            // Display products in chunks because nesting scrollable grids in LazyColumn is bad practice
            // Let's print them out using staggered grid simulation inside items block
            val productChunks = filteredProducts.chunked(2)
            items(productChunks) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (product in rowItems) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProductItemCard(product, viewModel)
                        }
                    }
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Safe spacing bottom spacer
        item {
            Spacer(modifier = Modifier.height(44.dp))
        }
    }

    // Voice search simulation overlay popup on screen
    if (voiceActive) {
        VoiceSearchDialog(viewModel)
    }
}

@Composable
fun AddressHeaderSection(viewModel: MallViewModel) {
    val addresses by viewModel.savedAddresses.collectAsState()
    val activeIdx by viewModel.selectedAddressIndex.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(PatelYellowLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBike,
                contentDescription = "Fast Delivery Icon",
                tint = PatelGreen,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { expanded = true }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = viewModel.getLabel("Fast Delivery to Kotda Jadodar", "ઝડપી ઘરપહોંચ સરનામું"),
                    fontSize = 11.sp,
                    color = PatelGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand Address select",
                    tint = PatelGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = addresses.getOrNull(activeIdx) ?: "Select location",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Address selection dropdown
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                addresses.forEachIndexed { idx, addr ->
                    DropdownMenuItem(
                        text = { Text(addr, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                        onClick = {
                            viewModel.selectedAddressIndex.value = idx
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchAndVoiceSection(viewModel: MallViewModel) {
    var textInput by remember { mutableStateOf(viewModel.searchKeyword.value) }

    LaunchedEffect(viewModel.searchKeyword.collectAsState().value) {
        textInput = viewModel.searchKeyword.value
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                1.5.dp,
                if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight,
                RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search Info",
            tint = PatelGreen,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = textInput,
            onValueChange = { input ->
                textInput = input
                viewModel.searchKeyword.value = input
            },
            placeholder = {
                Text(
                    text = viewModel.getLabel("Search fresh milk, atta, bataka...", "શાકભાજી, દૂધ, ચા કે ડુંગળી શોધો..."),
                    fontSize = 14.sp,
                    maxLines = 1
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                viewModel.searchKeyword.value = textInput
            }),
            modifier = Modifier
                .weight(1f)
                .testTag("home_search_bar")
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Simulated Voice Search Trigger Button!
        IconButton(
            onClick = { viewModel.simulateVoiceSearch() },
            modifier = Modifier
                .size(40.dp)
                .background(PatelYellow, CircleShape)
                .testTag("voice_search_trigger")
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice Search button",
                tint = TextDark,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FeaturedPromoSection(viewModel: MallViewModel, activeIndex: Int) {
    val items = listOf(
        Triple(
            viewModel.getLabel("FRESH VEGETABLES & FRUITS", "તાજા શાકભાજી અને ફળો"),
            viewModel.getLabel("Save 10% on Organic Kothmir & Bataka", "ખરીદો તાજા બટાકા અને કોથમીર ૧૦% ઓફર પર"),
            PatelGreen
        ),
        Triple(
            viewModel.getLabel("MILK & DIARY DAILY ESSENTIALS", "અમૂલ દૂધ અને પનીર ડેરી સ્કીમ"),
            viewModel.getLabel("Free Home Delivery on order values above ₹199", "રૂપિયા ૧૯૯ ઉપર મફત હોમ ડિલિવરી"),
            Color(0xFFE08D06)
        ),
        Triple(
            viewModel.getLabel("GUJARATI SWEETS SPECIAL", "શુદ્ધ કાજુ કતરી અને ઘી નાસ્તો"),
            viewModel.getLabel("Up to 15% discount for families in Kotda Jadodar", "સ્થાનિક ખાસ તહેવાર ડિસ્કાઉન્ટ ૧૫%"),
            PatelGreenDark
        )
    )

    val current = items[activeIndex]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(current.third)
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.75f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = current.first,
                color = PatelYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = current.second,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModel.getLabel("SHOP NOW", "ખરીદો"),
                    color = current.third,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Shop Now Arrow",
                    tint = current.third,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // Decorative subtle icons
        Icon(
            imageVector = Icons.Default.LocalMall,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.08f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(110.dp)
                .offset(y = 20.dp, x = 10.dp)
        )
    }
}

val DummyCategoriesList = listOf(
    DummyCategory("Vegetables & Fruits", "શાકભાજી અને ફળો", Icons.Default.Yard),
    DummyCategory("Milk & Dairy", "દૂધ અને ડેરી", Icons.Default.EggAlt),
    DummyCategory("Atta, Rice & Groceries", "કરિયાણું અને લોટ", Icons.Default.RiceBowl),
    DummyCategory("Snacks, Drinks & Sweets", "નાસ્તો અને સ્વીટ્સ", Icons.Default.LocalPizza)
)

@Composable
fun CategoryChipsSection(viewModel: MallViewModel) {
    val selectedCat by viewModel.selectedCategory.collectAsState()

    Column {
        Text(
            text = viewModel.getLabel("Shop by Category", "કેટેગરી પસંદ કરો"),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCat == null,
                    onClick = { viewModel.selectedCategory.value = null },
                    label = { Text(viewModel.getLabel("All", "બધા જ"), fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PatelGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }

            items(DummyCategoriesList) { cat ->
                val isSelected = selectedCat == cat.en
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedCategory.value = cat.en },
                    label = { Text(viewModel.getLabel(cat.en, cat.gu), fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.en,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) Color.White else PatelGreen
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PatelGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun HorizontalProductsSlider(products: List<ProductEntity>, viewModel: MallViewModel) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(products) { prd ->
            Box(modifier = Modifier.width(160.dp)) {
                ProductItemCard(prd, viewModel)
            }
        }
    }
}

@Composable
fun ProductItemCard(product: ProductEntity, viewModel: MallViewModel) {
    val cart by viewModel.cartState.collectAsState()
    val count = cart[product.id] ?: 0
    val isEnglish by viewModel.isLanguageEnglish.collectAsState()
    val wishlist by viewModel.wishlistState.collectAsState()
    val isWished = wishlist.contains(product.id)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Product Graphic Mock representation (uses overlapping elements based on frontend criteria)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(PatelGreenLight, PatelYellowLight)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Bold silhouette or graphic representing items
                        val itemIcon = when (product.categoryEn) {
                            "Vegetables & Fruits" -> Icons.Default.Agriculture
                            "Milk & Dairy" -> Icons.Default.Opacity
                            "Atta, Rice & Groceries" -> Icons.Default.RiceBowl
                            else -> Icons.Default.Fastfood
                        }
                        Icon(
                            imageVector = itemIcon,
                            contentDescription = product.nameEn,
                            tint = PatelGreenDark,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Quantity units variant label
                        Text(
                            text = "${product.variantSize} ${product.variantUnit.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PatelGreen
                        )
                    }

                    // Top Left Discount Badge
                    if (product.discountPercent > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .clip(RoundedCornerShape(bottomEnd = 10.dp))
                                .background(PatelYellow)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${product.discountPercent}% OFF",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = TextDark
                            )
                        }
                    }

                    // Stock indicators
                    if (product.stock == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = viewModel.getLabel("SOLD OUT", "સ્ટોક ખાલી"),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (product.stock <= 10) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .clip(RoundedCornerShape(topStart = 8.dp))
                                .background(Color.Red)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = viewModel.getLabel("LOW STOCK", "ઓછો સ્ટોક"),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product Title
                Text(
                    text = viewModel.getLabel(product.nameEn, product.nameGu),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Price display with discount strike
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "₹${product.discountedPrice.toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = PatelGreen
                    )
                    if (product.discountPercent > 0) {
                        Text(
                            text = "₹${product.price.toInt()}",
                            fontSize = 11.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Smart Cart Selector Add to cart block or incrementers
                if (count == 0) {
                    Button(
                        onClick = { if (product.stock > 0) viewModel.addToCart(product.id) },
                        enabled = product.stock > 0,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PatelGreenLight,
                            contentColor = PatelGreenDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("add_to_cart_btn_${product.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = viewModel.getLabel("ADD", "ઉમેરો"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PatelGreen)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { viewModel.removeFromCart(product.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Dec quantity",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = count.toString(),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { viewModel.addToCart(product.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Inc quantity",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Wishlist heart button (Top right)
            IconButton(
                onClick = { viewModel.toggleWishlist(product.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(34.dp)
            ) {
                Icon(
                    imageVector = if (isWished) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle wish",
                    tint = if (isWished) Color.Red else PatelGreenDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ------ CATEGORIES TAB ------

@Composable
fun CategoriesContent(viewModel: MallViewModel) {
    val isEnglish by viewModel.isLanguageEnglish.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()

    // Default category to Vegetables & Fruits on start
    LaunchedEffect(selectedCat) {
        if (selectedCat == null) {
            viewModel.selectedCategory.value = "Vegetables & Fruits"
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Column: Category menu selection
        Column(
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .background(if (viewModel.isDarkMode.collectAsState().value) SurfaceDark else PatelGreenLight.copy(alpha = 0.5f))
                .verticalScroll(rememberScrollState())
        ) {
            DummyCategoriesList.forEach { cat ->
                val isSelected = selectedCat == cat.en
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { viewModel.selectedCategory.value = cat.en }
                        .padding(horizontal = 8.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.en,
                            tint = if (isSelected) PatelGreen else TextMuted,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = viewModel.getLabel(cat.en, cat.gu),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PatelGreenDark else TextMuted,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        // Right Column: Products in selected category
        val categoryProducts = remember(products, selectedCat) {
            products.filter { it.categoryEn == selectedCat }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(12.dp)
        ) {
            Text(
                text = viewModel.getLabel(selectedCat ?: "", selectedCat ?: ""),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PatelGreenDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (categoryProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(viewModel.getLabel("No items cataloged here yet.", "આ શ્રેણીમાં હજી કોઈ ચિજો ઉપલબ્ધ નથી."))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categoryProducts) { prd ->
                        ProductItemCard(prd, viewModel)
                    }
                }
            }
        }
    }
}

// ------ CART TAB & CHECKOUT LEDGER ------

@Composable
fun CartContent(viewModel: MallViewModel, onNavigateToTracking: (String) -> Unit) {
    val items by viewModel.cartItemsList.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val discount by viewModel.promoDiscount.collectAsState()
    val tax by viewModel.gstTax.collectAsState()
    val delCharge by viewModel.deliveryCharge.collectAsState()
    val total by viewModel.cartTotal.collectAsState()
    val activeCoupon by viewModel.selectedCouponCode.collectAsState()

    var couponInput by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("UPI Payments") }

    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Empty Cart",
                    tint = PatelGreenLight,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = viewModel.getLabel("Your Patel Mall Cart is Empty!", "તમારું કાર્ટ ખાલી છે!"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = viewModel.getLabel("Quick grocery shipping starts on home tab.", "આઇટમ્સ ઉમેરવા શોપિંગ હોમ પેજ પર જાઓ."),
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.customerTab.value = CustomerTab.HOME },
                    colors = ButtonDefaults.buttonColors(containerColor = PatelGreen)
                ) {
                    Text(viewModel.getLabel("Shop Groceries Now", "હમણાં શોપિંગ કરો"), color = Color.White)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title Summary
        item {
            Text(
                text = viewModel.getLabel("Your Grocery Invoice Summary", "ઓર્ડર ઘરપહોંચ બિલ"),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 2. Ordered Items list
        items(items) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(PatelGreenLight, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.RestaurantMenu, contentDescription = "Item Logo", tint = PatelGreen)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viewModel.getLabel(item.product.nameEn, item.product.nameGu),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${item.product.variantSize} ${item.product.variantUnit} × ${item.quantity}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "₹${(item.product.discountedPrice * item.quantity).toInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PatelGreen
                    )
                }
            }
        }

        // 3. Coupon Promo entry section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PatelYellowLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.getLabel("Apply Promos (PATEL15, WELCOME20)", "ડિસ્કાઉન્ટ કૂપન (PATEL15, WELCOME20)"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = PatelGreenDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = couponInput,
                            onValueChange = { couponInput = it },
                            placeholder = { Text("PATEL15", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        )
                        Button(
                            onClick = {
                                if (viewModel.applyCoupon(couponInput)) {
                                    couponInput = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PatelGreen),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(viewModel.getLabel("Apply", "લાગુ કરો"), color = Color.White)
                        }
                    }
                    if (activeCoupon != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "✓ Promo code '$activeCoupon' applied!",
                            color = PatelGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Detailed Invoice PDF breakdown (Smart billing calculations requested!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(viewModel.getLabel("Order Subtotal", "આઈટમ્સ કુલ કિંમત"), color = TextMuted, fontSize = 13.sp)
                        Text("₹${subtotal.toInt()}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    }
                    if (discount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(viewModel.getLabel("Discount Promo Plan", "લાગુ કૂપન ડિસ્કાઉન્ટ"), color = Color(0xFFC78007), fontSize = 13.sp)
                            Text("-₹${discount.toInt()}", color = Color(0xFFC78007), fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(viewModel.getLabel("CGST + SGST tax (5%)", "જીએસટી ટેક્સ (૫%)"), color = TextMuted, fontSize = 13.sp)
                        Text("₹${tax.toInt()}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(viewModel.getLabel("Delivery Service Fee", "ઘરપહોંચ સર્વિસ ચાર્જ"), color = TextMuted, fontSize = 13.sp)
                        Text(if (delCharge == 0.0) viewModel.getLabel("FREE", "મફત") else "₹${delCharge.toInt()}", color = PatelGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(viewModel.getLabel("Grand Total Billings", "ચૂકવવાપાત્ર કુલ રકમ"), fontWeight = FontWeight.Bold, color = PatelGreenDark, fontSize = 15.sp)
                        Text("₹${total.toInt()}", fontWeight = FontWeight.Black, color = PatelGreen, fontSize = 16.sp)
                    }
                }
            }
        }

        // 5. Select payment gateway (GPay, PhonePe, Paytm, COD)
        item {
            Column {
                Text(
                    text = viewModel.getLabel("Select Secure Payment Mode", "ચુકવણી પદ્ધતિ પસંદ કરો"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val methods = listOf(
                    "UPI Payments (GPay/PhonePe)",
                    "Paytm Instant QR Mode",
                    "Visa/Master Credit Card",
                    "Cash on Delivery"
                )

                methods.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = method }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == method,
                            onClick = { selectedPaymentMethod = method },
                            colors = RadioButtonDefaults.colors(selectedColor = PatelGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(method, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // 6. Action button: "Place Order" with SAT GPS route loader!
        item {
            Button(
                onClick = { viewModel.placeOrder(selectedPaymentMethod) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PatelGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("place_order_button")
            ) {
                Text(
                    text = viewModel.getLabel("Pay ₹${total.toInt()} & Order Now", "ઓર્ડર કન્ફર્મ કરો - ₹${total.toInt()}"),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ------ ORDERS TAB ------

@Composable
fun OrdersContent(viewModel: MallViewModel, onNavigateToTracking: (String) -> Unit) {
    val orders by viewModel.allOrders.collectAsState()

    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                viewModel.getLabel("No historic orders logged.", "તમે હજી કોઈ સામાન મંગાવ્યો નથી."),
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = viewModel.getLabel("Your Patel Mall Orders Ledger", "નજીકના અને ચાલુ ઓર્ડર્સ"),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(orders) { order ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(order.orderId, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PatelGreen)
                        
                        // Status badge colorizer
                        val statusBg = when (order.orderStatus) {
                            "Order received" -> Color.LightGray
                            "Packing" -> PatelYellowLight
                            "Out for delivery" -> Color.Cyan
                            else -> PatelGreenLight
                        }
                        val statusTxt = when (order.orderStatus) {
                            "Order received" -> TextDark
                            "Packing" -> Color(0xFFC47600)
                            "Out for delivery" -> PatelGreenDark
                            else -> PatelGreenDark
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                order.orderStatus,
                                color = statusTxt,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = order.itemsSummary,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(viewModel.getLabel("Billing Total:", "કુલ બિલ અમાઉન્ટ:"), fontSize = 11.sp, color = TextMuted)
                            Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = PatelGreen)
                        }

                        // GPS live map navigation button if dispatch active!
                        if (order.orderStatus != "Delivered") {
                            Button(
                                onClick = { onNavigateToTracking(order.orderId) },
                                colors = ButtonDefaults.buttonColors(containerColor = PatelYellow)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Track order Map",
                                    tint = TextDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    viewModel.getLabel("LIVE GPS MAP", "લાઈવ નકશો"),
                                    color = TextDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            // Already delivered
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Done", tint = PatelGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    viewModel.getLabel("Order Fulfilled", "ડિલિવરી પૂર્ણ થઈ"),
                                    color = PatelGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------ PROFILE TAB ------

@Composable
fun ProfileContent(viewModel: MallViewModel, onBackToWelcome: () -> Unit) {
    val phoneNum by viewModel.userPhoneNumber.collectAsState()
    val ctx = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User primary profile card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PatelGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person2, contentDescription = "Avatar", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = viewModel.getLabel("Patel Mall Shopper", "પટેલ મોલ ગ્રાહક"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "+91 ${phoneNum.ifBlank { "9427126911" }}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // WhatsApp call details direct linkage button (+919427126911 requested!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.getLabel("Local Shop Helpline & Support", "સ્થાનિક હેલ્પલાઇન અને સહાય"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.getLabel(
                            "Have questions about your delivery in Kotda Jadodar? Connect on WhatsApp immediately.",
                            "શું તમને ડિલિવરી અંગે પ્રશ્ન છે? તો હમણાં જ વોટ્સએપ પર કોન્ટેક્ટ કરો."
                        ),
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // WhatsApp Direct trigger button
                        Button(
                            onClick = {
                                val url = "https://api.whatsapp.com/send?phone=919427126911&text=Hello%20Patel%20Mall"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                try {
                                    ctx.startActivity(intent)
                                } catch (e: Exception) {
                                    // fallback call dialer
                                    val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919427126911"))
                                    ctx.startActivity(dial)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = "WhatsApp Chat Support", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Phone Call Dialer
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919427126911"))
                                ctx.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PatelGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call support dial", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(viewModel.getLabel("Call Shop", "દુકાન કોલ"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Saved address management (address book edit)
        item {
            SavedAddressesProfileSection(viewModel)
        }

        // Logout
        item {
            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = "Log Out user icon", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(viewModel.getLabel("Logout Customer Profile", "પ્રોફાઇલમાંથી લોગઆઉટ"), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SavedAddressesProfileSection(viewModel: MallViewModel) {
    val addressList by viewModel.savedAddresses.collectAsState()
    val newAddress by viewModel.newAddressInput.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, if (viewModel.isDarkMode.collectAsState().value) BorderDark else BorderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = viewModel.getLabel("Saved Delivery Addresses", "સાચવેલા ઘરપહોંચ સરનામા"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            addressList.forEach { addr ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Address Location bubble", tint = PatelGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(addr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = viewModel.getLabel("Add New Kotda Address", "નવું સરનામું ઉમેરો"),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PatelGreen
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            OutlinedTextField(
                value = newAddress,
                onValueChange = { viewModel.newAddressInput.value = it },
                placeholder = { Text("Jakat Naka Chowk, Kotda Jadodar...", fontSize = 12.sp) },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { viewModel.addNewAddress() },
                colors = ButtonDefaults.buttonColors(containerColor = PatelGreenLight, contentColor = PatelGreenDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(viewModel.getLabel("Save", "સેવ") , fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ------ EXTRA COMPOSABLES (Voice dialogue overlay) ------

@Composable
fun VoiceSearchDialog(viewModel: MallViewModel) {
    val text by viewModel.voiceSearchText.collectAsState()
    val feedbackLabel by viewModel.voiceSearchFeedbackLabel.collectAsState()

    AlertDialog(
        onDismissRequest = { viewModel.voiceSearchActive.value = false },
        confirmButton = {},
        title = {
            Text(
                text = viewModel.getLabel("Voice Search Helper", "વોઇસ આસિસ્ટન્ટ શોધો"),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = PatelGreen
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(PatelYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mic listening indicator",
                        tint = TextDark,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = feedbackLabel,
                    fontSize = 14.sp,
                    color = PatelGreenDark,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (text.isNotBlank()) "\"$text\"" else viewModel.getLabel("Try saying 'milk', 'dudh' or 'mango'...", "કહો 'દૂધ', 'બેકરી' કે 'કેરી'..."),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun EmptyStateCard(message: String, icon: ImageVector) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PatelGreenLight.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(24.dp)
        ) {
            Icon(imageVector = icon, contentDescription = "Empty", tint = PatelGreen, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, fontSize = 13.sp, color = PatelGreenDark, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}
