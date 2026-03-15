package com.example.docvaultyape.presentation.screens.home

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.docvaultyape.domain.model.Document
import com.example.docvaultyape.domain.model.DocumentType
import com.example.docvaultyape.presentation.screens.home.interactor.HomeUiIntent
import com.example.docvaultyape.ui.theme.VaultBlue
import com.example.docvaultyape.ui.theme.VaultNavy
import com.example.docvaultyape.ui.theme.VaultCyan
import com.example.docvaultyape.ui.theme.VaultSurface
import com.example.docvaultyape.ui.theme.VaultSurfaceVariant
import com.example.docvaultyape.ui.theme.VaultGold
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

private const val OPACITY_DIM = 0.7f
private const val GOLD_DIM = 0.15f
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(onDocumentClick: (String) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryPermissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberMultiplePermissionsState(listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO))
    } else {
        rememberMultiplePermissionsState(listOf(Manifest.permission.READ_EXTERNAL_STORAGE))
    }

    val cameraPermissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
    )


    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val mimeType = context.contentResolver.getType(it)
            val ext = if (mimeType == "application/pdf") ".pdf" else ".jpg"
            val name = "doc_${System.currentTimeMillis()}$ext"
            viewModel.processIntent(HomeUiIntent.AddDocumentFromGallery(it, name))
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                val name = "photo_${System.currentTimeMillis()}.jpg"
                viewModel.processIntent(HomeUiIntent.AddDocumentFromCamera(uri, name))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultNavy)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                VaultBlue.copy(alpha = 0.9f),
                                VaultNavy
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Shield,
                            null,
                            tint = VaultCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "DocVault",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${state.documents.size} documentos cifrados",
                        fontSize = 13.sp,
                        color = VaultCyan.copy(alpha = 0.8f)
                    )
                }
            }

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedFilter == null,
                        onClick = { viewModel.processIntent(HomeUiIntent.FilterDocuments(null)) },
                        label = { Text("Todos") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.FolderOpen,
                                null,
                                Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VaultCyan,
                            selectedLabelColor = VaultNavy
                        )
                    )
                }
                items(DocumentType.entries) { type ->
                    FilterChip(
                        selected = state.selectedFilter == type,
                        onClick = { viewModel.processIntent(HomeUiIntent.FilterDocuments(type)) },
                        label = { Text(if (type == DocumentType.PDF) "PDF" else "Imágenes") },
                        leadingIcon = {
                            Icon(
                                if (type == DocumentType.PDF) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                null,
                                Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VaultGold,
                            selectedLabelColor = VaultNavy
                        )
                    )
                }
            }

            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    Alignment.Center
                ) { CircularProgressIndicator(color = VaultCyan) }

                state.documents.isEmpty() -> EmptyState()
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.documents, key = { it.id }) { doc ->
                        DocumentCard(doc) { onDocumentClick(doc.id) }
                    }
                    item { Spacer(Modifier.height(88.dp)) }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(state.showAddOptions) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FabOption("Desde Galería", Icons.Default.PhotoLibrary) {
                        viewModel.processIntent(HomeUiIntent.ToggleAddOptions)
                        if (galleryPermissions.allPermissionsGranted) {
                            galleryLauncher.launch(arrayOf("image/*", "application/pdf"))
                        } else {
                            galleryPermissions.launchMultiplePermissionRequest()
                        }
                    }
                    FabOption("Desde Cámara", Icons.Default.CameraAlt) {
                        viewModel.processIntent(HomeUiIntent.ToggleAddOptions)
                        if (cameraPermissions.allPermissionsGranted) {
                            val photoFile = File(
                                context.cacheDir,
                                "camera_${System.currentTimeMillis()}.jpg"
                            )
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            cameraImageUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            cameraPermissions.launchMultiplePermissionRequest()
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = { viewModel.processIntent(HomeUiIntent.ToggleAddOptions) },
                containerColor = VaultCyan, contentColor = VaultNavy
            ) {
                Icon(
                    if (state.showAddOptions) Icons.Default.Close else Icons.Default.Add,
                    "Agregar"
                )
            }
        }

        state.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                action = {
                    TextButton(onClick = { viewModel.processIntent(HomeUiIntent.DismissError) }) {
                        Text(
                            "OK",
                            color = VaultCyan
                        )
                    }
                }
            ) { Text(error) }
        }
    }
}

@Composable
fun DocumentCard(document: Document, onClick: () -> Unit) {
    val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (document.type == DocumentType.PDF) VaultGold.copy(GOLD_DIM) else VaultCyan.copy(
                            GOLD_DIM
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (document.type == DocumentType.PDF) Icons.Default.PictureAsPdf else Icons.Default.Image,
                    null,
                    tint = if (document.type == DocumentType.PDF) VaultGold else VaultCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    document.name,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(fmt.format(document.createdAt), fontSize = 12.sp, color = Color(0xFF9CA3AF))
                document.locationAddress?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = VaultCyan.copy(OPACITY_DIM),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            it,
                            fontSize = 11.sp,
                            color = VaultCyan.copy(OPACITY_DIM),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Icon(
                Icons.Default.Lock,
                null,
                tint = Color(0xFF4B5563),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun FabOption(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(8.dp), color = VaultSurfaceVariant) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 13.sp,
                color = Color.White
            )
        }
        Spacer(Modifier.width(8.dp))
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = VaultSurface,
            contentColor = VaultCyan
        ) {
            Icon(icon, contentDescription = label)
        }
    }
}

@Composable
fun EmptyState() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.FolderOpen,
                null,
                tint = Color(0xFF374151),
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Sin documentos",
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
            Text("Agrega documentos con el botón +", color = Color(0xFF4B5563), fontSize = 13.sp)
        }
    }
}
