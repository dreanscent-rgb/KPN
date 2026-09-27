package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.model.WarehouseItem
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseSyncManager(private val context: Context) {

    init {
        ensureFirebaseInitialized()
    }

    fun ensureFirebaseInitialized() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:159718070420:android:4cbde084fc6a52c3753c81")
                    .setProjectId("gudang-kpn2")
                    .setApiKey("AIzaSyB3v-kpnWarehouseDefaultClientKey")
                    .setGcmSenderId("159718070420")
                    .setStorageBucket("gudang-kpn2.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
        } catch (e: Exception) {
            Log.w("FirebaseSync", "Firebase init: ${e.message}")
        }
    }

    private val isFirebaseAvailable: Boolean
        get() {
            ensureFirebaseInitialized()
            return try {
                FirebaseApp.getApps(context).isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

    private val firestore: FirebaseFirestore?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.w("FirebaseSync", "Firestore not initialized: ${e.message}")
                null
            }
        } else null

    private val auth: FirebaseAuth?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                Log.w("FirebaseSync", "FirebaseAuth not initialized: ${e.message}")
                null
            }
        } else null

    suspend fun uploadRecord(item: WarehouseItem): Boolean {
        val db = firestore ?: return false
        return try {
            val recordMap = hashMapOf(
                "recordId" to item.id,
                "kodeBarang" to item.kodeBarang,
                "tanggal" to item.tanggal,
                "jumlah" to item.jumlah,
                "lokasi" to item.lokasi,
                "nomorKendaraan" to item.nomorKendaraan,
                "kondisi" to item.kondisi.name,
                "keterangan" to item.keterangan,
                "fotoKodeUrl" to item.fotoKodeUrl,
                "fotoBarangUrl" to item.fotoBarangUrl,
                "fotoKendaraanUrl" to item.fotoKendaraanUrl,
                "fotoBuktiKeteranganUrls" to item.fotoBuktiKeteranganUrls,
                "createdBy" to item.createdBy,
                "createdAt" to item.createdAt,
                "updatedAt" to item.updatedAt,
                "synced" to true,
                "status" to "ACTIVE"
            )

            db.collection("warehouse_records")
                .document(item.id)
                .set(recordMap, SetOptions.merge())
                .await()

            true
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error uploading record: ${e.message}")
            false
        }
    }

    suspend fun deleteRemoteRecord(id: String): Boolean {
        val db = firestore ?: return true
        return try {
            db.collection("warehouse_records").document(id).delete().await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting remote record: ${e.message}")
            false
        }
    }

    fun isConfigured(): Boolean = isFirebaseAvailable
}
