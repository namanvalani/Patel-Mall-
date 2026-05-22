package com.example.data.repository

import com.example.data.db.ProductDao
import com.example.data.db.OrderDao
import com.example.data.model.ProductEntity
import com.example.data.model.OrderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import android.util.Log

class MallRepository(
    private val productDao: ProductDao,
    private val orderDao: OrderDao
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val activeOrders: Flow<List<OrderEntity>> = orderDao.getActiveOrders()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts(10)

    fun getProductsByCategory(categoryEn: String): Flow<List<ProductEntity>> =
        productDao.getProductsByCategory(categoryEn)

    fun getDailyEssentials(): Flow<List<ProductEntity>> =
        productDao.getDailyEssentials()

    fun getFeaturedProducts(): Flow<List<ProductEntity>> =
        productDao.getFeaturedProducts()

    suspend fun insertProduct(product: ProductEntity) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProductById(id: Int) {
        productDao.deleteProductById(id)
    }

    suspend fun insertOrder(order: OrderEntity) {
        orderDao.insertOrder(order)
        // Auto deduct stock for items ordered
        deductStockFromOrder(order.itemsSummary)
    }

    suspend fun updateOrder(order: OrderEntity) {
        orderDao.updateOrder(order)
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        orderDao.updateOrderStatus(orderId, status)
    }

    suspend fun updateOrderLocation(orderId: String, lat: Double, lng: Double) {
        orderDao.updateOrderLocation(orderId, lat, lng)
    }

    suspend fun getOrderById(orderId: String): OrderEntity? {
        return orderDao.getOrderById(orderId)
    }

    private suspend fun deductStockFromOrder(itemsSummary: String) {
        try {
            // Summary contains lines like: "1x Amul Gold Milk (1 Packet)"
            // Let's match by product name or parsed elements and deduct stock.
            val lines = itemsSummary.split("\n")
            val productsList = productDao.getAllProducts().first()
            for (line in lines) {
                if (line.isBlank()) continue
                // Try to find if any product name is contained in this line
                val cleanLine = line.lowercase()
                val foundProduct = productsList.find {
                    cleanLine.contains(it.nameEn.lowercase()) || cleanLine.contains(it.nameGu.lowercase())
                }
                if (foundProduct != null) {
                    // Extract quantity multiplier
                    var qty = 1
                    val matchResult = Regex("(\\d+)x").find(line)
                    if (matchResult != null) {
                        qty = matchResult.groupValues[1].toIntOrNull() ?: 1
                    }
                    val newStock = (foundProduct.stock - qty).coerceAtLeast(0)
                    productDao.updateProduct(foundProduct.copy(stock = newStock))
                    Log.d("MallRepository", "Deducted stock for ${foundProduct.nameEn}: Old stock=${foundProduct.stock}, New stock=$newStock, Qty ordered=$qty")
                }
            }
        } catch (e: Exception) {
            Log.e("MallRepository", "Error deducting stock: ${e.message}")
        }
    }

    suspend fun prepopulateDatabaseIfEmpty() {
        val current = productDao.getAllProducts().first()
        if (current.isEmpty()) {
            Log.d("MallRepository", "Database is empty. Pre-populating default grocery products...")
            val defaults = listOf(
                // Vegetables
                ProductEntity(
                    nameEn = "Fresh Coriander Leaf (Kothmir)",
                    nameGu = "તાજી અર્ગેનિક કોથમીર",
                    categoryEn = "Vegetables & Fruits",
                    categoryGu = "શાકભાજી અને ફળો",
                    price = 15.0,
                    discountPercent = 10,
                    stock = 45,
                    variantUnit = "gram",
                    variantSize = "100",
                    isDailyEssential = true,
                    isFeatured = true,
                    isRecentlyOrdered = true
                ),
                ProductEntity(
                    nameEn = "Fresh Farm Potatoes (Bataka)",
                    nameGu = "તાજા બટાકા",
                    categoryEn = "Vegetables & Fruits",
                    categoryGu = "શાકભાજી અને ફળો",
                    price = 35.0,
                    discountPercent = 15,
                    stock = 150,
                    variantUnit = "kg",
                    variantSize = "1",
                    isDailyEssential = true,
                    isFeatured = false,
                    isRecentlyOrdered = true
                ),
                ProductEntity(
                    nameEn = "Local Red Onions (Dungri)",
                    nameGu = "લાલ ડુંગળી",
                    categoryEn = "Vegetables & Fruits",
                    categoryGu = "શાકભાજી અને ફળો",
                    price = 40.0,
                    discountPercent = 0,
                    stock = 8, // Low stock on purpose to trigger alert!
                    variantUnit = "kg",
                    variantSize = "1",
                    isDailyEssential = true,
                    isFeatured = true
                ),
                ProductEntity(
                    nameEn = "Junagadh Kesar Mango",
                    nameGu = "જૂનાગઢ કેસર કેરી",
                    categoryEn = "Vegetables & Fruits",
                    categoryGu = "શાકભાજી અને ફળો",
                    price = 180.0,
                    discountPercent = 20,
                    stock = 60,
                    variantUnit = "kg",
                    variantSize = "1",
                    isDailyEssential = false,
                    isFeatured = true,
                    isRecentlyOrdered = true
                ),
                ProductEntity(
                    nameEn = "Spicy Green Chillies (Marcha)",
                    nameGu = "તીખા લીલા મરચા",
                    categoryEn = "Vegetables & Fruits",
                    categoryGu = "શાકભાજી અને ફળો",
                    price = 25.0,
                    discountPercent = 5,
                    stock = 40,
                    variantUnit = "gram",
                    variantSize = "250",
                    isDailyEssential = true
                ),

                // Milk & Dairy
                ProductEntity(
                    nameEn = "Amul Gold Milk Premium",
                    nameGu = "અમૂલ ગોલ્ડ પ્રીમિયમ દૂધ",
                    categoryEn = "Milk & Dairy",
                    categoryGu = "દૂધ અને ડેરી",
                    price = 45.0, // Per 500ml
                    discountPercent = 0,
                    stock = 200,
                    variantUnit = "packet",
                    variantSize = "1",
                    isDailyEssential = true,
                    isRecentlyOrdered = true
                ),
                ProductEntity(
                    nameEn = "Amul Salted Butter",
                    nameGu = "અમૂલ બટર (નમકીન)",
                    categoryEn = "Milk & Dairy",
                    categoryGu = "દૂધ અને ડેરી",
                    price = 58.0,
                    discountPercent = 5,
                    stock = 85,
                    variantUnit = "gram",
                    variantSize = "100",
                    isDailyEssential = true,
                    isFeatured = true
                ),
                ProductEntity(
                    nameEn = "Fresh Malai Paneer",
                    nameGu = "તાજું મલાઈ પનીર",
                    categoryEn = "Milk & Dairy",
                    categoryGu = "દૂધ અને ડેરી",
                    price = 90.0,
                    discountPercent = 10,
                    stock = 30,
                    variantUnit = "gram",
                    variantSize = "200",
                    isDailyEssential = false,
                    isFeatured = true
                ),

                // Groceries (Atta, Sugar, Oil, Tea, Salt)
                ProductEntity(
                    nameEn = "Aashirvaad Shudh Chakki Atta",
                    nameGu = "આશીર્વાદ શુદ્ધ ચક્કી લોટ",
                    categoryEn = "Atta, Rice & Groceries",
                    categoryGu = "કરિયાણું અને લોટ",
                    price = 260.0,
                    discountPercent = 12,
                    stock = 120,
                    variantUnit = "kg",
                    variantSize = "5",
                    isDailyEssential = true,
                    isFeatured = true
                ),
                ProductEntity(
                    nameEn = "Fortune Soya Health Oil",
                    nameGu = "ફોર્ચ્યુન સોયાબીન તેલ",
                    categoryEn = "Atta, Rice & Groceries",
                    categoryGu = "કરિયાણું અને લોટ",
                    price = 145.0,
                    discountPercent = 15,
                    stock = 90,
                    variantUnit = "liter",
                    variantSize = "1",
                    isDailyEssential = true
                ),
                ProductEntity(
                    nameEn = "Madhur Pure Sugar",
                    nameGu = "મધુર શુદ્ધ ખાંડ",
                    categoryEn = "Atta, Rice & Groceries",
                    categoryGu = "કરિયાણું અને લોટ",
                    price = 50.0,
                    discountPercent = 0,
                    stock = 110,
                    variantUnit = "kg",
                    variantSize = "1",
                    isDailyEssential = true
                ),
                ProductEntity(
                    nameEn = "Wagh Bakri Premium Tea",
                    nameGu = "વાઘ બકરી પ્રીમિયમ ચા",
                    categoryEn = "Atta, Rice & Groceries",
                    categoryGu = "કરિયાણું અને લોટ",
                    price = 125.0,
                    discountPercent = 8,
                    stock = 75,
                    variantUnit = "packet",
                    variantSize = "1",
                    isDailyEssential = true,
                    isFeatured = true
                ),
                ProductEntity(
                    nameEn = "Tata Salt Iodized",
                    nameGu = "ટાટા આયોડાઇઝ્ડ મીઠું",
                    categoryEn = "Atta, Rice & Groceries",
                    categoryGu = "કરિયાણું અને લોટ",
                    price = 28.0,
                    discountPercent = 0,
                    stock = 140,
                    variantUnit = "kg",
                    variantSize = "1",
                    isDailyEssential = true
                ),

                // Snacks, Beverages & Sweets
                ProductEntity(
                    nameEn = "Maggi 2-Min Masala Noodles",
                    nameGu = "મેગી ઈન્સ્ટન્ટ નૂડલ્સ",
                    categoryEn = "Snacks, Drinks & Sweets",
                    categoryGu = "નાસ્તો અને સ્વીટ્સ",
                    price = 14.0,
                    discountPercent = 5,
                    stock = 4, // Low stock on purpose
                    variantUnit = "pieces",
                    variantSize = "1",
                    isDailyEssential = false,
                    isRecentlyOrdered = true
                ),
                ProductEntity(
                    nameEn = "Britannia Marie Gold",
                    nameGu = "બ્રિટાનિયા મેરી ગોલ્ડ બિસ્કિટ",
                    categoryEn = "Snacks, Drinks & Sweets",
                    categoryGu = "નાસ્તો અને સ્વીટ્સ",
                    price = 30.0,
                    discountPercent = 10,
                    stock = 95,
                    variantUnit = "packet",
                    variantSize = "1",
                    isDailyEssential = true
                ),
                ProductEntity(
                    nameEn = "Coca Cola Original Sweet",
                    nameGu = "કોકા કોલા કોલ્ડ ડ્રિંક",
                    categoryEn = "Snacks, Drinks & Sweets",
                    categoryGu = "નાસ્તો અને સ્વીટ્સ",
                    price = 40.0,
                    discountPercent = 0,
                    stock = 120,
                    variantUnit = "liter",
                    variantSize = "1",
                    isRecentlyOrdered = false
                ),
                ProductEntity(
                    nameEn = "Kaju Katli Sweets Premium",
                    nameGu = "શુદ્ધ કાજુ કતરી મીઠાઈ",
                    categoryEn = "Snacks, Drinks & Sweets",
                    categoryGu = "નાસ્તો અને સ્વીટ્સ",
                    price = 450.0,
                    discountPercent = 15,
                    stock = 25,
                    variantUnit = "gram",
                    variantSize = "500",
                    isFeatured = true
                )
            )
            productDao.insertProducts(defaults)
        }
    }
}
