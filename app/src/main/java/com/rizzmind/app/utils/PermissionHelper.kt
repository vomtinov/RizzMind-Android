package com.rizzmind.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.rizzmind.app.R

object PermissionHelper {

    fun checkOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun requestOverlayPermission(activity: Activity, onResult: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                showOverlayPermissionDialog(activity) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${activity.packageName}")
                    )
                    activity.startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
                }
            } else {
                onResult(true)
            }
        } else {
            onResult(true)
        }
    }

    fun requestRuntimePermissions(
        activity: Activity,
        permissions: List<String>,
        onAllPermissionsGranted: () -> Unit,
        onPermissionsDenied: (List<String>) -> Unit
    ) {
        Dexter.withContext(activity)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        onAllPermissionsGranted()
                    } else {
                        val deniedPermissions = report.deniedPermissionResponses.map { it.permissionName }
                        onPermissionsDenied(deniedPermissions)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .check()
    }

    private fun showOverlayPermissionDialog(activity: Activity, onPositive: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.permission_required_title))
            .setMessage(activity.getString(R.string.permission_required_message))
            .setPositiveButton("Grant") { _, _ ->
                onPositive()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    const val REQUEST_CODE_OVERLAY_PERMISSION = 1001
}