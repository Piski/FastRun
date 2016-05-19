package com.example.sergei.fastrun;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ScoreBoard extends AppCompatActivity {
    private CoordinatorLayout mParentLayout = null;
    private static final String apiUrl = "http://100.71.4.231:4567/run";
    private TextView ranking, first, second, third, me, first_text, second_text, third_text, me_text;
    OkHttpClient client = new OkHttpClient();
    private String myTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);
        mParentLayout = (CoordinatorLayout) findViewById(R.id.score);
        startAnim();
        myTime = getIntent().getStringExtra("time");

        ranking = (TextView) findViewById(R.id.ranking);
        first = (TextView) findViewById(R.id.first);
        second = (TextView) findViewById(R.id.second);
        third = (TextView) findViewById(R.id.third);
        me = (TextView) findViewById(R.id.me);
        first_text = (TextView) findViewById(R.id.first_text);
        second_text = (TextView) findViewById(R.id.second_text);
        third_text = (TextView) findViewById(R.id.third_text);
        me_text = (TextView) findViewById(R.id.me_text);

        mParentLayout.setOnTouchListener(new OnTouchSwipeListener(getApplicationContext()) {
            public void onSwipeRight() {
                Intent i = new Intent(ScoreBoard.this, MainActivity.class);
                startActivity(i);
            }
            public void onSwipeLeft() {
                Intent i = new Intent(ScoreBoard.this, MainActivity.class);
                startActivity(i);
            }
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        try {
            runGet();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

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
                String[] items = response.body().string()
                                    .replaceAll("\\[", "")
                                    .replaceAll("\\]", "")
                                    .split("\\, ");

                final JSONObject[] results = new JSONObject[items.length];
                for (int i = 0; i < items.length; i++) {
                    try {
                        results[i] = new JSONObject(items[i]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showRanking();
                        try {
                            first_text.setText(results[0].getString("time"));
                            second_text.setText(results[1].getString("time"));
                            third_text.setText(results[2].getString("time"));
                            if(myTime != null && myTime.length() > 0) {
                                me_text.setText(myTime);
                            } else {
                                me_text.setVisibility(View.INVISIBLE);
                                me.setVisibility(View.INVISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                stopAnim();

            }
        });
        return res[0];
    }

    private void showRanking() {
        ranking.setVisibility(View.VISIBLE);
        first.setVisibility(View.VISIBLE);
        second.setVisibility(View.VISIBLE);
        third.setVisibility(View.VISIBLE);
        me.setVisibility(View.VISIBLE);
        first_text.setVisibility(View.VISIBLE);
        second_text.setVisibility(View.VISIBLE);
        third_text.setVisibility(View.VISIBLE);
        me_text.setVisibility(View.VISIBLE);
    }

    void startAnim(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.avloadingIndicatorView2).setVisibility(View.VISIBLE);
            }
        });

    }

    void stopAnim(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.avloadingIndicatorView2).setVisibility(View.GONE);
            }
        });
    }
}
