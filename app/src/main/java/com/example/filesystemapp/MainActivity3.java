package com.example.filesystemapp;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.security.Provider;
import java.security.SecurityPermission;
import java.util.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MainActivity3 extends AppCompatActivity {

    public ArrayList<FileWatcher> watcherList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        try {
            Context self = this;
            self.startForegroundService(new Intent(self, MyService.class));

        } catch (Exception e) {
            e.printStackTrace();
            Log.println(
                    Log.DEBUG,
                    "fwatch_PROBLEM",
                    e.getMessage()
            );
        }
    }

    protected static void initialiseFolderWatcher(ArrayList<FileWatcher> arrWatcher, Context ctx){
        File rootStorage = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        Queue<File> children = new LinkedList<>();
        children.add(rootStorage);
        while(!children.isEmpty()){
            File f = children.remove();
            FileWatcher fw = new FileWatcher(f, ctx.getApplicationContext());
            arrWatcher.add(fw);
            Log.println(Log.DEBUG, "exploring_path", f.getAbsolutePath());
            File[] childFiles = f.listFiles();
            if(childFiles != null && f.isDirectory()){
                for(int i = 0; i < childFiles.length; i++){
                    if(childFiles[i].isDirectory()) {
                        children.add(childFiles[i]);
                    }
                }
            }
        }
    }
}

class FileWatcher extends FileObserver {
    public File file = null;
    private Context ctx = null;
    public FileWatcher(File file, Context ctx){
        super(file);
        this.file = file;
        this.ctx = ctx;
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        event = event & FileObserver.ALL_EVENTS;

        Log.println(Log.DEBUG, "moved_event", String.valueOf(event));

        if (event == FileObserver.MOVED_TO) {
            Log.println(Log.DEBUG, "moved_to", file.getAbsolutePath()+"/"+path.toString());
        }
        if (event == FileObserver.MOVED_FROM) {
            Toast.makeText(this.ctx, "[MOVED_FROM] "+file.getAbsolutePath()+"/"+path.toString(), Toast.LENGTH_LONG).show();
            Log.println(Log.DEBUG, "moved_from", file.getAbsolutePath()+"/"+path.toString());
        }
    }

    public boolean fileExists(){
        return this.file.isDirectory() && this.file.exists();
    }
}

