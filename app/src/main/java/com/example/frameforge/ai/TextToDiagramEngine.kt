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

data class TextToDiagramResult(
    val prompt: String,
    val mermaidCode: String,
    val strokes: List<DrawingStroke>,
    val isFromAi: Boolean,
    val message: String
)

object TextToDiagramEngine {

    /**
     * Converts a natural language diagram prompt into a Mermaid diagram and sketchy strokes.
     */
    suspend fun generateDiagram(
        prompt: String,
        originX: Float = 140f,
        originY: Float = 120f
    ): TextToDiagramResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY?.toString() ?: ""

        var mermaidCode: String? = null
        var isFromAi = false

        if (apiKey.isNotBlank() && apiKey != "placeholder") {
            try {
                mermaidCode = queryGeminiForMermaid(prompt, apiKey)
                if (!mermaidCode.isNullOrBlank()) {
                    isFromAi = true
                }
            } catch (e: Exception) {
                // Fall through to offline semantic generator
            }
        }

        if (mermaidCode.isNullOrBlank()) {
            mermaidCode = generateOfflineMermaid(prompt)
            isFromAi = false
        }

        val parsed = MermaidDiagramEngine.parseMermaid(mermaidCode)
        val strokes = MermaidDiagramEngine.renderDiagramToStrokes(
            diagram = parsed,
            originX = originX,
            originY = originY
        )

        TextToDiagramResult(
            prompt = prompt,
            mermaidCode = mermaidCode,
            strokes = strokes,
            isFromAi = isFromAi,
            message = if (isFromAi) "Diagram synthesized by Gemini AI" else "Generated via Smart Diagram Synthesizer"
        )
    }

    private fun queryGeminiForMermaid(prompt: String, apiKey: String): String? {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 30000
        conn.readTimeout = 45000
        conn.doOutput = true

        val systemInstruction = """
            You are an expert system architect and diagram engineer for Excalidraw.
            Given the user's prompt, generate ONLY a valid Mermaid.js flowchart (starting with 'flowchart TD' or 'flowchart LR').
            Use descriptive node labels like A[Client App] --> B{Valid?} --> C[(Database)].
            Use standard node shapes: [Rectangle], (Rounded), {Decision}, [(Database)], ((Circle)).
            Return ONLY the raw Mermaid diagram text without extra conversational commentary.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "System Instruction: $systemInstruction\n\nUser Prompt: $prompt"))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("maxOutputTokens", 1024)
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
                return extractMermaidCode(text)
            }
        }
        return null
    }

    private fun extractMermaidCode(rawText: String): String {
        val trimmed = rawText.trim()
        val codeBlockRegex = Regex("""```(?:mermaid)?\s*([\s\S]*?)```""", RegexOption.IGNORE_CASE)
        val match = codeBlockRegex.find(trimmed)
        if (match != null) {
            return match.groupValues[1].trim()
        }
        return trimmed
    }

    /**
     * Offline heuristic generator for rich diagrams when offline or without API key.
     */
    fun generateOfflineMermaid(prompt: String): String {
        val lower = prompt.lowercase()

        return when {
            lower.contains("auth") || lower.contains("login") || lower.contains("jwt") || lower.contains("token") -> {
                """
                flowchart TD
                    User([Mobile User]) --> Client[Client App]
                    Client -->|1. Submit Login| Gateway[API Gateway]
                    Gateway -->|2. Validate Hash| AuthServer[Authentication Service]
                    AuthServer -->|3. Query| UserDB[(User Accounts DB)]
                    UserDB -->|4. Valid Record| AuthServer
                    AuthServer -->|5. Issue Signed JWT| Client
                    Client -->|6. Bearer Token| Microservice[Protected Microservices]
                """.trimIndent()
            }
            lower.contains("kube") || lower.contains("docker") || lower.contains("container") || lower.contains("deploy") -> {
                """
                flowchart LR
                    Ingress[Ingress Controller] --> Router{Load Balancer}
                    Router --> PodA[App Pod Replica 1]
                    Router --> PodB[App Pod Replica 2]
                    PodA --> Redis[(Redis Cache Cluster)]
                    PodB --> Redis
                    PodA --> Postgres[(PostgreSQL Primary)]
                    PodB --> Postgres
                """.trimIndent()
            }
            lower.contains("stream") || lower.contains("video") || lower.contains("media") -> {
                """
                flowchart TD
                    Creator[Video Creator] --> Ingest[Ingest Server 4K]
                    Ingest --> Transcoder{FFmpeg Transcoding}
                    Transcoder -->|1080p| S3[(Cloud Storage CDN)]
                    Transcoder -->|720p / 480p| S3
                    S3 --> Edge[Global Edge Cache]
                    Edge --> Viewer([End User Player])
                """.trimIndent()
            }
            lower.contains("funnel") || lower.contains("onboard") || lower.contains("growth") -> {
                """
                flowchart TD
                    Visitor([Website Visitor]) --> Landing[Landing Page]
                    Landing --> Signup{Account Signup?}
                    Signup -->|Yes| Onboarding[Guided Tour]
                    Signup -->|No| Retarget[Email Campaign]
                    Onboarding --> Activation[First Animation Created]
                    Activation --> PaidPro([Pro Upgrade])
                """.trimIndent()
            }
            lower.contains("git") || lower.contains("branch") || lower.contains("pr") -> {
                """
                flowchart LR
                    Main[main Branch] --> Feature[Feature Branch]
                    Feature --> Commit[Local Commits]
                    Commit --> PR{Pull Request Review}
                    PR -->|Approved| CI[CI Automation]
                    CI --> Merge[Merge to main]
                """.trimIndent()
            }
            else -> {
                // General purpose intelligent graph synthesized from prompt keywords
                val words = prompt.split(" ", ",", "-").filter { it.length > 3 }.take(4)
                val node1 = words.getOrNull(0)?.replaceFirstChar { it.uppercase() } ?: "Input"
                val node2 = words.getOrNull(1)?.replaceFirstChar { it.uppercase() } ?: "Processor"
                val node3 = words.getOrNull(2)?.replaceFirstChar { it.uppercase() } ?: "Storage"
                val node4 = words.getOrNull(3)?.replaceFirstChar { it.uppercase() } ?: "Output"

                """
                flowchart TD
                    Start([Client Request: $node1]) --> Process[$node2 Engine]
                    Process --> Decision{Validated?}
                    Decision -->|Success| Store[($node3 Data Layer)]
                    Decision -->|Fail| Log[Audit Logging]
                    Store --> Result([Response: $node4])
                """.trimIndent()
            }
        }
    }
}
