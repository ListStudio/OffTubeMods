package com.liststudio.offtubemods.mods

import android.app.Application
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwnerAmbient
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

// Simple data model
data class PlayerNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val sticky: Boolean = false
)

sealed interface PlayerUiState {
    object Loading : PlayerUiState
    data class Success(val items: List<PlayerNotification>) : PlayerUiState
    data class Error(val message: String) : PlayerUiState
}

/**
 * ViewModel for the player notification mod. Keeps notifications and auto-dismiss logic.
 */
class PlayerNotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val _ui = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val ui: StateFlow<PlayerUiState> = _ui.asStateFlow()

    private val jobs = mutableMapOf<String, Job>()

    init {
        _ui.value = PlayerUiState.Success(emptyList())
    }

    private fun current() = (_ui.value as? PlayerUiState.Success)?.items ?: emptyList()

    fun push(notification: PlayerNotification, autoDismissMs: Long = 8_000L) {
        val list = listOf(notification) + current()
        _ui.value = PlayerUiState.Success(list)
        if (!notification.sticky) {
            val job = viewModelScope.launch {
                delay(autoDismissMs)
                dismiss(notification.id)
            }
            jobs[notification.id] = job
        }
    }

    fun dismiss(id: String) {
        val newList = current().filterNot { it.id == id }
        _ui.value = PlayerUiState.Success(newList)
        jobs.remove(id)?.cancel()
    }

    fun clearAll() {
        _ui.value = PlayerUiState.Success(emptyList())
        jobs.values.forEach { it.cancel() }
        jobs.clear()
    }

    fun playSystemSound() {
        try {
            val ctx = getApplication<Application>()
            val uri: Uri = RingtoneManager.getActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(ctx, uri)
            r.play()
        } catch (_: Exception) {}
    }

    fun generateDemo() {
        val titles = listOf("Now playing", "Track finished", "New message")
        val msgs = listOf("Arctic Monkeys — Fluorescent", "Download complete", "You have a new message")
        push(
            PlayerNotification(
                title = titles.random(),
                message = msgs.random(),
                sticky = Random.nextBoolean() && Random.nextInt(3) == 2
            )
        )
    }
}

/**
 * Composable: PlayerNotificationBar — a compact bar intended to sit above your player UI.
 * It shows active notifications and overlays the Winamp image via raw URL.
 */
@Composable
fun PlayerNotificationBar(
    vm: PlayerNotificationViewModel = viewModel(),
    visible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val ui by vm.ui.collectAsState(initial = PlayerUiState.Loading)
    val list = (ui as? PlayerUiState.Success)?.items ?: emptyList()

    AnimatedVisibility(
        visible = visible && list.isNotEmpty(),
        enter = fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(220))
    ) {
        Column(modifier = modifier) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Player notifications", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { vm.clearAll() }) {
                    Icon(Icons.Default.ClearAll, contentDescription = "Clear")
                }
            }

            // show notifications list minimal
            LazyColumn {
                items(list) { it ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(6.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(it.title, style = MaterialTheme.typography.bodyLarge)
                            Text(it.message, style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { vm.playSystemSound() }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                        }
                        IconButton(onClick = { vm.dismiss(it.id) }) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            // overlay winamp SVG as subtle decorative bug — use raw url in repo
            val rawWinamp = "https://raw.githubusercontent.com/ListStudio/OffTubeMods/main/assets/winamp_bug.svg"
            Box(modifier = Modifier.fillMaxWidth().height(84.dp)) {
                AsyncImage(
                    model = rawWinamp,
                    contentDescription = "Winamp overlay",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .alpha(0.85f)
                )
            }
        }
    }
}
