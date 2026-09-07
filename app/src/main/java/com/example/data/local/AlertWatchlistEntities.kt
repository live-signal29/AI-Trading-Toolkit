package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "price_alerts")
data class PriceAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val targetPrice: Double,
    val condition: String, // "ABOVE", "BELOW", "PERCENT_MOVE"
    val percentage: Double? = null,
    val initialPrice: Double = 0.0,
    val isActive: Boolean = true,
    val isTriggered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface PriceAlertDao {
    @Query("SELECT * FROM price_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<PriceAlert>>

    @Query("SELECT * FROM price_alerts WHERE isActive = 1 AND isTriggered = 0")
    suspend fun getActiveAlerts(): List<PriceAlert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlert): Long

    @Update
    suspend fun updateAlert(alert: PriceAlert)

    @Delete
    suspend fun deleteAlert(alert: PriceAlert)

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Entity(tableName = "watchlist_items")
data class WatchlistItem(
    @PrimaryKey
    val symbol: String,
    val assetType: String,
    val orderIndex: Int = 0,
    val isFavorite: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_items ORDER BY orderIndex ASC, addedAt DESC")
    fun getAllWatchlist(): Flow<List<WatchlistItem>>

    @Query("SELECT * FROM watchlist_items WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<WatchlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WatchlistItem)

    @Delete
    suspend fun deleteItem(item: WatchlistItem)

    @Query("DELETE FROM watchlist_items WHERE symbol = :symbol")
    suspend fun deleteBySymbol(symbol: String)

    @Query("UPDATE watchlist_items SET isFavorite = :isFavorite WHERE symbol = :symbol")
    suspend fun setFavorite(symbol: String, isFavorite: Boolean)
}

@Entity(tableName = "custom_market_assets")
data class CustomMarketAsset(
    @PrimaryKey
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String,
    val price: Double,
    val change24h: Double,
    val high24h: Double = 0.0,
    val low24h: Double = 0.0,
    val volume24h: Double = 0.0,
    val assetType: String,
    val source: String = "User Added",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface CustomMarketAssetDao {
    @Query("SELECT * FROM custom_market_assets ORDER BY updatedAt DESC")
    fun getAllCustomAssets(): Flow<List<CustomMarketAsset>>

    @Query("SELECT * FROM custom_market_assets")
    suspend fun getAllCustomAssetsList(): List<CustomMarketAsset>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(asset: CustomMarketAsset)

    @Delete
    suspend fun delete(asset: CustomMarketAsset)

    @Query("DELETE FROM custom_market_assets WHERE symbol = :symbol")
    suspend fun deleteBySymbol(symbol: String)
}

