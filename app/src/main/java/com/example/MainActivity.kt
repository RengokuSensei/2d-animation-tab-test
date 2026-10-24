package com.example
 
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.frameforge.log.AppLogger
import com.example.frameforge.ui.a2.A2StudioScreen
import com.example.frameforge.viewmodel.StudioViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val studioViewModel: StudioViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppLogger.i("LIFECYCLE", "MainActivity onCreate invoked")
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          A2StudioScreen(viewModel = studioViewModel)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    AppLogger.i("LIFECYCLE", "MainActivity onResume")
  }

  override fun onPause() {
    super.onPause()
    AppLogger.i("LIFECYCLE", "MainActivity onPause")
  }
}
