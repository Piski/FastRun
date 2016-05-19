package com.example.sergei.fastrun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private TextView steps_text;
    private FloatingActionButton btn_ready;
    private FloatingActionButton btn_stop;
    private Intent runService;
    private ResponseReceiver receiver;
    private CoordinatorLayout mParentLayout = null;
    private static final String apiUrl = "http://100.71.4.231:4567/run";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    private String myTime;

    /**
     * Broadcaster
     */

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP = "com.sergei.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {

                Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);

                final String TIME = extras.getString(RunService.PARAM_OUT_MSG_TIMER);
                final String STEPS = extras.getString(RunService.PARAM_OUT_MSG_STEPS);
                final String NAME = android.os.Build.MODEL;
                timer_text.setVisibility(View.VISIBLE);
                timer_text.setText(TIME);
                steps_text.setText("Steps: " + (int)Double.parseDouble(STEPS));
                myTime = TIME;
                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                    Date date = new Date();

                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("name", NAME + "-" + dateFormat.format(date));
                    jsonobj.put("time", TIME);
                    jsonobj.put("steps", STEPS);
                    runPost(jsonobj.toString());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }
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
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        btn_ready = (FloatingActionButton) findViewById(R.id.ready_button);
        btn_stop = (FloatingActionButton) findViewById(R.id.stop_button);
        timer_text = (TextView) findViewById(R.id.timer_text);
        steps_text = (TextView) findViewById(R.id.steps_text);
        btn_stop.setVisibility(View.INVISIBLE);

        mParentLayout = (CoordinatorLayout) findViewById(R.id.main);
        mParentLayout.setOnTouchListener(new OnTouchSwipeListener(getApplicationContext()) {
            public void onSwipeRight() {
                Intent i = new Intent(MainActivity.this, ScoreBoard.class);
                i.putExtra("time", myTime);
                startActivity(i);
            }
            public void onSwipeLeft() {
                Intent i = new Intent(MainActivity.this, ScoreBoard.class);
                startActivity(i);
            }
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

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
                timer_text.setVisibility(View.INVISIBLE);
                startAnim();
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(runService);
                btn_stop.setVisibility(View.INVISIBLE);
                btn_ready.setVisibility(View.VISIBLE);
                stopAnim();
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

    void startAnim(){
        findViewById(R.id.avloadingIndicatorView).setVisibility(View.VISIBLE);
    }

    void stopAnim(){
        findViewById(R.id.avloadingIndicatorView).setVisibility(View.GONE);
    }
}
