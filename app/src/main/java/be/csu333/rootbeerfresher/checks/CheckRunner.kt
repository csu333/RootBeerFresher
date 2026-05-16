package be.csu333.rootbeerfresher.checks

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CheckRunner(
    val checks: List<RootCheck>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun run(context: Context, onUpdate: (CheckState) -> Unit) =
        withContext(ioDispatcher) {
            val rootBeer = RootBeer(context).apply { setLogging(false) }
            checks.forEach { check ->
                onUpdate(check.toInitialState().copy(status = CheckStatus.RUNNING))
                val result = try {
                    val detail = check.run(rootBeer, context)
                    check.toInitialState().copy(
                        status = if (detail.detected) CheckStatus.FAIL else CheckStatus.PASS,
                        finding = detail.finding,
                        techLog = detail.techLog
                    )
                } catch (e: Exception) {
                    check.toInitialState().copy(
                        status = CheckStatus.ERROR,
                        techLog = "${e.javaClass.simpleName}: ${e.message}"
                    )
                }
                onUpdate(result)
            }
        }
}
