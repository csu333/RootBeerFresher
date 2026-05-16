package be.csu333.rootbeerfresher.checks

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CheckRunnerTest {

    private val passCheck = RootCheck("p1", "Pass Check", "desc", "fix") { _, _ ->
        CheckDetail(detected = false, techLog = "clean")
    }
    private val failCheck = RootCheck("f1", "Fail Check", "desc", "fix") { _, _ ->
        CheckDetail(detected = true, finding = "found x", techLog = "x found")
    }
    private val crashCheck = RootCheck("c1", "Crash Check", "desc", "fix") { _, _ ->
        throw RuntimeException("boom")
    }
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun run_emitsRunningThenPassForCleanCheck() = runTest {
        val runner = CheckRunner(listOf(passCheck))
        val emitted = mutableListOf<CheckState>()

        runner.run(context) { emitted.add(it) }

        assertThat(emitted).hasSize(2)
        assertThat(emitted[0].status).isEqualTo(CheckStatus.RUNNING)
        assertThat(emitted[0].id).isEqualTo("p1")
        assertThat(emitted[1].status).isEqualTo(CheckStatus.PASS)
        assertThat(emitted[1].id).isEqualTo("p1")
    }

    @Test
    fun run_emitsRunningThenFailWithFindingForDetectedCheck() = runTest {
        val runner = CheckRunner(listOf(failCheck))
        val emitted = mutableListOf<CheckState>()

        runner.run(context) { emitted.add(it) }

        assertThat(emitted).hasSize(2)
        assertThat(emitted[1].status).isEqualTo(CheckStatus.FAIL)
        assertThat(emitted[1].finding).isEqualTo("found x")
        assertThat(emitted[1].techLog).isEqualTo("x found")
    }

    @Test
    fun run_emitsErrorWithExceptionMessageOnCrash() = runTest {
        val runner = CheckRunner(listOf(crashCheck))
        val emitted = mutableListOf<CheckState>()

        runner.run(context) { emitted.add(it) }

        assertThat(emitted).hasSize(2)
        assertThat(emitted[1].status).isEqualTo(CheckStatus.ERROR)
        assertThat(emitted[1].techLog).contains("RuntimeException")
        assertThat(emitted[1].techLog).contains("boom")
    }

    @Test
    fun run_processesAllChecksInSequence() = runTest {
        val runner = CheckRunner(listOf(passCheck, failCheck))
        val emitted = mutableListOf<CheckState>()

        runner.run(context) { emitted.add(it) }

        assertThat(emitted).hasSize(4)
        assertThat(emitted[0].id).isEqualTo("p1")
        assertThat(emitted[0].status).isEqualTo(CheckStatus.RUNNING)
        assertThat(emitted[1].id).isEqualTo("p1")
        assertThat(emitted[1].status).isEqualTo(CheckStatus.PASS)
        assertThat(emitted[2].id).isEqualTo("f1")
        assertThat(emitted[2].status).isEqualTo(CheckStatus.RUNNING)
        assertThat(emitted[3].id).isEqualTo("f1")
        assertThat(emitted[3].status).isEqualTo(CheckStatus.FAIL)
    }
}
