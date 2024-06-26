package com.raival.fileexplorer.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.google.android.material.elevation.SurfaceColors
import com.raival.fileexplorer.App
import com.raival.fileexplorer.App.Companion.showMsg
import com.raival.fileexplorer.R
import com.raival.fileexplorer.util.PrefsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {

    protected open lateinit var binding: T

    abstract fun getViewBinding(): T

    @JvmField
    protected var currentTheme: String = PrefsUtils.Settings.themeMode

    /**
     * This method is called after checking storage permissions
     */
    open fun init() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        setContentView(binding.root)
        if (currentTheme == SettingsActivity.THEME_MODE_DARK) {
            setTheme(R.style.Theme_FileExplorer_Dark)
        } else if (currentTheme == SettingsActivity.THEME_MODE_LIGHT) {
            setTheme(R.style.Theme_FileExplorer_Light)
        }
        window.statusBarColor = SurfaceColors.SURFACE_2.getColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val imeInset =
                ViewCompat.getRootWindowInsets(view)!!.getInsets(WindowInsetsCompat.Type.ime())

            val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = systemBarInsets.left
                rightMargin = systemBarInsets.right
                topMargin = systemBarInsets.top
                bottomMargin = if (imeInset.bottom > 0) imeInset.bottom else systemBarInsets.bottom
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    protected fun checkPermissions() {
        if (grantStoragePermissions()) {
            init()
        }
    }

    private fun grantStoragePermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivityForResult(intent, 121121)
                return false
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    9011
                )
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 9011) {
            init()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.Default).launch {
            Glide.get(App.appContext).clearDiskCache()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 121121) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    init()
                } else {
                    showMsg("Storage permission is required")
                    finish()
                }
            }
        }
    }
}
