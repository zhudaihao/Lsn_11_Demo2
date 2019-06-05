package com.example.administrator.lsn_11_demo;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //测试 代理application 是否切换回主application
        Log.i("jett","activity:"+getApplication());
        Log.i("jett","activity:"+getApplicationContext());
        Log.i("jett","activity:"+getApplicationInfo().className);

        startService(new Intent(this, MyService.class));

        Intent intent = new Intent("com.dongnao.broadcast.test");
        intent.setComponent(new ComponentName(getPackageName(), MyBroadCastReciver.class.getName()));
        sendBroadcast(intent);

        getContentResolver().delete(Uri.parse("content://com.example.administrator.lsn_11_demo.MyProvider"), null, null);
    }


}
