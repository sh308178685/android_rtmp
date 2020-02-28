package com.example.myapplication2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity  {

    private Context context;
    boolean isrun = false;//用来标记录屏的状态
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;//录制视频的工具
    private int width, height, dpi;//屏幕宽高和dpi，后面会用到
    private ScreenRecorder screenRecorder;//这个是自己写的录视频的工具类，下文会放完整的代码
    Thread thread;//录视频要放在线程里去执行
    private Button btn;
    private EditText efu;
    private SharedPreferences sp;
    private String rtmpUrl = "rtmp://192.168.1.5:1935/live/555";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // restore data.
        sp = getSharedPreferences("Yasea", MODE_PRIVATE);
        rtmpUrl = sp.getString("rtmpUrl", rtmpUrl);
        // initialize url.
         efu = (EditText) findViewById(R.id.url);
        efu.setText(rtmpUrl);


        btn = (Button) findViewById(R.id.publish);



        context = this;
        mediaProjectionManager = (MediaProjectionManager) context.getSystemService(MEDIA_PROJECTION_SERVICE);
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        //width = outMetrics.widthPixels;
        //height = outMetrics.heightPixels;
        width = 640;
        height = 480;
        dpi = outMetrics.densityDpi;
    }

    public void click(View v)
    {
        if(!isrun) {

            //btn.setText("stop");
            rtmpUrl = efu.getText().toString();
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("rtmpUrl", rtmpUrl);
            editor.apply();
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 102);
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 103);
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 104);
            }

            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                intent = mediaProjectionManager.createScreenCaptureIntent();
                startActivityForResult(intent, 101);//正常情况是要执行到这里的,作用是申请捕捉屏幕
            } else {
                //ShowUtil.showToast(context, "Android版本太低，无法使用该功能");
            }
        }
        else

        {
            btn.setText("start");
            screenRecorder.stop();
        }
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == 102) {
            Toast.makeText(context, "缺少读写权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == 103) {
            Toast.makeText(context, "缺少录音权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == 104) {
            Toast.makeText(context, "缺少相机权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode != 101) {
            Log.e("HandDrawActivity", "error requestCode =" + requestCode);
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(context, "捕捉屏幕被禁止", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection != null) {
            screenRecorder = new ScreenRecorder(rtmpUrl,width, height, mediaProjection, dpi);
        }
        thread = new Thread() {
            @Override
            public void run() {
                screenRecorder.startRecorder();//跟ScreenRecorder有关的下文再说，总之这句话的意思就是开始录屏的意思
            }
        };
        thread.start();
        //binding.startPlayer.setText("停止");//开始和停止我用的同一个按钮，所以开始录屏之后把按钮文字改一下
        isrun = true;//录屏状态改成真
        btn.setText("stop");

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
