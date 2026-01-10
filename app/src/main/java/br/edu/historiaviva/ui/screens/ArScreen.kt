package br.edu.historiaviva.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.edu.historiaviva.data.HistoricalRepository
import androidx.lifecycle.compose.LocalLifecycleOwner
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

@Composable
fun ArRoute(
    characterId: String?,
    onBack: () -> Unit,
    onOpenInfo: (String) -> Unit
) {
    val character = remember(characterId) { HistoricalRepository.getCharacter(characterId) }
    ArScreen(
        title = character?.name ?: "RA",
        modelAssetFileLocation = character?.modelAssetFileLocation ?: "models/placeholder.glb",
        onBack = onBack,
        onOpenInfo = { character?.id?.let(onOpenInfo) }
    )
}

@Composable
private fun ArScreen(
    title: String,
    modelAssetFileLocation: String,
    onBack: () -> Unit,
    onOpenInfo: () -> Unit
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val scene = rememberScene(engine)
    val childNodes = rememberNodes()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var instructionsVisible by remember { mutableStateOf(true) }
    var arSceneView: ARSceneView? by remember { mutableStateOf(null) }
    var arError by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                config.depthMode =
                    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                        Config.DepthMode.AUTOMATIC
                    } else {
                        Config.DepthMode.DISABLED
                    }
                config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
            },
            onSessionCreated = { arError = null },
            onSessionFailed = { exception ->
                arError = exception.message ?: "Não foi possível iniciar a Realidade Aumentada."
            },
            onViewCreated = { arSceneView = this },
            onTouchEvent = { e, _ ->
                if (arError != null) return@ARScene false
                if (e.action == android.view.MotionEvent.ACTION_UP) {
                    val hitResult = arSceneView?.hitTestAR(
                        xPx = e.x,
                        yPx = e.y,
                        planeTypes = setOf(Plane.Type.HORIZONTAL_UPWARD_FACING)
                    )
                    if (hitResult != null) {
                        instructionsVisible = false
                        childNodes.clear()
                        val modelNode = ModelNode(
                            modelInstance = modelLoader.createModelInstance(
                                assetFileLocation = modelAssetFileLocation
                            ),
                            scaleToUnits = 0.5f,
                            centerOrigin = Position(y = -1.0f)
                        ).apply {
                            isEditable = true
                        }
                        childNodes += AnchorNode(
                            engine = engine,
                            anchor = hitResult.createAnchor()
                        ).apply {
                            isEditable = true
                        }.addChildNode(modelNode)
                    }
                }
                false
            }
        )

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
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (arError != null) {
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
                            text = "O Google Play Services para RA (ARCore) não pode ser instalado/atualizado. Isso geralmente acontece quando o aparelho (ou emulador) não é compatível.",
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(onClick = onBack) {
                                Text(text = "Voltar", color = Color.White)
                            }
                            TextButton(onClick = { arError = null; instructionsVisible = true }) {
                                Text(text = "Tentar novamente", color = Color.White)
                            }
                        }
                    }
                } else if (instructionsVisible) {
                    Text(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        text = "Toque em uma superfície para posicionar o personagem",
                        color = Color.White,
                        textAlign = TextAlign.Center
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
