package com.example.kenneth.MyPins;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kenneth on 2/23/2016.
 */

public class Addnote extends MainActivity implements View.OnClickListener {

    final String albumName = "ken.mypins";
    String caption;
    String filename;
    String date;
    File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addnote);

        final EditText captiontxt = (EditText) findViewById(R.id.captionarea);
        Button button = (Button) findViewById(R.id.button);
        Button button1 = (Button) findViewById(R.id.save);
        button.setOnClickListener(this);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                caption = captiontxt.getText().toString();
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                useInsertMethod(caption, filename, date);
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(getApplicationContext(), "Cannot take pictures on this device!", Toast.LENGTH_SHORT).show();
            return;
        }

        imageFile = createImageFile();

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));

        startActivityForResult(intent, 1234);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1234) return;

        if (resultCode != Activity.RESULT_OK) {
            imageFile.delete();
            return;
        }

        try {
            InputStream is = new FileInputStream(imageFile);

            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageDrawable(Drawable.createFromStream(is, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private File createImageFile() {
        File image = null;
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_.jpg";
            filename = imageFileName;
            File storageDir = getAlbumStorageDir();
            image = new File(storageDir, imageFileName);
        } catch (Exception e) {
            Log.e("ken", "failed to create image file.  We will crash soon!");
            // we should do some meaningful error handling here !!!
        }
        return image;
    }

    public File getAlbumStorageDir() {
        // Same as Environment.getExternalStorageDirectory() + "/Pictures/" + albumName
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
        if (file.exists()) {
            Log.d("ken", "Album directory exists");
        } else if (file.mkdirs()) {
            Log.i("ken", "Album directory is created");
        } else {
            Log.e("ken", "Failed to create album directory.  Check permissions and storage.");
        }
        return file;
    }
}