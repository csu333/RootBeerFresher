package be.csu333.rootbeerfresher.checks

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MethodVerdictTest {

    private fun check(id: String, method: RootMethod, status: CheckStatus) = CheckState(
        id = id, name = "n", description = "d", remediation = "r",
        methods = setOf(method), status = status
    )

    @Test
    fun returnsNull_whenNoChecksMatchMethod() {
        val checks = listOf(check("a", RootMethod.IS_ROOTED_WITH_BUSYBOX, CheckStatus.PASS))
        assertThat(methodVerdict(checks, RootMethod.IS_ROOTED)).isNull()
    }

    @Test
    fun returnsNull_whenAnyRelevantCheckIsPending() {
        val checks = listOf(
            check("a", RootMethod.IS_ROOTED, CheckStatus.PASS),
            check("b", RootMethod.IS_ROOTED, CheckStatus.PENDING)
        )
        assertThat(methodVerdict(checks, RootMethod.IS_ROOTED)).isNull()
    }

    @Test
    fun returnsNull_whenAnyRelevantCheckIsRunning() {
        val checks = listOf(check("a", RootMethod.IS_ROOTED, CheckStatus.RUNNING))
        assertThat(methodVerdict(checks, RootMethod.IS_ROOTED)).isNull()
    }

    @Test
    fun returnsPass_whenAllRelevantChecksDoneAndNoneFailed() {
        val checks = listOf(
            check("a", RootMethod.IS_ROOTED, CheckStatus.PASS),
            check("b", RootMethod.IS_ROOTED, CheckStatus.PASS)
        )
        assertThat(methodVerdict(checks, RootMethod.IS_ROOTED)).isEqualTo(MethodVerdict.PASS)
    }

    @Test
    fun returnsRooted_whenAnyRelevantCheckFailed() {
        val checks = listOf(
            check("a", RootMethod.IS_ROOTED, CheckStatus.PASS),
            check("b", RootMethod.IS_ROOTED, CheckStatus.FAIL)
        )
        assertThat(methodVerdict(checks, RootMethod.IS_ROOTED)).isEqualTo(MethodVerdict.ROOTED)
    }

    @Test
    fun returnsRooted_whenAnyRelevantCheckErrored() {
        val checks = listOf(check("a", RootMethod.IS_ROOTED, CheckStatus.ERROR))
        assertThat(methodVerdict(checks, RootMethod.IS_ROOTED)).isEqualTo(MethodVerdict.ROOTED)
    }

    @Test
    fun ignoresChecksNotInRequestedMethod() {
        val checks = listOf(
            check("a", RootMethod.IS_ROOTED, CheckStatus.FAIL),
            check("b", RootMethod.IS_ROOTED_WITH_BUSYBOX, CheckStatus.PASS)
        )
        // Only "b" is relevant for IS_ROOTED_WITH_BUSYBOX; "a" is ignored
        assertThat(methodVerdict(checks, RootMethod.IS_ROOTED_WITH_BUSYBOX)).isEqualTo(MethodVerdict.PASS)
    }
}
