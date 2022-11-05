package com.example.filesystemapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class SqlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sql);

        Context self = this;

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        DBHandler db = new DBHandler(self);

        try{
            String files = db.getNRandomFilePaths(25);
            Log.d("rec_sel", files);
            long startTime = System.currentTimeMillis();
            db.getFileTags(files);
            long endTime = System.currentTimeMillis();
            Log.d("rec_sel", "Time: "+(endTime-startTime));
        } catch(Exception e){
            Log.d("rec_err", e.toString());
        }
    }
}

class DBHandler extends SQLiteOpenHelper {

    // creating a constant variables for our database.
    // below variable is for our database name.
    private static final String DB_NAME = "filetagsrel";

    // below int is our database version
    private static final int DB_VERSION = 1;

    // below variable is for our table name.
    private static final String TABLE_NAME = "FilesToTags";

    // below variable is for our id column.
    private static final String absPath = "absolutePath";

    // below variable is for our course name column
    private static final String tags = "name";

    private Context ctx = null;

    // creating a constructor for our database handler.
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        ctx = context;
    }

    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {
        // on below line we are creating
        // an sqlite query and we are
        // setting our column names
        // along with their data types.
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + absPath + " TEXT PRIMARY KEY, "
                + tags + " TEXT)";

        // at last we are calling a exec sql
        // method to execute above sql query
        db.execSQL(query);
    }

    public static String generateTags(int tagLimit, int maxLimit){
        StringBuilder tags = new StringBuilder();
        Random r = new Random();
        tags.append("Tag"+((r.nextInt() & Integer.MAX_VALUE)%maxLimit));
        for(int i = 0; i < 5+((r.nextInt() & Integer.MAX_VALUE)%tagLimit); i++){
            tags.append(",Tag"+((r.nextInt()& Integer.MAX_VALUE)%maxLimit));
        };
        return tags.toString();
    }

    @SuppressLint("Range")
    public void getFileTags(String path){
        /*SELECT name FROM FilesToTags WHERE */
        Cursor c = this.getWritableDatabase().query(TABLE_NAME, new String[]{absPath, tags}, "absolutePath IN "+path, null, null, null, null);
        c.moveToFirst();
        Log.d("rec_sel", String.valueOf(c.getCount()));
        for(int i = 0; i < c.getCount(); i++){
            Log.d("rec_sel", c.getColumnIndex(tags) >= 0 ? c.getString(c.getColumnIndex(tags)) : "none");
            c.moveToNext();
        }
    }

    public String getNIntervaledFilePaths(int N, int interval, int offset){
        Cursor c = this.getWritableDatabase().query(TABLE_NAME, new String[]{absPath}, null, null, null, null, null);
        StringBuilder res = new StringBuilder();
        HashSet<Integer> s = new HashSet<>();
        int count = c.getCount();
        int l = 0;
        while(N > 0){
            int index = ((l*interval)+offset)%count;
            c.moveToPosition(index);
            res.append("'"+c.getString(0)+"',");
            Log.d("rec_sel", ""+index+" -> "+c.getString(0));
            N--;l++;
        }
        res.deleteCharAt(res.length()-1);
        return "("+res+")";
    }

    public String getNRandomFilePaths(int N){
        Cursor c = this.getWritableDatabase().query(TABLE_NAME, new String[]{absPath}, null, null, null, null, null);
        StringBuilder res = new StringBuilder();
        HashSet<Integer> s = new HashSet<>();
        int count = c.getCount();
        Random r = new Random();
        int l = 0;
        while(N > 0){
            int index = r.nextInt(count+1);
            if(s.contains(index)){
                c.moveToPosition(index);
                s.add(index);
                res.append("'"+c.getString(0)+"',");
                Log.d("rec_sel", ""+index+" -> "+c.getString(0));
                N--;l++;
            }
        }
        res.deleteCharAt(res.length()-1);
        return "("+res+")";
    }

    // this method is use to add new course to our sqlite database.
    public void populateDatabase(String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        File rootStorage = new File(filePath);
        Queue<File> children = new LinkedList<>();
        children.add(rootStorage);
        while(!children.isEmpty()){
            File f = children.remove();
            Log.println(Log.DEBUG, "exploring_path", f.getAbsolutePath());
            File[] childFiles = f.listFiles();
            if(childFiles != null && f.isDirectory()){
                for(int i = 0; i < childFiles.length; i++){
                    if(childFiles[i].isDirectory()) {
                        children.add(childFiles[i]);
                    } else {
                        ContentValues values = new ContentValues();
                        values.put(absPath, childFiles[i].getAbsolutePath());
                        values.put(tags, generateTags(23, 5131));
                        db.insert(TABLE_NAME, null, values);
                        Log.d("rec_ins", childFiles[i].getAbsolutePath());
                    }
                }
            }
        }
        Toast.makeText(ctx, "Database Populated", Toast.LENGTH_LONG).show();
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}