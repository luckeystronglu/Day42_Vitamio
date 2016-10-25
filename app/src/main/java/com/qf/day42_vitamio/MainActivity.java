package com.qf.day42_vitamio;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private String path = "http://10.0.2.2:8080/AndroidServer/file/cmdsj.rmvb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化唯她蜜
        Vitamio.isInitialized(getApplicationContext());

        setContentView(R.layout.activity_main);

        videoView = (VideoView) findViewById(R.id.videoview);
        videoView.setVideoPath(path);
        videoView.setMediaController(new MediaController(this));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //视频准备完成
                Log.d("print", "------>" + videoView.getVideoWidth() + "  "  + videoView.getVideoHeight());

                //设置拉伸模式
//                videoView.setVideoLayout(VideoView.VIDEO_LAYOUT_ZOOM, 0);

                //设置视频的播放速度 0.5 ~ 2.0
//                mp.setPlaybackSpeed(0.5f);
            }
        });

        videoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                Log.d("print", "---------->视频缓冲：" + percent);
            }
        });
        videoView.start();
    }
}
