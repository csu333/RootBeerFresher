package be.csu333.rootbeerfresher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.csu333.rootbeerfresher.checks.CheckState
import be.csu333.rootbeerfresher.checks.MethodVerdict
import be.csu333.rootbeerfresher.checks.RootMethod
import be.csu333.rootbeerfresher.checks.methodVerdict
import be.csu333.rootbeerfresher.ui.theme.FailRed
import be.csu333.rootbeerfresher.ui.theme.PassGreen
import be.csu333.rootbeerfresher.ui.theme.SummaryAmberBackground
import be.csu333.rootbeerfresher.ui.theme.SummaryGreenBackground
import be.csu333.rootbeerfresher.ui.theme.TextPrimary

@Composable
fun SummaryBanner(checks: List<CheckState>, modifier: Modifier = Modifier) {
    val isRootedVerdict = methodVerdict(checks, RootMethod.IS_ROOTED)
    val busyBoxVerdict = methodVerdict(checks, RootMethod.IS_ROOTED_WITH_BUSYBOX)

    if (isRootedVerdict == null || busyBoxVerdict == null) return

    val anyRooted = isRootedVerdict == MethodVerdict.ROOTED || busyBoxVerdict == MethodVerdict.ROOTED

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (anyRooted) SummaryAmberBackground else SummaryGreenBackground)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MethodVerdictRow("isRooted()", isRootedVerdict)
        MethodVerdictRow("isRootedWithBusyBoxCheck()", busyBoxVerdict)
    }
}

@Composable
private fun MethodVerdictRow(label: String, verdict: MethodVerdict) {
    val isPass = verdict == MethodVerdict.PASS
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextPrimary,
            fontFamily = FontFamily.Monospace
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isPass) "✓" else "✗",
                fontSize = 13.sp,
                color = if (isPass) PassGreen else FailRed
            )
            Text(
                text = if (isPass) "PASS" else "ROOTED",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPass) PassGreen else FailRed
            )
        }
    }
}
