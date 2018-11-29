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
import java.net.Socket;

public class Serve extends AppCompatActivity {
    private static final int PORT = Config.PORT;
    private static final String serverHostname = Config.serverHostname;
    private static String FILENAME = "target1.pdf";
    private int pageIndex;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor parcelFileDescriptor;
    private ImageView imageViewPdf;
    private FloatingActionButton prePageButton;
    private FloatingActionButton nextPageButton;
    private Socket s;
    private int id;
    private String channelName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serve);
        // get name from intent
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                channelName = "Presentation";
                FILENAME = "target1.pdf";
            } else {
                channelName = extras.getString("name");
                FILENAME = extras.getString("filename");
            }
        } else {
            channelName = (String) savedInstanceState.getSerializable("name");
            FILENAME = (String) savedInstanceState.getSerializable("filename");
        }
        Log.d("names", "onCreate: "+channelName+FILENAME);
        // get component pointers
        imageViewPdf = (ImageView) findViewById(R.id.pdf_image);
        prePageButton =
                (FloatingActionButton) findViewById(R.id.button_pre_doc);
        nextPageButton =
                (FloatingActionButton) findViewById(R.id.button_next_doc);
        pageIndex = 0;

        // set listeners to next and prev buttons
        prePageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPage(currentPage.getIndex() - 1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                            out.println("setPage;"+id+";"+pageIndex);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPage(currentPage.getIndex() + 1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                            out.println("setPage;"+id+";"+pageIndex);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });
        // open first page
        try {
            openRenderer(getApplicationContext());
            showPage(pageIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    s = new Socket(serverHostname, PORT);
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    out.println("new;"+channelName+";"+FILENAME);
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            s.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null)
                    {
                        String[] parts = inputLine.split(";");
                        if(parts[0].equals("id")){
                            id = Integer.parseInt(parts[1]);
                        }
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
        if(pdfRenderer != null) {
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
        updateUi();
    }

    private void updateUi() {
        int index = currentPage.getIndex();
        int pageCount = pdfRenderer.getPageCount();
        prePageButton.setEnabled(0 != index);
        nextPageButton.setEnabled(index + 1 < pageCount);
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
        outState.putString("name", channelName);
        outState.putString("filename", FILENAME);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }
}
