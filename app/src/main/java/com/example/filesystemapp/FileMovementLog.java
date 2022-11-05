package com.example.filesystemapp;

import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.LinkedList;
import java.util.Queue;

public class FileMovementLog {
    
    private static class Movement{
        public String event = null;
        public String parentDir = null;
        public String fileName = null;

        public Movement(WatchEvent<?> event, String parentDir, String fileName){
            this.event = event.kind().toString();
            this.parentDir = parentDir;
            this.fileName = fileName;
        }

        public String absolutePath(){
            return Paths.get(this.parentDir, this.fileName).toAbsolutePath().toString();
        }
    }

    enum Action {
        CREATE, DELETE, MOVE, RENAME
    }

    private static class LogEntry {
        public String previousPath = null;
        public String currentPath = null;
        public String tagsAssociated = null;
        public Action action = null;

        public LogEntry(@NonNull String currPath, String prevPath, Action action, String tags) throws IllegalArgumentException {
            if((action == Action.MOVE || action == Action.RENAME) && prevPath == null){
                throw new IllegalArgumentException("prevPath argument cannot be null for MOVE or RENAME action");
            }
            this.previousPath = prevPath;
            this.currentPath = currPath;
            this.action = action;
            this.tagsAssociated = tags;
            this.writeToDatabase();
        }

        public boolean writeToDatabase(){
            /*
                IMPLEMENTATION
             */
            Log.d("log_entry", this.toString());
            return true;
        }

        @Override
        public String toString() {
            return "PreviousPath= " + previousPath +
                    ", CurrentPath= " + currentPath +
                    ", Tags= [ " + tagsAssociated + "] " +
                    ", Action=" + action;
        }
    }

    private static FileMovementLog instance = null;

    private Queue<Movement> movementQ = new LinkedList<>();
    private Queue<LogEntry> entryQ = new LinkedList<>();

    private FileMovementLog(){
        Log.d("FileMovementLog", " ... instance initialised");
    }

    public static FileMovementLog getInstance(){
        if(FileMovementLog.instance == null){
            instance = new FileMovementLog();
        }
        return instance;
    }

    public void recordMovement(WatchEvent<?> event, Path path){
        Movement curr = new Movement(event, path.getParent().toAbsolutePath().toString(), path.getFileName().toString());
        if(entryQ.isEmpty()){
            switch(curr.event){
                case "ENTRY_DELETE" : {
                    movementQ.add(curr);
                    break;
                }
                case "ENTRY_CREATE" : {
                    entryQ.add(new LogEntry(curr.absolutePath(), null, Action.MOVE, "file-tag"));
                    break;
                }
            }
            return;
        }
        Movement prev = movementQ.peek();

        if(prev.event.equals("ENTRY_DELETE") && curr.event.equals("ENTRY_CREATE")){
            /*
                PREV -> DELETE , CURR -> CREATE
                Either file is renamed or moved
                Or separate actions on both
            * */
            boolean sameFileName = prev.fileName.equals(curr.fileName);
            boolean sameParentDir = prev.parentDir.equals(curr.parentDir);
            if(sameFileName){
                // file moved from prev.parentDir to curr.parentDir
                entryQ.add(new LogEntry(curr.absolutePath(), prev.absolutePath(), Action.MOVE, "file-tags"));
            } else if(sameParentDir){
                // file renamed from prev.fileName to curr.fileName
                entryQ.add(new LogEntry(curr.absolutePath(), prev.absolutePath(), Action.RENAME, "file-tags"));
            } else {
                // Previous is deleted and current is created
                entryQ.add(new LogEntry(curr.absolutePath(), null, Action.DELETE, "file-tags"));
                entryQ.add(new LogEntry(curr.absolutePath(), null, Action.CREATE, "file-tags"));
            }
            movementQ.remove();
        } else if (prev.event.equals(curr.event)){
            /*
                PREV = CURR
                CREATE -> Both files are created new at location
                DELETE -> Delete the prev file and add the current file to movementQ
            * */
            if(prev.event.equals("ENTRY_CREATE")){
                // create both the files
                entryQ.add(new LogEntry(curr.absolutePath(), null, Action.CREATE, "file-tags"));
                entryQ.add(new LogEntry(prev.absolutePath(), null, Action.CREATE, "file-tags"));
                movementQ.remove();
            } else {
                // delete the prev and add the current to movementQ
                entryQ.add(new LogEntry(prev.absolutePath(), null, Action.DELETE, "file-tags"));
                movementQ.remove();
                movementQ.add(curr);
            }
        }
    }
}