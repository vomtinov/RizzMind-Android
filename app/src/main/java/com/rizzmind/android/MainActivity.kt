package com.rizzmind.android

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.rizzmind.android.service.FloatingBubbleService
import com.rizzmind.android.ui.theme.RizzMindTheme

class MainActivity : ComponentActivity() {
    private lateinit var preferences: SharedPreferences
    private var isServiceRunning by mutableStateOf(false)
    
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Settings.canDrawOverlays(this)) {
            startFloatingService()
        } else {
            Toast.makeText(this, "Overlay permission is required", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        
        setContent {
            RizzMindTheme {
                MainScreen()
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        var apiKey by remember { mutableStateOf(preferences.getString("openai_api_key", "") ?: "") }
        var suggestionCount by remember { mutableStateOf(preferences.getInt("suggestion_count", 3)) }
        var suggestionMode by remember { mutableStateOf(preferences.getString("suggestion_mode", "gpt") ?: "gpt") }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("RizzMind Settings") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Service Control Section
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Service Control",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = if (isServiceRunning) "Service is running" else "Service is stopped",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Button(
                            onClick = {
                                if (isServiceRunning) {
                                    stopFloatingService()
                                } else {
                                    requestOverlayPermissionAndStart()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isServiceRunning) "Stop Floating Bubble" else "Start Floating Bubble")
                        }
                    }
                }
                
                // API Configuration Section
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "AI Configuration",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        // API Key Input
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("OpenAI API Key") },
                            placeholder = { Text("sk-...") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(
                            onClick = {
                                preferences.edit()
                                    .putString("openai_api_key", apiKey)
                                    .apply()
                                Toast.makeText(this@MainActivity, "API key saved", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save API Key")
                        }
                        
                        // Suggestion Mode Selection
                        Text(
                            text = "Suggestion Mode",
                            style = MaterialTheme.typography.labelMedium
                        )
                        
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = suggestionMode == "gpt",
                                    onClick = { 
                                        suggestionMode = "gpt"
                                        preferences.edit()
                                            .putString("suggestion_mode", "gpt")
                                            .apply()
                                    }
                                )
                                Text("GPT-4 (Requires API key)")
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = suggestionMode == "local",
                                    onClick = { 
                                        suggestionMode = "local"
                                        preferences.edit()
                                            .putString("suggestion_mode", "local")
                                            .apply()
                                    }
                                )
                                Text("Local patterns (Offline)")
                            }
                        }
                    }
                }
                
                // Suggestion Settings Section
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Suggestion Settings",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = "Number of suggestions: $suggestionCount",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Slider(
                            value = suggestionCount.toFloat(),
                            onValueChange = { 
                                suggestionCount = it.toInt()
                                preferences.edit()
                                    .putInt("suggestion_count", suggestionCount)
                                    .apply()
                            },
                            valueRange = 1f..5f,
                            steps = 3
                        )
                    }
                }
                
                // About Section
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "About RizzMind",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = "RizzMind uses AI to generate smart, contextual reply suggestions for your conversations. " +
                                    "The floating bubble can be dragged around your screen and tapped to get suggestions.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "Features:\n" +
                                    "• GPT-4 powered suggestions\n" +
                                    "• Local pattern matching\n" +
                                    "• Conversation memory\n" +
                                    "• Customizable suggestion count",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
    
    private fun requestOverlayPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                overlayPermissionLauncher.launch(intent)
                return
            }
        }
        startFloatingService()
    }
    
    private fun startFloatingService() {
        val intent = Intent(this, FloatingBubbleService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        isServiceRunning = true
        Toast.makeText(this, "Floating bubble started", Toast.LENGTH_SHORT).show()
    }
    
    private fun stopFloatingService() {
        val intent = Intent(this, FloatingBubbleService::class.java)
        stopService(intent)
        isServiceRunning = false
        Toast.makeText(this, "Floating bubble stopped", Toast.LENGTH_SHORT).show()
    }
}