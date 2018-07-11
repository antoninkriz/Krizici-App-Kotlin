package eu.antoninkriz.krizici.utils

object Consts {
    /**
     * Server timeout in milliseconds
     */
    const val SERVER_TIMEOUT: Int = 3000

    /**
     * URL pointing to the 'supl' website
     */
    const val URL_SUPL: String = "https://nastenka.skolakrizik.cz"

    /**
     * URL pointing to blank page
     */
    const val URL_BLANK: String = "about:blank"

    /**
     * Template of URL pointing to the JSON files
     */
    const val URL_SERVER_JSON: String = "https://files.antoninkriz.eu/apps/krizici/%s.json"

    /**
     * Template of URL pointing to specific image
     */
    val URL_SERVER_IMG: (String) -> String = { input -> "https://files.antoninkriz.eu/apps/krizici/img$input-%s.png" }

    enum class TABS(val value: String) {
        CLASSES("tridy"),
        TEACHERS("ucitele"),
        CLASSROOMS("ucebny")
    }
}