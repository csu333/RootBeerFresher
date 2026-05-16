package be.csu333.rootbeerfresher.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import be.csu333.rootbeerfresher.ui.theme.RootBeerFresherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RootBeerFresherTheme {
                RootChecksScreen()
            }
        }
    }
}
