package com.example.filesystemapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.nio.file.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(Environment.isExternalStorageManager()){
            Toast.makeText(this, "Permission Already Granted", Toast.LENGTH_LONG).show();
        } else {
            var x = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            Toast.makeText(this, "Permission Required : "+x.toString(), Toast.LENGTH_LONG).show();
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE), 1)
            var i = Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivity(i)
        }

        var rootStorage = File(Environment.getExternalStorageDirectory().absolutePath)
        Log.d("perm_fs", Environment.isExternalStorageManager().toString())
        Log.println(Log.DEBUG, "test_fs_filename", rootStorage.name)
        var variable = rootStorage.listFiles()
        for (f in variable[9].listFiles()) {
            Log.println(Log.DEBUG, "test_fs_filename", f.name+" "+f.isFile)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }
}