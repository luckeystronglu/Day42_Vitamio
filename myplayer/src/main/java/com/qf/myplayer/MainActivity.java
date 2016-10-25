package com.qf.myplayer;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.qf.player.QFPlayer;

public class MainActivity extends AppCompatActivity {

    private QFPlayer qfPlayer;
//    private String path = "http://baobab.wandoujia.com/api/v1/playUrl?vid=2614&editionType=normal";
    private String path = "http://10.20.153.219:8080/AndroidServer/file/cmdsj.rmvb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qfPlayer = (QFPlayer) findViewById(R.id.player);
        qfPlayer.play(path);
    }

    /**
     * 横竖屏切换时会调用该方法
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(qfPlayer != null){
            qfPlayer.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(qfPlayer != null){
            qfPlayer.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(qfPlayer != null){
            qfPlayer.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(qfPlayer != null){
            qfPlayer.onDestory();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(qfPlayer != null && qfPlayer.onKeyDown(keyCode, event)){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
