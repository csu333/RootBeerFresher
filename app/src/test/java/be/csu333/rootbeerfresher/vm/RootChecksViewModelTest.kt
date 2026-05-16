package be.csu333.rootbeerfresher.vm

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import be.csu333.rootbeerfresher.checks.CheckDetail
import be.csu333.rootbeerfresher.checks.CheckRunner
import be.csu333.rootbeerfresher.checks.CheckStatus
import be.csu333.rootbeerfresher.checks.RootCheck
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class RootChecksViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun fakeRunner(vararg pairs: Pair<String, Boolean>): CheckRunner {
        val checks = pairs.map { (id, detected) ->
            RootCheck(id, "Check $id", "desc $id", "fix $id") { _, _ ->
                CheckDetail(detected = detected, finding = if (detected) "found $id" else null)
            }
        }
        return CheckRunner(checks, mainDispatcherRule.testDispatcher)
    }

    @Test
    fun initialState_allChecksPending() {
        val vm = RootChecksViewModel(fakeRunner("a" to false))
        assertThat(vm.checks.value).hasSize(1)
        assertThat(vm.checks.value[0].status).isEqualTo(CheckStatus.PENDING)
        assertThat(vm.checks.value[0].id).isEqualTo("a")
    }

    @Test
    fun runChecks_updatesChecksToPASSAndFAIL() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = RootChecksViewModel(fakeRunner("a" to false, "b" to true))
        vm.runChecks(context)
        advanceUntilIdle()
        assertThat(vm.checks.value[0].status).isEqualTo(CheckStatus.PASS)
        assertThat(vm.checks.value[1].status).isEqualTo(CheckStatus.FAIL)
        assertThat(vm.checks.value[1].finding).isEqualTo("found b")
    }

    @Test
    fun toggleExpanded_flipsIsExpandedForTarget() {
        val vm = RootChecksViewModel(fakeRunner("a" to true))
        assertThat(vm.checks.value[0].isExpanded).isFalse()
        vm.toggleExpanded("a")
        assertThat(vm.checks.value[0].isExpanded).isTrue()
        vm.toggleExpanded("a")
        assertThat(vm.checks.value[0].isExpanded).isFalse()
    }

    @Test
    fun toggleExpanded_doesNotAffectOtherChecks() {
        val vm = RootChecksViewModel(fakeRunner("a" to true, "b" to true))
        vm.toggleExpanded("a")
        assertThat(vm.checks.value[0].isExpanded).isTrue()
        assertThat(vm.checks.value[1].isExpanded).isFalse()
    }

    @Test
    fun runChecks_resetsAllStatesToPendingBeforeRunning() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = RootChecksViewModel(fakeRunner("a" to true))
        vm.runChecks(context)
        advanceUntilIdle()
        assertThat(vm.checks.value[0].status).isEqualTo(CheckStatus.FAIL)

        vm.runChecks(context)
        // After reset, immediately pending again (before advanceUntilIdle)
        assertThat(vm.checks.value[0].status).isEqualTo(CheckStatus.PENDING)
    }

    @Test
    fun isRunning_trueWhileChecksInProgress_falseWhenDone() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = RootChecksViewModel(fakeRunner("a" to false))
        assertThat(vm.isRunning.value).isFalse()
        vm.runChecks(context)
        // isRunning becomes true synchronously at start of runChecks
        assertThat(vm.isRunning.value).isTrue()
        advanceUntilIdle()
        assertThat(vm.isRunning.value).isFalse()
    }
}
