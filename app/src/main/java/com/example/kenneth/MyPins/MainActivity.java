package com.example.kenneth.MyPins;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    SQLiteDatabase db;
    private Cursor noteCursor;
    private NotesCursorAdaptor notesAdapter;
    private ListView lv;
    private String extStorageDirectory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirstRun();

        try {
            openDatabase(); // open (create if needed) database
            dropTable();
            insertSomeDbData(); // create-populate notes

        } catch (Exception e) {
            finish();
        }

        populateListView();
        lv.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        noteCursor.moveToPosition(position);

        Bundle args = new Bundle();
        args.putString("caption", noteCursor.getString(noteCursor.getColumnIndex("caption")));
        args.putString("path", noteCursor.getString(noteCursor.getColumnIndex("path")));
        args.putString("date", noteCursor.getString(noteCursor.getColumnIndex("date")));

        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtras(args);
        startActivity(intent);

    }

    private void populateListView() {
        lv = (ListView) findViewById(R.id.listView);
        noteCursor = db.rawQuery("SELECT recID as _id, caption, path, date FROM notes ORDER BY date DESC", null);
        notesAdapter = new NotesCursorAdaptor(MainActivity.this, noteCursor, 1);
        lv.setAdapter(notesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.Addnote:
                Intent intent = new Intent(MainActivity.this, Addnote.class);
                startActivityForResult(intent, 1000);
                break;
            case R.id.action_uninstall:
                Intent uninstall = new Intent(Intent.ACTION_DELETE, Uri.parse("package:com.example.kenneth.MyPins"));
                startActivity(uninstall);
                break;
            default:
                toast("unknown action ...");
        }

        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 1000)
        {
            populateListView();
        }
    }


    private void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void openDatabase() {
        try {

            // path to internal memory file system
            File storagePath = getApplication().getFilesDir();

            String myDbPath = storagePath + "/" + "mynotes";


            db = SQLiteDatabase.openDatabase(myDbPath, null,
                    SQLiteDatabase.CREATE_IF_NECESSARY);


        } catch (SQLiteException e) {

            finish();
        }
    }

    private void insertSomeDbData() {
        // create table: notes
        db.beginTransaction();
        try {
            // create table
            db.execSQL("create table notes ("
                    + " recID integer PRIMARY KEY autoincrement, "
                    + " caption text, " + " path text, " + " date text );   ");
            // commit your changes
            db.setTransactionSuccessful();


        } catch (SQLException e1) {
            finish();
        } finally {
            db.endTransaction();
        }

        // populate table: notes
        db.beginTransaction();
        try {

            // insert rows
            db.execSQL("insert into notes(caption, path, date) "
                    + " values ('Today I ate a huge burger, really donot wanna eat anything else.', 'bigburger.jpg', '2015-10-07 14:23' );");
            db.execSQL("insert into notes(caption, path, date) "
                    + " values ('Golden Gate Bridge is so beautiful, wonderful view here!', 'goldenbridge.jpg', '2015-11-02 16:23' );");

            // commit your changes
            db.setTransactionSuccessful();


        } catch (SQLiteException e2) {

        } finally {
            db.endTransaction();
        }

    }


    private void dropTable() {
        // (clean start) action query to drop table

        try {
            db.execSQL("DROP TABLE IF EXISTS notes;");

        } catch (Exception e) {
            finish();
        }
    }
    public void useInsertMethod(String caption, String path, String date) {
        // an alternative to SQL "insert into table values(...)"
        // ContentValues is an Android dynamic row-like container
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("caption", caption);
            initialValues.put("path", path);
            initialValues.put("date", date);

            db.insert("notes", null, initialValues);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void FirstRun() {
        SharedPreferences settings = this.getSharedPreferences("com.example.kenneth.MyPins", 0);
        boolean firstrun = settings.getBoolean("firstrun", true);
        if (firstrun) { // Checks to see if we've ran the application b4
            SharedPreferences.Editor e = settings.edit();
            e.putBoolean("firstrun", false);
            e.commit();
            // If not, run these methods:
            SetDirectory();

        } else { // Otherwise start the application here:
            return;
        }
    }

    /**
     * -- Check to see if the sdCard is mounted and create a directory w/in it
     * ========================================================================
     **/
    private void SetDirectory() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {

            extStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

            File txtDirectory = new File(extStorageDirectory + "/ken.mypins/");

            txtDirectory.mkdirs();// Have the object build the directory

            CopyAssets(); // Then run the method to copy the file.

        } else if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY)) {

            toast("SD CARD MISSING");
        }

    }

    // Copy the default pictures from the assets folder to the external storage

    private void CopyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
        for (int i = 0; i < files.length; i++) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(files[i]);
                out = new FileOutputStream(extStorageDirectory + "/ken.mypins/" + files[i]);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (Exception e) {
                Log.e("tag", e.getMessage());
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


}
