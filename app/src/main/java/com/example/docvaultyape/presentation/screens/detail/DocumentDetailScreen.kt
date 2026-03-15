package com.example.docvaultyape.presentation.screens.detail

import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.docvaultyape.ui.theme.VaultError
import com.example.docvaultyape.ui.theme.VaultNavy
import com.example.docvaultyape.ui.theme.VaultCyan
import com.example.docvaultyape.ui.theme.VaultSurface
import com.example.docvaultyape.ui.theme.VaultSurfaceVariant
import com.example.docvaultyape.ui.theme.VaultGold
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.docvaultyape.core.biometric.BiometricResult
import com.example.docvaultyape.domain.model.AccessLog
import com.example.docvaultyape.domain.model.Document
import com.example.docvaultyape.core.biometric.BiometricAuthManager
import com.example.docvaultyape.presentation.screens.detail.interactor.DocumentDetailUiEvent
import com.example.docvaultyape.presentation.screens.detail.interactor.DocumentDetailUiIntent
import com.example.docvaultyape.presentation.screens.detail.interactor.DocumentDetailUiState
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

private const val WATERMARK_ALPHA = 55
private const val WATERMARK_ROTATION = -28f
private const val ZOOM_MIN_SCALE = 1f
private const val ZOOM_MAX_SCALE = 5f
private const val OPACITY_MEDIUM = 0.6f
private const val KB_UNIT = 1024
private const val ARG_UNIT = 255

@Composable
fun DocumentDetailScreen(
    documentId: String,
    onNavigateBack: () -> Unit,
    viewModel: DocumentDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val activity = context.findActivity()

    LaunchedEffect(Unit) {
        android.util.Log.d("BIOMETRIC", "Context class: ${context::class.java.name}")
        android.util.Log.d("BIOMETRIC", "Activity: $activity")
    }

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }

    LaunchedEffect(documentId) {
        viewModel.processIntent(DocumentDetailUiIntent.LoadDocument(documentId))
        viewModel.processIntent(DocumentDetailUiIntent.RequestBiometricAuth)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DocumentDetailUiEvent.LaunchBiometric -> {
                    activity?.let {
                        BiometricAuthManager().authenticate(it, "Autenticación requerida", "Verifica tu identidad") { result ->
                            when (result) {
                                is BiometricResult.Success -> viewModel.processIntent(DocumentDetailUiIntent.OnBiometricSuccess(documentId))
                                else -> onNavigateBack()
                            }
                        }
                    }
                }
                is DocumentDetailUiEvent.NavigateBack -> onNavigateBack()
                else -> {}
            }
        }
    }

    if (state.isDeleted) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(VaultNavy)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(VaultSurface).padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                }
                Text(state.document?.name ?: "Documento", color = Color.White, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (state.isAuthenticated) {
                    IconButton(onClick = { viewModel.processIntent(DocumentDetailUiIntent.ShowDeleteConfirmation) }) {
                        Icon(Icons.Default.DeleteForever, "Eliminar", tint = VaultError)
                    }
                }
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = VaultCyan) }
                !state.isAuthenticated -> BiometricWaitingScreen {
                    activity?.let {
                        BiometricAuthManager().authenticate(it, "DocVault", "Accede al documento") { result ->
                            if (result is BiometricResult.Success)
                                viewModel.processIntent(DocumentDetailUiIntent.OnBiometricSuccess(documentId))
                        }
                    }
                }
                else -> DocumentContent(state)
            }
        }

        if (state.showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.processIntent(DocumentDetailUiIntent.DismissDeleteConfirmation) },
                icon = { Icon(Icons.Default.Warning, null, tint = VaultError) },
                title = { Text("Eliminar documento", color = Color.White) },
                text = { Text("Esta acción es irreversible. El documento será eliminado permanentemente.", color = Color(0xFF9CA3AF)) },
                confirmButton = {
                    Button(onClick = { viewModel.processIntent(DocumentDetailUiIntent.ConfirmDelete(documentId)) },
                        colors = ButtonDefaults.buttonColors(containerColor = VaultError)) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.processIntent(DocumentDetailUiIntent.DismissDeleteConfirmation) }) {
                        Text("Cancelar", color = VaultCyan)
                    }
                },
                containerColor = VaultSurface
            )
        }

        state.error?.let {
            Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) { Text(it) }
        }
    }
}

@Composable
fun DocumentContent(state: DocumentDetailUiState) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            state.decryptedFilePath?.let { path ->
                DocumentViewer(path, state.document?.locationAddress ?: "DocVault Secure")
            }
        }
        state.document?.let { doc ->
            item { DocumentInfoCard(doc) }
        }
        item {
            Text("Últimos accesos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        if (state.accessLogs.isEmpty()) {
            item { Text("Sin registros de acceso aún", color = Color(0xFF6B7280), modifier = Modifier.padding(16.dp)) }
        } else {
            items(state.accessLogs.take(10)) { log -> AccessLogItem(log) }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun DocumentViewer(filePath: String, watermarkText: String) {
    var scale by remember { mutableFloatStateOf(ZOOM_MIN_SCALE) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier.fillMaxWidth().height(420.dp).background(Color.Black)
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
    ) {
        AsyncImage(
            model = filePath,
            contentDescription = "Documento seguro",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
                .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = max(ZOOM_MIN_SCALE, min(ZOOM_MAX_SCALE, scale * zoom))
                        offset = if (scale == ZOOM_MIN_SCALE) Offset.Zero else offset + pan
                    }
                }
                .drawWithContent {
                    drawContent()
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(WATERMARK_ALPHA, ARG_UNIT, ARG_UNIT, ARG_UNIT)
                        textSize = 34f
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    val canvas = drawContext.canvas.nativeCanvas
                    canvas.save()
                    canvas.rotate(WATERMARK_ROTATION, size.width / 2f, size.height / 2f)
                    var y = -size.height
                    while (y < size.height * 2) {
                        var x = -size.width
                        while (x < size.width * 2) {
                            canvas.drawText(watermarkText, x, y, textPaint)
                            x += 320f
                        }
                        y += 160f
                    }
                    canvas.restore()
                }
        )
        if (scale == 1f) {
            Box(
                Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    .background(Color.Black.copy(0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) { Text("Pellizca para zoom", fontSize = 11.sp, color = Color(0xFFD1D5DB)) }
        }
    }
}

@Composable
fun DocumentInfoCard(document: Document) {
    val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoRow(Icons.Default.CalendarToday, "Creado", fmt.format(document.createdAt))
            InfoRow(Icons.Default.Storage, "Tamaño", "${document.sizeBytes / KB_UNIT} KB")
            document.locationAddress?.let { InfoRow(Icons.Default.LocationOn, "Dirección", it) }
            if (document.latitude != null && document.longitude != null) {
                InfoRow(Icons.Default.GpsFixed, "Coordenadas", "%.6f, %.6f".format(document.latitude, document.longitude))
            }
            InfoRow(Icons.Default.Lock, "Cifrado", "AES-256-GCM (Android Keystore)")
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = VaultCyan, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color(0xFF6B7280))
            Text(value, fontSize = 13.sp, color = Color.White)
        }
    }
}

@Composable
fun AccessLogItem(log: AccessLog) {
    val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Visibility, null, tint = VaultCyan.copy(OPACITY_MEDIUM), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(fmt.format(log.accessedAt), fontSize = 13.sp, color = Color(0xFF9CA3AF))
        Spacer(Modifier.width(8.dp))
        Text(log.action.name, fontSize = 11.sp, color = VaultGold.copy(0.7f))
    }
}

@Composable
fun BiometricWaitingScreen(onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.Fingerprint, null, tint = VaultCyan, modifier = Modifier.size(80.dp))
            Text("Autenticación requerida", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center)
            Text("Verifica tu identidad para acceder al documento seguro",
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = VaultCyan)) {
                Icon(Icons.Default.Fingerprint, null)
                Spacer(Modifier.width(8.dp))
                Text("Autenticar", color = VaultNavy, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun Context.findActivity(): FragmentActivity? {
    var ctx = this
    while (true) {
        when (ctx) {
            is FragmentActivity -> return ctx
            is ContextWrapper -> ctx = ctx.baseContext
            else -> return null
        }
    }
}
