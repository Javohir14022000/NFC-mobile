package dev.mobile.nfcmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dev.mobile.nfcmobile.hce.CardEmulationService
import dev.mobile.nfcmobile.model.ReceivedData
import dev.mobile.nfcmobile.ui.ReceivedScreen
import dev.mobile.nfcmobile.ui.theme.NFCMobileTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var receivedData by mutableStateOf<ReceivedData?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            CardEmulationService.dataFlow.collect { data ->
                receivedData = data
            }
        }

        enableEdgeToEdge()
        setContent {
            NFCMobileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ReceivedScreen(
                        data = receivedData,
                        onClear = { receivedData = null },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
