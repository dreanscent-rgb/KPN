package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.SyncState
import com.example.model.UserProfile
import com.example.model.WarehouseItem
import com.example.ui.components.KondisiBadge
import com.example.ui.components.KpnBrandHeader
import com.example.ui.components.RoleBadge
import com.example.ui.components.SyncStatusChip
import com.example.ui.theme.KpnBlue
import com.example.ui.theme.KpnBlueDark
import com.example.ui.theme.KpnBlueLight
import com.example.ui.theme.KpnYellow
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusNormal
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.WarehouseStats

@Composable
fun DashboardScreen(
    user: UserProfile,
    stats: WarehouseStats,
    recentItems: List<WarehouseItem>,
    isOnline: Boolean,
    syncState: SyncState,
    onInputClick: () -> Unit,
    onDataListClick: () -> Unit,
    onSearchFilterClick: () -> Unit,
    onExportClick: () -> Unit,
    onItemClick: (String) -> Unit,
    onSyncClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Branding Bar & Sync Status
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    KpnBrandHeader(compact = true)
                    SyncStatusChip(
                        isOnline = isOnline,
                        syncState = syncState,
                        onSyncClick = onSyncClick
                    )
                }
            }
        }

        // Greeting Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = KpnBlue)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(KpnBlue, KpnBlueDark)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Selamat Datang, 欢迎",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                            RoleBadge(role = user.role)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${user.name}\n${user.role.labelZh}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Siap untuk mengelola data barang gudang secara mobile dan real-time.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Quick Actions (4 Menu Utama)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Menu Utama / 主要菜单",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AddBox,
                        titleId = "Input Data",
                        titleZh = "输入数据",
                        iconBg = KpnBlueLight,
                        iconTint = KpnBlue,
                        enabled = user.role.canInput,
                        onClick = onInputClick
                    )
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Inventory,
                        titleId = "Data Gudang",
                        titleZh = "仓库数据",
                        iconBg = Color(0xFFF3E8FF),
                        iconTint = Color(0xFF9333EA),
                        onClick = onDataListClick
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Search,
                        titleId = "Cari / Filter",
                        titleZh = "搜索 / 筛选",
                        iconBg = Color(0xFFECFDF5),
                        iconTint = Color(0xFF059669),
                        onClick = onSearchFilterClick
                    )
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FileDownload,
                        titleId = "Export Excel",
                        titleZh = "导出 Excel",
                        iconBg = Color(0xFFFFFBEB),
                        iconTint = Color(0xFFD97706),
                        enabled = user.role.canExport,
                        onClick = onExportClick
                    )
                }
            }
        }

        // Statistics Cards
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Statistik Gudang / 仓库统计",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        count = stats.totalCount.toString(),
                        labelId = "Total Data",
                        labelZh = "全部数据",
                        accentColor = KpnBlue
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        count = stats.todayCount.toString(),
                        labelId = "Hari Ini",
                        labelZh = "今日数据",
                        accentColor = Color(0xFF0284C7)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        count = stats.normalCount.toString(),
                        labelId = "Normal",
                        labelZh = "正常",
                        accentColor = StatusNormal
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        count = stats.bermasalahCount.toString(),
                        labelId = "Bermasalah",
                        labelZh = "异常",
                        accentColor = StatusDanger
                    )
                }
            }
        }

        // Recent Data Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Data Terbaru / 最近数据",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = onDataListClick) {
                    Text(
                        text = "Lihat Semua (查看全部)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KpnBlue
                    )
                }
            }
        }

        if (recentItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum Ada Data / 暂无数据",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mulai input data barang gudang sekarang.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onInputClick,
                            colors = ButtonDefaults.buttonColors(containerColor = KpnBlue)
                        ) {
                            Text("Input Data / 输入数据")
                        }
                    }
                }
            }
        } else {
            items(recentItems.take(5)) { item ->
                RecentItemCard(
                    item = item,
                    onClick = { onItemClick(item.id) }
                )
            }
        }
    }
}

@Composable
fun ActionTile(
    icon: ImageVector,
    titleId: String,
    titleZh: String,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = titleId,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) TextPrimary else TextMuted
                )
                Text(
                    text = titleZh,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun StatCard(
    count: String,
    labelId: String,
    labelZh: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = labelId,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = labelZh,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun RecentItemCard(
    item: WarehouseItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail image
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9))
            ) {
                val displayPhoto = item.fotoKodeUrl.ifBlank { item.fotoBarangUrl }
                if (displayPhoto.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(displayPhoto)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.kodeBarang,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    KondisiBadge(kondisi = item.kondisi)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = item.lokasi,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Plat: ${item.nomorKendaraan}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = KpnBlueDark
                    )
                    Text(
                        text = item.tanggal,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Detail",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
