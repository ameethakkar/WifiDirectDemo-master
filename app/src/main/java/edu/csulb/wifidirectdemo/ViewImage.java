package edu.csulb.wifidirectdemo;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

public class ViewImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        ImageView imageView = (ImageView) findViewById(R.id.image);
        Intent i = getIntent();
        String imagePath = i.getStringExtra("Path");
        if(imagePath !=null){
            Uri uri = Uri.parse(imagePath);
            imageView.setImageURI(uri);
        }
        else
            Toast.makeText(this, "Image path is not received", Toast.LENGTH_SHORT).show();

    }
}
