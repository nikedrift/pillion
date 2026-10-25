package app.pillion.core

/**
 * Persists user preferences across launches. The UI depends on this abstraction (DIP); platforms
 * provide it (Android: SharedPreferences). A null store simply means "use defaults" (e.g. previews).
 */
interface SettingsStore {
    fun themeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)

    /** Whether the user has opted into "dedicated dash display" mode (completed onboarding). */
    fun dashEnabled(): Boolean
    fun setDashEnabled(enabled: Boolean)

    /** Virtual display resolution for the dedicated dash helper. */
    fun dashResolution(): DashResolution
    fun setDashResolution(resolution: DashResolution)

    /** Virtual display density for the dedicated dash helper (how big apps render). */
    fun dashTextSize(): DashTextSize
    fun setDashTextSize(size: DashTextSize)

    /** The head-unit the user picked at onboarding ([app.pillion.core.headunit.HeadUnitProfile.id]),
     *  or null if they haven't chosen yet (→ show the bike-selection screen). */
    fun selectedBikeId(): String?
    fun setSelectedBikeId(id: String)
}
