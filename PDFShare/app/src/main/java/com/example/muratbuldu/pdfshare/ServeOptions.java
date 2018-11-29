package com.example.muratbuldu.pdfshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ServeOptions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serve_options);
        final EditText serveNameText = (EditText) findViewById(R.id.serve_name);
        final EditText fileNameText = (EditText) findViewById(R.id.file_name);
        Button serveButton = (Button) findViewById(R.id.start_serve);
        serveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),Serve.class);
                if(serveNameText.getText().toString().equals("")){
                    intent.putExtra("name","Presentation");
                }else{
                    intent.putExtra("name",serveNameText.getText().toString());
                }
                if(fileNameText.getText().toString().equals("")){
                    intent.putExtra("filename","target1.pdf");
                }else{
                    intent.putExtra("filename",fileNameText.getText().toString());
                }
                startActivity(intent);
            }
        });
    }
}
