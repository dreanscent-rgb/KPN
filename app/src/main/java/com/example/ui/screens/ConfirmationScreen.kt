package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.model.WarehouseItem
import com.example.ui.components.KondisiBadge
import com.example.ui.theme.BorderLight
import com.example.ui.theme.KpnBlue
import com.example.ui.theme.KpnBlueDark
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.WarehouseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationScreen(
    item: WarehouseItem,
    isOnline: Boolean,
    viewModel: WarehouseViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Konfirmasi Data",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "确认数据",
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
                    Button(
                        onClick = {
                            if (isSaving) return@Button
                            isSaving = true
                            viewModel.saveDraftRecord(item) {
                                val message = if (isOnline) {
                                    "Data ${item.kodeBarang} berhasil disimpan! (数据已保存)"
                                } else {
                                    "Data disimpan sementara dan akan disinkronkan ketika internet tersedia.\n数据已暂存，网络恢复后将自动同步。"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KpnBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSaving) "Menyimpan... / 正在保存..." else "Simpan Data / 保存数据",
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
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Offline Notification Banner
            if (!isOnline) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StatusWarningBg)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = StatusWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Mode Offline Aktif / 离线模式生效中",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusWarning
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Data disimpan sementara dan akan disinkronkan ketika internet tersedia.\n数据已暂存，网络恢复后将自动同步。",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Summary Info Card
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
                            Text(
                                text = "Informasi Data / 数据详情",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = KpnBlueDark
                            )
                            KondisiBadge(kondisi = item.kondisi)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        ConfirmDetailRow("Kode Barang (货物编码)", item.kodeBarang)
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ConfirmDetailRow("Tanggal (日期)", item.tanggal)
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ConfirmDetailRow("Jumlah (数量)", "${item.jumlah} Unit / 件")
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ConfirmDetailRow("Lokasi (位置)", item.lokasi)
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ConfirmDetailRow("Nomor Kendaraan (车牌号码)", item.nomorKendaraan)

                        if (item.keterangan.isNotBlank()) {
                            HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                            ConfirmDetailRow("Keterangan (备注)", item.keterangan)
                        }
                    }
                }
            }

            // Photo Previews
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Dokumentasi Foto / 照片凭证",
                            fontSize = 15.sp,
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
                                text = "Bukti Masalah / 情况证明 (${item.fotoBuktiKeteranganUrls.size} foto):",
                                fontSize = 13.sp,
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
                                            model = ImageRequest.Builder(context)
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
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ConfirmDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.weight(0.45f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.weight(0.55f),
            lineHeight = 18.sp
        )
    }
}

@Composable
fun ConfirmPhotoThumb(
    label: String,
    url: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9))
                .clickable { if (url.isNotBlank()) onClick() }
        ) {
            if (url.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak Ada",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            maxLines = 1
        )
    }
}
