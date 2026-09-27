package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.WarehouseRepository
import com.example.model.KondisiBarang
import com.example.model.SyncState
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.model.WarehouseItem
import com.example.util.ExcelExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class FilterCategory(val labelId: String, val labelZh: String) {
    ALL("Semua", "全部"),
    NORMAL("Normal", "正常"),
    RUSAK("Rusak", "损坏"),
    ADA_MASALAH("Ada Masalah", "存在问题"),
    TODAY("Hari Ini", "今天")
}

data class WarehouseStats(
    val totalCount: Int = 0,
    val todayCount: Int = 0,
    val normalCount: Int = 0,
    val bermasalahCount: Int = 0
)

sealed interface Screen {
    data object Splash : Screen
    data object Login : Screen
    data object Dashboard : Screen
    data object DataList : Screen
    data object InputForm : Screen
    data class ConfirmData(val item: WarehouseItem) : Screen
    data class Detail(val recordId: String) : Screen
    data class EditForm(val recordId: String) : Screen
    data object Export : Screen
    data object Notifications : Screen
    data object Profile : Screen
}

class WarehouseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WarehouseRepository(application)

    val allRecords: StateFlow<List<WarehouseItem>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserProfile> = repository.currentUser
    val isOnline: StateFlow<Boolean> = repository.isOnline
    val syncState: StateFlow<SyncState> = repository.syncState
    val notifications = repository.notifications

    // Navigation state with simple backstack
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenStack = mutableListOf<Screen>()

    // Search and filters
    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow(FilterCategory.ALL)

    // Filtered records
    val filteredRecords: StateFlow<List<WarehouseItem>> = combine(
        allRecords,
        searchQuery,
        selectedFilter
    ) { records, query, filter ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val q = query.trim().lowercase(Locale.getDefault())

        records.filter { item ->
            // Filter by category
            val matchesFilter = when (filter) {
                FilterCategory.ALL -> true
                FilterCategory.NORMAL -> item.kondisi == KondisiBarang.NORMAL
                FilterCategory.RUSAK -> item.kondisi == KondisiBarang.RUSAK
                FilterCategory.ADA_MASALAH -> item.kondisi == KondisiBarang.ADA_MASALAH
                FilterCategory.TODAY -> item.tanggal == todayStr
            }

            // Filter by search query
            val matchesQuery = if (q.isEmpty()) true else {
                item.kodeBarang.lowercase(Locale.getDefault()).contains(q) ||
                item.lokasi.lowercase(Locale.getDefault()).contains(q) ||
                item.nomorKendaraan.lowercase(Locale.getDefault()).contains(q) ||
                item.keterangan.lowercase(Locale.getDefault()).contains(q)
            }

            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics
    val stats: StateFlow<WarehouseStats> = allRecords.combine(MutableStateFlow(Unit)) { records, _ ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        WarehouseStats(
            totalCount = records.size,
            todayCount = records.count { it.tanggal == todayStr },
            normalCount = records.count { it.kondisi == KondisiBarang.NORMAL },
            bermasalahCount = records.count {
                it.kondisi == KondisiBarang.RUSAK || it.kondisi == KondisiBarang.ADA_MASALAH
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WarehouseStats())

    // Form Draft State for Input/Edit
    val formKodeBarang = MutableStateFlow("")
    val formTanggal = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val formJumlah = MutableStateFlow("")
    val formLokasi = MutableStateFlow("")
    val formNomorKendaraan = MutableStateFlow("")
    val formKondisi = MutableStateFlow(KondisiBarang.NORMAL)
    val formKeterangan = MutableStateFlow("")
    val formFotoKode = MutableStateFlow("")
    val formFotoBarang = MutableStateFlow("")
    val formFotoKendaraan = MutableStateFlow("")
    val formFotoBuktiList = MutableStateFlow<List<String>>(emptyList())
    val formEditingId = MutableStateFlow<String?>(null)

    // Export status
    val isExporting = MutableStateFlow(false)
    val lastExportedFile = MutableStateFlow<File?>(null)

    // Fullscreen photo preview state
    val fullscreenPhotoUrl = MutableStateFlow<String?>(null)

    fun navigateTo(screen: Screen, clearStack: Boolean = false) {
        if (clearStack) {
            screenStack.clear()
        } else {
            screenStack.add(_currentScreen.value)
        }
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (fullscreenPhotoUrl.value != null) {
            fullscreenPhotoUrl.value = null
            return true
        }

        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.size - 1)
            return true
        }
        return false
    }

    fun openFullscreenPhoto(url: String) {
        fullscreenPhotoUrl.value = url
    }

    fun closeFullscreenPhoto() {
        fullscreenPhotoUrl.value = null
    }

    fun startNewInputForm() {
        formEditingId.value = null
        val nextNum = (1000..9999).random()
        formKodeBarang.value = "KB-KPN-$nextNum"
        formTanggal.value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        formJumlah.value = ""
        formLokasi.value = ""
        formNomorKendaraan.value = ""
        formKondisi.value = KondisiBarang.NORMAL
        formKeterangan.value = ""
        formFotoKode.value = ""
        formFotoBarang.value = ""
        formFotoKendaraan.value = ""
        formFotoBuktiList.value = emptyList()
        navigateTo(Screen.InputForm)
    }

    fun startEditForm(item: WarehouseItem) {
        formEditingId.value = item.id
        formKodeBarang.value = item.kodeBarang
        formTanggal.value = item.tanggal
        formJumlah.value = item.jumlah.toString()
        formLokasi.value = item.lokasi
        formNomorKendaraan.value = item.nomorKendaraan
        formKondisi.value = item.kondisi
        formKeterangan.value = item.keterangan
        formFotoKode.value = item.fotoKodeUrl
        formFotoBarang.value = item.fotoBarangUrl
        formFotoKendaraan.value = item.fotoKendaraanUrl
        formFotoBuktiList.value = item.fotoBuktiKeteranganUrls
        navigateTo(Screen.InputForm)
    }

    fun prepareConfirmation(): WarehouseItem? {
        val jumlahInt = formJumlah.value.toIntOrNull() ?: 0
        if (formKodeBarang.value.isBlank() || formLokasi.value.isBlank() ||
            formNomorKendaraan.value.isBlank() || jumlahInt <= 0
        ) {
            return null
        }

        val id = formEditingId.value ?: "REC-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
        return WarehouseItem(
            id = id,
            kodeBarang = formKodeBarang.value.trim(),
            tanggal = formTanggal.value,
            jumlah = jumlahInt,
            lokasi = formLokasi.value.trim(),
            nomorKendaraan = formNomorKendaraan.value.trim().uppercase(),
            kondisi = formKondisi.value,
            keterangan = formKeterangan.value.trim(),
            fotoKodeUrl = formFotoKode.value,
            fotoBarangUrl = formFotoBarang.value,
            fotoKendaraanUrl = formFotoKendaraan.value,
            fotoBuktiKeteranganUrls = formFotoBuktiList.value,
            createdBy = currentUser.value.name
        )
    }

    fun saveDraftRecord(item: WarehouseItem, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.saveRecord(item)
            onComplete()
            navigateTo(Screen.Dashboard, clearStack = true)
        }
    }

    fun deleteRecord(id: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteRecord(id)
            onComplete()
            navigateTo(Screen.DataList, clearStack = true)
        }
    }

    fun syncNow(onResult: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val count = repository.syncPendingRecords()
            onResult(count)
        }
    }

    fun switchRole(role: UserRole) {
        repository.switchRole(role)
    }

    fun setUser(user: UserProfile) {
        repository.setUser(user)
    }

    fun loginWithGoogle(email: String, name: String) {
        val googleUser = UserProfile(
            id = "GGL-${System.currentTimeMillis() % 100000}",
            email = email,
            name = name,
            role = UserRole.ADMIN
        )
        repository.setUser(googleUser)
    }

    fun logoutUser() {
        val defaultUser = UserProfile(
            id = "USR-001",
            email = "operator.kpn@example.com",
            name = "Operator Gudang",
            role = UserRole.OPERATOR
        )
        repository.setUser(defaultUser)
    }

    fun isFirebaseConfigured(): Boolean = repository.isFirebaseConfigured()

    fun exportExcel(
        filter: FilterCategory,
        onReady: (File) -> Unit
    ) {
        viewModelScope.launch {
            isExporting.value = true
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val recordsToExport = allRecords.value.filter { item ->
                when (filter) {
                    FilterCategory.ALL -> true
                    FilterCategory.NORMAL -> item.kondisi == KondisiBarang.NORMAL
                    FilterCategory.RUSAK -> item.kondisi == KondisiBarang.RUSAK
                    FilterCategory.ADA_MASALAH -> item.kondisi == KondisiBarang.ADA_MASALAH
                    FilterCategory.TODAY -> item.tanggal == todayStr
                }
            }

            val file = ExcelExporter.exportToExcelWithEmbeddedImages(
                context = getApplication(),
                items = recordsToExport
            )
            lastExportedFile.value = file
            isExporting.value = false
            onReady(file)
        }
    }

    fun shareLastExportedFile() {
        val file = lastExportedFile.value ?: return
        ExcelExporter.shareExportedFile(getApplication(), file)
    }
}
