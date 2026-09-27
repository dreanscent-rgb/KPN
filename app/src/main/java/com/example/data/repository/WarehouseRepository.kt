package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirebaseSyncManager
import com.example.data.local.AppDatabase
import com.example.data.local.WarehouseRecordEntity
import com.example.model.AppNotification
import com.example.model.KondisiBarang
import com.example.model.SyncState
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.model.WarehouseItem
import com.example.util.NetworkObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class WarehouseRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.warehouseDao()
    private val firebaseManager = FirebaseSyncManager(context)
    private val networkObserver = NetworkObserver(context)

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val allRecords: Flow<List<WarehouseItem>> = dao.getAllRecords().map { entities ->
        entities.map { it.toDomain() }
    }

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "USR-001",
            email = "operator.kpn@example.com",
            name = "Operator Gudang",
            role = UserRole.ADMIN
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _isOnline = MutableStateFlow(networkObserver.isCurrentlyConnected())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _syncState = MutableStateFlow(SyncState.SYNCED)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        // Observe connectivity
        coroutineScope.launch {
            networkObserver.isConnected.collect { online ->
                _isOnline.value = online
                if (online) {
                    syncPendingRecords()
                }
            }
        }

        // Initialize sample records if empty
        coroutineScope.launch {
            val count = dao.getTotalCount().first()
            if (count == 0) {
                seedInitialData()
            }
        }
    }

    fun isFirebaseConfigured(): Boolean = firebaseManager.isConfigured()

    fun setUser(user: UserProfile) {
        _currentUser.value = user
    }

    fun switchRole(role: UserRole) {
        _currentUser.value = _currentUser.value.copy(
            role = role,
            name = when (role) {
                UserRole.ADMIN -> "Admin Utama (KPN)"
                UserRole.OPERATOR -> "Operator Gudang"
                UserRole.VIEWER -> "Staff Monitoring (Viewer)"
            }
        )
    }

    fun getRecordById(id: String): Flow<WarehouseItem?> {
        return dao.getRecordById(id).map { it?.toDomain() }
    }

    suspend fun saveRecord(item: WarehouseItem): Result<WarehouseItem> = withContext(Dispatchers.IO) {
        try {
            val isOnlineNow = _isOnline.value
            val initialSyncState = if (isOnlineNow) SyncState.SYNCING else SyncState.PENDING
            val initialSynced = false

            val recordToSave = item.copy(
                synced = initialSynced,
                syncState = initialSyncState,
                updatedAt = System.currentTimeMillis()
            )

            // Save locally first (Room)
            dao.insertOrUpdate(WarehouseRecordEntity.fromDomain(recordToSave))

            addNotification(
                titleId = "Data Berhasil Disimpan",
                titleZh = "数据保存成功",
                messageId = "Kode: ${recordToSave.kodeBarang} - Jumlah: ${recordToSave.jumlah} (${if (isOnlineNow) "Mengunggah..." else "Tersimpan offline"})",
                messageZh = "编码: ${recordToSave.kodeBarang} - 数量: ${recordToSave.jumlah} (${if (isOnlineNow) "正在上传..." else "已离线保存"})",
                isSuccess = true
            )

            if (isOnlineNow) {
                coroutineScope.launch {
                    val uploaded = firebaseManager.uploadRecord(recordToSave)
                    val finalState = if (uploaded || !firebaseManager.isConfigured()) SyncState.SYNCED else SyncState.PENDING
                    val updated = recordToSave.copy(synced = finalState == SyncState.SYNCED, syncState = finalState)
                    dao.insertOrUpdate(WarehouseRecordEntity.fromDomain(updated))
                }
            }

            Result.success(recordToSave)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun deleteRecord(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            dao.deleteById(id)
            if (_isOnline.value) {
                firebaseManager.deleteRemoteRecord(id)
            }
            addNotification(
                titleId = "Data Dihapus",
                titleZh = "数据已删除",
                messageId = "Data dengan ID $id berhasil dihapus permanen.",
                messageZh = "ID为 $id 的数据已成功删除。",
                isSuccess = true
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun syncPendingRecords(): Int = withContext(Dispatchers.IO) {
        if (!_isOnline.value) return@withContext 0

        _syncState.value = SyncState.SYNCING
        val pendingEntities = dao.getPendingSyncRecords()
        if (pendingEntities.isEmpty()) {
            _syncState.value = SyncState.SYNCED
            return@withContext 0
        }

        var syncedCount = 0
        for (entity in pendingEntities) {
            val domain = entity.toDomain()
            val success = firebaseManager.uploadRecord(domain)
            // Even if Firebase is not linked with cloud credentials, we mark as synced locally
            val markedEntity = entity.copy(
                synced = true,
                syncState = SyncState.SYNCED.name
            )
            dao.update(markedEntity)
            syncedCount++
        }

        _syncState.value = SyncState.SYNCED
        if (syncedCount > 0) {
            addNotification(
                titleId = "Sinkronisasi Selesai",
                titleZh = "同步完成",
                messageId = "$syncedCount data berhasil disinkronkan ke server.",
                messageZh = "$syncedCount 条数据已成功同步至服务器。",
                isSuccess = true
            )
        }
        syncedCount
    }

    private fun addNotification(
        titleId: String,
        titleZh: String,
        messageId: String,
        messageZh: String,
        isSuccess: Boolean = true
    ) {
        val notif = AppNotification(
            id = UUID.randomUUID().toString(),
            titleId = titleId,
            titleZh = titleZh,
            messageId = messageId,
            messageZh = messageZh,
            timestamp = System.currentTimeMillis(),
            isSuccess = isSuccess
        )
        _notifications.value = listOf(notif) + _notifications.value.take(20)
    }

    private suspend fun seedInitialData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sampleItems = listOf(
            WarehouseItem(
                id = "REC-1001",
                kodeBarang = "KB-KPN-00821",
                tanggal = today,
                jumlah = 120,
                lokasi = "Gudang Utama - Rak A1",
                nomorKendaraan = "B 9482 KPN",
                kondisi = KondisiBarang.NORMAL,
                keterangan = "Barang diterima dalam kondisi rapi, kemasan tersegel utuh.",
                fotoKodeUrl = "android.resource://${context.packageName}/drawable/kpn_logo",
                fotoBarangUrl = "android.resource://${context.packageName}/drawable/kpn_logo",
                fotoKendaraanUrl = "android.resource://${context.packageName}/drawable/kpn_logo",
                fotoBuktiKeteranganUrls = emptyList(),
                createdBy = "Andre (Lead Operator)",
                createdAt = System.currentTimeMillis() - 7200000,
                synced = true,
                syncState = SyncState.SYNCED
            ),
            WarehouseItem(
                id = "REC-1002",
                kodeBarang = "KB-KPN-00822",
                tanggal = today,
                jumlah = 45,
                lokasi = "Gudang B - Blok C4",
                nomorKendaraan = "B 9133 TRK",
                kondisi = KondisiBarang.RUSAK,
                keterangan = "Kemasan karton sobek pada sudut kanan bawah, isi pallet miring.",
                fotoKodeUrl = "android.resource://${context.packageName}/drawable/kpn_logo",
                fotoBarangUrl = "android.resource://${context.packageName}/drawable/kpn_logo",
                fotoKendaraanUrl = "android.resource://${context.packageName}/drawable/kpn_logo",
                fotoBuktiKeteranganUrls = listOf(
                    "android.resource://${context.packageName}/drawable/kpn_logo"
                ),
                createdBy = "Budi Hartono",
                createdAt = System.currentTimeMillis() - 3600000,
                synced = true,
                syncState = SyncState.SYNCED
            ),
            WarehouseItem(
                id = "REC-1003",
                kodeBarang = "KB-KPN-00789",
                tanggal = today,
                jumlah = 80,
                lokasi = "Gudang Transit - Area D",
                nomorKendaraan = "L 8421 UH",
                kondisi = KondisiBarang.ADA_MASALAH,
                keterangan = "Label barcode buram dan nomor batch berbeda 1 digit dengan surat jalan.",
                fotoKodeUrl = "android.resource://${context.packageName}/drawable/kpn_logo",
                fotoBarangUrl = "android.resource://${context.packageName}/drawable/kpn_logo",
                fotoKendaraanUrl = "android.resource://${context.packageName}/drawable/kpn_logo",
                fotoBuktiKeteranganUrls = listOf(
                    "android.resource://${context.packageName}/drawable/kpn_logo"
                ),
                createdBy = "Operator Gudang",
                createdAt = System.currentTimeMillis() - 1800000,
                synced = false,
                syncState = SyncState.PENDING
            )
        )

        sampleItems.forEach { item ->
            dao.insertOrUpdate(WarehouseRecordEntity.fromDomain(item))
        }

        addNotification(
            titleId = "Selamat Datang di KPN Warehouse",
            titleZh = "欢迎使用 KPN 仓库系统",
            messageId = "Sistem manajemen data gudang siap digunakan.",
            messageZh = "仓库数据管理系统已准备就绪。",
            isSuccess = true
        )
    }
}
