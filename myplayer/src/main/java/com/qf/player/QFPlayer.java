package com.qf.player;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qf.myplayer.R;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by Ken on 2016/10/25.9:47
 * 封装的播放器
 */
public class QFPlayer extends RelativeLayout implements MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener {

    //activity对象
    private Activity activity;

    /**
     * 播放器对象
     */
    private VideoView videoView;
    private String url;//播放的路径

    /**
     * 底部控制栏
     */
    private LinearLayout bottomLayout;
    private ImageView ivPlay;//播放按钮
    private TextView tvBtimer, tvEtimer;
    private SeekBar seekbar;//进度
    private ImageView ivFullScreen;//切屏按钮

    /**
     * 中间层
     */

    //缓冲
    private LinearLayout centerBufferLayout;
    private ProgressBar centerProgressBar;
    private TextView centertv;

    //声音
    private LinearLayout centerSoundLayout;
    private ImageView centerSoundiv;
    private TextView centerSoundtv;

    //声音管理器
    private AudioManager audioManager;
    private int audio = -1;//当前的音量
    private int maxAudio;//最大的音量

    //亮度
    private LinearLayout centerLightLayout;
    private ImageView centerLightiv;
    private TextView centerLighttv;

    private float ligth = -1;//当前的亮度

    /**
     * 是否全屏
     */
    private boolean isFullScreen = false;

    /**
     * 缩放模式
     */
    private int scaletype = VideoView.VIDEO_LAYOUT_STRETCH;

    /**
     * 控件宽高
     */
    private int viedoWidth;
    private int videoHeight;

    /**
     * TODO Handler
     */
    private static final int SEND_MESSAGE_SETPROGRESS = 1;//设置当前的播放进度
    private static final int SEND_MESSAGE_HIDE_BOTTOM = 2;//隐藏底部的控件
    private Handler hander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SEND_MESSAGE_SETPROGRESS:
                    setProgress();
                    this.sendEmptyMessageDelayed(SEND_MESSAGE_SETPROGRESS, 1000);
                    break;
                case SEND_MESSAGE_HIDE_BOTTOM:
                    hideBottom();
                    break;
            }
        }
    };

    /**
     * 手势检测器
     */
    private GestureDetector gestureDetector;

    public QFPlayer(Context context) {
        this(context, null);
    }

    public QFPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QFPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(context instanceof Activity){
            this.activity = (Activity) context;
        }
        initView();
    }


    /**
     * TODO 初始化方法
     */
    private void initView() {
        //初始化维他密
        Vitamio.isInitialized(getContext());

        LayoutInflater.from(getContext()).inflate(R.layout.custem_player, this, true);

        //播放器
        videoView = (VideoView) findViewById(R.id.videoview);
        videoView.setOnPreparedListener(this);
        videoView.setOnInfoListener(this);
        videoView.setOnBufferingUpdateListener(this);
        videoView.setOnCompletionListener(this);

        //底部控件
        bottomLayout = (LinearLayout) findViewById(R.id.view_bottom_layout);
        ivPlay = (ImageView) findViewById(R.id.view_bottom_play);
        ivFullScreen = (ImageView) findViewById(R.id.view_bottom_fullscreen);
        tvBtimer = (TextView) findViewById(R.id.view_bottom_btime);
        tvEtimer = (TextView) findViewById(R.id.view_bottom_etime);
        seekbar = (SeekBar) findViewById(R.id.view_bottom_seek);

        ivPlay.setOnClickListener(this);
        seekbar.setOnSeekBarChangeListener(this);
        ivFullScreen.setOnClickListener(this);

        //中间控件
        centerBufferLayout = (LinearLayout) findViewById(R.id.view_center_buffer);
        centertv = (TextView) findViewById(R.id.view_center_jd);

        //声音相关
        centerSoundLayout = (LinearLayout) findViewById(R.id.view_sound_layout);
        centerSoundiv = (ImageView) findViewById(R.id.view_sound_iv);
        centerSoundtv = (TextView) findViewById(R.id.view_sound_tv);

        //获得系统的音频管理器
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        maxAudio = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//获得最大音量

        //亮度
        centerLightLayout = (LinearLayout) findViewById(R.id.view_light_layout);
        centerLightiv = (ImageView) findViewById(R.id.view_light_iv);
        centerLighttv = (TextView) findViewById(R.id.view_light_tv);

        //初始化手势检测器
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
    }

    /**
     * 设置伸缩模式
     * @param scale
     * @return
     */
    public QFPlayer setScaleType(int scale){
        this.scaletype = scale;
        return this;
    }

    /**
     * 播放的方法
     */
    public void play(String url){
        play(url, 0);
    }

    /**
     * 按进度播放
     * @param url
     * @param position
     */
    public void play(String url, int position){
        this.url = url;
        if(this.url != null){
            videoView.setVideoPath(url);
            if(position > 0){
                //设置进度
                videoView.seekTo(position);
            }

            //开始播放
            videoView.start();
            //正在播放中
            ivPlay.setImageResource(R.drawable.ic_pause);
            //设置当前的进度
            hander.sendEmptyMessageDelayed(SEND_MESSAGE_SETPROGRESS, 1000);
        }
    }

    /**
     * 准备就绪后回调
     * @param mp the MediaPlayer that is ready for playback
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        videoView.setVideoLayout(scaletype, 0);
        //获得宽高
        viedoWidth = getWidth();
        videoHeight = getHeight();
    }

    /**
     * TODO 状态改变的监听
     * @param mp    the MediaPlayer the info pertains to.
     * @param what  the type of info or warning.
     *              <ul>
     *              </ul>
     * @param extra an extra code, specific to the info. Typically implementation
     *              dependant.
     * @return
     */
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what){
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                //正在缓冲
                centerBufferLayout.setVisibility(VISIBLE);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                //缓冲结束
                centerBufferLayout.setVisibility(GONE);
                break;
        }
        return false;
    }

    /**
     * 缓冲回调方法
     * @param mp      the MediaPlayer the update pertains to
     * @param percent the percentage (0-100) of the buffer that has been filled thus
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        centertv.setText(percent + "%");
    }

    /**
     * TODO 控件的点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.view_bottom_play:
                //播放和暂停
                if(videoView != null && videoView.isPlaying()){
                    videoView.pause();
                    ivPlay.setImageResource(R.drawable.ic_play);
                } else {
                    videoView.start();
                    ivPlay.setImageResource(R.drawable.ic_pause);
                }
                break;
            case R.id.view_bottom_fullscreen:
                //横竖屏切换
                toggleScreen();
                break;
        }
    }

    /**
     * TODO 设置横竖屏
     */
    public void toggleScreen(){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            //当前为横屏
            if(activity != null){
                //设置屏幕为竖屏
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                ivFullScreen.setImageResource(R.drawable.ic_enlarge);
                isFullScreen = false;
            }
        } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            //当前为竖屏
            if(activity != null){
                //设置屏幕为横屏
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                ivFullScreen.setImageResource(R.drawable.ic_not_fullscreen);
                isFullScreen = true;
            }
        }
    }

    /**
     * 横竖屏切换时回调
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //隐藏底部状态栏
        hideBottom();
        if(isFullScreen){
            //横屏

            //隐藏状态栏
            if(activity != null){
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            //设置播放器的宽度
            int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;

            ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
            layoutParams.width = screenWidth;
            layoutParams.height = screenHeight;
            setLayoutParams(layoutParams);
        } else {
            //竖屏

            //显示状态栏
            if(activity != null){
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            //设置宽高
            ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
            layoutParams.width = viedoWidth;
            layoutParams.height = videoHeight;
            setLayoutParams(layoutParams);
        }

        //重新设置拉伸模式
        videoView.setVideoLayout(scaletype, 0);
    }

    /**
     * 拖动条方法
     * @param seekBar
     * @param progress
     * @param fromUser
     */
    boolean isTouch = false;
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        tvBtimer.setText(getTimer(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTouch = true;
        //撤销隐藏底部控件的消息
        hander.removeMessages(SEND_MESSAGE_HIDE_BOTTOM);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isTouch = false;
        int progress = seekBar.getProgress();
        videoView.seekTo(progress);

        //发送隐藏底部控件的消息
        hander.sendEmptyMessageDelayed(SEND_MESSAGE_HIDE_BOTTOM, 2000);
    }

    /**
     * 设置当前的播放进度
     */
    private void setProgress(){
        //设置进度条
        int position = (int) videoView.getCurrentPosition();//获得当前的进度
        int duration = (int) videoView.getDuration();//获得总耗时
        if(!isTouch) {//如果没有拖动
            if (seekbar != null) {
                seekbar.setMax(duration);
                seekbar.setProgress(position);
            }
            //设置文本
            tvBtimer.setText(getTimer(position));
            tvEtimer.setText(getTimer(duration));
        }
    }

    /**
     * 播放完成的回调
     * @param mp the MediaPlayer that reached the end of the file
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        //移除
        hander.removeMessages(SEND_MESSAGE_SETPROGRESS);
    }


    /**
     * TODO 手势监听器
     */

    private class GestureListener extends GestureDetector.SimpleOnGestureListener{
        //是否第一滑动
        private boolean isFirst = false;
        //处理的滑动方向
        private boolean isLand = false;
        //处理亮度和声音
        private boolean isLight = false;

        //按下
        @Override
        public boolean onDown(MotionEvent e) {
            isFirst = true;
            return true;
        }

        //轻按
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //显示或者隐藏底部和顶部的控件
            if(isShow(bottomLayout)){
                hideBottom();
            } else {
                showBottom(2000);
            }
            return true;
        }

        //双击
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        //滑动
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            float bx =  e1.getX();
            float by =  e1.getY();

            float ex =  e2.getX();
            float ey =  e2.getY();

            float mx = ex - bx;
            float my = by - ey;

            if(isFirst){
                //第一处理滑动
                if(Math.abs(distanceX) <= Math.abs(distanceY)){
                    //处理纵向滑动
                    isLand = false;
                    if(bx <= getWidth()/2){
                        //在左半屏
                        isLight = true;
                    } else {
                        isLight = false;
                    }
                } else {
                    //处理横向滑动
                    isLand = true;
                }

                isFirst = false;
            }

            //处理滑动事件
            if(isLand){
                //控制播放进度
            } else {
                //获得滑动的百分比
                float p = my / getHeight();
                if(isLight){
                    //控制亮度
                    setLight(p);
                } else {
                    //控制声音
                    setSound(p);
                }
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    /**
     * 设置音量
     * @param p
     */
    private void setSound(float p){
        if (audio == -1){
            audio = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//获得当前的音量
        }

        //获得当前的音量
        int newAudio = (int) (p * maxAudio + audio);
        if(newAudio > maxAudio){
            newAudio = maxAudio;
        } else if(newAudio < 0){
            newAudio = 0;
        }

        //设置音量
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newAudio, 0);


        //显示中间的控件
        String s = (int)(((float)newAudio/maxAudio) * 100) + "%";
        centerSoundLayout.setVisibility(VISIBLE);
        centerSoundtv.setText(s);
        centerSoundiv.setImageResource(newAudio == 0 ? R.drawable.ic_volume_off_white_36dp : R.drawable.ic_volume_up_white_36dp);
    }


    /**
     * 设置亮度
     */
    public void setLight(float p){
        if(ligth == -1){
            ligth = activity.getWindow().getAttributes().screenBrightness;//获得当前的亮度 0 ~ 1
        }

        float newligth = p * 1 + ligth;
        if(newligth > 1){
            newligth = 1;
        } else if(newligth < 0.01){
            newligth = 0.01f;
        }

        //设置亮度
        WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();
        attributes.screenBrightness = newligth;
        activity.getWindow().setAttributes(attributes);

        //控制文本
        centerLightLayout.setVisibility(VISIBLE);
        centerLighttv.setText((int)(newligth * 100) + "%");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(gestureDetector.onTouchEvent(event)){
            return true;
        }

        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                audio = -1;
                ligth = -1;
                centerSoundLayout.setVisibility(GONE);
                centerLightLayout.setVisibility(GONE);
                break;
        }
        return false;
    }

    /**
     * 显示底部控件
     */
    private void showBottom(){
        showBottom(0);
    }

    /**
     * 显示多少毫秒
     * @param timer
     */
    private void showBottom(long timer){
        if(bottomLayout != null && bottomLayout.getVisibility() == GONE){
            bottomLayout.setVisibility(VISIBLE);
            if(timer > 0){
                hander.sendEmptyMessageDelayed(SEND_MESSAGE_HIDE_BOTTOM, timer);
            }
        }
    }

    /**
     * 把毫秒数转换成00:00:00
     * @param timer
     * @return
     */
    private String getTimer(long timer){
        int h = (int) (timer / 1000 / 60 / 60);
        int m = (int) ((timer / 1000 / 60 % 60));
        int s = (int) (timer / 1000 % 60);
        return (h >= 10 ? h : ("0" + h)) + ":" + (m >= 10 ? m : ("0" + m)) + ":" + (s >= 10 ? s : ("0" + s));
    }

    /**
     * 隐藏底部控件
     */
    private void hideBottom(){
        if(bottomLayout != null && bottomLayout.getVisibility() == VISIBLE){
            bottomLayout.setVisibility(GONE);
        }
    }

    /**
     * 设置Activity
     * @param activity
     */
    public QFPlayer setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    /**
     * 是否显示
     * @return
     */
    private boolean isShow(View view){
        return view.getVisibility() == VISIBLE;
    }

    /**
     * 声明周期方法
     */
    int currentPosition = -1;
    public void onPause(){
        if(videoView != null){
            videoView.pause();
            //记录当前的播放位置
            currentPosition = (int) videoView.getCurrentPosition();
        }
    }

    public void onResume(){
        if(videoView != null){
            videoView.resume();
            //根据记录的位置重新开始播放
            if(currentPosition != -1){
                play(this.url, currentPosition);
            }
        }
    }

    public void onDestory(){
        if(videoView != null){
            //清空handler中的所有事件
            hander.removeCallbacksAndMessages(null);
            videoView.stopPlayback();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && isFullScreen){
            toggleScreen();
            return true;
        }
        return false;
    }
}
