package com.example.model

enum class KondisiBarang(
    val titleId: String,
    val titleZh: String,
    val colorHex: Long,
    val bgHex: Long
) {
    NORMAL("Normal", "正常", 0xFF10B981, 0xFFECFDF5),
    ADA_MASALAH("Ada Masalah", "存在问题", 0xFFF59E0B, 0xFFFFFBEB),
    RUSAK("Rusak", "损坏", 0xFFEF4444, 0xFFFEF2F2);

    companion object {
        fun fromString(value: String): KondisiBarang {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NORMAL
        }
    }
}

enum class UserRole(
    val id: String,
    val labelId: String,
    val labelZh: String,
    val canInput: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canExport: Boolean
) {
    ADMIN("ADMIN", "Administrator", "管理员", canInput = true, canEdit = true, canDelete = true, canExport = true),
    OPERATOR("OPERATOR", "Operator Gudang", "操作员", canInput = true, canEdit = true, canDelete = false, canExport = true),
    VIEWER("VIEWER", "Viewer", "查阅员", canInput = false, canEdit = false, canDelete = false, canExport = false);

    companion object {
        fun fromString(role: String): UserRole {
            return entries.firstOrNull { it.name.equals(role, ignoreCase = true) } ?: OPERATOR
        }
    }
}

enum class SyncState(val labelId: String, val labelZh: String) {
    SYNCED("Tersinkron", "已同步"),
    PENDING("Menunggu Sinkronisasi", "待同步"),
    SYNCING("Sinkronisasi...", "同步中..."),
    FAILED("Gagal Sinkronisasi", "同步失败")
}

data class WarehouseItem(
    val id: String,
    val kodeBarang: String,
    val tanggal: String,
    val jumlah: Int,
    val lokasi: String,
    val nomorKendaraan: String,
    val kondisi: KondisiBarang,
    val keterangan: String = "",
    val fotoKodeUrl: String = "",
    val fotoBarangUrl: String = "",
    val fotoKendaraanUrl: String = "",
    val fotoBuktiKeteranganUrls: List<String> = emptyList(),
    val createdBy: String = "Operator Gudang",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    val syncState: SyncState = SyncState.PENDING,
    val remoteId: String? = null
)

data class UserProfile(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole
)

data class AppNotification(
    val id: String,
    val titleId: String,
    val titleZh: String,
    val messageId: String,
    val messageZh: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true
)
