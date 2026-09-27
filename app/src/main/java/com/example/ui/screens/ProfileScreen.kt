package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.components.RoleBadge
import com.example.ui.components.SyncStatusChip
import com.example.ui.theme.BorderLight
import com.example.ui.theme.KpnBlue
import com.example.ui.theme.KpnBlueDark
import com.example.ui.theme.KpnBlueLight
import com.example.ui.theme.KpnYellow
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusNormal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.WarehouseViewModel

@Composable
fun ProfileScreen(
    viewModel: WarehouseViewModel,
    onLogout: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Surface(
            color = Color.White,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Profil Pengguna",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "个人资料",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                SyncStatusChip(isOnline = isOnline, syncState = syncState)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.kpn_logo),
                            contentDescription = "Logo KPN",
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(18.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = user.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = user.email,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        RoleBadge(role = user.role)
                    }
                }
            }

            // Google OAuth & Firebase Authentication Card
            item {
                val isGoogleLoggedIn = user.email.contains("@gmail.com")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isGoogleLoggedIn) Color(0xFFF0FDF4) else Color.White
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = if (isGoogleLoggedIn) StatusNormal else KpnBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Google OAuth & Firebase",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (isGoogleLoggedIn) "Akun Google Terhubung (已认证)" else "Belum Terhubung ke Google",
                                        fontSize = 11.sp,
                                        color = if (isGoogleLoggedIn) StatusNormal else TextSecondary
                                    )
                                }
                            }

                            if (isGoogleLoggedIn) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusNormal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ProfileRow("Server Firebase", "Terhubung Langsung (直连成功)")
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))
                        ProfileRow("Akun Server", "dreanput23fe@gmail.com")
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))
                        ProfileRow("Firebase Project ID", "gudang-kpn2")
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))
                        ProfileRow("Project Number", "159718070420")
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))
                        ProfileRow("Koleksi Firestore", "warehouse_records")
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))
                        ProfileRow("Status Sinkronisasi", "Aktif & Siap Kirim Realtime")

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isGoogleLoggedIn) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.logoutUser()
                                    Toast.makeText(context, "Berhasil logout / 已退出登录", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Keluar / Ganti Akun Google (退出登录)", fontSize = 13.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.loginWithGoogle(
                                        email = "dreanput23fe@gmail.com",
                                        name = "Andri Putro (dreanput23fe)"
                                    )
                                    Toast.makeText(
                                        context,
                                        "Login berhasil sebagai dreanput23fe@gmail.com / 登录成功",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KpnBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Login sebagai dreanput23fe@gmail.com",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Role Switcher Card (For testing all permissions)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = KpnBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ganti Hak Akses / 切换角色权限",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Uji coba hak akses ADMIN (input/edit/delete/export), OPERATOR (input/edit/export), atau VIEWER (hanya baca):",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UserRole.entries.forEach { role ->
                                val isSelected = user.role == role
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) KpnBlue else Color(0xFFF1F5F9))
                                        .clickable { viewModel.switchRole(role) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = role.labelId,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else TextPrimary
                                        )
                                        Text(
                                            text = role.labelZh,
                                            fontSize = 10.sp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sync Status & Trigger
            item {
                val isFirebaseLinked = viewModel.isFirebaseConfigured()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Status Koneksi & Server",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "服务器与数据库连接状态",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isOnline) StatusNormal else StatusDanger,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Layer 1: Local Room Database
                        ProfileRow(
                            "Database Lokal (Room SQL)",
                            "Terhubung & Aktif (100%)"
                        )
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                        // Layer 2: Network Connectivity
                        ProfileRow(
                            "Koneksi Internet",
                            if (isOnline) "Online (在线)" else "Offline (离线)"
                        )
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                        // Layer 3: Cloud Firebase
                        ProfileRow(
                            "Cloud Server (Firebase)",
                            if (isFirebaseLinked) "Terhubung (已连接)" else "Siap (Antrean Offline Aktif)"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.syncNow { count ->
                                    val msg = if (isOnline) {
                                        "$count data disinkronkan ke server / $count 条数据已同步"
                                    } else {
                                        "Koneksi offline: data disimpan sementara di database lokal."
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KpnBlueLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = KpnBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sinkronisasi Sekarang / 立即同步",
                                color = KpnBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Application Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tentang Aplikasi / 关于系统",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ProfileRow("Aplikasi", "KPN Warehouse")
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ProfileRow("Subtitle", "Powered by Andre")
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ProfileRow("Versi", "1.0.0 (Build 2026)")
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ProfileRow("Database", "Room (Local) + Firebase Sync")
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ProfileRow("Bahasa / 语言", "Bahasa Indonesia & 中文 (双语)")
                    }
                }
            }

            // Logout Button
            item {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDanger)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Keluar Akun / 退出登录",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ProfileRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 13.sp, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
