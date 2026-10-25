package app.pillion.core

/**
 * How big apps render inside the off-screen dash display.
 *
 * The dash panel is always 480x240 px, and the helper creates the virtual display at
 * [dashResolution] pixels with a chosen **density** (dpi). Density is what controls apparent text
 * size: at 160 dpi the app sees 480x240 **dp** and lays itself out like a small tablet (normal text);
 * at 320 dpi it sees 240x120 dp and falls back to its compact phone layout, so the same text is
 * drawn twice as large in pixels. Nothing is cropped or zoomed after the fact.
 */
enum class DashTextSize(val dpi: Int) {
    /** 480x240 px = 480x240 dp — the app's widest layout, smallest text, most map visible. */
    Normal(160),

    /** 480x240 px = 384x192 dp — slightly larger text, still plenty of map. */
    Large(200),

    /** 480x240 px = 320x160 dp — noticeably larger text, less map. */
    Huge(240),

    /** 480x240 px = 240x120 dp — phone layout: huge text, the map mostly disappears. */
    Max(320),

    ;

    val label: String
        get() = when (this) {
            Normal -> "Normal"
            Large -> "Large"
            Huge -> "Huge"
            Max -> "Max"
        }

    companion object {
        val DEFAULT = Normal

        fun fromName(name: String?): DashTextSize =
            values().firstOrNull { it.name == name } ?: DEFAULT
    }
}
