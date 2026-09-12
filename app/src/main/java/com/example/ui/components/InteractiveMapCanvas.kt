package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MechanicEntity
import com.example.ui.theme.*
import kotlin.math.*

/**
 * Filter mode for mechanics on the interactive map
 */
enum class MapMechanicFilter(val label: String) {
    ALL("Semua"),
    ONLINE("Online"),
    BUSY("Sibuk"),
    OFFLINE("Offline")
}

/**
 * Helper calculation for Haversine distance in kilometers
 */
fun calculateMapDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val dist = r * c
    return round(dist * 10.0) / 10.0
}

/**
 * Calculate estimated arrival time in minutes (based on 25 km/h + 5 mins prep)
 */
fun calculateMapEtaMinutes(distanceKm: Double): Int {
    val minutes = (distanceKm / 25.0 * 60.0).toInt() + 5
    return minutes.coerceIn(4, 120)
}

@Composable
fun InteractiveMapCanvas(
    modifier: Modifier = Modifier,
    customerLat: Double = -6.8887,
    customerLng: Double = 109.6753,
    customerAddress: String = "Pekalongan, Jawa Tengah",
    activeMechanics: List<MechanicEntity> = emptyList(),
    allMechanics: List<MechanicEntity> = emptyList(),
    selectedMechanic: MechanicEntity? = null,
    onSelectMechanic: ((MechanicEntity?) -> Unit)? = null,
    onOrderWithMechanic: ((MechanicEntity) -> Unit)? = null,
    isSearching: Boolean = false,
    routeActive: Boolean = false,
    mechanicEtaMinutes: Int? = null,
    distanceKm: Double? = null,
    enableInteraction: Boolean = true,
    showControls: Boolean = true,
    showFilterTabs: Boolean = true,
    onRecenter: () -> Unit = {},
    onExpandClick: (() -> Unit)? = null
) {
    // 1. Gesture Zoom and Pan state
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // 2. Active status filter
    var selectedFilter by remember { mutableStateOf(MapMechanicFilter.ALL) }

    // Selected mechanic state (internal or forwarded)
    var localSelectedMechanic by remember { mutableStateOf<MechanicEntity?>(selectedMechanic) }
    LaunchedEffect(selectedMechanic) {
        localSelectedMechanic = selectedMechanic
    }

    // Consolidated list of mechanics: prefer allMechanics if provided, else activeMechanics
    val baseMechanicsList = if (allMechanics.isNotEmpty()) allMechanics else activeMechanics

    // Filtered mechanics based on current filter selection
    val displayedMechanics = remember(baseMechanicsList, selectedFilter) {
        when (selectedFilter) {
            MapMechanicFilter.ALL -> baseMechanicsList
            MapMechanicFilter.ONLINE -> baseMechanicsList.filter { it.status == "ACTIVE" }
            MapMechanicFilter.BUSY -> baseMechanicsList.filter { it.status == "BUSY" }
            MapMechanicFilter.OFFLINE -> baseMechanicsList.filter { it.status == "OFFLINE" }
        }
    }

    // Pulsing radar animation for real-time customer GPS beacon
    val infiniteTransition = rememberInfiniteTransition(label = "mapRadarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    // Current focused mechanic for route line: either user-selected, or first active if routeActive
    val routeMechanic = localSelectedMechanic ?: if (routeActive) baseMechanicsList.firstOrNull() else null
    val currentDistance = if (routeMechanic != null) {
        distanceKm ?: calculateMapDistanceKm(customerLat, customerLng, routeMechanic.latitude, routeMechanic.longitude)
    } else distanceKm
    val currentEta = if (currentDistance != null) {
        mechanicEtaMinutes ?: calculateMapEtaMinutes(currentDistance)
    } else mechanicEtaMinutes

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Slate900)
            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
            .pointerInput(enableInteraction) {
                if (enableInteraction) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.5f)
                        panOffset = Offset(
                            x = (panOffset.x + pan.x).coerceIn(-1000f * zoomScale, 1000f * zoomScale),
                            y = (panOffset.y + pan.y).coerceIn(-1000f * zoomScale, 1000f * zoomScale)
                        )
                    }
                }
            }
            .pointerInput(enableInteraction) {
                if (enableInteraction) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            // Double tap to toggle zoom level
                            zoomScale = if (zoomScale > 1.4f) 1.0f else 1.8f
                        },
                        onTap = {
                            // Tap on blank map closes selection callout
                            localSelectedMechanic = null
                            onSelectMechanic?.invoke(null)
                        }
                    )
                }
            }
    ) {
        val boxWidth = constraints.maxWidth.toFloat()
        val boxHeight = constraints.maxHeight.toFloat()
        val center = Offset(boxWidth / 2f, boxHeight / 2f)

        // Map scale multiplier according to current zoom
        val coordScale = 16000f * zoomScale
        val customerScreenPos = Offset(center.x + panOffset.x, center.y + panOffset.y)

        // -------------------------------------------------------------
        // LAYER 1: CANVAS - Roads, Grid, Waterways, Radar Waves & Route
        // -------------------------------------------------------------
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = customerScreenPos.x
            val cy = customerScreenPos.y

            // 1. Waterway / River (Sungai Pekalongan)
            val riverPath = Path().apply {
                moveTo(-100f + panOffset.x * 0.4f, cy - 140f * zoomScale)
                cubicTo(
                    cx - 80f * zoomScale, cy - 100f * zoomScale,
                    cx + 60f * zoomScale, cy - 160f * zoomScale,
                    size.width + 100f, cy - 80f * zoomScale
                )
            }
            drawPath(
                path = riverPath,
                color = Color(0xFF0E3A5A),
                style = Stroke(width = 24f * zoomScale)
            )
            drawPath(
                path = riverPath,
                color = Color(0xFF1E5B88),
                style = Stroke(width = 16f * zoomScale)
            )

            // 2. City Urban Parks (Alun-Alun Pekalongan & Taman Kota)
            val parkCenter = Offset(cx - 30f * zoomScale, cy + 40f * zoomScale)
            drawRoundRect(
                color = Color(0xFF0F3D32),
                topLeft = Offset(parkCenter.x - 35f * zoomScale, parkCenter.y - 25f * zoomScale),
                size = Size(70f * zoomScale, 50f * zoomScale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )

            // 3. Stylized Grid Roads
            val roadColor = Color(0xFF1E293B)
            val mainRoadColor = Color(0xFF334155)
            val highwayColor = Color(0xFF475569)

            val spacing = (60f * zoomScale).coerceAtLeast(30f)
            val offsetXMod = (panOffset.x % spacing)
            val offsetYMod = (panOffset.y % spacing)

            // Secondary Roads Grid
            var yPos = offsetYMod - spacing
            while (yPos <= size.height + spacing) {
                drawLine(
                    color = roadColor,
                    start = Offset(0f, yPos),
                    end = Offset(size.width, yPos),
                    strokeWidth = 2f
                )
                yPos += spacing
            }

            var xPos = offsetXMod - spacing
            while (xPos <= size.width + spacing) {
                drawLine(
                    color = roadColor,
                    start = Offset(xPos, 0f),
                    end = Offset(xPos, size.height),
                    strokeWidth = 2f
                )
                xPos += spacing
            }

            // Primary Arterial Highway (Pantura / Jalan Utama)
            val panturaY = cy + 10f * zoomScale
            drawLine(
                color = highwayColor,
                start = Offset(-100f, panturaY),
                end = Offset(size.width + 100f, panturaY),
                strokeWidth = 8f * zoomScale.coerceIn(0.8f, 2.0f)
            )
            // Highway lane center dashes
            drawLine(
                color = BengkelAmber.copy(alpha = 0.4f),
                start = Offset(-100f, panturaY),
                end = Offset(size.width + 100f, panturaY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
            )

            // Major North-South Avenue (Jl. Hayam Wuruk / Cipto)
            val mainAveX = cx - 15f * zoomScale
            drawLine(
                color = mainRoadColor,
                start = Offset(mainAveX, -100f),
                end = Offset(mainAveX, size.height + 100f),
                strokeWidth = 6f * zoomScale.coerceIn(0.8f, 2.0f)
            )

            // Diagonal Ring Avenue
            drawLine(
                color = Color(0xFF2B3A52),
                start = Offset(0f, cy + 120f * zoomScale),
                end = Offset(size.width, cy - 120f * zoomScale),
                strokeWidth = 5f * zoomScale.coerceIn(0.8f, 2.0f)
            )

            // 4. Radar concentric distance range rings around Customer
            val ring1 = 70f * zoomScale
            val ring2 = 140f * zoomScale
            val ring3 = 210f * zoomScale

            drawCircle(
                color = BengkelBluePrimary.copy(alpha = 0.18f),
                radius = ring1,
                center = customerScreenPos,
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
            )
            drawCircle(
                color = BengkelBluePrimary.copy(alpha = 0.12f),
                radius = ring2,
                center = customerScreenPos,
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
            )
            drawCircle(
                color = BengkelBluePrimary.copy(alpha = 0.08f),
                radius = ring3,
                center = customerScreenPos,
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
            )

            // 5. Pulsing GPS Wave Animation
            if (isSearching || routeActive || true) {
                drawCircle(
                    color = BengkelBlueCyan.copy(alpha = pulseAlpha),
                    radius = pulseRadius * zoomScale,
                    center = customerScreenPos,
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = BengkelBluePrimary.copy(alpha = (pulseAlpha * 0.5f)),
                    radius = (pulseRadius * 1.5f * zoomScale).coerceAtMost(250f),
                    center = customerScreenPos,
                    style = Stroke(width = 1.5f)
                )
            }

            // 6. Draw Directional Route Line if mechanic is selected or active
            if (routeMechanic != null) {
                val dx = ((routeMechanic.longitude - customerLng) * coordScale).toFloat()
                val dy = ((customerLat - routeMechanic.latitude) * coordScale).toFloat()
                val mechPos = Offset(customerScreenPos.x + dx, customerScreenPos.y + dy)

                val routePath = Path().apply {
                    moveTo(mechPos.x, mechPos.y)
                    // Bezier bend representing city road curve
                    val controlX = (mechPos.x + customerScreenPos.x) / 2f + 25f * zoomScale
                    val controlY = (mechPos.y + customerScreenPos.y) / 2f - 25f * zoomScale
                    quadraticTo(controlX, controlY, customerScreenPos.x, customerScreenPos.y)
                }

                // Road outer glow
                val routeGlowColor = if (routeMechanic.status == "ACTIVE") BengkelGreen.copy(alpha = 0.35f)
                else if (routeMechanic.status == "BUSY") BengkelAmber.copy(alpha = 0.35f)
                else Slate400.copy(alpha = 0.25f)

                val routeLineColor = if (routeMechanic.status == "ACTIVE") BengkelGreen
                else if (routeMechanic.status == "BUSY") BengkelAmber
                else Slate400

                drawPath(
                    path = routePath,
                    color = routeGlowColor,
                    style = Stroke(width = 12f)
                )
                drawPath(
                    path = routePath,
                    color = routeLineColor,
                    style = Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                    )
                )
            }
        }

        // -------------------------------------------------------------
        // LAYER 2: INTERACTIVE MARKERS (CUSTOMER & MECHANICS)
        // -------------------------------------------------------------

        // 1. Customer Marker (Interactive Pin with Realtime Coordinates)
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (customerScreenPos.x - 22.dp.toPx()).roundToInt(),
                        (customerScreenPos.y - 22.dp.toPx()).roundToInt()
                    )
                }
                .size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing Halo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BengkelBlueCyan.copy(alpha = 0.3f))
            )
            // Center Pin
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(BengkelBluePrimary)
                    .border(2.dp, Color.White, CircleShape)
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Lokasi Anda",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Customer Label Callout Pill
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (customerScreenPos.x - 55.dp.toPx()).roundToInt(),
                        (customerScreenPos.y - 50.dp.toPx()).roundToInt()
                    )
                }
                .clip(RoundedCornerShape(12.dp))
                .background(Slate900.copy(alpha = 0.9f))
                .border(1.dp, BengkelBlueCyan.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(BengkelBlueCyan)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Lokasi Anda",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. Mechanic Markers
        displayedMechanics.forEach { mechanic ->
            val dx = ((mechanic.longitude - customerLng) * coordScale).toFloat()
            val dy = ((customerLat - mechanic.latitude) * coordScale).toFloat()
            val markerX = customerScreenPos.x + dx
            val markerY = customerScreenPos.y + dy

            val isSelected = localSelectedMechanic?.mechanicId == mechanic.mechanicId
            val mechDist = calculateMapDistanceKm(customerLat, customerLng, mechanic.latitude, mechanic.longitude)

            val statusColor = when (mechanic.status) {
                "ACTIVE" -> BengkelGreen
                "BUSY" -> BengkelAmber
                else -> Slate400
            }

            val statusLabel = when (mechanic.status) {
                "ACTIVE" -> "Online"
                "BUSY" -> "Sibuk"
                else -> "Offline"
            }

            // Mechanic Pin Box
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (markerX - 22.dp.toPx()).roundToInt(),
                            (markerY - 22.dp.toPx()).roundToInt()
                        )
                    }
                    .size(44.dp)
                    .clickable {
                        localSelectedMechanic = mechanic
                        onSelectMechanic?.invoke(mechanic)
                    }
                    .testTag("mechanic_marker_${mechanic.mechanicId}"),
                contentAlignment = Alignment.Center
            ) {
                // Highlight ring if selected
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.35f))
                            .border(2.dp, statusColor, CircleShape)
                    )
                }

                // Inner Marker Pin
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 30.dp else 24.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                        .border(2.dp, Color.White, CircleShape)
                        .shadow(4.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (mechanic.status == "BUSY") Icons.Default.AccessTime else Icons.Default.Build,
                        contentDescription = mechanic.fullName,
                        tint = Color.White,
                        modifier = Modifier.size(if (isSelected) 16.dp else 12.dp)
                    )
                }
            }

            // Mechanic Label Pill with Distance & Status
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (markerX - 45.dp.toPx()).roundToInt(),
                            (markerY + 16.dp.toPx()).roundToInt()
                        )
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Slate800 else Slate900.copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        if (isSelected) statusColor else Slate700,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        localSelectedMechanic = mechanic
                        onSelectMechanic?.invoke(mechanic)
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${mechanic.fullName.split(" ").first()} • ${mechDist}km",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // LAYER 3: TOP OVERLAYS - GPS Status, Filter Tabs & Zoom Controls
        // -------------------------------------------------------------

        // Top Status Header (GPS badge + Filter tabs)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        ) {
            // Live Real-Time GPS Status Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Slate900.copy(alpha = 0.9f))
                    .border(1.dp, Slate700, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BengkelGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GPS: $customerAddress",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Quick Status Filter Tabs (Semua, Online, Sibuk, Offline)
            if (showFilterTabs) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MapMechanicFilter.values().forEach { filter ->
                        val isFilterActive = selectedFilter == filter
                        val filterColor = when (filter) {
                            MapMechanicFilter.ALL -> BengkelBlueCyan
                            MapMechanicFilter.ONLINE -> BengkelGreen
                            MapMechanicFilter.BUSY -> BengkelAmber
                            MapMechanicFilter.OFFLINE -> Slate400
                        }
                        val filterCount = when (filter) {
                            MapMechanicFilter.ALL -> baseMechanicsList.size
                            MapMechanicFilter.ONLINE -> baseMechanicsList.count { it.status == "ACTIVE" }
                            MapMechanicFilter.BUSY -> baseMechanicsList.count { it.status == "BUSY" }
                            MapMechanicFilter.OFFLINE -> baseMechanicsList.count { it.status == "OFFLINE" }
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedFilter = filter }
                                .testTag("map_filter_${filter.name.lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isFilterActive) filterColor else Slate900.copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isFilterActive) filterColor else Slate700
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (filter != MapMechanicFilter.ALL) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (isFilterActive) Color.White else filterColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "${filter.label} ($filterCount)",
                                    fontSize = 10.sp,
                                    color = if (isFilterActive) Color.White else Slate300,
                                    fontWeight = if (isFilterActive) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Right Controls (Zoom In, Zoom Out, Recenter, Fullscreen)
        if (showControls) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Zoom In
                SmallFloatingActionButton(
                    onClick = {
                        zoomScale = (zoomScale * 1.25f).coerceIn(0.6f, 3.5f)
                    },
                    containerColor = Slate800.copy(alpha = 0.95f),
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("map_zoom_in_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Perbesar Peta", modifier = Modifier.size(18.dp))
                }

                // Zoom Out
                SmallFloatingActionButton(
                    onClick = {
                        zoomScale = (zoomScale / 1.25f).coerceIn(0.6f, 3.5f)
                    },
                    containerColor = Slate800.copy(alpha = 0.95f),
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("map_zoom_out_button")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Perkecil Peta", modifier = Modifier.size(18.dp))
                }

                // Recenter to Customer GPS
                SmallFloatingActionButton(
                    onClick = {
                        panOffset = Offset.Zero
                        zoomScale = 1.0f
                        onRecenter()
                    },
                    containerColor = Slate800.copy(alpha = 0.95f),
                    contentColor = BengkelBlueCyan,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("map_recenter_button")
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Pusatkan Lokasi Saya", modifier = Modifier.size(18.dp))
                }

                // Expand / Fullscreen (if callback provided)
                if (onExpandClick != null) {
                    SmallFloatingActionButton(
                        onClick = onExpandClick,
                        containerColor = Slate800.copy(alpha = 0.95f),
                        contentColor = Color.White,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("map_expand_button")
                    ) {
                        Icon(Icons.Default.Fullscreen, contentDescription = "Layar Penuh", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Map Scale Indicator (Bottom-Start)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Slate900.copy(alpha = 0.75f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val scaleLabel = when {
                zoomScale >= 2.0f -> "250 m"
                zoomScale >= 1.2f -> "500 m"
                zoomScale >= 0.8f -> "1 km"
                else -> "2 km"
            }
            Text(
                text = "Scale: $scaleLabel",
                color = Slate400,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // -------------------------------------------------------------
        // LAYER 4: BOTTOM POPUP / CALLOUT FOR SELECTED MECHANIC
        // -------------------------------------------------------------
        if (localSelectedMechanic != null) {
            val selected = localSelectedMechanic!!
            val distance = calculateMapDistanceKm(customerLat, customerLng, selected.latitude, selected.longitude)
            val eta = calculateMapEtaMinutes(distance)

            val statusColor = when (selected.status) {
                "ACTIVE" -> BengkelGreen
                "BUSY" -> BengkelAmber
                else -> Slate400
            }
            val statusTitle = when (selected.status) {
                "ACTIVE" -> "Online (Siap Menerima Panggilan)"
                "BUSY" -> "Sibuk (Sedang Pengerjaan)"
                else -> "Offline (Sedang Istirahat)"
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(14.dp),
                color = Slate900.copy(alpha = 0.96f),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.8f)),
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header: Mechanic Name, Rating & Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(statusColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selected.fullName.take(2).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = selected.fullName,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = BengkelAmber,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${selected.rating} (${selected.reviewCount} ulasan) • ${selected.completedJobsCount} selesai",
                                        color = Slate300,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                localSelectedMechanic = null
                                onSelectMechanic?.invoke(null)
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate400, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status & Distance row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status Badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusTitle,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Distance and ETA Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate800)
                                .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Jarak: $distance km (~$eta mnt)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Skills
                    if (selected.skills.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Keahlian: ${selected.skills}",
                            color = Slate400,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }

                    // Action Button (Panggil Montir Ini)
                    if (onOrderWithMechanic != null && selected.status == "ACTIVE") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onOrderWithMechanic(selected) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("order_selected_mechanic_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BengkelBluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Panggil Montir Ini", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        // If route is active and no mechanic card selected, show route banner
        else if (currentDistance != null && currentEta != null && routeActive) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = RoundedCornerShape(12.dp),
                color = Slate900.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BengkelAmber.copy(alpha = 0.8f)),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = BengkelAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Montir Menuju Lokasi",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Jarak: $currentDistance km",
                                color = Slate400,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BengkelAmberDark)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Tiba dlm ~$currentEta mnt",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Interactive Map Dialog with nearby mechanics list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerExpandedMapDialog(
    customerLat: Double,
    customerLng: Double,
    customerAddress: String,
    allMechanics: List<MechanicEntity>,
    onDismiss: () -> Unit,
    onOrderWithMechanic: (MechanicEntity) -> Unit
) {
    var selectedMechanic by remember { mutableStateOf<MechanicEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf(MapMechanicFilter.ALL) }

    val filteredList = remember(allMechanics, searchQuery, statusFilter, customerLat, customerLng) {
        allMechanics
            .filter { mech ->
                when (statusFilter) {
                    MapMechanicFilter.ALL -> true
                    MapMechanicFilter.ONLINE -> mech.status == "ACTIVE"
                    MapMechanicFilter.BUSY -> mech.status == "BUSY"
                    MapMechanicFilter.OFFLINE -> mech.status == "OFFLINE"
                }
            }
            .filter { mech ->
                searchQuery.isBlank() ||
                mech.fullName.contains(searchQuery, ignoreCase = true) ||
                mech.skills.contains(searchQuery, ignoreCase = true)
            }
            .sortedBy { mech ->
                calculateMapDistanceKm(customerLat, customerLng, mech.latitude, mech.longitude)
            }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Peta Montir Sekitar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = customerAddress,
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
                )
            },
            containerColor = Slate950
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Interactive Map Component filling top section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                ) {
                    InteractiveMapCanvas(
                        modifier = Modifier.fillMaxSize(),
                        customerLat = customerLat,
                        customerLng = customerLng,
                        customerAddress = customerAddress,
                        allMechanics = allMechanics,
                        selectedMechanic = selectedMechanic,
                        onSelectMechanic = { selectedMechanic = it },
                        onOrderWithMechanic = { mech ->
                            onDismiss()
                            onOrderWithMechanic(mech)
                        },
                        showControls = true,
                        showFilterTabs = true
                    )
                }

                // Bottom Panel: Search and List of Nearby Mechanics
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.9f),
                    color = Slate900,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    ) {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daftar Montir Terdekat (${filteredList.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Urutkan: Terdekat",
                                fontSize = 11.sp,
                                color = BengkelBlueCyan,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari nama montir atau keahlian...", fontSize = 12.sp, color = Slate400) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800,
                                focusedBorderColor = BengkelBluePrimary,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Scrollable List of Mechanics
                        androidx.compose.foundation.lazy.LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredList.size) { index ->
                                val mech = filteredList[index]
                                val dist = calculateMapDistanceKm(customerLat, customerLng, mech.latitude, mech.longitude)
                                val eta = calculateMapEtaMinutes(dist)
                                val isSelected = selectedMechanic?.mechanicId == mech.mechanicId

                                val statusColor = when (mech.status) {
                                    "ACTIVE" -> BengkelGreen
                                    "BUSY" -> BengkelAmber
                                    else -> Slate400
                                }
                                val statusLabel = when (mech.status) {
                                    "ACTIVE" -> "Online"
                                    "BUSY" -> "Sibuk"
                                    else -> "Offline"
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedMechanic = mech }
                                        .testTag("expanded_mechanic_card_${mech.mechanicId}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Slate800 else Slate850
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) statusColor else Slate700
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(statusColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = mech.fullName.take(2).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = mech.fullName,
                                                        color = Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(statusColor.copy(alpha = 0.2f))
                                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = statusLabel,
                                                            color = statusColor,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = "★ ${mech.rating} • ${mech.skills}",
                                                    color = Slate300,
                                                    fontSize = 11.sp,
                                                    maxLines = 1
                                                )

                                                Text(
                                                    text = "Jarak: $dist km (~$eta mnt)",
                                                    color = BengkelBlueCyan,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        if (mech.status == "ACTIVE") {
                                            Button(
                                                onClick = {
                                                    onDismiss()
                                                    onOrderWithMechanic(mech)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = BengkelBluePrimary),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text("Pilih", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

val Slate850 = Color(0xFF162032)
val Slate950 = Color(0xFF020617)

