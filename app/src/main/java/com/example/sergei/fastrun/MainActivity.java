package com.example.sergei.fastrun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private TextView timer_text;
    private Button btn_ready;
    private Button btn_stop;
    private Intent runService;
    private ResponseReceiver receiver;
    private static final String apiUrl = "http://192.168.0.102:4567/run";
    private String NAME;


    /**
     * Broadcaster
     */

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP = "com.sergei.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                final String TIME = extras.getString(RunService.PARAM_OUT_MSG_TIMER);
                final String STEPS = extras.getString(RunService.PARAM_OUT_MSG_STEPS);
                timer_text.setText(TIME);
                try {
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("name", NAME);
                    jsonobj.put("time", TIME);
                    jsonobj.put("steps", STEPS);
                    runPost(jsonobj.toString());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    String runGet() throws IOException {
        final String[] res = {""};
        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }
                System.out.println(response.body().string());
                res[0] = response.body().string();
            }
        });
        return res[0];
    }

    String runPost(String json) throws IOException {
        final String[] res = {""};
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }
                System.out.println(response.body().string());
                res[0] = response.body().string();
            }
        });
        return res[0];
    }

    /**
     * MainActivity
     */

    @Override
    protected void onResume() {
        super.onResume();
        //stepCounter.resume();
//        activityRunning = true;
//        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
//        if (countSensor != null) {
//            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
//        } else {
//            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
//        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //stepCounter.pause();
        // if you unregister the last listener, the hardware will stop detecting step events
        // sensorManager.unregisterListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        btn_ready = (Button) findViewById(R.id.ready_button);
        btn_stop = (Button) findViewById(R.id.stop_button);
        timer_text = (TextView) findViewById(R.id.timer_text);
        setSupportActionBar(toolbar);
        btn_stop.setVisibility(View.INVISIBLE);

        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
        runService = new Intent(getBaseContext(), RunService.class);

        btn_ready.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startService(runService);
                btn_ready.setVisibility(View.INVISIBLE);
                btn_stop.setVisibility(View.VISIBLE);
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(runService);
                btn_stop.setVisibility(View.INVISIBLE);
                btn_ready.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
