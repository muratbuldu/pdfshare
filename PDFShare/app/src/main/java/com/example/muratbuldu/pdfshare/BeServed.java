package com.example.muratbuldu.pdfshare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class BeServed extends AppCompatActivity {
    private static final int PORT = Config.PORT;
    private static final String serverHostname = Config.serverHostname;
    private static String FILENAME = "target1.pdf";
    private int pageIndex;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor parcelFileDescriptor;
    private ImageView imageViewPdf;
    private String id;
    private Socket s;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_be_served);
        // get name from intent
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                id= "0";
            } else {
                id = extras.getString("name");
            }
        } else {
            id = (String) savedInstanceState.getSerializable("name");
        }
        // get component pointers
        imageViewPdf = (ImageView) findViewById(R.id.pdf_image);
        pageIndex = 0;

        // open first page
        /*
        try {
            openRenderer(getApplicationContext());
            showPage(pageIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    s = new Socket(serverHostname, PORT);
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    out.println("beServed;"+id);
                    BufferedReader in = new BufferedReader(new InputStreamReader(
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
                                    if(parts[0].equals("init")){
                                        pageIndex = Integer.parseInt(parts[1]);
                                        FILENAME = parts[2];
                                        openRenderer(getApplicationContext());
                                        showPage(pageIndex);
                                    }else if(parts[0].equals("page")){
                                        pageIndex = Integer.parseInt(parts[1]);
                                        showPage(pageIndex);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void openRenderer(Context context) throws IOException {
        // In this sample, we read a PDF from the assets directory.
        File file = new File(context.getCacheDir(), FILENAME);
        if (!file.exists()) {
            InputStream asset = context.getAssets().open(FILENAME);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        if (parcelFileDescriptor != null) {
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        }
    }

    private void closeRenderer() throws IOException {
        if (null != currentPage) {
            currentPage.close();
        }
        if(pdfRenderer != null){
            pdfRenderer.close();
            parcelFileDescriptor.close();
        }
    }

    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index) {
            return;
        }
        if (null != currentPage) {
            currentPage.close();
        }
        currentPage = pdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(),
                Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        imageViewPdf.setImageBitmap(bitmap);
        pageIndex = index;
    }


    @Override
    public void onStop() {
        try {
            closeRenderer();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("name", id);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }
}
