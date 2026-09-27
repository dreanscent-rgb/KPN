package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.model.WarehouseItem
import com.example.ui.components.KondisiBadge
import com.example.ui.theme.BorderLight
import com.example.ui.theme.KpnBlue
import com.example.ui.theme.KpnBlueDark
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusNormal
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailDataScreen(
    recordId: String,
    viewModel: WarehouseViewModel,
    user: UserProfile,
    onNavigateBack: () -> Unit,
    onEditClick: (WarehouseItem) -> Unit
) {
    BackHandler { onNavigateBack() }

    val allRecords by viewModel.allRecords.collectAsState()
    val item = allRecords.firstOrNull { it.id == recordId }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (item == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Data tidak ditemukan / 数据未找到")
        }
        return
    }

    val primaryPhoto = item.fotoKodeUrl.ifBlank { item.fotoBarangUrl }.ifBlank { item.fotoKendaraanUrl }
    val formattedDateTime = remember(item.createdAt) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(item.createdAt))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Detail Data Gudang",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "数据详情 · ${item.kodeBarang}",
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
                actions = {
                    if (user.role.canEdit) {
                        IconButton(onClick = { onEditClick(item) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = KpnBlue
                            )
                        }
                    }
                    if (user.role.canDelete) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = StatusDanger
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (user.role.canEdit || user.role.canDelete) {
                Surface(
                    color = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (user.role.canDelete) {
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = StatusDanger
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Hapus / 删除", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (user.role.canEdit) {
                            Button(
                                onClick = { onEditClick(item) },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KpnBlue)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Data / 编辑数据", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Photo Card
            if (primaryPhoto.isNotBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { viewModel.openFullscreenPhoto(primaryPhoto) }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(primaryPhoto)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto Utama",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Perbesar / 全屏查看",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Core Info Card
            item {
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
                            Column {
                                Text(
                                    text = item.kodeBarang,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KpnBlueDark
                                )
                                Text(
                                    text = "ID: ${item.id}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            KondisiBadge(kondisi = item.kondisi)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        ConfirmDetailRow("Tanggal (日期)", item.tanggal)
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ConfirmDetailRow("Jumlah (数量)", "${item.jumlah} Unit / 件")
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ConfirmDetailRow("Lokasi (位置)", item.lokasi)
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ConfirmDetailRow("Nomor Kendaraan (车牌号码)", item.nomorKendaraan)
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ConfirmDetailRow("Petugas Input (录入员)", item.createdBy)
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ConfirmDetailRow("Waktu Input (时间)", formattedDateTime)
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ConfirmDetailRow(
                            "Status Sinkronisasi (同步状态)",
                            "${item.syncState.labelId} / ${item.syncState.labelZh}"
                        )
                    }
                }
            }

            // Keterangan Card
            if (item.keterangan.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Keterangan / 备注",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = KpnBlueDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.keterangan,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            // Photo Documentation Grid
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Dokumentasi Lengkap / 完整照片凭证",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = KpnBlueDark
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ConfirmPhotoThumb(
                                label = "Foto Kode",
                                url = item.fotoKodeUrl,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.openFullscreenPhoto(item.fotoKodeUrl) }
                            )
                            ConfirmPhotoThumb(
                                label = "Foto Barang",
                                url = item.fotoBarangUrl,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.openFullscreenPhoto(item.fotoBarangUrl) }
                            )
                            ConfirmPhotoThumb(
                                label = "Foto Kendaraan",
                                url = item.fotoKendaraanUrl,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.openFullscreenPhoto(item.fotoKendaraanUrl) }
                            )
                        }

                        if (item.fotoBuktiKeteranganUrls.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Foto Bukti Masalah (${item.fotoBuktiKeteranganUrls.size} foto):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(item.fotoBuktiKeteranganUrls) { url ->
                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.openFullscreenPhoto(url) }
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(url)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Bukti",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
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

    // Delete Confirmation Dialog (Admin Only)
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Hapus data ini? / 确定删除此数据？",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Data ${item.kodeBarang} yang sudah dihapus tidak dapat dikembalikan.\n已删除的数据将无法恢复。"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRecord(item.id) {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                ) {
                    Text("Hapus / 删除", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal / 取消", color = TextSecondary)
                }
            }
        )
    }
}
