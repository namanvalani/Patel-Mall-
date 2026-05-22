package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.repository.MallRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppRole {
    SPLASH, LOGIN, CUSTOMER, ADMIN, DELIVERY_BOY
}

enum class CustomerTab {
    HOME, CATEGORIES, CART, ORDERS, PROFILE
}

data class CartItem(val product: ProductEntity, val quantity: Int)

class MallViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MallRepository(database.productDao(), database.orderDao())

    // --- Core State Variables ---
    val currentRole = MutableStateFlow(AppRole.SPLASH)
    val customerTab = MutableStateFlow(CustomerTab.HOME)
    val isLanguageEnglish = MutableStateFlow(true) // Toggle English vs Gujarati
    val isDarkMode = MutableStateFlow(false) // Toggle Light vs Dark

    val allProducts = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allOrders = repository.allOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeOrders = repository.activeOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lowStockProducts = repository.lowStockProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Authentication ---
    val userPhoneNumber = MutableStateFlow("")
    val isLoggedIn = MutableStateFlow(false)
    val otpSent = MutableStateFlow(false)
    val enteredOtp = MutableStateFlow("")
    val generatedOtp = MutableStateFlow("")

    // Saved Addresses
    val savedAddresses = MutableStateFlow(
        listOf(
            "Main Bazar Road, Near Patel Chowk, Kotda Jadodar, Gujarat - 370001",
            "Sardar Patel Society, Block C-14, Kotda Jadodar - 370001"
        )
    )
    val selectedAddressIndex = MutableStateFlow(0)
    val newAddressInput = MutableStateFlow("")

    // --- Customer Shopping State ---
    val cartState = MutableStateFlow<Map<Int, Int>>(emptyMap()) // productId -> quantity
    val searchKeyword = MutableStateFlow("")
    val voiceSearchActive = MutableStateFlow(false)
    val voiceSearchText = MutableStateFlow("")
    val voiceSearchFeedbackLabel = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null)
    val activeBannerIndex = MutableStateFlow(0)
    val selectedCouponCode = MutableStateFlow<String?>(null)
    val wishlistState = MutableStateFlow<Set<Int>>(emptySet()) // set of productIds for wishlist

    // --- Live Tracking System ---
    val currentTrackingOrderId = MutableStateFlow<String?>(null)
    val trackingOrderState = MutableStateFlow<OrderEntity?>(null)
    private var trackingSimJob: Job? = null

    // --- User Dashboard Notification Banner Alerts ---
    val notifyMessage = MutableStateFlow<String?>(null)

    // --- Admin state variables ---
    val newProductNameEn = MutableStateFlow("")
    val newProductNameGu = MutableStateFlow("")
    val newProductCategoryEn = MutableStateFlow("Vegetables & Fruits")
    val newProductPrice = MutableStateFlow("")
    val newProductDiscount = MutableStateFlow("0")
    val newProductStock = MutableStateFlow("50")
    val newProductVariantUnit = MutableStateFlow("kg") // kg, gram, liter, packet, pieces
    val newProductVariantSize = MutableStateFlow("1")

    // --- Delivery Boy Panel state ---
    val selectedDeliveryBoyId = MutableStateFlow(1) // ID of current delivery boy
    val deliveryBoyName = MutableStateFlow("Gopal Patel")

    init {
        viewModelScope.launch {
            // Trigger database pre-population
            repository.prepopulateDatabaseIfEmpty()
            
            // Periodically cycle offers banners automatically
            launch {
                while (true) {
                    delay(5000)
                    activeBannerIndex.value = (activeBannerIndex.value + 1) % 3
                }
            }
        }
    }

    // --- Auth Logic ---
    fun requestOtp() {
        if (userPhoneNumber.value.length < 10) {
            triggerNotification(
                en = "Please enter a valid 10-digit mobile number.",
                gu = "કૃપા કરીને સાચો ૧૦ આંકડાનો મોબાઇલ નંબર દાખલ કરો."
            )
            return
        }
        val otp = String.format("%04d", Random.nextInt(1000, 9999))
        generatedOtp.value = otp
        otpSent.value = true
        // Prefill OTP for user-friendly testing execution
        enteredOtp.value = otp
        triggerNotification(
            en = "OTP Code is $otp. Simulated for Patel Mall.",
            gu = "OTP કોડ $otp છે. પટેલ મોલ માટે સિમ્યુલેટેડ."
        )
    }

    fun verifyOtp() {
        if (enteredOtp.value == generatedOtp.value && enteredOtp.value.isNotEmpty()) {
            isLoggedIn.value = true
            currentRole.value = AppRole.CUSTOMER
            triggerNotification(
                en = "Login successful! Welcome to Patel Mall.",
                gu = "લોગિન સફળ રહ્યું! પટેલ મોલમાં આપનું સ્વાગત છે."
            )
        } else {
            triggerNotification(
                en = "Invalid OTP. Please try again.",
                gu = "ખોટો OTP. કૃપા કરીને ફરી પ્રયાસ કરો."
            )
        }
    }

    fun logout() {
        isLoggedIn.value = false
        otpSent.value = false
        enteredOtp.value = ""
        userPhoneNumber.value = ""
        cartState.value = emptyMap()
        currentRole.value = AppRole.LOGIN
    }

    // --- Language Translation Helpers ---
    fun getLabel(en: String, gu: String): String {
        return if (isLanguageEnglish.value) en else gu
    }

    // --- Cart Actions ---
    fun addToCart(productId: Int) {
        val currentCart = cartState.value.toMutableMap()
        val currentQty = currentCart[productId] ?: 0
        
        // Check stock
        val product = allProducts.value.find { it.id == productId }
        if (product != null) {
            if (currentQty >= product.stock) {
                triggerNotification(
                    en = "Only ${product.stock} items available in stock.",
                    gu = "સ્ટોકમાં માત્ર ${product.stock} વસ્તુઓ જ ઉપલબ્ધ છે."
                )
                return
            }
            currentCart[productId] = currentQty + 1
            cartState.value = currentCart
            triggerNotification(
                en = "${product.nameEn} added to cart.",
                gu = "${product.nameGu} કાર્ટમાં મેળવવામાં આવ્યો."
            )
        }
    }

    fun removeFromCart(productId: Int) {
        val currentCart = cartState.value.toMutableMap()
        val currentQty = currentCart[productId] ?: 0
        if (currentQty > 1) {
            currentCart[productId] = currentQty - 1
        } else {
            currentCart.remove(productId)
        }
        cartState.value = currentCart
    }

    fun clearCart() {
        cartState.value = emptyMap()
        selectedCouponCode.value = null
    }

    fun toggleWishlist(productId: Int) {
        val currentWishlist = wishlistState.value.toMutableSet()
        if (currentWishlist.contains(productId)) {
            currentWishlist.remove(productId)
            triggerNotification(
                en = "Removed from wishlist.",
                gu = "ઇચ્છાસૂચિમાંથી કાઢી નાખવામાં આવ્યું."
            )
        } else {
            currentWishlist.add(productId)
            triggerNotification(
                en = "Added to wishlist.",
                gu = "ઇચ્છાસૂચિમાં ઉમેરવામાં આવ્યું."
            )
        }
        wishlistState.value = currentWishlist
    }

    // --- Calculated Cart Values ---
    val cartItemsList: StateFlow<List<CartItem>> = cartState
        .combine(allProducts) { CartMap, ProductList ->
            CartMap.mapNotNull { (prodId, qty) ->
                val prd = ProductList.find { it.id == prodId }
                if (prd != null) CartItem(prd, qty) else null
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartSubtotal: StateFlow<Double> = cartItemsList
        .map { items ->
            items.sumOf { it.product.discountedPrice * it.quantity }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val promoDiscount: StateFlow<Double> = cartSubtotal
        .combine(selectedCouponCode) { subtotal, coupon ->
            when (coupon) {
                "PATEL15" -> subtotal * 0.15
                "WELCOME20" -> subtotal * 0.20
                "BLINKIT20" -> (subtotal * 0.20).coerceAtMost(50.0)
                else -> 0.0
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val gstTax: StateFlow<Double> = cartSubtotal
        .map { subtotal ->
            subtotal * 0.05 // 5% GST standard on fresh foods & groceries
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val deliveryCharge: StateFlow<Double> = cartSubtotal
        .map { subtotal ->
            if (subtotal >= 199.0 || subtotal == 0.0) 0.0 else 25.0 // Free delivery over Rs 199
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTotal: StateFlow<Double> = combine(
        cartSubtotal,
        promoDiscount,
        gstTax,
        deliveryCharge
    ) { sub, disc, tax, del ->
        (sub - disc + tax + del).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Apply coupon checks
    fun applyCoupon(code: String): Boolean {
        val uppercaseCode = code.trim().uppercase()
        return if (uppercaseCode == "PATEL15" || uppercaseCode == "WELCOME20" || uppercaseCode == "BLINKIT20") {
            selectedCouponCode.value = uppercaseCode
            triggerNotification(
                en = "Coupon code '$uppercaseCode' applied successfully!",
                gu = "કૂપન કોડ '$uppercaseCode' સફળતાપૂર્વક લાગુ થયો!"
            )
            true
        } else {
            triggerNotification(
                en = "Invalid coupon code.",
                gu = "અમાન્ય કૂપન કોડ."
            )
            false
        }
    }

    // --- Voice Search Simulation ---
    fun simulateVoiceSearch() {
        voiceSearchActive.value = true
        voiceSearchText.value = ""
        voiceSearchFeedbackLabel.value = "Listening..."
        
        viewModelScope.launch {
            delay(1500)
            voiceSearchFeedbackLabel.value = "Processing voice..."
            delay(1000)
            // Pick a random product phrase representing voice search query
            val randomQueries = listOf(
                "dudh" to "Amul Gold Milk Premium",
                "milk" to "Amul Gold Milk Premium",
                "bataka" to "Potatoes",
                "potato" to "Fresh Farm Potatoes (Bataka)",
                "mango" to "Junagadh Kesar Mango",
                "cha" to "Wagh Bakri Premium Tea",
                "tea" to "Wagh Bakri Premium Tea",
                "atta" to "Aashirvaad Shudh Chakki Atta",
                "sugar" to "Madhur Pure Sugar"
            )
            val selected = randomQueries.random()
            val textToSpeech = selected.first
            val matchName = selected.second
            
            voiceSearchText.value = textToSpeech.uppercase()
            voiceSearchFeedbackLabel.value = "Searching for: '$matchName'"
            delay(1000)
            
            searchKeyword.value = textToSpeech
            selectedCategory.value = null
            customerTab.value = CustomerTab.HOME
            voiceSearchActive.value = false
            
            triggerNotification(
                en = "Searched for $matchName via voice search.",
                gu = "વોઇસ સર્ચ દ્વારા $matchName શોધવામાં આવ્યું."
            )
        }
    }

    // Add address
    fun addNewAddress() {
        if (newAddressInput.value.isNotBlank()) {
            val updated = savedAddresses.value.toMutableList()
            updated.add(newAddressInput.value.trim())
            savedAddresses.value = updated
            newAddressInput.value = ""
            triggerNotification(
                en = "Address saved, Kotda Jadodar.",
                gu = "સરનામું સાચવવામાં આવ્યું છે."
            )
        }
    }

    // --- Invoice Creation and PDF Simulator ---
    fun placeOrder(paymentMethod: String) {
        val items = cartItemsList.value
        if (items.isEmpty()) return

        val id = "PM-KT-" + Random.nextInt(10000, 99999).toString()
        val summaryText = items.joinToString("\n") {
            "${it.quantity}x ${if (isLanguageEnglish.value) it.product.nameEn else it.product.nameGu} (${it.product.variantSize} ${it.product.variantUnit})"
        }

        viewModelScope.launch {
            val order = OrderEntity(
                orderId = id,
                customerName = if (userPhoneNumber.value.isNotBlank()) "Customer (+91-${userPhoneNumber.value})" else "Guest User",
                customerPhone = userPhoneNumber.value.ifBlank { "9427126911" },
                deliveryAddress = savedAddresses.value.getOrNull(selectedAddressIndex.value) ?: "Main Bazar, Kotda Jadodar",
                itemsSummary = summaryText,
                subtotal = cartSubtotal.value,
                gstAmount = gstTax.value,
                deliveryCharges = deliveryCharge.value,
                couponCode = selectedCouponCode.value,
                discountAmount = promoDiscount.value,
                totalAmount = cartTotal.value,
                paymentMethod = paymentMethod,
                paymentStatus = if (paymentMethod == "Cash on Delivery") "Pending" else "Paid",
                orderStatus = "Order received",
                deliveryBoyName = "Gopal Patel",
                deliveryBoyPhone = "+919427126911", // Patel Mall quick service
                deliveryBoyId = selectedDeliveryBoyId.value
            )

            repository.insertOrder(order)
            // Clear cart & variables
            clearCart()
            
            // Navigate to Order tab
            customerTab.value = CustomerTab.ORDERS
            currentTrackingOrderId.value = id
            
            triggerNotification(
                en = "Order $id Placed successfully! Quick delivery boy assigned.",
                gu = "ઓર્ડર $id સફળતાપૂર્વક મૂકાયો છે અને ડિલિવરી બોય ફાળવેલ છે!"
            )
            
            // Start GPS live route simulation to Kotda Jadodar base service location!
            startLiveLocationSimulation(id)
        }
    }

    // --- Live Location Simulation Engine ---
    fun startLiveLocationSimulation(orderId: String) {
        trackingSimJob?.cancel()
        trackingSimJob = viewModelScope.launch {
            Log.d("MallViewModel", "Starting GPS simulation for order: $orderId")
            
            // Simulating coordinates wrapping around Kotda Jadodar village, Nakhatrana, Kutch, Gujarat
            // Center is roughly: Lat: 22.1384, Lng: 69.9576
            val routeCoords = listOf(
                Pair(22.1310, 69.9510), // Warehousing / Patel Mall Outlet Kotda Jadodar
                Pair(22.1332, 69.9535), // Village Main Chauk
                Pair(22.1350, 69.9555), // Temple road junction
                Pair(22.1370, 69.9568), // Shopping lane Kotda
                Pair(22.1384, 69.9576), // Customer House / Base Destination
            )

            repository.updateOrderStatus(orderId, "Order received")
            var orderObj = repository.getOrderById(orderId)
            trackingOrderState.value = orderObj

            // Step 1: Packing (Delay 4 sec)
            delay(4000)
            if (currentTrackingOrderId.value == orderId) {
                repository.updateOrderStatus(orderId, "Packing")
                orderObj = repository.getOrderById(orderId)
                trackingOrderState.value = orderObj
                triggerNotification(
                    en = "Order is being packed at Patel Mall Kotda outlet...",
                    gu = "પટેલ મોલ કોટડા આઉટલેટ પર ઓર્ડર પેક થઈ રહ્યો છે..."
                )
            }

            // Step 2: Out for Delivery (Delay 4 sec, then route begins)
            delay(4000)
            if (currentTrackingOrderId.value == orderId) {
                repository.updateOrderStatus(orderId, "Out for delivery")
                orderObj = repository.getOrderById(orderId)
                trackingOrderState.value = orderObj
                triggerNotification(
                    en = "Delivery partner is out for delivery with your groceries! Live GPS tracking active.",
                    gu = "ડિલિવરી પાર્ટનર તમારો સામાન લઈને આવી રહ્યો છે! લાઈવ GPS ટ્રેકિંગ સક્રિય છે."
                )
            }

            // Step 3: Moving GPS markers on the map simulation
            for (coord in routeCoords) {
                delay(3000)
                if (currentTrackingOrderId.value == orderId) {
                    repository.updateOrderLocation(orderId, coord.first, coord.second)
                    orderObj = repository.getOrderById(orderId)
                    trackingOrderState.value = orderObj
                } else {
                    break
                }
            }

            // Step 4: Delivered
            delay(3000)
            if (currentTrackingOrderId.value == orderId) {
                repository.updateOrderStatus(orderId, "Delivered")
                orderObj = repository.getOrderById(orderId)
                trackingOrderState.value = orderObj
                triggerNotification(
                    en = "Your groceries from Patel Mall have been delivered. Thank you!",
                    gu = "પટેલ મોલ તરફથી તમારી ડિલિવરી મળી ગઈ છે. આભાર!"
                )
            }
        }
    }

    fun selectOrderForTracking(orderId: String) {
        currentTrackingOrderId.value = orderId
        viewModelScope.launch {
            val orderObj = repository.getOrderById(orderId)
            trackingOrderState.value = orderObj
            if (orderObj != null && orderObj.orderStatus != "Delivered") {
                // Restart simulation if active
                startLiveLocationSimulation(orderId)
            }
        }
    }

    // --- Admin Operations ---
    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            val p = allProducts.value.find { it.id == id }
            repository.deleteProductById(id)
            triggerNotification(
                en = "Deleted product: ${p?.nameEn}",
                gu = "પ્રોડક્ટ કાઢી નાખી: ${p?.nameGu}"
            )
        }
    }

    fun saveProductFromAdmin() {
        val enName = newProductNameEn.value.trim()
        val guName = newProductNameGu.value.trim()
        val priceVal = newProductPrice.value.toDoubleOrNull() ?: 0.0
        val discountVal = newProductDiscount.value.toIntOrNull() ?: 0
        val stockVal = newProductStock.value.toIntOrNull() ?: 50
        val unitStr = newProductVariantUnit.value
        val sizeStr = newProductVariantSize.value

        if (enName.isBlank() || guName.isBlank() || priceVal <= 0.0) {
            triggerNotification(
                en = "Name and correct Price are mandatory.",
                gu = "નામ અને સાચી કિંમત ફરજિયાત છે."
            )
            return
        }

        viewModelScope.launch {
            val newProduct = ProductEntity(
                nameEn = enName,
                nameGu = guName,
                categoryEn = newProductCategoryEn.value,
                categoryGu = when (newProductCategoryEn.value) {
                    "Vegetables & Fruits" -> "શાકભાજી અને ફળો"
                    "Milk & Dairy" -> "દૂધ અને ડેરી"
                    "Atta, Rice & Groceries" -> "કરિયાણું અને લોટ"
                    "Snacks, Drinks & Sweets" -> "નાસ્તો અને સ્વીટ્સ"
                    else -> "જનરલ કરિયાણું"
                },
                price = priceVal,
                discountPercent = discountVal,
                stock = stockVal,
                variantUnit = unitStr,
                variantSize = sizeStr,
                isFeatured = Random.nextBoolean()
            )

            repository.insertProduct(newProduct)
            // Reset input fields
            newProductNameEn.value = ""
            newProductNameGu.value = ""
            newProductPrice.value = ""
            newProductDiscount.value = "0"
            newProductStock.value = "50"
            
            triggerNotification(
                en = "Saved '$enName' to database catalog successfully.",
                gu = "ડેટાબેઝ કેટલોગમાં '$enName' સફળતાપૂર્વક સાચવવામાં આવ્યું."
            )
        }
    }

    fun updateOrderStateFromAdmin(orderId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            val updatedObj = repository.getOrderById(orderId)
            if (currentTrackingOrderId.value == orderId) {
                trackingOrderState.value = updatedObj
            }
            triggerNotification(
                en = "Admin updated status of $orderId to: $newStatus",
                gu = "એડમિને $orderId ના ઓર્ડર સ્ટેટસ અપડેટ કરીને '$newStatus' કર્યું છે."
            )
        }
    }

    fun restockProduct(productId: Int, qtyToAdd: Int) {
        viewModelScope.launch {
            val prd = allProducts.value.find { it.id == productId }
            if (prd != null) {
                val updated = prd.copy(stock = prd.stock + qtyToAdd)
                repository.updateProduct(updated)
                triggerNotification(
                    en = "Restocked ${prd.nameEn} with +$qtyToAdd units.",
                    gu = "${prd.nameGu} માં +$qtyToAdd નો નવો સ્ટોક ઉમેરાયો."
                )
            }
        }
    }

    // --- Delivery Boy Operations ---
    fun completeDelivery(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "Delivered")
            val updatedObj = repository.getOrderById(orderId)
            if (currentTrackingOrderId.value == orderId) {
                trackingOrderState.value = updatedObj
            }
            triggerNotification(
                en = "Logistics partner completed delivery for counter slip $orderId.",
                gu = "ડિલિવરી પાર્ટનરે $orderId માટે ડિલિવરી પૂર્ણ કરી દીધી."
            )
        }
    }

    fun updateDeliveryLocationByPartner(orderId: String) {
        viewModelScope.launch {
            // Pick a random location within Kotda Jadodar village
            val lat = 22.1384 + Random.nextDouble(-0.005, 0.005)
            val lng = 69.9576 + Random.nextDouble(-0.005, 0.005)
            repository.updateOrderLocation(orderId, lat, lng)
            val updatedObj = repository.getOrderById(orderId)
            if (currentTrackingOrderId.value == orderId) {
                trackingOrderState.value = updatedObj
            }
            triggerNotification(
                en = "GPS coordinates broadcasted to map satellite.",
                gu = "GPS કોઓર્ડિનેટ્સ મેપ સેટેલાઇટ પર પ્રસારિત થયા છે."
            )
        }
    }

    // --- Alert Notification Manager ---
    private fun triggerNotification(en: String, gu: String) {
        viewModelScope.launch {
            notifyMessage.value = if (isLanguageEnglish.value) en else gu
            delay(3000)
            if (notifyMessage.value == en || notifyMessage.value == gu) {
                notifyMessage.value = null
            }
        }
    }

    fun dismissNotification() {
        notifyMessage.value = null
    }
}
