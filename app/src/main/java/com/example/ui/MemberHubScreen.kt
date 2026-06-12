package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SubtitleLine
import com.example.data.VocabPreset
import com.example.viewmodel.CineLingoViewModel
import com.example.ui.theme.*

@Composable
fun MemberHubScreen(
    viewModel: CineLingoViewModel
) {
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val registrationStatus by viewModel.registrationStatus.collectAsState()
    val loginStatus by viewModel.loginStatus.collectAsState()
    val movies by viewModel.allMoviesFlow.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var isSignUpMode by remember { mutableStateOf(false) }

    // Admin state
    var isAdminModeActive by remember { mutableStateOf(false) }
    var adminPasswordInput by remember { mutableStateOf("") }
    var adminLoginError by remember { mutableStateOf("") }

    // Local input for transaction code
    var transactionCodeInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Screen header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isAdminModeActive) "АДМИНИСТРАТОР СТУДИ" else "КИНО Продюсер Портал",
                    color = NeonMagenta,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = when {
                        isAdminModeActive -> "Систем удирдах хэсэг"
                        loggedInUser != null -> "Гишүүдийн Студи"
                        else -> "Гишүүний бүртгэл"
                    },
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isAdminModeActive -> NeonYellow.copy(alpha = 0.15f)
                            loggedInUser?.isVipApproved == true -> NeonGreen.copy(alpha = 0.15f)
                            loggedInUser != null -> NeonCyan.copy(alpha = 0.15f)
                            else -> Color.White.copy(alpha = 0.05f)
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isAdminModeActive -> NeonYellow
                            loggedInUser?.isVipApproved == true -> NeonGreen
                            loggedInUser != null -> NeonCyan
                            else -> TextMuted
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when {
                        isAdminModeActive -> "АДМИН"
                        loggedInUser?.isVipApproved == true -> "VIP ГИШҮҮН"
                        loggedInUser != null -> "ҮНЭГҮЙ ЭРХ"
                        else -> "ХОЛБОГДООГҮЙ"
                    },
                    color = when {
                        isAdminModeActive -> NeonYellow
                        loggedInUser?.isVipApproved == true -> NeonGreen
                        loggedInUser != null -> NeonCyan
                        else -> TextMuted
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isAdminModeActive) {
            // ==========================================
            // ADMIN MANAGEMENT CONSOLE
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonYellow.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NeonCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👑 ВИП ЗӨВШӨӨРӨЛ БА ХҮСЭЛТҮҮД",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { isAdminModeActive = false },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, NeonMagenta),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Хаах", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Хэрэглэгчдийн ирүүлсэн Хаан Банкны төлбөр болон шилжүүлгийг баталгаажуулж VIP эрх олгох систем.",
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val pendingUsers = allUsers.filter { it.isVipRequested && !it.isVipApproved }
                    val activeVipUsers = allUsers.filter { it.isVipApproved }
                    val regularUsers = allUsers.filter { !it.isVipRequested && !it.isVipApproved }

                    Text(
                        text = "ХҮЛЭЭГДЭЖ БУЙ ХҮСЭЛТҮҮД (${pendingUsers.size})",
                        color = NeonYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (pendingUsers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.02f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Шинэ хүсэлт одоогоор байхгүй байна.", color = TextMuted, fontSize = 11.sp)
                        }
                    } else {
                        pendingUsers.forEach { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(1.dp, NeonYellow.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = NeonDarkBg)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(user.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(user.email, color = TextGray, fontSize = 11.sp)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NeonMagenta.copy(alpha = 0.1f))
                                                .border(1.dp, NeonMagenta.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Шалгах шаардлагатай", color = NeonMagenta, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.ReceiptLong, "", tint = NeonYellow, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Гүйлгээний код/утга: ",
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = user.vipTxId.ifBlank { "Хоосон" },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.approveVip(user.email, true) },
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Check, "", tint = NeonDarkBg, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Батлах (VIP)", color = NeonDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.approveVip(user.email, false) },
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            border = BorderStroke(1.dp, NeonMagenta),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Цуцлах", color = NeonMagenta, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "VIP ГИШҮҮДИЙН ЖАГСААЛТ (${activeVipUsers.size})",
                        color = NeonGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (activeVipUsers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.02f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Идэвхтэй VIP хэрэглэгч байхгүй байна.", color = TextMuted, fontSize = 11.sp)
                        }
                    } else {
                        activeVipUsers.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(user.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.WorkspacePremium, "", tint = NeonYellow, modifier = Modifier.size(14.dp))
                                    }
                                    Text(user.email, color = TextGray, fontSize = 10.sp)
                                }

                                IconButton(
                                    onClick = { viewModel.approveVip(user.email, false) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.RemoveCircleOutline, "Cancel VIP", tint = NeonMagenta, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Нийт бүртгэлтэй продюсерүүд: ${allUsers.size}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // ==========================================
            // NORMAL PRODUCER MODE
            // ==========================================
            if (loggedInUser == null) {
                // RENDER AUTHENTICATION VIEW (SIGN UP / LOGIN)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = NeonCardBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = if (isSignUpMode) "Шинээр бүртгэл үүсгэх" else "Системд нэвтрэх",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isSignUpMode) "Студид нэгдэж, өөрийн киноны үг хэллэг нэмэх боломжтой болно." 
                                   else "Бүртгэлтэй продюсерүүд хамтрах боломжтой.",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        var emailInput by remember { mutableStateOf("") }
                        var nameInput by remember { mutableStateOf("") }
                        var passwordInput by remember { mutableStateOf("") }

                        // Email field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            modifier = Modifier.fillMaxWidth().testTag("auth_email"),
                            label = { Text("И-мэйл хаяг", color = TextGray) },
                            placeholder = { Text("producer@example.com", color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = TextMuted,
                                focusedContainerColor = NeonDarkBg.copy(alpha = 0.5f),
                                unfocusedContainerColor = NeonDarkBg.copy(alpha = 0.2f)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isSignUpMode) {
                            // Name field (Sign Up Only)
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                modifier = Modifier.fillMaxWidth().testTag("auth_name"),
                                label = { Text("Таны нэр", color = TextGray) },
                                placeholder = { Text("Бат-Эрдэнэ", color = TextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = TextMuted,
                                    focusedContainerColor = NeonDarkBg.copy(alpha = 0.5f),
                                    unfocusedContainerColor = NeonDarkBg.copy(alpha = 0.2f)
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Password field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            modifier = Modifier.fillMaxWidth().testTag("auth_password"),
                            label = { Text("Нууц үг", color = TextGray) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = TextMuted,
                                focusedContainerColor = NeonDarkBg.copy(alpha = 0.5f),
                                unfocusedContainerColor = NeonDarkBg.copy(alpha = 0.2f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Status Message Display
                        val activeStatus = if (isSignUpMode) registrationStatus else loginStatus
                        if (!activeStatus.isNullOrEmpty()) {
                            Text(
                                text = activeStatus,
                                color = if (activeStatus.startsWith("Алдаа")) NeonMagenta else NeonGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        // Action buttons
                        Button(
                            onClick = {
                                if (isSignUpMode) {
                                    viewModel.registerUser(emailInput, nameInput, passwordInput)
                                } else {
                                    viewModel.loginUser(emailInput, passwordInput)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(
                                    1.dp,
                                    Brush.linearGradient(listOf(NeonCyan, NeonMagenta)),
                                    RoundedCornerShape(12.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isSignUpMode) "Одоо бүртгүүлэх" else "Нэвтрэх",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSignUpMode) "Бүртгэлтэй юу?" else "Шинэ гишүүн үү?",
                                color = TextGray,
                                fontSize = 13.sp
                            )
                            TextButton(
                                onClick = { isSignUpMode = !isSignUpMode }
                            ) {
                                Text(
                                    text = if (isSignUpMode) "Нэвтрэх" else "Бүртгэл нээх",
                                    color = NeonMagenta,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // RENDER LOGGED IN WORKSPACE & MOVIE CREATOR FORM
                val currentUser = loggedInUser!!

                // Profile info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(
                                1.dp,
                                Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.5f), Color.Transparent))
                            ),
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = NeonCardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        if (currentUser.isVipApproved) NeonGreen.copy(alpha = 0.2f) 
                                        else NeonMagenta.copy(alpha = 0.2f)
                                    )
                                    .border(
                                        1.dp, 
                                        if (currentUser.isVipApproved) NeonGreen else NeonMagenta, 
                                        RoundedCornerShape(24.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentUser.isVipApproved) Icons.Default.WorkspacePremium else Icons.Default.Person,
                                    contentDescription = "",
                                    tint = if (currentUser.isVipApproved) NeonGreen else NeonMagenta,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentUser.name,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (currentUser.isVipApproved) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.Verified, "VIP", tint = NeonGreen, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(
                                    text = currentUser.email,
                                    color = TextGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.logoutUser() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NeonMagenta),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Гарах", color = NeonMagenta, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // PAYMENT AND VIP SUBMISSION SECTION
                // ==========================================
                if (!currentUser.isVipApproved) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                BorderStroke(
                                    1.dp,
                                    Brush.linearGradient(listOf(NeonYellow, NeonMagenta))
                                ),
                                RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = NeonDarkBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "",
                                    tint = NeonYellow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VIP Продюсер Эрх Идэвхжүүлэх",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "VIP эрхтэй болсноор өөрийн кино, хичээлүүдийг Монгол суралцагчдад зориулан хязгааргүй нэмж оруулах боломжтой болно.",
                                color = TextGray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Khan Bank visual card box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NeonCardBg)
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "ТӨЛБӨР ТӨЛӨХ ДАНСНЫ МЭДЭЭЛЭЛ",
                                        color = NeonCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Банк:", color = TextGray, fontSize = 12.sp)
                                        Text("Хаан Банк (Khan Bank)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Дансны дугаар:", color = TextGray, fontSize = 12.sp)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "280005005114065124",
                                                color = NeonYellow,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Хүлээн авагч:", color = TextGray, fontSize = 12.sp)
                                        Text("Б.Бадамгарав (CineLingo)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Хэмжээ (Хураамж):", color = TextGray, fontSize = 12.sp)
                                        Text("15,000 ₮ (Нэг удаа)", color = NeonGreen, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (currentUser.isVipRequested) {
                                // Request is already sent and pending
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NeonMagenta.copy(alpha = 0.08f))
                                        .border(1.dp, NeonMagenta, RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.HourglassEmpty, "", tint = NeonMagenta, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Хүсэлт хүлээн авсан, хүлээгдэж байна",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Гүйлгээний код: ${currentUser.vipTxId}. Админ шуурхай шалгаж баталгаажуулна.",
                                                color = TextGray,
                                                fontSize = 11.sp,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Input transaction reference to submit request
                                Text(
                                    text = "Гүйлгээ хийсний дараа энд баталгаажуулах код эсвэл гүйлгээний утгаа оруулна уу:",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = transactionCodeInput,
                                    onValueChange = { transactionCodeInput = it },
                                    label = { Text("Шилжүүлгийн утга/код оруулна уу", color = TextGray, fontSize = 11.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonYellow,
                                        unfocusedBorderColor = TextMuted,
                                        focusedContainerColor = NeonDarkBg,
                                        unfocusedContainerColor = NeonDarkBg
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("vip_tx_id_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (transactionCodeInput.isNotBlank()) {
                                            viewModel.requestVip(transactionCodeInput)
                                        }
                                    },
                                    enabled = transactionCodeInput.isNotBlank(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonYellow,
                                        disabledContainerColor = Color.White.copy(alpha = 0.05f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        "ТӨЛБӨР ТӨЛСӨН ХҮСЭЛТ ИЛГЭЭХ",
                                        color = if (transactionCodeInput.isNotBlank()) NeonDarkBg else TextMuted,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Locked studio message block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(NeonCardBg)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked Studio",
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Кино Продюсер Студи цоожтой",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Та дээрх Хаан Банкны данс руу VIP эрх шилжүүлж, идэвхжүүлэлтээ батлуулснаар өөрийн хүссэн киногоо манай нүүр хуудсанд нэмэх боломжтой болно.",
                                color = TextGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    // ==========================================
                    // VIP MEMBER UNLOCKED STUDIO
                    // ==========================================
                    Spacer(modifier = Modifier.height(16.dp))

                    // Movie Form Panel
                    Text(
                        text = "ШИНЭ КИНО НЭМЭХ ХЭСЭГ (VIP 👑)",
                        color = NeonYellow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Их сурвалж өгөгдөл, хадмал ба тайлбар үгийг холбоорой. Таны оруулсан кино нүүр хуудаст шууд нээгдэнэ.",
                        color = TextGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Dynamic Custom creation inputs
                    var inputTitle by remember { mutableStateOf("") }
                    var inputTitleMn by remember { mutableStateOf("") }
                    var inputGenre by remember { mutableStateOf("Драм, Философи") }
                    var inputLevel by remember { mutableStateOf("Intermediate (Дунд шат)") }
                    var inputAccent by remember { mutableStateOf("Америк аялга") }
                    var inputYear by remember { mutableStateOf("2026") }

                    var isUsingSampleData by remember { mutableStateOf(true) }

                    // Sample Data Definition
                    val sampleTitle = "The Matrix"
                    val sampleTitleMn = "Матрикс"
                    val sampleGenre = "Шинжлэх ухаан, Зөгнөлт"
                    val sampleLevel = "Intermediate (Дунд шат)"
                    val sampleAccent = "Америк аялга"
                    val sampleYear = "1999"

                    val sampleSubs = listOf(
                        SubtitleLine(0, "Morpheus", "Морфеус сониуч Неод сонголтын тухай хэлж байна", "Choose the blue pill, you wake up in your bed.", "Хөх эмээ уувал, чи орондоо сэрээд хүссэн зүйлдээ итгэнэ.", "00:04"),
                        SubtitleLine(1, "Morpheus", "Нео руу улаан эмийг сунгаж байна", "Choose the red pill, and I show you how deep the rabbit-hole goes.", "Улаан хабыг сонговол, би чамд туулайн нүх хэр гүн болохыг харуулъя.", "00:10"),
                        SubtitleLine(2, "Neo", "Улаан эмийг сонгоод Морфеус руу итгэлтэй харна", "I choose the truth.", "Би үнэнийг сонгож байна.", "00:16")
                    )

                    val sampleVocab = listOf(
                        VocabPreset("truth", "үнэн", "Noun", "Бодит байдалд тохирох зөв баримт."),
                        VocabPreset("choose", "сонгох", "Verb", "Аль нэг хувилбарыг шийдэх үйлдэл."),
                        VocabPreset("rabbit-hole", "туулайн нүх (хонгил)", "Noun", "Урьдчилан таамаглахын аргагүй гүн, хачин нөхцөл байдал.")
                    )

                    if (isUsingSampleData) {
                        LaunchedEffect(Unit) {
                            inputTitle = sampleTitle
                            inputTitleMn = sampleTitleMn
                            inputGenre = sampleGenre
                            inputLevel = sampleLevel
                            inputAccent = sampleAccent
                            inputYear = sampleYear
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = NeonCardBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Өгөгдөл бөглөх хэлбэр:",
                                    color = NeonYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isUsingSampleData,
                                        onCheckedChange = {
                                            isUsingSampleData = it
                                            if (it) {
                                                inputTitle = sampleTitle
                                                inputTitleMn = sampleTitleMn
                                                inputGenre = sampleGenre
                                                inputLevel = sampleLevel
                                                inputAccent = sampleAccent
                                                inputYear = sampleYear
                                            } else {
                                                inputTitle = ""
                                                inputTitleMn = ""
                                                inputGenre = ""
                                                inputLevel = "Intermediate (Дунд шат)"
                                                inputAccent = "Америк аялга"
                                                inputYear = "2026"
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = NeonCyan,
                                            uncheckedColor = TextMuted,
                                            checkmarkColor = NeonDarkBg
                                        )
                                    )
                                    Text(
                                        "Жишээ утга ашиглах",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = inputTitle,
                                onValueChange = { inputTitle = it },
                                modifier = Modifier.fillMaxWidth().testTag("movie_title"),
                                label = { Text("Киноны нэр (English)", color = TextGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = TextMuted,
                                    focusedContainerColor = NeonDarkBg,
                                    unfocusedContainerColor = NeonDarkBg
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = inputTitleMn,
                                onValueChange = { inputTitleMn = it },
                                modifier = Modifier.fillMaxWidth().testTag("movie_title_mn"),
                                label = { Text("Киноны нэр (Монголоор)", color = TextGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = TextMuted,
                                    focusedContainerColor = NeonDarkBg,
                                    unfocusedContainerColor = NeonDarkBg
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = inputGenre,
                                    onValueChange = { inputGenre = it },
                                    modifier = Modifier.weight(1f).testTag("movie_genre"),
                                    label = { Text("Төрөл жанр", color = TextGray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = TextMuted,
                                        focusedContainerColor = NeonDarkBg,
                                        unfocusedContainerColor = NeonDarkBg
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = inputYear,
                                    onValueChange = { inputYear = it },
                                    modifier = Modifier.weight(1f).testTag("movie_year"),
                                    label = { Text("Бүтээсэн он", color = TextGray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = TextMuted,
                                        focusedContainerColor = NeonDarkBg,
                                        unfocusedContainerColor = NeonDarkBg
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = inputLevel,
                                    onValueChange = { inputLevel = it },
                                    modifier = Modifier.weight(1f).testTag("movie_level"),
                                    label = { Text("Түвшин", color = TextGray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = TextMuted,
                                        focusedContainerColor = NeonDarkBg,
                                        unfocusedContainerColor = NeonDarkBg
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = inputAccent,
                                    onValueChange = { inputAccent = it },
                                    modifier = Modifier.weight(1f).testTag("movie_accent"),
                                    label = { Text("Аялга", color = TextGray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = TextMuted,
                                        focusedContainerColor = NeonDarkBg,
                                        unfocusedContainerColor = NeonDarkBg
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Divider(color = TextMuted.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "ХАДМАЛ ОРУУЛАХ (SUBTITLES):",
                                fontSize = 11.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )

                            if (isUsingSampleData) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonDarkBg)
                                        .padding(8.dp)
                                ) {
                                    sampleSubs.forEach { sub ->
                                        Text(
                                            text = "${sub.speaker}: \"${sub.english}\" -> \"${sub.mongolian}\"",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "💡 'Жишээ утга ашиглах' чагтыг идэвхжүүлж анхны киногоо шууд оруулахыг зөвлөж байна. Дараа нь өөрчлөх боломжтой.",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "ТОЛЬ БИЧГИЙН ҮГС (VOCABULARY PRESET):",
                                fontSize = 11.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )

                            if (isUsingSampleData) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonDarkBg)
                                        .padding(8.dp)
                                ) {
                                    sampleVocab.forEach { voc ->
                                        Text(
                                            text = "• ${voc.English} - [${voc.pos}] : ${voc.Mongolian}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            var successMessage by remember { mutableStateOf("") }
                            if (successMessage.isNotEmpty()) {
                                Text(
                                    text = successMessage,
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    if (inputTitle.isBlank() || inputTitleMn.isBlank()) {
                                        successMessage = "Алдаа: Гарчиг бөглөнө үү!"
                                    } else {
                                        viewModel.addNewMovie(
                                            title = inputTitle,
                                            titleMn = inputTitleMn,
                                            genre = inputGenre,
                                            level = inputLevel,
                                            accent = inputAccent,
                                            year = inputYear,
                                            vocabList = if (isUsingSampleData) sampleVocab else emptyList(),
                                            subtitles = if (isUsingSampleData) sampleSubs else emptyList()
                                        )
                                        successMessage = "Батлагдсан VIP продюсерийн кино амжилттай нэмэгдлээ! 'Нүүр хуудас' хэсэгт орж харна уу."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .border(1.dp, NeonCyan, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Кино нэмэх 🎬", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // User contributed movies list
                val userCreatorMovies = movies.filter { it.id.startsWith("custom_") }
                if (userCreatorMovies.isNotEmpty()) {
                    Text(
                        text = "ТАНЫ ҮҮСГЭСЭН КИНОНУУД",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    userCreatorMovies.forEach { movie ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, NeonMagenta.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = NeonCardBg)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = movie.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "${movie.genre} | ${movie.level}",
                                        color = TextGray,
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteCustomMovie(movie.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Устгах",
                                        tint = NeonMagenta
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // FOOTER: SECRET ADMIN ENTRY PORTAL
        // ==========================================
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color.White.copy(alpha = 0.05f))
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = NeonCardBg.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "",
                            tint = NeonYellow.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Вип эрх батлах (Админ нэвтрэх)",
                            color = TextGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isAdminModeActive) {
                        Text(
                            text = "ИДЭВХТЭЙ",
                            color = NeonGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (!isAdminModeActive) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Хөгжүүлэгч болон админ гүйлгээ давхар шалгаж батлах нууц хэсэг. Нууц үг: admin123",
                        color = TextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = adminPasswordInput,
                            onValueChange = {
                                adminPasswordInput = it
                                adminLoginError = ""
                            },
                            label = { Text("Админ нууц үг", color = TextMuted, fontSize = 11.sp) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonYellow,
                                unfocusedBorderColor = TextMuted,
                                focusedContainerColor = NeonDarkBg,
                                unfocusedContainerColor = NeonDarkBg
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("admin_password_input")
                        )

                        Button(
                            onClick = {
                                if (adminPasswordInput == "admin123") {
                                    isAdminModeActive = true
                                    adminPasswordInput = ""
                                    adminLoginError = ""
                                } else {
                                    adminLoginError = "Алдаа: Нууц үг буруу"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonYellow),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Нэвтрэх", color = NeonDarkBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (adminLoginError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(adminLoginError, color = NeonMagenta, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { isAdminModeActive = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Админ горимоос гарах", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
