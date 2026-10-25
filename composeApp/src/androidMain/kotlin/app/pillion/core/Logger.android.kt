package app.pillion.core

import android.util.Log
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

    actual fun snapshot(): List<String> = synchronized(lines) { lines.toList() }

    actual fun clear() {
        synchronized(lines) { lines.clear() }
    }

    private fun record(level: String, message: String) {
        val line = "${stamp()} $level ${oneLine(message)}"
        synchronized(lines) {
            if (lines.size >= MAX_LINES) lines.removeAt(0)
            lines.add(line)
        }
    }

    private fun stamp(): String = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())

    private fun oneLine(text: String): String {
        val flat = text.replace('\n', ' ').replace(Regex("\\s+"), " ").trim()
        return if (flat.length <= 600) flat else flat.take(600) + "…"
    }

    private const val TAG = "Pillion"
    private const val MAX_LINES = 400
    private val lines = mutableListOf<String>()
}

