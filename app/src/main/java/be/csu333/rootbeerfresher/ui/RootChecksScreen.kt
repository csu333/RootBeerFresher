package be.csu333.rootbeerfresher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import be.csu333.rootbeerfresher.R
import be.csu333.rootbeerfresher.ui.theme.BeerAmber
import be.csu333.rootbeerfresher.vm.RootChecksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootChecksScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val vm: RootChecksViewModel = viewModel()
    val checks by vm.checks.collectAsStateWithLifecycle()
    val isRunning by vm.isRunning.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.runChecks(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "App icon",
                            modifier = Modifier.size(48.dp)
                        )
                        Text("RootBeerFresher")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { vm.runChecks(context) },
                        enabled = !isRunning
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Re-run checks"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BeerAmber,
                    scrolledContainerColor = BeerAmber,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { SummaryBanner(checks) }
            items(checks, key = { it.id }) { check ->
                CheckItem(
                    state = check,
                    onToggleExpanded = { vm.toggleExpanded(check.id) }
                )
            }
        }
    }
}
