package be.csu333.rootbeerfresher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.csu333.rootbeerfresher.checks.CheckState
import be.csu333.rootbeerfresher.checks.CheckStatus
import be.csu333.rootbeerfresher.checks.RootMethod
import be.csu333.rootbeerfresher.ui.theme.DetailBackground
import be.csu333.rootbeerfresher.ui.theme.ErrorOrange
import be.csu333.rootbeerfresher.ui.theme.FailBackground
import be.csu333.rootbeerfresher.ui.theme.FailBorder
import be.csu333.rootbeerfresher.ui.theme.FailBorderExpanded
import be.csu333.rootbeerfresher.ui.theme.FailRed
import be.csu333.rootbeerfresher.ui.theme.PassBackground
import be.csu333.rootbeerfresher.ui.theme.PassBadgeText
import be.csu333.rootbeerfresher.ui.theme.PassBorder
import be.csu333.rootbeerfresher.ui.theme.PassGreen
import be.csu333.rootbeerfresher.ui.theme.PendingBackground
import be.csu333.rootbeerfresher.ui.theme.PendingBadgeText
import be.csu333.rootbeerfresher.ui.theme.PendingBorder
import be.csu333.rootbeerfresher.ui.theme.PendingIcon
import be.csu333.rootbeerfresher.ui.theme.PendingText
import be.csu333.rootbeerfresher.ui.theme.RemediationBackground
import be.csu333.rootbeerfresher.ui.theme.RemediationBorder
import be.csu333.rootbeerfresher.ui.theme.RemediationLabelColor
import be.csu333.rootbeerfresher.ui.theme.RemediationText
import be.csu333.rootbeerfresher.ui.theme.RunningBackground
import be.csu333.rootbeerfresher.ui.theme.RunningGrey
import be.csu333.rootbeerfresher.ui.theme.TechLogBackground
import be.csu333.rootbeerfresher.ui.theme.TextPrimary
import be.csu333.rootbeerfresher.ui.theme.TextSecondary

@Composable
fun CheckItem(
    state: CheckState,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpandable = state.status == CheckStatus.FAIL || state.status == CheckStatus.ERROR
    val bgColor = when (state.status) {
        CheckStatus.PENDING -> PendingBackground
        CheckStatus.RUNNING -> RunningBackground
        CheckStatus.PASS -> PassBackground
        CheckStatus.FAIL, CheckStatus.ERROR -> FailBackground
    }
    val borderColor = when (state.status) {
        CheckStatus.PASS -> PassBorder
        CheckStatus.FAIL, CheckStatus.ERROR ->
            if (state.isExpanded) FailBorderExpanded else FailBorder

        else -> PendingBorder
    }
    val borderWidth = if (state.isExpanded) 2.dp else 1.dp
    val shape = RoundedCornerShape(6.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(borderWidth, borderColor, shape)
            .background(bgColor)
            .then(if (isExpandable) Modifier.clickable { onToggleExpanded() } else Modifier)
    ) {
        CheckRow(state)
        AnimatedVisibility(visible = state.isExpanded) {
            CheckDetail(state)
        }
    }
}

@Composable
private fun CheckRow(state: CheckState) {
    Row(
        modifier = Modifier.padding(horizontal = 9.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusIcon(state.status)
        Spacer(Modifier.width(10.dp))
        Text(
            text = state.name,
            fontSize = 13.sp,
            color = if (state.status == CheckStatus.PENDING) PendingText else TextPrimary,
            fontWeight = if (state.isExpanded) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (state.methods.isNotEmpty()) {
            Spacer(Modifier.width(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                state.methods.sortedBy { it.ordinal }.forEach { method ->
                    MethodChip(method)
                }
            }
            Spacer(Modifier.width(4.dp))
        }
        StatusBadge(state.status)
        if (state.status == CheckStatus.FAIL || state.status == CheckStatus.ERROR) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (state.isExpanded) "▲" else "▼",
                fontSize = 12.sp,
                color = PendingBadgeText
            )
        }
    }
}

@Composable
private fun MethodChip(method: RootMethod) {
    val label = when (method) {
        RootMethod.IS_ROOTED -> "isRooted"
        RootMethod.IS_ROOTED_WITH_BUSYBOX -> "+BusyBox"
    }
    Text(
        text = label,
        fontSize = 9.sp,
        color = PendingBadgeText,
        modifier = Modifier
            .border(1.dp, PendingBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    )
}

@Composable
private fun StatusIcon(status: CheckStatus) {
    val (symbol, color) = when (status) {
        CheckStatus.PENDING -> "○" to PendingIcon
        CheckStatus.RUNNING -> "⟳" to RunningGrey
        CheckStatus.PASS -> "✓" to PassGreen
        CheckStatus.FAIL -> "✗" to FailRed
        CheckStatus.ERROR -> "!" to ErrorOrange
    }
    Text(text = symbol, fontSize = 15.sp, color = color)
}

@Composable
private fun StatusBadge(status: CheckStatus) {
    val (label, textColor, bgColor) = when (status) {
        CheckStatus.PENDING -> Triple("PENDING", PendingBadgeText, PendingBorder)
        CheckStatus.RUNNING -> Triple("RUNNING", Color.White, RunningGrey)
        CheckStatus.PASS -> Triple("PASS", PassBadgeText, PassBorder)
        CheckStatus.FAIL -> Triple("FAIL", Color.White, FailRed)
        CheckStatus.ERROR -> Triple("ERROR", Color.White, ErrorOrange)
    }
    Text(
        text = label,
        fontSize = 10.sp,
        color = textColor,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    )
}

@Composable
private fun CheckDetail(state: CheckState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DetailBackground)
            .padding(start = 10.dp, end = 12.dp, top = 10.dp, bottom = 10.dp)
    ) {
        DetailSection(label = "WHAT IT CHECKS", labelColor = TextSecondary) {
            Text(state.description, fontSize = 12.sp, color = TextPrimary, lineHeight = 18.sp)
        }
        if (state.finding != null) {
            Spacer(Modifier.height(8.dp))
            DetailSection(label = "FOUND", labelColor = FailRed) {
                Text(
                    text = state.finding,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            }
        }
        if (state.techLog != null) {
            Spacer(Modifier.height(8.dp))
            DetailSection(label = "TECHNICAL LOG", labelColor = TextSecondary) {
                Surface(shape = RoundedCornerShape(4.dp), color = TechLogBackground) {
                    Text(
                        text = state.techLog,
                        fontSize = 11.sp,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = RemediationBackground,
            border = BorderStroke(1.dp, RemediationBorder)
        ) {
            Column(Modifier.padding(8.dp)) {
                DetailSection(label = "REMEDIATION", labelColor = RemediationLabelColor) {
                    Text(
                        state.remediation,
                        fontSize = 12.sp,
                        color = RemediationText,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    label: String,
    labelColor: Color,
    content: @Composable () -> Unit
) {
    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = labelColor,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 2.dp)
    )
    content()
}
