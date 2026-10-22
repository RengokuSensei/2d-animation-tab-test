package com.example.frameforge.ai

import com.example.BuildConfig
import com.example.frameforge.model.DrawingStroke
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class WireframeToCodeResult(
    val title: String,
    val composeCode: String,
    val isFromAi: Boolean,
    val wireframeStrokes: List<DrawingStroke>,
    val explanation: String
)

enum class WireframePreset(val title: String, val description: String) {
    LOGIN_AUTH("Mobile Login & Onboarding", "Clean authentication flow with email, password, and social buttons"),
    SOCIAL_FEED("Social Media Feed Card", "Content feed card with user avatar, media preview, and interaction buttons"),
    ANALYTICS_DASHBOARD("Analytics & Metrics Dashboard", "KPI summary stat cards, performance charts, and activity list"),
    PRODUCT_CHECKOUT("E-Commerce Product Details", "Hero image header, pricing badge, size selector pills, and Add to Cart"),
    USER_SETTINGS("Settings & Profile Hub", "Account settings with notification switches, sliders, and profile avatar")
}

object WireframeToCodeEngine {

    suspend fun convertWireframeToCompose(
        prompt: String,
        preset: WireframePreset? = null,
        detectedStrokesCount: Int = 0
    ): WireframeToCodeResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY?.toString() ?: ""
        var generatedCode: String? = null
        var isFromAi = false

        val activePreset = preset ?: detectPresetFromPrompt(prompt)

        if (apiKey.isNotBlank() && apiKey != "placeholder") {
            try {
                generatedCode = queryGeminiForCompose(prompt, activePreset, detectedStrokesCount, apiKey)
                if (!generatedCode.isNullOrBlank()) {
                    isFromAi = true
                }
            } catch (e: Exception) {
                // Fall back to offline code generator
            }
        }

        if (generatedCode.isNullOrBlank()) {
            generatedCode = getOfflineComposeTemplate(activePreset)
            isFromAi = false
        }

        // Generate sketched wireframe vector strokes for the canvas
        val strokes = generateWireframeSketchedStrokes(activePreset, 200f, 100f)

        WireframeToCodeResult(
            title = activePreset.title,
            composeCode = generatedCode,
            isFromAi = isFromAi,
            wireframeStrokes = strokes,
            explanation = if (isFromAi) "Generated with Gemini 3.5 Flash from sketch wireframe" else "Compiled from verified Material 3 Jetpack Compose template"
        )
    }

    private fun detectPresetFromPrompt(prompt: String): WireframePreset {
        val lower = prompt.lowercase()
        return when {
            lower.contains("login") || lower.contains("auth") || lower.contains("signin") || lower.contains("signup") ->
                WireframePreset.LOGIN_AUTH
            lower.contains("social") || lower.contains("feed") || lower.contains("post") || lower.contains("card") ->
                WireframePreset.SOCIAL_FEED
            lower.contains("metric") || lower.contains("dashboard") || lower.contains("stat") || lower.contains("analytics") ->
                WireframePreset.ANALYTICS_DASHBOARD
            lower.contains("product") || lower.contains("shop") || lower.contains("cart") || lower.contains("store") ->
                WireframePreset.PRODUCT_CHECKOUT
            else ->
                WireframePreset.USER_SETTINGS
        }
    }

    private fun queryGeminiForCompose(
        prompt: String,
        preset: WireframePreset,
        strokeCount: Int,
        apiKey: String
    ): String? {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 30000
        conn.readTimeout = 45000
        conn.doOutput = true

        val systemInstruction = """
            You are a Principal Android UI Engineer and Jetpack Compose expert implementing Excalidraw's 'Wireframe to Code' (Make Real) feature.
            Analyze the user's wireframe description / sketch details and generate production-ready, beautiful, complete Jetpack Compose code.
            Requirements:
            - Use Material Design 3 (M3) components (Card, Button, OutlinedTextField, Scaffold, TopAppBar, Icon, etc.).
            - Include Modifier.testTag() on all interactive buttons and text fields.
            - Ensure state is properly managed with remember and mutableStateOf.
            - Provide a clean @Composable function and a @Preview.
            - Return ONLY the Kotlin code enclosed in a ```kotlin ``` block without extra chatter.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        val userContext = "User Wireframe: $prompt\nPreset Type: ${preset.title}\nCanvas Strokes Count: $strokeCount"
                        put(JSONObject().put("text", "System Instruction: $systemInstruction\n\n$userContext"))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("maxOutputTokens", 2048)
            })
        }

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(jsonBody.toString())
            writer.flush()
        }

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val jsonResponse = JSONObject(responseText)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                return extractKotlinCode(text)
            }
        }
        return null
    }

    private fun extractKotlinCode(rawText: String): String {
        val regex = Regex("""```(?:kotlin)?\s*([\s\S]*?)```""", RegexOption.IGNORE_CASE)
        val match = regex.find(rawText)
        return match?.groupValues?.get(1)?.trim() ?: rawText.trim()
    }

    /**
     * Generates hand-drawn vector wireframe outlines on the canvas
     */
    fun generateWireframeSketchedStrokes(preset: WireframePreset, originX: Float, originY: Float): List<DrawingStroke> {
        val strokes = mutableListOf<DrawingStroke>()
        val phoneWidth = 340f
        val phoneHeight = 580f
        val strokeColor = 0xFFFFFFFF.toInt()
        val accentColor = 0xFF38BDF8.toInt()

        // 1. Phone Bezel Outline
        strokes.addAll(
            RoughShapeGenerator.createRoughRectangle(
                x = originX,
                y = originY,
                width = phoneWidth,
                height = phoneHeight,
                roughness = 1.0f,
                strokeColorArgb = strokeColor,
                strokeWidth = 3.5f
            )
        )

        // 2. Camera notch pill
        strokes.addAll(
            RoughShapeGenerator.createRoughRectangle(
                x = originX + phoneWidth / 2f - 30f,
                y = originY + 12f,
                width = 60f,
                height = 12f,
                roughness = 0.8f,
                strokeColorArgb = strokeColor,
                strokeWidth = 2f
            )
        )

        when (preset) {
            WireframePreset.LOGIN_AUTH -> {
                // Header Title
                strokes.addAll(RoughShapeGenerator.createSketchedLabel("Welcome Back", originX + phoneWidth / 2f, originY + 80f, accentColor))
                // Email Field
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 24f, originY + 140f, phoneWidth - 48f, 50f, 0.9f, strokeColorArgb = strokeColor, strokeWidth = 2.5f))
                strokes.addAll(RoughShapeGenerator.createSketchedLabel("Email Address", originX + 90f, originY + 165f, 0xFF94A3B8.toInt()))
                // Password Field
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 24f, originY + 210f, phoneWidth - 48f, 50f, 0.9f, strokeColorArgb = strokeColor, strokeWidth = 2.5f))
                strokes.addAll(RoughShapeGenerator.createSketchedLabel("Password", originX + 75f, originY + 235f, 0xFF94A3B8.toInt()))
                // Primary Action Button (Filled)
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 24f, originY + 290f, phoneWidth - 48f, 52f, 1.0f, RoughFillStyle.HACHURE, strokeColorArgb = accentColor, fillColorArgb = accentColor, strokeWidth = 3f))
                strokes.addAll(RoughShapeGenerator.createSketchedLabel("Sign In", originX + phoneWidth / 2f, originY + 316f, 0xFFFFFFFF.toInt()))
                // Social buttons row
                strokes.addAll(RoughShapeGenerator.createRoughEllipse(originX + 90f, originY + 400f, 22f, 22f, 1.0f, strokeColorArgb = strokeColor))
                strokes.addAll(RoughShapeGenerator.createRoughEllipse(originX + 170f, originY + 400f, 22f, 22f, 1.0f, strokeColorArgb = strokeColor))
                strokes.addAll(RoughShapeGenerator.createRoughEllipse(originX + 250f, originY + 400f, 22f, 22f, 1.0f, strokeColorArgb = strokeColor))
            }

            WireframePreset.SOCIAL_FEED -> {
                // App Top Bar
                strokes.addAll(RoughShapeGenerator.createSketchedLabel("FrameFeed", originX + 70f, originY + 50f, accentColor))
                // Card 1
                val cardY = originY + 90f
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 16f, cardY, phoneWidth - 32f, 260f, 1.0f, strokeColorArgb = strokeColor, strokeWidth = 2.5f))
                // User Avatar
                strokes.addAll(RoughShapeGenerator.createRoughEllipse(originX + 45f, cardY + 32f, 16f, 16f, 1.0f, strokeColorArgb = accentColor))
                strokes.addAll(RoughShapeGenerator.createSketchedLabel("Alex Rivera", originX + 110f, cardY + 32f, strokeColor))
                // Image Placeholder
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 32f, cardY + 60f, phoneWidth - 64f, 140f, 1.0f, RoughFillStyle.CROSS_HATCH, strokeColorArgb = 0xFF64748B.toInt(), fillColorArgb = 0x3364748B.toInt(), strokeWidth = 2f))
                // Heart & Comment icons
                strokes.addAll(RoughShapeGenerator.createRoughEllipse(originX + 48f, cardY + 225f, 12f, 12f, 0.9f, strokeColorArgb = 0xFFEF4444.toInt()))
                strokes.addAll(RoughShapeGenerator.createRoughEllipse(originX + 85f, cardY + 225f, 12f, 12f, 0.9f, strokeColorArgb = strokeColor))
            }

            WireframePreset.ANALYTICS_DASHBOARD -> {
                // Title
                strokes.addAll(RoughShapeGenerator.createSketchedLabel("Analytics Overview", originX + phoneWidth / 2f, originY + 55f, strokeColor))
                // Stat Card 1
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 18f, originY + 85f, 140f, 95f, 1.0f, RoughFillStyle.HACHURE, strokeColorArgb = accentColor, fillColorArgb = 0x3338BDF8.toInt()))
                strokes.addAll(RoughShapeGenerator.createSketchedLabel("+24.8%", originX + 88f, originY + 130f, accentColor))
                // Stat Card 2
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 178f, originY + 85f, 140f, 95f, 1.0f, RoughFillStyle.HACHURE, strokeColorArgb = 0xFF10B981.toInt(), fillColorArgb = 0x3310B981.toInt()))
                strokes.addAll(RoughShapeGenerator.createSketchedLabel("$18,420", originX + 248f, originY + 130f, 0xFF10B981.toInt()))
                // Big Chart Box
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 18f, originY + 200f, phoneWidth - 36f, 160f, 1.0f, strokeColorArgb = strokeColor, strokeWidth = 2.5f))
                // Chart Trend Line
                strokes.addAll(RoughShapeGenerator.createRoughLine(originX + 35f, originY + 310f, originX + 100f, originY + 280f, 1.2f, accentColor, 3f))
                strokes.addAll(RoughShapeGenerator.createRoughLine(originX + 100f, originY + 280f, originX + 180f, originY + 320f, 1.2f, accentColor, 3f))
                strokes.addAll(RoughShapeGenerator.createRoughLine(originX + 180f, originY + 320f, originX + 250f, originY + 240f, 1.2f, accentColor, 3f))
                strokes.addAll(RoughShapeGenerator.createRoughLine(originX + 250f, originY + 240f, originX + 300f, originY + 225f, 1.2f, accentColor, 3f))
            }

            WireframePreset.PRODUCT_CHECKOUT -> {
                // Product Image Box
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 20f, originY + 60f, phoneWidth - 40f, 200f, 1.0f, RoughFillStyle.HACHURE, strokeColorArgb = strokeColor, fillColorArgb = 0x33CBD5E1.toInt()))
                // Product Title & Price
                strokes.addAll(RoughShapeGenerator.createSketchedLabel("Pro Wireless Headphones", originX + 120f, originY + 285f, strokeColor))
                strokes.addAll(RoughShapeGenerator.createSketchedLabel("$299.00", originX + phoneWidth - 60f, originY + 285f, accentColor))
                // Size Pills
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 24f, originY + 330f, 60f, 36f, 0.9f, strokeColorArgb = strokeColor))
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 96f, originY + 330f, 60f, 36f, 0.9f, RoughFillStyle.SOLID, strokeColorArgb = accentColor, fillColorArgb = accentColor))
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 168f, originY + 330f, 60f, 36f, 0.9f, strokeColorArgb = strokeColor))
                // Add to Cart Button
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 24f, originY + 420f, phoneWidth - 48f, 54f, 1.0f, RoughFillStyle.HACHURE, strokeColorArgb = accentColor, fillColorArgb = accentColor, strokeWidth = 3f))
                strokes.addAll(RoughShapeGenerator.createSketchedLabel("Add to Cart", originX + phoneWidth / 2f, originY + 447f, 0xFFFFFFFF.toInt()))
            }

            WireframePreset.USER_SETTINGS -> {
                // Profile Avatar & Name
                strokes.addAll(RoughShapeGenerator.createRoughEllipse(originX + phoneWidth / 2f, originY + 90f, 36f, 36f, 1.0f, strokeColorArgb = accentColor))
                strokes.addAll(RoughShapeGenerator.createSketchedLabel("Jordan Smith", originX + phoneWidth / 2f, originY + 150f, strokeColor))
                // Setting Row 1 (Push Notifications with Switch)
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 18f, originY + 190f, phoneWidth - 36f, 52f, 0.9f, strokeColorArgb = strokeColor))
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + phoneWidth - 65f, originY + 204f, 36f, 22f, 0.8f, RoughFillStyle.SOLID, strokeColorArgb = 0xFF10B981.toInt(), fillColorArgb = 0xFF10B981.toInt()))
                // Setting Row 2 (Dark Mode Switch)
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 18f, originY + 255f, phoneWidth - 36f, 52f, 0.9f, strokeColorArgb = strokeColor))
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + phoneWidth - 65f, originY + 269f, 36f, 22f, 0.8f, RoughFillStyle.SOLID, strokeColorArgb = accentColor, fillColorArgb = accentColor))
                // Setting Row 3 (Audio Volume Slider)
                strokes.addAll(RoughShapeGenerator.createRoughRectangle(originX + 18f, originY + 320f, phoneWidth - 36f, 52f, 0.9f, strokeColorArgb = strokeColor))
                strokes.addAll(RoughShapeGenerator.createRoughLine(originX + 40f, originY + 346f, originX + 220f, originY + 346f, 0.6f, accentColor, 3f))
                strokes.addAll(RoughShapeGenerator.createRoughEllipse(originX + 180f, originY + 346f, 8f, 8f, 0.8f, strokeColorArgb = accentColor))
            }
        }

        return strokes
    }

    private fun getOfflineComposeTemplate(preset: WireframePreset): String {
        return when (preset) {
            WireframePreset.LOGIN_AUTH -> """
                package com.example.app.ui

                import androidx.compose.foundation.layout.*
                import androidx.compose.foundation.shape.RoundedCornerShape
                import androidx.compose.material3.*
                import androidx.compose.runtime.*
                import androidx.compose.ui.Alignment
                import androidx.compose.ui.Modifier
                import androidx.compose.ui.platform.testTag
                import androidx.compose.ui.text.font.FontWeight
                import androidx.compose.ui.text.input.PasswordVisualTransformation
                import androidx.compose.ui.tooling.preview.Preview
                import androidx.compose.ui.unit.dp
                import androidx.compose.ui.unit.sp

                @Composable
                fun LoginScreen(
                    onLoginClick: (String, String) -> Unit = { _, _ -> },
                    modifier: Modifier = Modifier
                ) {
                    var email by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }

                    Surface(
                        modifier = modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Welcome Back",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sign in to access your animated projects",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_input")
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            Button(
                                onClick = { onLoginClick(email, password) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("login_submit_button")
                            ) {
                                Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                @Preview(showBackground = true)
                @Composable
                fun LoginScreenPreview() {
                    MaterialTheme {
                        LoginScreen()
                    }
                }
            """.trimIndent()

            WireframePreset.SOCIAL_FEED -> """
                package com.example.app.ui

                import androidx.compose.foundation.background
                import androidx.compose.foundation.layout.*
                import androidx.compose.foundation.lazy.LazyColumn
                import androidx.compose.foundation.shape.CircleShape
                import androidx.compose.foundation.shape.RoundedCornerShape
                import androidx.compose.material.icons.Icons
                import androidx.compose.material.icons.filled.Favorite
                import androidx.compose.material.icons.filled.Share
                import androidx.compose.material.icons.outlined.ChatBubbleOutline
                import androidx.compose.material3.*
                import androidx.compose.runtime.*
                import androidx.compose.ui.Alignment
                import androidx.compose.ui.Modifier
                import androidx.compose.ui.draw.clip
                import androidx.compose.ui.graphics.Color
                import androidx.compose.ui.platform.testTag
                import androidx.compose.ui.text.font.FontWeight
                import androidx.compose.ui.tooling.preview.Preview
                import androidx.compose.ui.unit.dp
                import androidx.compose.ui.unit.sp

                @Composable
                fun SocialFeedCard(
                    authorName: String = "Alex Rivera",
                    authorHandle: String = "@alex_animates",
                    caption: String = "Working on the 4K keyframe action sequence in FrameForge! 🎨✨",
                    likesCount: Int = 142,
                    commentsCount: Int = 28,
                    modifier: Modifier = Modifier
                ) {
                    var isLiked by remember { mutableStateOf(false) }

                    ElevatedCard(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .testTag("social_feed_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .testTag("avatar_circle")
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(authorName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(authorHandle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(caption, style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(12.dp))
                            // Media Preview Canvas Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E293B))
                                    .testTag("media_preview_box")
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            // Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { isLiked = !isLiked },
                                        modifier = Modifier.testTag("like_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Favorite,
                                            contentDescription = "Like",
                                            tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text("${'$'}{likesCount + if (isLiked) 1 else 0}", fontSize = 13.sp)

                                    Spacer(modifier = Modifier.width(16.dp))
                                    IconButton(onClick = {}, modifier = Modifier.testTag("comment_button")) {
                                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comments")
                                    }
                                    Text("${'$'}commentsCount", fontSize = 13.sp)
                                }

                                IconButton(onClick = {}, modifier = Modifier.testTag("share_button")) {
                                    Icon(Icons.Default.Share, contentDescription = "Share")
                                }
                            }
                        }
                    }
                }

                @Preview(showBackground = true)
                @Composable
                fun SocialFeedCardPreview() {
                    MaterialTheme {
                        SocialFeedCard()
                    }
                }
            """.trimIndent()

            WireframePreset.ANALYTICS_DASHBOARD -> """
                package com.example.app.ui

                import androidx.compose.foundation.layout.*
                import androidx.compose.foundation.shape.RoundedCornerShape
                import androidx.compose.material3.*
                import androidx.compose.runtime.Composable
                import androidx.compose.ui.Modifier
                import androidx.compose.ui.platform.testTag
                import androidx.compose.ui.text.font.FontWeight
                import androidx.compose.ui.tooling.preview.Preview
                import androidx.compose.ui.unit.dp
                import androidx.compose.ui.unit.sp

                @Composable
                fun AnalyticsDashboard(modifier: Modifier = Modifier) {
                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Project Analytics",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                title = "Render Speed",
                                value = "60 FPS",
                                change = "+15% 4K HW",
                                modifier = Modifier.weight(1f).testTag("kpi_render_speed")
                            )
                            MetricCard(
                                title = "Active Layers",
                                value = "12 Tracks",
                                change = "Keyframed",
                                modifier = Modifier.weight(1f).testTag("kpi_active_layers")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .testTag("timeline_performance_chart"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Frame Timeline Exposure", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("Real-time GPU hardware compositing active", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                @Composable
                fun MetricCard(
                    title: String,
                    value: String,
                    change: String,
                    modifier: Modifier = Modifier
                ) {
                    Card(
                        modifier = modifier,
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(title, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(change, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                @Preview(showBackground = true)
                @Composable
                fun AnalyticsDashboardPreview() {
                    MaterialTheme { AnalyticsDashboard() }
                }
            """.trimIndent()

            WireframePreset.PRODUCT_CHECKOUT -> """
                package com.example.app.ui

                import androidx.compose.foundation.background
                import androidx.compose.foundation.layout.*
                import androidx.compose.foundation.shape.RoundedCornerShape
                import androidx.compose.material.icons.Icons
                import androidx.compose.material.icons.filled.ShoppingCart
                import androidx.compose.material3.*
                import androidx.compose.runtime.*
                import androidx.compose.ui.Alignment
                import androidx.compose.ui.Modifier
                import androidx.compose.ui.draw.clip
                import androidx.compose.ui.graphics.Color
                import androidx.compose.ui.platform.testTag
                import androidx.compose.ui.text.font.FontWeight
                import androidx.compose.ui.tooling.preview.Preview
                import androidx.compose.ui.unit.dp
                import androidx.compose.ui.unit.sp

                @Composable
                fun ProductDetailsScreen(
                    onAddToCart: () -> Unit = {},
                    modifier: Modifier = Modifier
                ) {
                    var selectedColorIndex by remember { mutableStateOf(0) }
                    val colorOptions = listOf("Space Gray", "Midnight Blue", "Silver White")

                    Scaffold(
                        modifier = modifier.fillMaxSize(),
                        bottomBar = {
                            Surface(
                                tonalElevation = 8.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Total Price", style = MaterialTheme.typography.labelSmall)
                                        Text("$299.00", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Button(
                                        onClick = onAddToCart,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("add_to_cart_button")
                                    ) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add to Cart")
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1E293B))
                                    .testTag("product_hero_image")
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Pro Stylus Studio Edition", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("4096 pressure levels, tilt sensing, zero-lag Bluetooth low-energy precision.", style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(20.dp))
                            Text("Select Color Variant", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                colorOptions.forEachIndexed { index, name ->
                                    FilterChip(
                                        selected = selectedColorIndex == index,
                                        onClick = { selectedColorIndex = index },
                                        label = { Text(name) },
                                        modifier = Modifier.testTag("chip_color_${'$'}index")
                                    )
                                }
                            }
                        }
                    }
                }

                @Preview(showBackground = true)
                @Composable
                fun ProductDetailsPreview() {
                    MaterialTheme { ProductDetailsScreen() }
                }
            """.trimIndent()

            WireframePreset.USER_SETTINGS -> """
                package com.example.app.ui

                import androidx.compose.foundation.layout.*
                import androidx.compose.foundation.shape.RoundedCornerShape
                import androidx.compose.material3.*
                import androidx.compose.runtime.*
                import androidx.compose.ui.Alignment
                import androidx.compose.ui.Modifier
                import androidx.compose.ui.platform.testTag
                import androidx.compose.ui.text.font.FontWeight
                import androidx.compose.ui.tooling.preview.Preview
                import androidx.compose.ui.unit.dp
                import androidx.compose.ui.unit.sp

                @Composable
                fun UserSettingsScreen(modifier: Modifier = Modifier) {
                    var notificationsEnabled by remember { mutableStateOf(true) }
                    var darkModeEnabled by remember { mutableStateOf(true) }
                    var audioVolume by remember { mutableStateOf(0.85f) }

                    Surface(
                        modifier = modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Settings & Studio Preferences",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            SettingSwitchRow(
                                title = "Real-time Push Notifications",
                                subtitle = "Receive render exports and collaboration updates",
                                isChecked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                testTag = "setting_notifications"
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            SettingSwitchRow(
                                title = "OLED Dark Glass Theme",
                                subtitle = "Battery saving true black canvas frame",
                                isChecked = darkModeEnabled,
                                onCheckedChange = { darkModeEnabled = it },
                                testTag = "setting_dark_mode"
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text("Audio Playback Volume (${'$'}{(audioVolume * 100).toInt()}%)", fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = audioVolume,
                                onValueChange = { audioVolume = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("volume_slider")
                            )
                        }
                    }
                }

                @Composable
                fun SettingSwitchRow(
                    title: String,
                    subtitle: String,
                    isChecked: Boolean,
                    onCheckedChange: (Boolean) -> Unit,
                    testTag: String
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag(testTag)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = isChecked, onCheckedChange = onCheckedChange)
                        }
                    }
                }

                @Preview(showBackground = true)
                @Composable
                fun UserSettingsPreview() {
                    MaterialTheme { UserSettingsScreen() }
                }
            """.trimIndent()
        }
    }
}
