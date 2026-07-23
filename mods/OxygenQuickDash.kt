package com.liststudio.offtubemods.mods

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Oxygen-style quick dash toggles.
 * Keeps small state in-memory (Wi‑Fi, Bluetooth, Brightness).
 */
class OxygenDashViewModel : ViewModel() {
    private val _wifi = MutableStateFlow(true)
    val wifi: StateFlow<Boolean> = _wifi.asStateFlow()

    private val _bluetooth = MutableStateFlow(false)
    val bluetooth: StateFlow<Boolean> = _bluetooth.asStateFlow()

    private val _brightness = MutableStateFlow(0.6f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    fun toggleWifi() { _wifi.value = !_wifi.value }
    fun toggleBluetooth() { _bluetooth.value = !_bluetooth.value }
    fun setBrightness(v: Float) { _brightness.value = v }
}

/**
 * OxygenQuickDash composable implemented with Material3 and collectAsStateWithLifecycle.
 * Keeps UI purely declarative; pass the ViewModel and visibility flag from your app.
 */
@Composable
fun OxygenQuickDash(
    vm: OxygenDashViewModel,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val wifi by vm.wifi.collectAsStateWithLifecycle()
    val bt by vm.bluetooth.collectAsStateWithLifecycle()
    val br by vm.brightness.collectAsStateWithLifecycle()

    AnimatedVisibility(visible = visible) {
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { vm.toggleWifi() }) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "Wi‑Fi",
                                tint = if (wifi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Text(text = if (wifi) "Wi‑Fi ON" else "Wi‑Fi OFF")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { vm.toggleBluetooth() }) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = "Bluetooth",
                                tint = if (bt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Text(text = if (bt) "BT ON" else "BT OFF")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.BrightnessMedium, contentDescription = "Brightness")
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = br,
                        onValueChange = { vm.setBrightness(it) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${(br * 100).toInt()}%")
                }
            }
        }
    }
}
