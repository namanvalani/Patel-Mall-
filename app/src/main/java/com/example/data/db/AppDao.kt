package com.example.data.db

import androidx.room.*
import com.example.data.model.ProductEntity
import com.example.data.model.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE categoryEn = :categoryEn ORDER BY id ASC")
    fun getProductsByCategory(categoryEn: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isDailyEssential = 1 LIMIT 15")
    fun getDailyEssentials(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isFeatured = 1 LIMIT 8")
    fun getFeaturedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock <= :lowLevel")
    fun getLowStockProducts(lowLevel: Int = 10): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Int)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE customerPhone = :phone ORDER BY timestamp DESC")
    fun getOrdersByCustomer(phone: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderStatus != 'Delivered' ORDER BY timestamp DESC")
    fun getActiveOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE deliveryBoyId = :deliveryBoyId ORDER BY timestamp DESC")
    fun getOrdersByDeliveryBoy(deliveryBoyId: Int): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET orderStatus = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("UPDATE orders SET currentLat = :lat, currentLng = :lng WHERE orderId = :orderId")
    suspend fun updateOrderLocation(orderId: String, lat: Double, lng: Double)
}
