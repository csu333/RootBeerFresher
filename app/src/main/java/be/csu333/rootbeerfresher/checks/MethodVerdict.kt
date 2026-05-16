package be.csu333.rootbeerfresher.checks

enum class MethodVerdict { PASS, ROOTED }

fun methodVerdict(checks: List<CheckState>, method: RootMethod): MethodVerdict? {
    val relevant = checks.filter { method in it.methods }
    if (relevant.isEmpty()) return null
    if (relevant.any { it.status == CheckStatus.PENDING || it.status == CheckStatus.RUNNING }) return null
    return if (relevant.any { it.status == CheckStatus.FAIL || it.status == CheckStatus.ERROR }) {
        MethodVerdict.ROOTED
    } else {
        MethodVerdict.PASS
    }
}
