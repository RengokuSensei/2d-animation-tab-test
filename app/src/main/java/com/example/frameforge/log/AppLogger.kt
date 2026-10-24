package com.example.frameforge.log

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicLong

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    CRASH
}

data class LogEntry(
    val id: Long,
    val timestamp: Long,
    val level: LogLevel,
    val category: String,
    val message: String,
    val details: String = "",
    val throwable: Throwable? = null
) {
    fun formatDisplay(): String {
        val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(timestamp))
        val lvl = when (level) {
            LogLevel.DEBUG -> "DEBUG"
            LogLevel.INFO -> "INFO "
            LogLevel.WARN -> "WARN "
            LogLevel.ERROR -> "ERROR"
            LogLevel.CRASH -> "CRASH"
        }
        val base = "[$timeStr] [$lvl] [$category] $message"
        val extra = if (details.isNotEmpty()) " -> $details" else ""
        val errorText = throwable?.let { "\n" + AppLogger.getStackTraceString(it) } ?: ""
        return "$base$extra$errorText"
    }
}

/**
 * Robust Persistent Action and Crash Logger ("Logs Taker").
 * Preserves all UI touches, playback, NLE edits, and runtime crashes even if the app crashes mid-session.
 */
object AppLogger {

    private const val TAG = "FrameForgeLogger"
    private const val MAX_MEMORY_LOGS = 1500
    private const val MAX_LOG_FILE_SIZE = 5 * 1024 * 1024L // 5MB

    private val idCounter = AtomicLong(1)
    private val memoryLogBuffer = ConcurrentLinkedDeque<LogEntry>()

    private var appContext: Context? = null
    private var logFile: File? = null
    private var crashFile: File? = null
    private val diskLock = Any()

    /**
     * Initializes logger, sets up disk file, and hooks into JVM uncaught exception handler.
     */
    fun init(application: Application) {
        appContext = application.applicationContext

        try {
            val logsDir = getLogsDirectory(application)
            if (!logsDir.exists()) logsDir.mkdirs()

            logFile = File(logsDir, "app_runtime.log")
            crashFile = File(logsDir, "crash_latest.txt")

            // Rotate log file if it grew too large
            if (logFile?.exists() == true && (logFile?.length() ?: 0) > MAX_LOG_FILE_SIZE) {
                val backup = File(logsDir, "app_runtime.old.log")
                if (backup.exists()) backup.delete()
                logFile?.renameTo(backup)
                logFile = File(logsDir, "app_runtime.log")
            }

            // Install Crash Taker
            val defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    recordCrash(thread, throwable)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to record crash", e)
                } finally {
                    defaultUncaughtHandler?.uncaughtException(thread, throwable)
                }
            }

            i("SYSTEM", "AppLogger initialized. Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AppLogger", e)
        }
    }

    private fun getLogsDirectory(context: Context): File {
        val ext = context.getExternalFilesDir("logs")
        if (ext != null && ext.canWrite()) return ext
        return File(context.filesDir, "logs")
    }

    // --- LOGGING METHODS ---

    fun ui(action: String, details: String = "") {
        append(LogLevel.INFO, "UI", action, details)
    }

    fun touch(action: String, details: String = "") {
        append(LogLevel.DEBUG, "TOUCH", action, details)
    }

    fun playback(action: String, details: String = "") {
        append(LogLevel.INFO, "PLAYBACK", action, details)
    }

    fun video(action: String, details: String = "") {
        append(LogLevel.INFO, "VIDEO", action, details)
    }

    fun timeline(action: String, details: String = "") {
        append(LogLevel.INFO, "TIMELINE", action, details)
    }

    fun engine(action: String, details: String = "") {
        append(LogLevel.DEBUG, "ENGINE", action, details)
    }

    fun d(category: String, message: String, details: String = "") {
        append(LogLevel.DEBUG, category, message, details)
    }

    fun i(category: String, message: String, details: String = "") {
        append(LogLevel.INFO, category, message, details)
    }

    fun w(category: String, message: String, details: String = "") {
        append(LogLevel.WARN, category, message, details)
    }

    fun e(category: String, message: String, throwable: Throwable? = null, details: String = "") {
        append(LogLevel.ERROR, category, message, details, throwable)
    }

    private fun append(
        level: LogLevel,
        category: String,
        message: String,
        details: String = "",
        throwable: Throwable? = null
    ) {
        val entry = LogEntry(
            id = idCounter.getAndIncrement(),
            timestamp = System.currentTimeMillis(),
            level = level,
            category = category,
            message = message,
            details = details,
            throwable = throwable
        )

        // 1. Android Logcat
        val line = entry.formatDisplay()
        when (level) {
            LogLevel.DEBUG -> Log.d(category, message)
            LogLevel.INFO -> Log.i(category, message)
            LogLevel.WARN -> Log.w(category, message)
            LogLevel.ERROR, LogLevel.CRASH -> Log.e(category, message, throwable)
        }

        // 2. In-Memory Ring Buffer
        memoryLogBuffer.addLast(entry)
        while (memoryLogBuffer.size > MAX_MEMORY_LOGS) {
            memoryLogBuffer.pollFirst()
        }

        // 3. Immediate Disk Flush
        try {
            synchronized(diskLock) {
                logFile?.let { file ->
                    FileOutputStream(file, true).use { fos ->
                        val writer = fos.bufferedWriter()
                        writer.appendLine(line)
                        writer.flush()
                        fos.fd.sync() // Force commit to physical storage
                    }
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Crash Taker: Saves complete forensic snapshot to crash_latest.txt and crash_<timestamp>.txt
     */
    private fun recordCrash(thread: Thread, throwable: Throwable) {
        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date(now))
        val stackTrace = getStackTraceString(throwable)

        val report = buildString {
            appendLine("================================================================================")
            appendLine("                   FRAMEFORGE STUDIO CRASH REPORT                              ")
            appendLine("================================================================================")
            appendLine("Timestamp:     $dateStr")
            appendLine("Thread:        ${thread.name} (id: ${thread.id}, priority: ${thread.priority})")
            appendLine("Device:        ${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT}, ${Build.DEVICE})")
            appendLine("Hardware:      ${Build.HARDWARE} / ${Build.BOARD}")
            appendLine("Android OS:    ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            val runtime = Runtime.getRuntime()
            val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
            val maxMem = runtime.maxMemory() / (1024 * 1024)
            appendLine("Memory:        ${usedMem}MB used / ${maxMem}MB max heap")
            appendLine("--------------------------------------------------------------------------------")
            appendLine("EXCEPTION:     ${throwable.javaClass.name}: ${throwable.message}")
            appendLine("--------------------------------------------------------------------------------")
            appendLine(stackTrace)
            appendLine("--------------------------------------------------------------------------------")
            appendLine("RECENT ACTION BREADCRUMBS (Last ${memoryLogBuffer.size} events):")
            appendLine("--------------------------------------------------------------------------------")
            memoryLogBuffer.toList().takeLast(250).forEach { entry ->
                appendLine(entry.formatDisplay())
            }
            appendLine("================================================================================")
        }

        // Also append CRASH entry to regular stream
        append(LogLevel.CRASH, "CRASH", "FATAL UNCAUGHT EXCEPTION on ${thread.name}: ${throwable.message}", throwable = throwable)

        appContext?.let { ctx ->
            val logsDir = getLogsDirectory(ctx)
            val timestampedCrash = File(logsDir, "crash_${now}.txt")
            val latestCrash = File(logsDir, "crash_latest.txt")

            try {
                FileOutputStream(timestampedCrash).use { fos ->
                    fos.write(report.toByteArray(Charsets.UTF_8))
                    fos.flush()
                    fos.fd.sync()
                }
                FileOutputStream(latestCrash).use { fos ->
                    fos.write(report.toByteArray(Charsets.UTF_8))
                    fos.flush()
                    fos.fd.sync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed writing crash files", e)
            }
        }
    }

    // --- ACCESS & UTILITY METHODS ---

    fun getRecentLogs(categoryFilter: String? = null): List<LogEntry> {
        val list = memoryLogBuffer.toList()
        if (categoryFilter.isNullOrEmpty() || categoryFilter == "ALL") return list
        if (categoryFilter == "ERRORS") return list.filter { it.level == LogLevel.ERROR || it.level == LogLevel.CRASH }
        return list.filter { it.category.equals(categoryFilter, ignoreCase = true) }
    }

    fun getAllLogsText(): String {
        return buildString {
            memoryLogBuffer.forEach { appendLine(it.formatDisplay()) }
        }
    }

    fun hasCrashReport(): Boolean {
        return crashFile?.exists() == true && (crashFile?.length() ?: 0) > 0
    }

    fun getLatestCrashReportText(): String? {
        return try {
            if (hasCrashReport()) crashFile?.readText() else null
        } catch (e: Exception) {
            "Failed to read crash report: ${e.message}"
        }
    }

    fun clearLogs() {
        memoryLogBuffer.clear()
        synchronized(diskLock) {
            try {
                logFile?.writeText("")
            } catch (_: Exception) {}
        }
        i("SYSTEM", "Logs cleared by user")
    }

    fun copyLogsToClipboard(context: Context, text: String? = null) {
        val content = text ?: getAllLogsText()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("FrameForge Studio Logs", content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied logs to clipboard", Toast.LENGTH_SHORT).show()
        ui("COPY_LOGS", "Length: ${content.length} chars")
    }

    fun shareLogFile(context: Context) {
        try {
            val targetFile = if (hasCrashReport()) crashFile ?: logFile else logFile
            if (targetFile == null || !targetFile.exists()) {
                Toast.makeText(context, "No log file available to share", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "FrameForge Studio Logs - ${Build.MODEL}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Studio Logs"))
            ui("SHARE_LOGS", targetFile.name)
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing logs: ${e.message}", Toast.LENGTH_LONG).show()
            e("SYSTEM", "Failed sharing logs", e)
        }
    }

    fun getStackTraceString(t: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }
}
