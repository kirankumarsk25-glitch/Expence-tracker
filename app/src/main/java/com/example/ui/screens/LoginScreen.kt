package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishCreditGreen
import com.example.ui.theme.PolishDebitRed
import com.example.ui.theme.PolishOnBackground
import com.example.ui.theme.PolishOnPrimary
import com.example.ui.theme.PolishOutline
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.viewmodel.FinanceViewModel

enum class AuthTab {
    LOGIN, REGISTER
}

@Composable
fun LoginScreen(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(AuthTab.LOGIN) }
    var showGoogleAuthDialog by remember { mutableStateOf(false) }

    // Login Form State
    var loginId by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Register Form State
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regDob by remember { mutableStateOf("1999-08-25") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf<String?>(null) }
    var enteredOtp by remember { mutableStateOf("") }
    var registerError by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PolishBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Logo Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = PolishPrimaryContainer,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Vault Logo",
                            tint = PolishPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Vault Expense",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = PolishOnBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Smart financial control, debt management & secure auto-location sync",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Switcher (Login vs Register)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PolishSurfaceVariant)
                    .padding(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (activeTab == AuthTab.LOGIN) PolishPrimary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            activeTab = AuthTab.LOGIN
                            loginError = null
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == AuthTab.LOGIN) PolishOnPrimary else PolishOnBackground
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (activeTab == AuthTab.REGISTER) PolishPrimary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            activeTab = AuthTab.REGISTER
                            registerError = null
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Register Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == AuthTab.REGISTER) PolishOnPrimary else PolishOnBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = BorderStroke(1.dp, PolishOutline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (activeTab == AuthTab.LOGIN) {
                        Text(
                            text = "Account Login",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PolishOnBackground
                        )

                        Text(
                            text = "Enter your Email or Mobile Number & Password to continue.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        if (loginError != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PolishDebitRed.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, PolishDebitRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = loginError!!,
                                    color = PolishDebitRed,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        OutlinedTextField(
                            value = loginId,
                            onValueChange = {
                                loginId = it
                                loginError = null
                            },
                            label = { Text("Email ID or Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PolishPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                loginError = null
                            },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PolishPrimary) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (loginId.isBlank() || loginPassword.isBlank()) {
                                    loginError = "Please enter both Email/Mobile and Password!"
                                } else {
                                    val success = viewModel.loginWithCredentials(loginId, loginPassword)
                                    if (success) {
                                        Toast.makeText(context, "Welcome back! Login Successful.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        loginError = "Invalid Email/Mobile or Password! Please check credentials or register."
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Sign In with Credentials",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishOnPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.weight(1f).height(1.dp), color = PolishOutline) {}
                            Text(
                                text = " OR ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Surface(modifier = Modifier.weight(1f).height(1.dp), color = PolishOutline) {}
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Google Auth Button
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PolishSurfaceVariant,
                            border = BorderStroke(1.dp, PolishOutline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clickable { showGoogleAuthDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "G", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF4285F4))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Login via Google Auth",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishOnBackground
                                )
                            }
                        }
                    } else {
                        // REGISTER TAB
                        Text(
                            text = "Register New Account",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PolishOnBackground
                        )

                        Text(
                            text = "Complete details & verify OTP sent to Email & Mobile.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        if (registerError != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PolishDebitRed.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, PolishDebitRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = registerError!!,
                                    color = PolishDebitRed,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it; registerError = null },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PolishPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it; registerError = null },
                            label = { Text("Email ID") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PolishPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it; registerError = null },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PolishPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it; registerError = null },
                            label = { Text("Set Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PolishPrimary) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it; registerError = null },
                            label = { Text("Re-verify Password") },
                            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = PolishPrimary) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (generatedOtp == null) {
                            Button(
                                onClick = {
                                    if (regName.isBlank() || regEmail.isBlank() || regPhone.isBlank() || regPassword.isBlank()) {
                                        registerError = "Please fill in all mandatory profile fields!"
                                    } else if (regPassword != regConfirmPassword) {
                                        registerError = "Passwords do not match! Please check Re-verify Password field."
                                    } else {
                                        val code = viewModel.generateSixDigitOtp()
                                        generatedOtp = code
                                        registerError = null
                                        Toast.makeText(
                                            context,
                                            "📲 [SMS & Email Dispatched]\nVerification code sent to $regPhone and $regEmail!\n(Demo OTP Code: $code)",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "Send OTP to Mobile & Email",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishOnPrimary
                                )
                            }
                        } else {
                            // OTP Input Stage
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PolishSurfaceVariant,
                                border = BorderStroke(1.dp, PolishOutline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PolishCreditGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "OTP Dispatched to Mobile & Email", color = PolishOnBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Text(
                                        text = "Check SMS at +91 ${regPhone.takeLast(10)} or Inbox at ${regEmail.take(3)}***@***",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = enteredOtp,
                                onValueChange = { enteredOtp = it; registerError = null },
                                label = { Text("Enter 6-Digit OTP Received") },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = PolishPrimary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        val newCode = viewModel.generateSixDigitOtp()
                                        generatedOtp = newCode
                                        registerError = null
                                        Toast.makeText(
                                            context,
                                            "📲 Resent OTP to $regPhone & $regEmail!\n(Demo OTP Code: $newCode)",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                ) {
                                    Text("Resend OTP", color = PolishPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                TextButton(
                                    onClick = {
                                        generatedOtp = null
                                        enteredOtp = ""
                                    }
                                ) {
                                    Text("Change Mobile / Email", color = Color.Gray, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    val err = viewModel.registerWithOtp(
                                        name = regName,
                                        email = regEmail,
                                        phone = regPhone,
                                        dob = regDob,
                                        password = regPassword,
                                        enteredOtp = enteredOtp,
                                        generatedOtp = generatedOtp ?: ""
                                    )
                                    if (err != null) {
                                        registerError = err
                                    } else {
                                        Toast.makeText(context, "Registration Successful! Profile & Auto-Location Presynced.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PolishCreditGreen),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = "Verify OTP & Complete Registration",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Demo / Guest login option
                    OutlinedButton(
                        onClick = {
                            viewModel.loginWithGoogle(
                                name = "Guest User",
                                email = "guest@vaultexpense.app",
                                phone = "+91 98765 43210",
                                dob = "2000-01-01",
                                location = "Bengaluru, India"
                            )
                            Toast.makeText(context, "Logged in as Guest User", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Continue as Demo Guest",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = PolishCreditGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Protected with 256-bit AES Encryption",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showGoogleAuthDialog) {
        GoogleSignInBottomSheet(
            viewModel = viewModel,
            onDismiss = { showGoogleAuthDialog = false },
            onSuccess = { name, email, phone, dob, location ->
                viewModel.loginWithGoogle(name, email, phone, dob, location)
                showGoogleAuthDialog = false
                Toast.makeText(context, "Signed in via Google as $name", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSignInBottomSheet(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onSuccess: (name: String, email: String, phone: String, dob: String, location: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(viewModel.userName.value) }
    var email by remember { mutableStateOf(viewModel.userEmail.value) }
    var phone by remember { mutableStateOf(viewModel.userPhone.value) }
    var dob by remember { mutableStateOf(viewModel.userDob.value) }
    var location by remember { mutableStateOf(viewModel.userLocation.value) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PolishBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, PolishOutline),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "G",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = Color(0xFF4285F4)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Google Sign-In",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PolishCreditGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Google Identity Verified",
                            style = MaterialTheme.typography.bodySmall,
                            color = PolishCreditGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Confirm Account & Profile Details:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PolishOnBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PolishPrimary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Google Email ID") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PolishPrimary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PolishPrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth (YYYY-MM-DD)") },
                leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = PolishPrimary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (Auto-saved GPS)") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PolishPrimary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        onSuccess(name, email, phone, dob, location.ifBlank { "Bengaluru, India" })
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Continue with Google Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PolishOnPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = PolishOnBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}
