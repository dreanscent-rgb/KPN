package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.KondisiBarang
import com.example.model.WarehouseItem
import com.example.ui.components.PhotoSlotCard
import com.example.ui.components.PhotoSourcePickerBottomSheet
import com.example.ui.theme.BorderLight
import com.example.ui.theme.KpnBlue
import com.example.ui.theme.KpnBlueDark
import com.example.ui.theme.KpnBlueLight
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusNormal
import com.example.ui.theme.StatusNormalBg
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ImageUtils
import com.example.viewmodel.WarehouseViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TargetPhotoSlot {
    KODE,
    BARANG,
    KENDARAAN,
    BUKTI_TAMBAHAN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputFormScreen(
    viewModel: WarehouseViewModel,
    onNavigateBack: () -> Unit,
    onProceedConfirmation: (WarehouseItem) -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isEditing = viewModel.formEditingId.collectAsState().value != null

    val kodeBarang by viewModel.formKodeBarang.collectAsState()
    val tanggal by viewModel.formTanggal.collectAsState()
    val jumlah by viewModel.formJumlah.collectAsState()
    val lokasi by viewModel.formLokasi.collectAsState()
    val nomorKendaraan by viewModel.formNomorKendaraan.collectAsState()
    val kondisi by viewModel.formKondisi.collectAsState()
    val keterangan by viewModel.formKeterangan.collectAsState()

    val fotoKode by viewModel.formFotoKode.collectAsState()
    val fotoBarang by viewModel.formFotoBarang.collectAsState()
    val fotoKendaraan by viewModel.formFotoKendaraan.collectAsState()
    val fotoBuktiList by viewModel.formFotoBuktiList.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var activeSlot by remember { mutableStateOf<TargetPhotoSlot?>(null) }
    var showPhotoPickerSheet by remember { mutableStateOf(false) }
    var currentCameraUri by remember { mutableStateOf<Uri?>(null) }

    var validationError by remember { mutableStateOf<String?>(null) }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentCameraUri != null) {
            scope.launch {
                val savedPath = ImageUtils.processAndSaveImage(context, currentCameraUri!!)
                when (activeSlot) {
                    TargetPhotoSlot.KODE -> viewModel.formFotoKode.value = savedPath
                    TargetPhotoSlot.BARANG -> viewModel.formFotoBarang.value = savedPath
                    TargetPhotoSlot.KENDARAAN -> viewModel.formFotoKendaraan.value = savedPath
                    TargetPhotoSlot.BUKTI_TAMBAHAN -> {
                        if (fotoBuktiList.size < 5) {
                            viewModel.formFotoBuktiList.value = fotoBuktiList + savedPath
                        }
                    }
                    null -> {}
                }
            }
        }
    }

    // Permission launcher for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = ImageUtils.createTempCameraUri(context)
            currentCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo picker launcher (Gallery)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val savedPath = ImageUtils.processAndSaveImage(context, uri)
                when (activeSlot) {
                    TargetPhotoSlot.KODE -> viewModel.formFotoKode.value = savedPath
                    TargetPhotoSlot.BARANG -> viewModel.formFotoBarang.value = savedPath
                    TargetPhotoSlot.KENDARAAN -> viewModel.formFotoKendaraan.value = savedPath
                    TargetPhotoSlot.BUKTI_TAMBAHAN -> {
                        if (fotoBuktiList.size < 5) {
                            viewModel.formFotoBuktiList.value = fotoBuktiList + savedPath
                        }
                    }
                    null -> {}
                }
            }
        }
    }

    fun openSlotPicker(slot: TargetPhotoSlot) {
        activeSlot = slot
        showPhotoPickerSheet = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEditing) "Edit Data Gudang" else "Input Data Gudang",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isEditing) "编辑仓库数据" else "录入仓库数据",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (validationError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusDangerBg)
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = validationError ?: "",
                                    fontSize = 12.sp,
                                    color = StatusDanger,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Button(
                        onClick = {
                            val jumlahInt = jumlah.toIntOrNull() ?: 0
                            when {
                                kodeBarang.isBlank() -> validationError = "Kode Barang wajib diisi / 货物编码必填"
                                jumlahInt <= 0 -> validationError = "Jumlah harus berupa angka positif / 数量必须为正整数"
                                lokasi.isBlank() -> validationError = "Lokasi gudang wajib diisi / 仓库位置必填"
                                nomorKendaraan.isBlank() -> validationError = "Nomor kendaraan wajib diisi / 车牌号码必填"
                                (kondisi == KondisiBarang.RUSAK || kondisi == KondisiBarang.ADA_MASALAH) && keterangan.isBlank() -> {
                                    validationError = "Keterangan wajib diisi jika kondisi Rusak/Bermasalah / 异常状态必须填写备注"
                                }
                                (kondisi == KondisiBarang.RUSAK || kondisi == KondisiBarang.ADA_MASALAH) && fotoBuktiList.isEmpty() -> {
                                    validationError = "Minimal 1 foto bukti kerusakan/masalah wajib diunggah / 必须至少上传1张问题证明照片"
                                }
                                else -> {
                                    validationError = null
                                    val item = viewModel.prepareConfirmation()
                                    if (item != null) {
                                        onProceedConfirmation(item)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KpnBlue)
                    ) {
                        Text(
                            text = "Lanjut Konfirmasi / 确认数据",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues)
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 1: DATA BARANG
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SECTION 1: DATA BARANG (货物数据)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = KpnBlueDark
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Kode Barang
                        Text(
                            text = "Kode Barang / 货物编码 *",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = kodeBarang,
                            onValueChange = { viewModel.formKodeBarang.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Contoh: KB-KPN-1029", color = TextMuted) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = KpnBlue)
                            },
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tanggal & Jumlah in Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Tanggal
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tanggal / 日期 *",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, BorderLight, RoundedCornerShape(10.dp))
                                        .clickable { showDatePicker = true }
                                        .padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = KpnBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = tanggal, fontSize = 14.sp, color = TextPrimary)
                                    }
                                }
                            }

                            // Jumlah
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Jumlah / 数量 *",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = jumlah,
                                    onValueChange = {
                                        if (it.all { char -> char.isDigit() }) {
                                            viewModel.formJumlah.value = it
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("0", color = TextMuted) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Lokasi
                        Text(
                            text = "Lokasi Gudang / 仓库位置 *",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = lokasi,
                            onValueChange = { viewModel.formLokasi.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Contoh: Gudang Utama - Rak A3", color = TextMuted) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = KpnBlue)
                            },
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Nomor Kendaraan
                        Text(
                            text = "Nomor Kendaraan / 车牌号码 *",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = nomorKendaraan,
                            onValueChange = { viewModel.formNomorKendaraan.value = it.uppercase() },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Contoh: B 1234 KPN", color = TextMuted) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = KpnBlue)
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // SECTION 2: KONDISI BARANG
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SECTION 2: KONDISI BARANG (货物状态) *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = KpnBlueDark
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KondisiBarang.entries.forEach { itemKondisi ->
                                val isSelected = kondisi == itemKondisi
                                val (bgCol, borderCol, textCol) = when (itemKondisi) {
                                    KondisiBarang.NORMAL -> if (isSelected) Triple(StatusNormalBg, StatusNormal, StatusNormal) else Triple(Color.White, BorderLight, TextPrimary)
                                    KondisiBarang.ADA_MASALAH -> if (isSelected) Triple(StatusWarningBg, StatusWarning, StatusWarning) else Triple(Color.White, BorderLight, TextPrimary)
                                    KondisiBarang.RUSAK -> if (isSelected) Triple(StatusDangerBg, StatusDanger, StatusDanger) else Triple(Color.White, BorderLight, TextPrimary)
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.formKondisi.value = itemKondisi },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = bgCol),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = itemKondisi.titleId,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = textCol
                                        )
                                        Text(
                                            text = itemKondisi.titleZh,
                                            fontSize = 11.sp,
                                            color = if (isSelected) textCol else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: KETERANGAN
            item {
                val isBermasalah = kondisi == KondisiBarang.RUSAK || kondisi == KondisiBarang.ADA_MASALAH

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "SECTION 3: KETERANGAN (备注)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = KpnBlueDark
                            )
                            if (isBermasalah) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StatusDangerBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Wajib Diisi / 必填",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusDanger
                                    )
                                }
                            } else {
                                Text(text = "Opsional / 可选", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = keterangan,
                            onValueChange = { viewModel.formKeterangan.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            placeholder = {
                                Text(
                                    text = if (isBermasalah)
                                        "Contoh: Kemasan sobek di bagian sisi kanan, pallet retak..."
                                    else
                                        "Catatan tambahan barang (opsional)...",
                                    color = TextMuted
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isBermasalah) StatusDanger else KpnBlue,
                                unfocusedBorderColor = BorderLight
                            )
                        )
                    }
                }
            }

            // SECTION 4: FOTO DOKUMENTASI
            item {
                Text(
                    text = "SECTION 4: FOTO DOKUMENTASI (照片凭证)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = KpnBlueDark
                )
            }

            // Foto Kode Barang
            item {
                PhotoSlotCard(
                    title = "Foto Kode Barang",
                    subtitleZh = "货物编码照片",
                    photoUrl = fotoKode,
                    isRequired = true,
                    onAddOrReplace = { openSlotPicker(TargetPhotoSlot.KODE) },
                    onDelete = { viewModel.formFotoKode.value = "" },
                    onPreview = { viewModel.openFullscreenPhoto(fotoKode) }
                )
            }

            // Foto Barang
            item {
                PhotoSlotCard(
                    title = "Foto Barang",
                    subtitleZh = "货物照片",
                    photoUrl = fotoBarang,
                    isRequired = true,
                    onAddOrReplace = { openSlotPicker(TargetPhotoSlot.BARANG) },
                    onDelete = { viewModel.formFotoBarang.value = "" },
                    onPreview = { viewModel.openFullscreenPhoto(fotoBarang) }
                )
            }

            // Bukti Foto Kendaraan (Opsional)
            item {
                PhotoSlotCard(
                    title = "Bukti Foto Kendaraan",
                    subtitleZh = "车辆照片凭证",
                    photoUrl = fotoKendaraan,
                    isRequired = false,
                    onAddOrReplace = { openSlotPicker(TargetPhotoSlot.KENDARAAN) },
                    onDelete = { viewModel.formFotoKendaraan.value = "" },
                    onPreview = { viewModel.openFullscreenPhoto(fotoKendaraan) }
                )
            }

            // Foto Bukti Keterangan / Masalah (Conditional)
            item {
                val isBermasalah = kondisi == KondisiBarang.RUSAK || kondisi == KondisiBarang.ADA_MASALAH

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Foto Bukti Keterangan / Masalah",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "情况证明照片 (${fotoBuktiList.size}/5)",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            if (isBermasalah) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StatusDangerBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Wajib min. 1 foto",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusDanger
                                    )
                                }
                            } else {
                                Text(text = "Opsional", fontSize = 10.sp, color = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (fotoBuktiList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                    .clickable { openSlotPicker(TargetPhotoSlot.BUKTI_TAMBAHAN) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        tint = if (isBermasalah) StatusDanger else KpnBlue,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tambah Foto Bukti Masalah (Maks. 5)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isBermasalah) StatusDanger else KpnBlue
                                    )
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(fotoBuktiList) { idx, url ->
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { viewModel.openFullscreenPhoto(url) }
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(url)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Bukti ${idx + 1}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = StatusDanger.copy(alpha = 0.85f),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(24.dp)
                                                .clickable {
                                                    viewModel.formFotoBuktiList.value =
                                                        fotoBuktiList.filterIndexed { i, _ -> i != idx }
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Hapus",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (fotoBuktiList.size < 5) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFF1F5F9))
                                                .border(1.dp, BorderLight, RoundedCornerShape(10.dp))
                                                .clickable { openSlotPicker(TargetPhotoSlot.BUKTI_TAMBAHAN) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = KpnBlue,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text(
                                                    text = "Tambah",
                                                    fontSize = 11.sp,
                                                    color = KpnBlue
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Photo Source Picker Bottom Sheet
    PhotoSourcePickerBottomSheet(
        visible = showPhotoPickerSheet,
        onDismiss = { showPhotoPickerSheet = false },
        onTakePhoto = {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        },
        onPickGallery = {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    )

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                            viewModel.formTanggal.value = formatted
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih / 选择", color = KpnBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal / 取消", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
