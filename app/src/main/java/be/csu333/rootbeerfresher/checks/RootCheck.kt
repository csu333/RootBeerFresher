package be.csu333.rootbeerfresher.checks

import android.content.Context
import com.scottyab.rootbeer.RootBeer

data class RootCheck(
    val id: String,
    val name: String,
    val description: String,
    val remediation: String,
    val methods: Set<RootMethod> = emptySet(),
    val run: suspend (RootBeer, Context) -> CheckDetail
)

data class CheckDetail(
    val detected: Boolean,
    val finding: String? = null,
    val techLog: String? = null
)

fun RootCheck.toInitialState() = CheckState(
    id = id,
    name = name,
    description = description,
    status = CheckStatus.PENDING,
    remediation = remediation,
    methods = methods
)
