package br.edu.historiaviva.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.edu.historiaviva.data.HistoricalRepository
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberRenderer
import io.github.sceneview.rememberScene
import io.github.sceneview.rememberView

@Composable
fun ArDemoRoute(
    characterId: String?,
    onBack: () -> Unit,
    onOpenInfo: (String) -> Unit
) {
    val character = remember(characterId) { HistoricalRepository.getCharacter(characterId) }
    ArDemoScreen(
        title = character?.name ?: "Modo Demo",
        modelAssetFileLocation = character?.modelAssetFileLocation ?: "models/placeholder.glb",
        arScaleToUnits = character?.arScaleToUnits ?: 1.4f,
        arCenterOriginY = character?.arCenterOriginY ?: -0.5f,
        onBack = onBack,
        onOpenInfo = { character?.id?.let(onOpenInfo) }
    )
}

@Composable
private fun ArDemoScreen(
    title: String,
    modelAssetFileLocation: String,
    arScaleToUnits: Float,
    arCenterOriginY: Float,
    onBack: () -> Unit,
    onOpenInfo: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var isPreviewReady by remember { mutableStateOf(false) }
    var previewViewRef: PreviewView? by remember { mutableStateOf(null) }

    val requestCameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            cameraPermissionGranted = granted
        }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val view = rememberView(engine)
    val renderer = rememberRenderer(engine)
    val scene = rememberScene(engine)
    val cameraNode = rememberCameraNode(engine) {
        position = Position(z = 3.8f)
    }

    val nodes = rememberNodes()
    var modelNode: ModelNode? by remember { mutableStateOf(null) }
    var rotationY by remember { mutableFloatStateOf(0f) }
    var instructionsVisible by remember { mutableStateOf(true) }
    var modelError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(modelAssetFileLocation) {
        modelError = null
        runCatching {
            val instance = modelLoader.createModelInstance(assetFileLocation = modelAssetFileLocation)
            val node = ModelNode(
                modelInstance = instance,
                autoAnimate = false,
                scaleToUnits = arScaleToUnits,
                centerOrigin = Position(y = arCenterOriginY)
            ).apply {
                rotation = Rotation(y = rotationY)
            }
            nodes.clear()
            nodes += node
            modelNode = node
        }.onFailure { t ->
            modelError = t.message ?: t::class.java.simpleName
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            update = { previewView ->
                if (previewViewRef !== previewView) {
                    previewViewRef = previewView
                }
            }
        )

        DisposableEffect(cameraPermissionGranted, previewViewRef) {
            val previewView = previewViewRef
            if (!cameraPermissionGranted || previewView == null || isPreviewReady) {
                return@DisposableEffect onDispose {}
            }

            cameraError = null

            val future = ProcessCameraProvider.getInstance(previewView.context)
            var cameraProvider: ProcessCameraProvider? = null
            val executor = ContextCompat.getMainExecutor(previewView.context)
            val runnable = Runnable {
                runCatching {
                    cameraProvider = future.get()
                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }
                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview
                    )
                    isPreviewReady = true
                }.onFailure { t ->
                    cameraError = t.message ?: t::class.java.simpleName
                }
            }
            future.addListener(runnable, executor)

            onDispose {
                runCatching { cameraProvider?.unbindAll() }
                isPreviewReady = false
            }
        }

        if (cameraPermissionGranted && modelError == null) {
            Scene(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                view = view,
                renderer = renderer,
                scene = scene,
                modelLoader = modelLoader,
                isOpaque = false,
                cameraNode = cameraNode,
                childNodes = nodes,
                onFrame = {
                    modelNode?.rotation = Rotation(y = rotationY)
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { instructionsVisible = false },
                        onDrag = { _, dragAmount ->
                            rotationY += dragAmount.x * 0.15f
                        }
                    )
                }
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Text(
                    modifier = Modifier.weight(1f),
                    text = "$title (Demo)",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onOpenInfo) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                when {
                    !cameraPermissionGranted -> {
                        DemoOverlay(
                            title = "Permissão de câmera necessária",
                            text = "O Modo Demo usa a câmera como fundo. Autorize a câmera nas permissões do app.",
                            primaryButton = {
                                Button(onClick = { requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                    Text("Permitir câmera")
                                }
                            }
                        )
                    }
                    cameraError != null -> {
                        DemoOverlay(
                            title = "Câmera indisponível",
                            text = "Falha ao abrir a câmera: $cameraError",
                            primaryButton = {
                                Button(onClick = onBack) {
                                    Text("Voltar")
                                }
                            }
                        )
                    }
                    modelError != null -> {
                        DemoOverlay(
                            title = "Falha ao carregar modelo 3D",
                            text = "Erro: $modelError",
                            primaryButton = {
                                Button(onClick = onBack) {
                                    Text("Voltar")
                                }
                            }
                        )
                    }
                    instructionsVisible -> {
                        Text(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .background(Color.Black.copy(alpha = 0.45f))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            text = "Arraste para girar o personagem (Demo sem ARCore)",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RA simplificada (sem rastrear plano/ambiente)",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.size(8.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "3D", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DemoOverlay(
    title: String,
    text: String,
    primaryButton: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .clip(MaterialTheme.shapes.large)
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
        primaryButton()
    }
}
