package br.edu.historiaviva.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.edu.historiaviva.data.HistoricalRepository
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.Availability
import com.google.ar.core.HitResult
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import com.google.ar.core.Plane
import com.google.ar.core.Config
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberScene

private fun formatThrowable(t: Throwable): String {
    val parts = buildList {
        add(t::class.java.simpleName + (t.message?.let { ": $it" } ?: ""))
        var c = t.cause
        var depth = 0
        while (c != null && depth < 4) {
            add("caused by " + c::class.java.simpleName + (c.message?.let { ": $it" } ?: ""))
            c = c.cause
            depth++
        }
    }
    return parts.joinToString(" | ")
}

private fun getPackageVersion(context: android.content.Context, packageName: String): String {
    val pm = context.packageManager
    return try {
        val info =
            if (Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
        val versionName = info.versionName ?: "?"
        val versionCode =
            if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong()
        "$versionName ($versionCode)"
    } catch (_: Throwable) {
        "não instalado"
    }
}

@Composable
fun ArRoute(
    characterId: String?,
    onBack: () -> Unit,
    onOpenInfo: (String) -> Unit,
    onOpenDemo: (String) -> Unit
) {
    val character = remember(characterId) { HistoricalRepository.getCharacter(characterId) }
    ArScreen(
        title = character?.name ?: "RA",
        modelAssetFileLocation = character?.modelAssetFileLocation ?: "models/placeholder.glb",
        modelUrl = character?.modelUrl,
        arScaleToUnits = character?.arScaleToUnits ?: 1.4f,
        arCenterOriginY = character?.arCenterOriginY ?: -0.5f,
        onBack = onBack,
        onOpenInfo = { character?.id?.let(onOpenInfo) },
        onOpenDemo = { character?.id?.let(onOpenDemo) }
    )
}

@Composable
private fun ArScreen(
    title: String,
    modelAssetFileLocation: String,
    modelUrl: String? = null,
    arScaleToUnits: Float,
    arCenterOriginY: Float,
    onBack: () -> Unit,
    onOpenInfo: () -> Unit,
    onOpenDemo: () -> Unit
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val scene = rememberScene(engine)
    val childNodes = rememberNodes()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val context = LocalContext.current
    var instructionsVisible by remember { mutableStateOf(true) }
    var arSceneView: ARSceneView? by remember { mutableStateOf(null) }
    var arError by remember { mutableStateOf<String?>(null) }
    var showDebug by remember { mutableStateOf(false) }
    var trackingState by remember { mutableStateOf<TrackingState?>(null) }
    var trackingFailure by remember { mutableStateOf<TrackingFailureReason?>(null) }
    var hasTrackedPlanes by remember { mutableStateOf(false) }
    var stableTrackingStartNs by remember { mutableLongStateOf(0L) }
    var arCoreAvailability by remember { mutableStateOf<Availability?>(null) }
    val arServicesVersion = remember { getPackageVersion(context, "com.google.ar.core") }
    val gmsCoreVersion = remember { getPackageVersion(context, "com.google.android.gms") }
    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraPermissionDenied by remember { mutableStateOf(false) }

    val requestCameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            cameraPermissionGranted = granted
            cameraPermissionDenied = !granted
        }

    LaunchedEffect(Unit) {
        var availability = ArCoreApk.getInstance().checkAvailability(context)
        while (availability.isTransient) {
            arCoreAvailability = availability
            kotlinx.coroutines.delay(500)
            availability = ArCoreApk.getInstance().checkAvailability(context)
        }
        arCoreAvailability = availability
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionGranted) {
            ARScene(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
                scene = scene,
                childNodes = childNodes,
                lifecycle = lifecycle,
                planeRenderer = true,
                sessionConfiguration = { session, config ->
                    config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                    config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    config.focusMode = Config.FocusMode.AUTO
                    config.depthMode =
                        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                            Config.DepthMode.AUTOMATIC
                        } else {
                            Config.DepthMode.DISABLED
                        }
                    config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
                },
            onSessionCreated = { arError = null },
            onSessionFailed = { exception ->
                arError = formatThrowable(exception)
            },
            onTrackingFailureChanged = { reason -> trackingFailure = reason },
            onSessionUpdated = { session, frame ->
                trackingState = frame.camera.trackingState
                if (trackingState == TrackingState.TRACKING && trackingFailure == null) {
                    if (stableTrackingStartNs == 0L) stableTrackingStartNs = frame.timestamp
                } else {
                    stableTrackingStartNs = 0L
                }
                if (!hasTrackedPlanes) {
                    hasTrackedPlanes =
                        frame.getUpdatedTrackables(Plane::class.java).any { it.trackingState == TrackingState.TRACKING }
                }
            },
                onViewCreated = { arSceneView = this },
                onTouchEvent = { e, _ ->
                    if (arError != null) return@ARScene false
                    if (e.action == android.view.MotionEvent.ACTION_UP) {
                        val startNs = stableTrackingStartNs
                        val currentNs = arSceneView?.frame?.timestamp ?: 0L
                        val isStable = startNs != 0L && currentNs != 0L && (currentNs - startNs) >= 1_500_000_000L
                        if (!isStable) {
                            instructionsVisible = true
                            return@ARScene false
                        }
                        if (!hasTrackedPlanes) {
                            instructionsVisible = true
                            return@ARScene false
                        }
                        val hitResult = arSceneView?.hitTestAR(
                            xPx = e.x,
                            yPx = e.y,
                            planeTypes = setOf(Plane.Type.HORIZONTAL_UPWARD_FACING),
                            predicate = { hr: HitResult ->
                                val plane = hr.trackable as? Plane ?: return@hitTestAR false
                                if (plane.trackingState != TrackingState.TRACKING) return@hitTestAR false
                                if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) return@hitTestAR false
                                if (plane.subsumedBy != null) return@hitTestAR false
                                plane.isPoseInPolygon(hr.hitPose)
                            }
                        )
                        if (hitResult != null) {
                            instructionsVisible = false
                            childNodes.clear()
                            val modelNode = ModelNode(
                                modelInstance = modelLoader.createModelInstance(
                                    assetFileLocation = modelUrl ?: modelAssetFileLocation
                                ),
                                autoAnimate = false,
                                scaleToUnits = arScaleToUnits,
                                centerOrigin = Position(y = arCenterOriginY)
                            )
                            childNodes += AnchorNode(
                                engine = engine,
                                anchor = hitResult.createAnchor()
                            ).addChildNode(modelNode)
                        }
                    }
                    false
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
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
                    text = title,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showDebug = !showDebug }) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (!cameraPermissionGranted) {
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
                            text = "Permissão de câmera necessária",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "A Realidade Aumentada precisa da câmera para funcionar. Autorize a permissão e tente novamente.",
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                        if (cameraPermissionDenied) {
                            Text(
                                text = "Se você marcou “Não perguntar novamente”, habilite a câmera nas configurações do app.",
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                        Button(onClick = { requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text(text = "Permitir câmera")
                        }
                    }
                } else if (arError != null) {
                    val availability = arCoreAvailability
                    val fatalHintVisible =
                        availability == Availability.SUPPORTED_INSTALLED && arError?.contains("FatalException") == true
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
                            text = "Realidade Aumentada indisponível neste dispositivo",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (fatalHintVisible) {
                                "O ARCore está instalado, mas falhou ao iniciar a sessão. Em emuladores isso costuma ser incompatibilidade/bug da imagem do sistema."
                            } else {
                                "O Google Play Services para RA (ARCore) não pode ser instalado/atualizado ou a sessão AR falhou ao iniciar."
                            },
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "ARCore availability: ${availability ?: "?"}",
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Play Services para RA: $arServicesVersion",
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Detalhes: $arError",
                            color = Color.White.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        )
                        if (fatalHintVisible) {
                            Text(
                                text = "Sugestão: crie um AVD Android 14 (API 34) Google Play x86_64 com VirtualScene e reinstale o APK “x86_for_emulator” do Play Services para RA. Depois faça Wipe Data + Cold Boot.",
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(onClick = onBack) {
                                Text(text = "Voltar", color = Color.White)
                            }
                            TextButton(onClick = { arError = null; instructionsVisible = true }) {
                                Text(text = "Tentar novamente", color = Color.White)
                            }
                            TextButton(onClick = onOpenDemo) {
                                Text(text = "Modo Demo", color = Color.White)
                            }
                        }
                    }
                } else if (instructionsVisible) {
                    val isEmulator = Build.FINGERPRINT.contains("generic") || Build.MODEL.contains("sdk")
                    val message = when {
                        trackingState == TrackingState.PAUSED -> "Inicializando RA..."
                        trackingState == TrackingState.STOPPED -> "RA Parada"
                        stableTrackingStartNs == 0L -> "Estabilizando rastreamento..."
                        !hasTrackedPlanes -> if (isEmulator) "Use Alt + WASD e Mouse para mover a câmera e detectar o chão" else "Mova o celular para detectar o chão"
                        else -> "Toque no chão para posicionar ${title}"
                    }
                    
                    Text(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        text = message,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (showDebug) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Debug AR",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "trackingState=${trackingState ?: "?"}",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "trackingFailure=${trackingFailure ?: "null"}",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "stableTrackingStartNs=${if (stableTrackingStartNs == 0L) "0" else "set"}",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "ARCore availability=${arCoreAvailability ?: "?"}",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "Play Services RA=$arServicesVersion",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "GMSCore=$gmsCoreVersion",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "sdk=${Build.VERSION.SDK_INT} abi=${Build.SUPPORTED_ABIS.firstOrNull() ?: "?"}",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "arError=${arError ?: "null"}",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Personagens",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }

                IconButton(onClick = onOpenInfo) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            modifier = Modifier.size(28.dp),
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Info",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(6.dp))
        }
    }
}
