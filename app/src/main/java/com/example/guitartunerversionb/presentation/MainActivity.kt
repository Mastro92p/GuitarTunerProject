package com.example.guitartunerversionb.presentation

import AudioProcessor // Ensure you import your AudioProcessor here
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RequestAudioPermission() // Request audio permission
        }
    }
}

@Composable
fun RequestAudioPermission(onPermissionGranted: () -> Unit = {}) {
    var permissionGranted by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            permissionGranted = isGranted
            if (isGranted) {
                onPermissionGranted() // Call when permission is granted
            } else {
                permissionDenied = true
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Permission request UI
    if (permissionDenied) {
        PermissionRequestUI(permissionDenied) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    if (permissionGranted) {
        TunerContent() // Call TunerContent when permission is granted
    }
}

@Composable
fun PermissionRequestUI(permissionDenied: Boolean, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (permissionDenied) {
            Text(text = "Audio permission is required", color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(text = "Retry", color = Color.Black)
            }
        } else {
            Text(text = "Requesting audio permission...", color = Color.White)
        }
    }
}

@Composable
fun TunerContent() {
    var frequency by remember { mutableFloatStateOf(0f) }
    var note by remember { mutableStateOf("") }
    var octave by remember { mutableIntStateOf(0) }
    var deviation by remember { mutableFloatStateOf(0f) }
    var semitone by remember { mutableFloatStateOf(0f) }
    var cents by remember { mutableFloatStateOf(0f) }

    // Initialize audio processing when permission is granted
    DisposableEffect(Unit) {
        // Start listening to audio input
        AudioProcessor.startListening { data ->
            frequency = data.frequency
            note = data.note
            octave = data.octave.toInt()
            deviation = data.deviation
            semitone = data.semitoneDifference
            cents = data.cents
        }

        // Clean up resources when the composable is disposed
        onDispose {
            AudioProcessor.stopListening()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        FrequencyDisplay(note, octave, frequency, deviation, cents)
        DialView(cents) // Use the existing DialView
    }
}
