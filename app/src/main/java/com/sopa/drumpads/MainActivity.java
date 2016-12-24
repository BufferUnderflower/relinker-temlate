package com.sopa.drumpads;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.getkeepsafe.relinker.ReLinker;
import com.getkeepsafe.relinker.elf.ElfParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final static String TAG= "MainActivity";
    private ReLinker.Logger logcatLogger = new ReLinker.Logger() {
        @Override
        public void log(String message) {
            Log.d("ReLinker", message);
        }
    };


            // Used to load the 'native-lib' library on application startup.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      relink();


        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public void relink(){
    try{
        ReLinker.loadLibrary(this,"native-lib");
    }
    catch (UnsatisfiedLinkError e) {

        ReLinker.log(logcatLogger)
                .force()
                .recursively()
                .loadLibrary(MainActivity.this, "native-lib",
                        new ReLinker.LoadListener() {
                            @Override
                            public void success() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG,"success");
                                    }
                                });

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            final String
                                                file = "native-lib.so";

                                            final File filesDir = getDir("lib", MODE_PRIVATE);
                                            final File lib = new File(filesDir, file);
                                            if (!lib.exists()) return;

                                            final ElfParser parser = new ElfParser(lib);
                                            final List<String> deps = parser.parseNeededDependencies();
                                            final StringBuilder builder = new StringBuilder("Library dependencies:\n");
                                            for (final String str : deps) {
                                                builder.append(str).append(", ");
                                            }
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                   Log.d("",builder.toString());
                                                }
                                            });
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }

                            @Override
                            public void failure(Throwable t) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((TextView) findViewById(R.id.text)).setText(
                                                "Couldn't load! Report this issue to the github please!");
                                    }
                                });
                            }
                        });
    }
}
}
