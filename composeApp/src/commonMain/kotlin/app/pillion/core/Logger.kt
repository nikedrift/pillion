package app.pillion.core

/** Minimal multiplatform logger (Android: logcat tag "Pillion"). */
expect object Logger {
    fun d(message: String)
    fun e(message: String, error: Throwable? = null)

    /**
     * Most recent lines, oldest first, for the in-app diagnostics screen. Bounded ring buffer on
     * Android (a few hundred lines); empty on platforms without a buffer.
     */
    fun snapshot(): List<String>

    /** Drop the buffered lines (the diagnostics screen's "Clear" action). */
    fun clear()

    /**
     * Mirror every line to [path] so the log survives process death — a kill (OOM/aggressive OEM
     * task killer) or a native crash leaves no uncaught exception, and an in-memory buffer would be
     * lost. Null detaches. No-op on platforms without a file sink.
     */
    fun attachFileSink(path: String?)
}
