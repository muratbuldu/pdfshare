package com.example.muratbuldu.pdfshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button serveButton = (Button) findViewById(R.id.serve);
        Button beServedButton = (Button) findViewById(R.id.be_served);
        // go to serve activity
        serveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ServeOptions.class);
                startActivity(intent);
            }
        });
        // go to be served activity
        beServedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ListToBeServed.class);
                startActivity(intent);
            }
        });
    }

}
