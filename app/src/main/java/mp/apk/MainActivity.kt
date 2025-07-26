package mp.apk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import mp.apk.presentation.theme.PlantEYETheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlantEYETheme {
                PlantEYEComp()
            }
        }
    }
}

@Composable
fun PlantEYEComp() {
    val navController = rememberNavController()
    NavGraph(navController = navController)
}
