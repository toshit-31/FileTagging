package com.example.filesystemapp

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.util.*


var fileCount = 0
var folderCount = 0


class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

    }

    override fun onStop() {
        super.onStop()
        Log.println(Log.DEBUG, "fwatch_problem", "Stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.println(Log.DEBUG, "fwatch_problem", "Destroyed")
    }


}

class Watcher(file: List<File>) : FileObserver(file) {

    val file = file

    override fun onEvent(event: Int, path: String?) {
        Log.println(Log.DEBUG, "obs:event", event.toString())
//        var p: String
//        if (path == null) p = "no path"
//        else p = path
//        Log.println(Log.DEBUG, "obs:path", p)
        if (event == FileObserver.MOVED_TO) {
            Log.println(Log.DEBUG, "moved_to", path.toString())
        }
        if (event == FileObserver.MOVED_FROM) {
            Log.println(Log.DEBUG, "moved_from", path.toString())
        }
    }
}
fun countFilesAndFolders(path: String?){
    var rootStorage = if(path == null)
        File(Environment.getExternalStorageDirectory().absolutePath)
    else
        File(path)
    var children = LinkedList<File>()
    children.addAll(rootStorage.listFiles())
    while(children.size > 0){
        var f: File = children.removeFirst()
        if (f.isDirectory){
            f.listFiles()?.forEach {
                children.addLast(it)
            }
            folderCount++
            } else {
            fileCount++
        }
    }
}

fun initialiseFolderWatcher(){
    var arrWatcher = mutableListOf<File>()
    var rootStorage = File(Environment.getExternalStorageDirectory().absolutePath)
    var children = LinkedList<File>()
    children.add(rootStorage)
    while(children.size > 0){
        var f: File = children.removeFirst()
        arrWatcher.add(f)
        Log.println(Log.DEBUG, "exploring_path", f.absolutePath)
        f.listFiles()?.forEach {
            if(it.isDirectory) children.addLast(it)
        }
    }
    Watcher(arrWatcher).startWatching()
    Log.println(
        Log.DEBUG,
        "fwatch_start",
        "Watching ${arrWatcher.size} folders ..."
    )
}