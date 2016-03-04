package com.example.kenneth.MyPins;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class NotesCursorAdaptor extends CursorAdapter {

    private LayoutInflater cursorInflater;


    public NotesCursorAdaptor(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.custom_row, parent, false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView path = (TextView) view.findViewById(R.id.label);
            TextView dateview = (TextView) view.findViewById(R.id.date);
            // Extract properties from cursor
            String img = cursor.getString(cursor.getColumnIndex("path"));
            String words = cursor.getString(cursor.getColumnIndex("caption"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String lat = cursor.getString(cursor.getColumnIndex("lat"));
            String lng = cursor.getString(cursor.getColumnIndex("lng"));
            // Populate fields with extracted properties

            path.setText(words);
            dateview.setText(getTimeAgo(date));
            String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ken.mypins/" + img;

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 16; // Experiment with different sizes
                Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);
                ImageView imageView = (ImageView) view.findViewById(R.id.icon);

                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(String date) {

        String currentdate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date cDate = null;
        Date mDate = null;
        try {
            cDate = sdf.parse(currentdate);
            mDate = sdf.parse(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        long now = cDate.getTime();
        long time = mDate.getTime();
        System.out.println("Date in milli :: " + time);


        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "Just Now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1 min";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " mins";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days";
        }
    }


}
