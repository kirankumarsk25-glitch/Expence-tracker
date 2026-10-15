package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishDebitRed
import com.example.ui.theme.PolishOnBackground
import com.example.ui.theme.PolishOutline
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurfaceVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Modern preset avatars with distinct themes and vibrant gradients
 */
data class AvatarPreset(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val iconColor: Color = Color.White
)

val AVATAR_PRESETS = listOf(
    AvatarPreset("executive", "Executive", Icons.Default.AccountBalance, listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))),
    AvatarPreset("investor", "Investor", Icons.Default.TrendingUp, listOf(Color(0xFF065F46), Color(0xFF10B981))),
    AvatarPreset("tech", "Fintech", Icons.Default.Devices, listOf(Color(0xFF581C87), Color(0xFF9333EA))),
    AvatarPreset("banker", "Vault Pro", Icons.Default.Security, listOf(Color(0xFF78350F), Color(0xFFD97706))),
    AvatarPreset("crown", "VIP Elite", Icons.Default.Star, listOf(Color(0xFFB45309), Color(0xFFF59E0B))),
    AvatarPreset("sparkle", "Creator", Icons.Default.AutoAwesome, listOf(Color(0xFF831843), Color(0xFFEC4899))),
    AvatarPreset("rocket", "Founder", Icons.Default.RocketLaunch, listOf(Color(0xFF312E81), Color(0xFF6366F1))),
    AvatarPreset("globe", "Nomad", Icons.Default.Public, listOf(Color(0xFF0E7490), Color(0xFF06B6D4))),
    AvatarPreset("shield", "Guardian", Icons.Default.Shield, listOf(Color(0xFF7F1D1D), Color(0xFFEF4444))),
    AvatarPreset("analytics", "Analyst", Icons.Default.Insights, listOf(Color(0xFF1E293B), Color(0xFF475569))),
    AvatarPreset("eco", "Green", Icons.Default.Eco, listOf(Color(0xFF14532D), Color(0xFF22C55E))),
    AvatarPreset("coffee", "Builder", Icons.Default.LocalCafe, listOf(Color(0xFF451A03), Color(0xFF92400E)))
)

/**
 * Universal Avatar display component supporting:
 * 1. Preset avatars (e.g. "avatar:executive")
 * 2. Local saved files or content URIs
 * 3. Initials fallback
 */
@Composable
fun ProfileAvatarView(
    avatarUri: String?,
    userName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showCameraBadge: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val preset = remember(avatarUri) {
        if (avatarUri != null && avatarUri.startsWith("avatar:")) {
            val key = avatarUri.removePrefix("avatar:")
            AVATAR_PRESETS.firstOrNull { it.id == key }
        } else null
    }

    val photoBitmap = remember(avatarUri) {
        if (!avatarUri.isNullOrBlank() && !avatarUri.startsWith("avatar:")) {
            try {
                if (avatarUri.startsWith("/")) {
                    val file = File(avatarUri)
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                } else {
                    val stream = context.contentResolver.openInputStream(Uri.parse(avatarUri))
                    stream?.use { BitmapFactory.decodeStream(it) }
                }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val initials = remember(userName) {
        userName.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")
            .ifEmpty { "VE" }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        when {
            preset != null -> {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(preset.gradient)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = preset.icon,
                            contentDescription = preset.name,
                            tint = preset.iconColor,
                            modifier = Modifier.size(size * 0.52f)
                        )
                    }
                }
            }
            photoBitmap != null -> {
                Image(
                    bitmap = photoBitmap.asImageBitmap(),
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = PolishPrimaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = (size.value * 0.38f).sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = PolishPrimary
                        )
                    }
                }
            }
        }

        if (showCameraBadge) {
            Surface(
                shape = CircleShape,
                color = PolishPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.34f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Photo",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.20f)
                    )
                }
            }
        }
    }
}

/**
 * Robust Modal Bottom Sheet for changing profile photo.
 * Provides:
 * - Choose from Device Gallery (with persistent local saving)
 * - Take Photo with Camera
 * - Select from 12 Modern Avatars
 * - Remove Photo option
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeProfilePhotoBottomSheet(
    currentUri: String?,
    userName: String,
    onDismiss: () -> Unit,
    onPhotoSelected: (String?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Temp file for camera capture
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Helper to safely save picked/captured Uri to internal app files
    fun processAndSaveUri(sourceUri: Uri) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                if (inputStream != null) {
                    val targetFile = File(context.filesDir, "user_profile_photo.jpg")
                    targetFile.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }
                    withContext(Dispatchers.Main) {
                        onPhotoSelected(targetFile.absolutePath)
                        Toast.makeText(context, "Profile Photo Updated!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to load selected photo", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error saving photo: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 1. GetContent launcher (Universal Android Image picker)
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            processAndSaveUri(uri)
        }
    }

    // 2. PickVisualMedia fallback launcher
    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            processAndSaveUri(uri)
        }
    }

    // 3. Camera capture launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            processAndSaveUri(tempCameraUri!!)
        }
    }

    // Function to launch gallery with fail-safe fallback
    fun launchGallery() {
        try {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                pickVisualMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } else {
                getContentLauncher.launch("image/*")
            }
        } catch (e: Exception) {
            try {
                getContentLauncher.launch("image/*")
            } catch (e2: Exception) {
                Toast.makeText(context, "Gallery could not be opened: ${e2.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to launch camera safely
    fun launchCamera() {
        try {
            val cacheFile = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No camera application found on device", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Camera error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PolishBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with current preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatarView(
                    avatarUri = currentUri,
                    userName = userName,
                    size = 64.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Profile Avatar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnBackground
                    )
                    Text(
                        text = "Select an avatar preset or upload a custom photo",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { launchGallery() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gallery", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { launchCamera() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PolishPrimary)
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = PolishPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Camera", color = PolishPrimary, fontWeight = FontWeight.Bold)
                }

                if (!currentUri.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            onPhotoSelected(null)
                            try {
                                val file = File(context.filesDir, "user_profile_photo.jpg")
                                if (file.exists()) file.delete()
                            } catch (_: Exception) {}
                            Toast.makeText(context, "Photo removed. Showing initials.", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PolishDebitRed)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove Photo", tint = PolishDebitRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Presets Section Header
            Text(
                text = "Preset Avatars",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PolishOnBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pick a custom icon tailored for wealth, trading and vault management",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Grid of Avatars
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val chunkedPresets = AVATAR_PRESETS.chunked(4)
                chunkedPresets.forEach { rowPresets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowPresets.forEach { preset ->
                            val isSelected = currentUri == "avatar:${preset.id}"
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        onPhotoSelected("avatar:${preset.id}")
                                        Toast.makeText(context, "Selected ${preset.name} Avatar", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(preset.gradient))
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(3.dp, PolishPrimary, CircleShape)
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = preset.icon,
                                        contentDescription = preset.name,
                                        tint = preset.iconColor,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = PolishPrimary,
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PolishPrimary else PolishOnBackground,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
