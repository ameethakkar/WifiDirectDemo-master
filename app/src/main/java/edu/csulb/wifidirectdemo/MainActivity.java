package edu.csulb.wifidirectdemo;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bluetooth = (Button) findViewById(R.id.bluetooth);
        final Button wifi = (Button) findViewById(R.id.wifi);
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,BluetoothMainActivity.class);
                startActivity(i);
            }
        });

        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, WifiDirectActivity.class);
                startActivity(i);
            }
        });
        getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI, true,
                new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        onChange(selfChange,null);
                        Log.d("your_tag","Internal Media has been changed");
                    }

                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        //  super.onChange(selfChange, uri);
                        Log.d("your_tag","Internal Media has been changed");
                        Log.d("your_image",uri.toString());
                        String path = getPath(uri);
                        Log.d ("your_path",path);
                        uri = Uri.parse(path);
                        Long timestamp = readLastDateFromMediaStore(getApplicationContext(), MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                        // comapare with your stored last value and do what you need to do
                        if(timestamp>0){
                            Toast.makeText(getApplicationContext(),"Something is changed"+timestamp,Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(MainActivity.this,ViewImage.class);
                            i.putExtra("Path",path);
                            startActivity(i);
                        }
                    }
                }
        );
        getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
                new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        onChange(selfChange,null);
                        Log.d("your_tag","External Media has been changed");
                    }

                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        // super.onChange(selfChange, uri);
                        Log.d("your_tag","External Media has been changed");
                        Log.d("your_image",uri.toString());
                        String path = getPath(uri);
                        Log.d ("your_path",path);
                        uri = Uri.parse(path);
                        Long timestamp = readLastDateFromMediaStore(getApplicationContext(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        // comapare with your stored last value and do what you need to do
                        if(timestamp>0){
                            Toast.makeText(getApplicationContext(),"Something is changed"+timestamp,Toast.LENGTH_SHORT).show();
                          Intent i = new Intent(MainActivity.this,ViewImage.class);
                            i.putExtra("Path",path);
                            startActivity(i);
                        }
                    }
                }
        );
    }

    private Long readLastDateFromMediaStore(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, "date_added DESC");
        //PhotoHolder media = null;
        Long dateAdded = 0L;
        if (cursor.moveToNext()) {
            dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED));
        }
        cursor.close();
        return dateAdded;
    }

    public String getPath(Uri uri) {
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, MediaStore.Images.Media.DATE_ADDED + " ASC");
        if (cursor != null) {
            cursor.moveToLast();
            return cursor.getString(0);
            //  while (cursor.moveToNext()) {
            //      Uri imageUri = Uri.parse(cursor.getString(0));
            //  }
            //  cursor.close();
        }
        else return "No Path";
    }


}
