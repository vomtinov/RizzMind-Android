package com.rizzmind.app.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.rizzmind.app.R
import com.rizzmind.app.data.repository.OCRRepository
import com.rizzmind.app.databinding.ActivityMainBinding
import com.rizzmind.app.service.FloatingBubbleService
import com.rizzmind.app.ui.viewmodel.MainViewModel
import com.rizzmind.app.ui.viewmodel.MainViewModelFactory
import com.rizzmind.app.utils.PermissionHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var mediaProjectionManager: MediaProjectionManager

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startFloatingService(result.resultCode, result.data)
        } else {
            Toast.makeText(this, "Screen capture permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (PermissionHelper.checkOverlayPermission(this)) {
            viewModel.setPermissionGranted(true)
            Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            requestMediaProjectionPermission()
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupUI()
        setupObservers()
        
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private fun setupViewModel() {
        val repository = OCRRepository()
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
    }

    private fun setupUI() {
        binding.btnStartService.setOnClickListener {
            if (PermissionHelper.checkOverlayPermission(this)) {
                requestMediaProjectionPermission()
            } else {
                requestOverlayPermission()
            }
        }

        binding.btnStopService.setOnClickListener {
            stopFloatingService()
        }
    }

    private fun setupObservers() {
        viewModel.serviceRunning.observe(this) { isRunning ->
            binding.btnStartService.isEnabled = !isRunning
            binding.btnStopService.isEnabled = isRunning
            binding.tvStatus.text = if (isRunning) {
                getString(R.string.service_running)
            } else {
                getString(R.string.service_stopped)
            }
        }

        viewModel.permissionGranted.observe(this) { granted ->
            if (granted) {
                binding.btnStartService.text = getString(R.string.start_service)
            }
        }
    }

    private fun requestOverlayPermission() {
        PermissionHelper.requestOverlayPermission(this) { granted ->
            if (granted) {
                viewModel.setPermissionGranted(true)
                requestMediaProjectionPermission()
            }
        }
    }

    private fun requestMediaProjectionPermission() {
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        mediaProjectionLauncher.launch(intent)
    }

    private fun startFloatingService(resultCode: Int, data: Intent?) {
        val serviceIntent = Intent(this, FloatingBubbleService::class.java).apply {
            putExtra(FloatingBubbleService.EXTRA_RESULT_CODE, resultCode)
            putExtra(FloatingBubbleService.EXTRA_MEDIA_PROJECTION_DATA, data)
        }
        
        startForegroundService(serviceIntent)
        viewModel.setServiceRunning(true)
        
        Toast.makeText(this, "Floating bubble started", Toast.LENGTH_SHORT).show()
    }

    private fun stopFloatingService() {
        val serviceIntent = Intent(this, FloatingBubbleService::class.java)
        stopService(serviceIntent)
        viewModel.setServiceRunning(false)
        
        Toast.makeText(this, "Floating bubble stopped", Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PermissionHelper.REQUEST_CODE_OVERLAY_PERMISSION) {
            if (PermissionHelper.checkOverlayPermission(this)) {
                viewModel.setPermissionGranted(true)
                Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                requestMediaProjectionPermission()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (viewModel.serviceRunning.value == true) {
            stopFloatingService()
        }
    }
}