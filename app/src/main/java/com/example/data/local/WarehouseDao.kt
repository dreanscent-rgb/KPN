package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouse_records ORDER BY createdAt DESC")
    fun getAllRecords(): Flow<List<WarehouseRecordEntity>>

    @Query("SELECT * FROM warehouse_records WHERE id = :id LIMIT 1")
    fun getRecordById(id: String): Flow<WarehouseRecordEntity?>

    @Query("SELECT * FROM warehouse_records WHERE id = :id LIMIT 1")
    suspend fun getRecordDirect(id: String): WarehouseRecordEntity?

    @Query("SELECT * FROM warehouse_records WHERE synced = 0 ORDER BY createdAt ASC")
    suspend fun getPendingSyncRecords(): List<WarehouseRecordEntity>

    @Query("""
        SELECT * FROM warehouse_records 
        WHERE kodeBarang LIKE '%' || :query || '%' 
           OR lokasi LIKE '%' || :query || '%' 
           OR nomorKendaraan LIKE '%' || :query || '%' 
           OR keterangan LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchRecords(query: String): Flow<List<WarehouseRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: WarehouseRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<WarehouseRecordEntity>)

    @Update
    suspend fun update(record: WarehouseRecordEntity)

    @Query("DELETE FROM warehouse_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM warehouse_records")
    fun getTotalCount(): Flow<Int>
}
