package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.KondisiBarang
import com.example.model.SyncState
import com.example.model.WarehouseItem
import org.json.JSONArray

@Entity(tableName = "warehouse_records")
data class WarehouseRecordEntity(
    @PrimaryKey
    val id: String,
    val kodeBarang: String,
    val tanggal: String,
    val jumlah: Int,
    val lokasi: String,
    val nomorKendaraan: String,
    val kondisi: String,
    val keterangan: String,
    val fotoKodeUrl: String,
    val fotoBarangUrl: String,
    val fotoKendaraanUrl: String,
    val fotoBuktiKeteranganUrlsJson: String,
    val createdBy: String,
    val createdAt: Long,
    val updatedAt: Long,
    val synced: Boolean,
    val syncState: String,
    val remoteId: String? = null
) {
    fun toDomain(): WarehouseItem {
        val buktiList = mutableListOf<String>()
        if (fotoBuktiKeteranganUrlsJson.isNotBlank()) {
            try {
                val array = JSONArray(fotoBuktiKeteranganUrlsJson)
                for (i in 0 until array.length()) {
                    buktiList.add(array.getString(i))
                }
            } catch (_: Exception) {
                // Ignore parse errors
            }
        }

        return WarehouseItem(
            id = id,
            kodeBarang = kodeBarang,
            tanggal = tanggal,
            jumlah = jumlah,
            lokasi = lokasi,
            nomorKendaraan = nomorKendaraan,
            kondisi = KondisiBarang.fromString(kondisi),
            keterangan = keterangan,
            fotoKodeUrl = fotoKodeUrl,
            fotoBarangUrl = fotoBarangUrl,
            fotoKendaraanUrl = fotoKendaraanUrl,
            fotoBuktiKeteranganUrls = buktiList,
            createdBy = createdBy,
            createdAt = createdAt,
            updatedAt = updatedAt,
            synced = synced,
            syncState = try { SyncState.valueOf(syncState) } catch (_: Exception) { SyncState.PENDING },
            remoteId = remoteId
        )
    }

    companion object {
        fun fromDomain(item: WarehouseItem): WarehouseRecordEntity {
            val jsonArray = JSONArray()
            item.fotoBuktiKeteranganUrls.forEach { jsonArray.put(it) }

            return WarehouseRecordEntity(
                id = item.id,
                kodeBarang = item.kodeBarang,
                tanggal = item.tanggal,
                jumlah = item.jumlah,
                lokasi = item.lokasi,
                nomorKendaraan = item.nomorKendaraan,
                kondisi = item.kondisi.name,
                keterangan = item.keterangan,
                fotoKodeUrl = item.fotoKodeUrl,
                fotoBarangUrl = item.fotoBarangUrl,
                fotoKendaraanUrl = item.fotoKendaraanUrl,
                fotoBuktiKeteranganUrlsJson = jsonArray.toString(),
                createdBy = item.createdBy,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt,
                synced = item.synced,
                syncState = item.syncState.name,
                remoteId = item.remoteId
            )
        }
    }
}
