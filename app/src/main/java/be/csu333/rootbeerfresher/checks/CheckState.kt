package be.csu333.rootbeerfresher.checks

enum class CheckStatus { PENDING, RUNNING, PASS, FAIL, ERROR }

data class CheckState(
    val id: String,
    val name: String,
    val description: String,
    val status: CheckStatus = CheckStatus.PENDING,
    val finding: String? = null,
    val techLog: String? = null,
    val remediation: String,
    val methods: Set<RootMethod> = emptySet(),
    val isExpanded: Boolean = false
)
