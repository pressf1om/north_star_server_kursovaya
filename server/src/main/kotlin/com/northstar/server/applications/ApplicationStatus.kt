package com.northstar.server.applications

object ApplicationStatus {
    const val FREE = "1"
    const val ASSIGNED = "2"

    private val driverChain = mapOf(
        "2" to "3",
        "3" to "4",
        "4" to "5",
        "5" to "6",
        "6" to "7",
        "7" to "8",
        "8" to FREE,
    )

    fun driverNext(current: String?): String? = driverChain[current]

    fun isAllowedCode(code: String): Boolean =
        code.length == 1 && code[0] in '1'..'9'
}
