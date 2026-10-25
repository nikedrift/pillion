package app.pillion.core

import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Logcat plus a bounded in-memory ring buffer, so the diagnostics screen can show what happened
 * without a PC and without root (reading another process's logcat needs READ_LOGS).
 */
actual object Logger {
    actual fun d(message: String) {
        Log.d(TAG, message)
        record("D", message)
    }

    actual fun e(message: String, error: Throwable?) {
        Log.e(TAG, message, error)
        record("E", message + (error?.let { " — " + oneLine(it.stackTraceToString()) } ?: ""))
    }

    actual fun snapshot(): List<String> {
        // Prefer the file: it also holds the previous runs, including the last lines written before a
        // crash or a kill, which the in-memory buffer cannot show.
        val file = sink
        if (file != null) {
            val tail = runCatching {
                if (!file.exists()) emptyList() else file.readLines().takeLast(MAX_LINES)
            }.getOrDefault(emptyList())
            if (tail.isNotEmpty()) return tail
        }
        return synchronized(lines) { lines.toList() }
    }

    actual fun clear() {
        synchronized(lines) { lines.clear() }
        runCatching { sink?.delete() }
    }

    actual fun attachFileSink(path: String?) {
        sink = path?.let { File(it) }
    }

    private fun record(level: String, message: String) {
        val line = "${stamp()} $level ${oneLine(message)}"
        synchronized(lines) {
            if (lines.size >= MAX_LINES) lines.removeAt(0)
            lines.add(line)
        }
        val file = sink ?: return
        runCatching {
            if (file.length() > MAX_FILE_BYTES) {
                val keep = file.readLines().takeLast(MAX_LINES)
                file.writeText(keep.joinToString("\n", postfix = "\n"))
            }
            file.appendText(line + "\n")
        }
    }

    private fun stamp(): String = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())

    private fun oneLine(text: String): String {
        val flat = text.replace('\n', ' ').replace(Regex("\\s+"), " ").trim()
        return if (flat.length <= 600) flat else flat.take(600) + "…"
    }

    private const val TAG = "Pillion"
    private const val MAX_LINES = 400
    private const val MAX_FILE_BYTES = 256L * 1024
    private val lines = mutableListOf<String>()
    @Volatile private var sink: File? = null
}

