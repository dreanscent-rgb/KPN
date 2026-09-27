package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.KondisiBarang
import com.example.model.SyncState
import com.example.model.UserRole
import com.example.ui.theme.BorderLight
import com.example.ui.theme.KpnBlue
import com.example.ui.theme.KpnBlueDark
import com.example.ui.theme.KpnBlueLight
import com.example.ui.theme.KpnYellow
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusNormal
import com.example.ui.theme.StatusNormalBg
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.Screen

@Composable
fun KpnBrandHeader(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.kpn_logo),
            contentDescription = "Logo KPN",
            modifier = Modifier
                .size(if (compact) 40.dp else 52.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "KPN",
                    fontSize = if (compact) 18.sp else 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KpnBlue
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(KpnYellow)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Powered by Andre",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                }
            }
            Text(
                text = "Aplikasi Manajemen Data Gudang 仓库数据管理",
                fontSize = if (compact) 11.sp else 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SyncStatusChip(
    isOnline: Boolean,
    syncState: SyncState,
    modifier: Modifier = Modifier,
    onSyncClick: () -> Unit = {}
) {
    val (bgColor, textColor, icon, labelId, labelZh) = when {
        !isOnline -> Tuple5(
            Color(0xFFF1F5F9),
            Color(0xFF64748B),
            Icons.Default.CloudOff,
            "Offline",
            "离线"
        )
        syncState == SyncState.SYNCING -> Tuple5(
            KpnBlueLight,
            KpnBlue,
            Icons.Default.CloudSync,
            "Syncing",
            "同步中"
        )
        syncState == SyncState.PENDING -> Tuple5(
            StatusWarningBg,
            StatusWarning,
            Icons.Default.CloudOff,
            "Pending",
            "待同步"
        )
        else -> Tuple5(
            StatusNormalBg,
            StatusNormal,
            Icons.Default.CloudDone,
            "Online",
            "在线"
        )
    }

    Surface(
        modifier = modifier.clickable { onSyncClick() },
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "$labelId / $labelZh",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)

@Composable
fun KondisiBadge(
    kondisi: KondisiBarang,
    modifier: Modifier = Modifier
) {
    val (bg, textCol) = when (kondisi) {
        KondisiBarang.NORMAL -> StatusNormalBg to StatusNormal
        KondisiBarang.ADA_MASALAH -> StatusWarningBg to StatusWarning
        KondisiBarang.RUSAK -> StatusDangerBg to StatusDanger
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${kondisi.titleId} · ${kondisi.titleZh}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textCol
        )
    }
}

@Composable
fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (role) {
        UserRole.ADMIN -> KpnBlue to Color.White
        UserRole.OPERATOR -> KpnBlueLight to KpnBlueDark
        UserRole.VIEWER -> Color(0xFFF1F5F9) to TextSecondary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "${role.labelId} (${role.labelZh})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourcePickerBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pilih Sumber Foto / 选择照片来源",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Silakan pilih kamera untuk mengambil foto baru atau pilih dari galeri",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onTakePhoto()
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(containerColor = KpnBlueLight),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(KpnBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ambil Foto",
                            fontWeight = FontWeight.Bold,
                            color = KpnBlueDark,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "拍照",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onPickGallery()
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Pilih Galeri",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "从相册选择",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PhotoSlotCard(
    title: String,
    subtitleZh: String,
    photoUrl: String,
    isRequired: Boolean = true,
    onAddOrReplace: () -> Unit,
    onDelete: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = subtitleZh,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                if (isRequired) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StatusDangerBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Wajib / 必填",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusDanger
                        )
                    }
                } else {
                    Text(
                        text = "Opsional / 可选",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (photoUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPreview() }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay actions
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { onAddOrReplace() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Ganti Foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = StatusDanger.copy(alpha = 0.85f),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { onDelete() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                        .clickable { onAddOrReplace() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = KpnBlue,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tekan untuk Tambah Foto",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KpnBlue
                        )
                        Text(
                            text = "点击添加照片 (Camera / Gallery)",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FullscreenImageViewerDialog(
    imageUrl: String?,
    onClose: () -> Unit
) {
    if (imageUrl == null) return

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Pratinjau Foto Penuh",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 20.dp)
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun AppBottomNavBar(
    currentScreen: Screen,
    onSelectScreen: (Screen) -> Unit,
    unreadNotifCount: Int = 0
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        val isHome = currentScreen is Screen.Dashboard
        val isData = currentScreen is Screen.DataList
        val isNotif = currentScreen is Screen.Notifications
        val isProfile = currentScreen is Screen.Profile

        NavigationBarItem(
            selected = isHome,
            onClick = { onSelectScreen(Screen.Dashboard) },
            icon = {
                Icon(
                    imageVector = if (isHome) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Beranda"
                )
            },
            label = {
                Text(
                    text = "Beranda\n首页",
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = KpnBlue,
                indicatorColor = KpnBlueLight
            )
        )

        NavigationBarItem(
            selected = isData,
            onClick = { onSelectScreen(Screen.DataList) },
            icon = {
                Icon(
                    imageVector = if (isData) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                    contentDescription = "Data Gudang"
                )
            },
            label = {
                Text(
                    text = "Data\n数据",
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = KpnBlue,
                indicatorColor = KpnBlueLight
            )
        )

        NavigationBarItem(
            selected = isNotif,
            onClick = { onSelectScreen(Screen.Notifications) },
            icon = {
                Box {
                    Icon(
                        imageVector = if (isNotif) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Notifikasi"
                    )
                    if (unreadNotifCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(KpnYellow)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            },
            label = {
                Text(
                    text = "Notifikasi\n通知",
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = KpnBlue,
                indicatorColor = KpnBlueLight
            )
        )

        NavigationBarItem(
            selected = isProfile,
            onClick = { onSelectScreen(Screen.Profile) },
            icon = {
                Icon(
                    imageVector = if (isProfile) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profil"
                )
            },
            label = {
                Text(
                    text = "Profil\n个人资料",
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = KpnBlue,
                indicatorColor = KpnBlueLight
            )
        )
    }
}
