package app.pillion.android

import android.app.Application
import android.os.Build
import app.pillion.core.Logger
import com.smartdevicelink.util.DebugTool
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Application for the Android app. Enables SDL's internal logging and installs a guard against a known
 * SDL library crash (the `RouterServiceMessageEmitter` NPE that fires when the router Messenger drops
 * mid-send during transport churn). Without that guard the uncaught exception kills the whole app.
 *
 * Also records fatal crashes to a file so the diagnostics screen can show the stack trace of a crash
 * that happened during a previous run — the user has no PC and no root to pull logcat.
 */
class PillionApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DebugTool.enableDebugTool()
        Logger.d(
            "device ${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE} " +
                "(SDK ${Build.VERSION.SDK_INT}) · build ${Build.DISPLAY}",
        )
        hydrateLastCrash()
        installCrashRecorder()
        installSdlCrashGuard()
    }

    /** Outer-most handler: persist the trace, then let the SDL guard / default handler decide. */
    private fun installCrashRecorder() {
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
            runCatching {
                val file = File(filesDir, CRASH_FILE)
                if (file.length() > MAX_CRASH_BYTES) file.delete()
                val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                file.appendText(
                    "---- $stamp · thread=${thread.name} · ${ex::class.simpleName}\n" +
                        ex.stackTraceToString() + "\n",
                )
            }
            runCatching { Logger.e("FATAL on ${thread.name}", ex) }
            prev?.uncaughtException(thread, ex)
        }
    }

    /** Surface the previous run's crash at the top of the diagnostics buffer. */
    private fun hydrateLastCrash() {
        runCatching {
            val file = File(filesDir, CRASH_FILE)
            if (!file.exists() || file.length() == 0L) return
            Logger.d("==== crash report from a previous run ====")
            file.readLines().takeLast(120).forEach { Logger.d("  $it") }
            Logger.d("==== end of previous crash report ====")
        }
    }

    private fun installSdlCrashGuard() {
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
            if (isSdlEmitterNpe(thread, ex)) {
                Logger.d("ignored SDL RouterServiceMessageEmitter NPE (transport churn)")
                return@setDefaultUncaughtExceptionHandler
            }
            prev?.uncaughtException(thread, ex)
        }
    }

    private fun isSdlEmitterNpe(thread: Thread, ex: Throwable): Boolean {
        if (ex !is NullPointerException) return false
        if (thread.name?.contains("RouterServiceMessageEmitter") == true) return true
        return ex.stackTrace.any {
            it.className.contains("TransportBroker") || it.className.contains("RouterServiceMessageEmitter")
        }
    }

    private companion object {
        const val CRASH_FILE = "last-crash.txt"
        const val MAX_CRASH_BYTES = 128 * 1024
    }
}

