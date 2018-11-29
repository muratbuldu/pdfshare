package com.example.muratbuldu.pdfshare;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListToBeServed extends AppCompatActivity {
    private static final int PORT = Config.PORT;
    private static final String serverHostname = Config.serverHostname;
    private Socket s;
    private PrintWriter out;
    private BufferedReader in;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_to_be_served);
        ListView listview = (ListView) findViewById(R.id.listview);
        final ArrayList<String> list = new ArrayList<String>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                Intent intent = new Intent(getApplicationContext(),BeServed.class);
                intent.putExtra("name",""+position);
                startActivity(intent);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    s = new Socket(serverHostname, PORT);
                    out = new PrintWriter(s.getOutputStream(), true);
                    out.println("list");
                    in = new BufferedReader(new InputStreamReader(
                            s.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null)
                    {
                        final String finalInputLine = inputLine;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String[] parts = finalInputLine.split(";");
                                    for (int i = 0; i < parts.length; ++i) {
                                        list.add(parts[i]);
                                    }
                                    adapter.notifyDataSetChanged();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.e("asdasdasdsd","asdsad");
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
