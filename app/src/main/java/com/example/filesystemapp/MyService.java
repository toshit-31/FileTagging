package com.example.filesystemapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MyService extends Service{
    ArrayList<FileWatcher> arr = new ArrayList<>();
    private String CHANNEL_ID = "NOTIFICATION_CHANNEL";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("service_msg", "Service Created ...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Intent notificationIntent = new Intent(this, MainActivity3.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Service is Running")
                .setContentText("Listening for Screen Off/On events")
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        Log.d("service_msg", "Service Started ...");
        arr.removeAll(arr);
//        try {
//            WatchService ws = FileSystems.getDefault().newWatchService();
//            Path p1 = Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath(), "Download");
//            Path p2 = Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath(), "Documents");
//            p1.register(ws, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.OVERFLOW);
//            p2.register(ws, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.OVERFLOW);
//            while(true){
//                WatchKey wk = ws.poll(2, TimeUnit.MINUTES);
//                List<WatchEvent<?>> events = wk.pollEvents();
//                for(WatchEvent event : events){
//                    Log.d("ws_event", event.kind().toString() + " " + event.context().toString());
//                }
//                if(!wk.reset()){
//
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        MainActivity3.initialiseFolderWatcher(arr, this);
        for (int i = 0; i < arr.size(); i++) {
            FileWatcher fwInstance = arr.get(i);
            if (fwInstance.fileExists()) {
                fwInstance.startWatching();
            }
        }
        Toast.makeText(this, "Watching " + arr.size() + " folders ...", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("service_msg", "Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }
}