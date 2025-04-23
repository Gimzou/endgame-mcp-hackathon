package application

data class AppConfig(
    val taostatsBaseUrl: String = "https://api.taostats.io",
    val gitHubBaseUrl: String = "https://api.github.com",
    val taostatsApiToken: String = "",
    val gitHubApiToken: String = ""
) {
    companion object {
        fun fromEnv(): AppConfig {
            return AppConfig(
                taostatsBaseUrl = System.getenv("TAOSTATS_BASE_URL") ?: "https://api.taostats.io",
                gitHubBaseUrl = System.getenv("GITHUB_BASE_URL") ?: "https://api.github.com",
                taostatsApiToken = System.getenv("TAOSTATS_API_KEY") ?: "",
                gitHubApiToken = System.getenv("GITHUB_PERSONAL_ACCESS_TOKEN") ?: "",
            )
        }
    }
}
