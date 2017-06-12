package com.example.yls.test;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private static final int START_SHAKE = 0x1;
    private static final int AGAIN_SHAKE = 0x2;
    private static final int END_SHAKE = 0x3;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Vibrator mVibrator;//手机震动
    private boolean isShake = false;
    private LinearLayout mTopLayout;
    private LinearLayout mBottomLayout;
    private ImageView mTopLine;
    private ImageView mBottomLine;
    private MyHandler mHandler;
//    private int mWeiChatAudio;
//    private SoundPool mSoundPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //只竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        initView();
        mHandler = new MyHandler(this);

        //初始化SoundPool
//        mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
//        mWeiChatAudio = mSoundPool.load(this, R.raw.weichat_audio, 1);

        //获取Vibrator震动服务
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

    }

    private void initView() {

        mTopLayout = (LinearLayout) findViewById(R.id.main_linear_top);
        mBottomLayout = ((LinearLayout) findViewById(R.id.main_linear_bottom));
        mTopLine = (ImageView) findViewById(R.id.main_shake_top_line);
        mBottomLine = (ImageView) findViewById(R.id.main_shake_bottom_line);


        mTopLine.setVisibility(View.GONE);
        mBottomLine.setVisibility(View.GONE);


    }

    @Override
    protected void onStart() {
        super.onStart();
        //获取 SensorManager 负责管理传感器
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        if (mSensorManager != null) {
            //获取加速度传感器
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mAccelerometerSensor != null) {
                mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    protected void onPause() {
        // 务必要在pause中注销 mSensorManager
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        super.onPause();
    }




    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            //获取三个方向值
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];

            if ((Math.abs(x) > 17 || Math.abs(y) > 17 || Math
                    .abs(z) > 17) && !isShake) {
                isShake = true;
                //创建线程
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Log.d(TAG, "onSensorChanged: 摇动");

                            //开始震动 发出提示音 展示动画效果
                            mHandler.obtainMessage(START_SHAKE).sendToTarget();
                            Thread.sleep(500);
                            //再来一次震动提示
                            mHandler.obtainMessage(AGAIN_SHAKE).sendToTarget();
                            Thread.sleep(500);
                            mHandler.obtainMessage(END_SHAKE).sendToTarget();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    private static class MyHandler extends Handler {
        private WeakReference<MainActivity> mReference;
        private MainActivity mActivity;
        public MyHandler(MainActivity activity) {
            mReference = new WeakReference<MainActivity>(activity);
            if (mReference != null) {
                mActivity = mReference.get();
            }
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_SHAKE:
                    //This method requires the caller to hold the permission VIBRATE.
                    mActivity.mVibrator.vibrate(300);
                    //发出提示音
//                    mActivity.mSoundPool.play(mActivity.mWeiChatAudio, 1, 1, 0, 0, 1);
                    mActivity.mTopLine.setVisibility(View.VISIBLE);
                    mActivity.mBottomLine.setVisibility(View.VISIBLE);
                    mActivity.startAnimation(false);
                    break;
                case AGAIN_SHAKE:
                    mActivity.mVibrator.vibrate(300);
                    break;
                case END_SHAKE:
                    mActivity.isShake = false;
                    mActivity.startAnimation(true);
                    break;
            }
        }
    }


    private void startAnimation(boolean isBack) {
        int type = Animation.RELATIVE_TO_SELF;
        float topFromY;
        float topToY;
        float bottomFromY;
        float bottomToY;
        if (isBack) {
            topFromY = -0.5f;
            topToY = 0;
            bottomFromY = 0.5f;
            bottomToY = 0;
        } else {
            topFromY = 0;
            topToY = -0.5f;
            bottomFromY = 0;
            bottomToY = 0.5f;
        }


        TranslateAnimation topAnim = new TranslateAnimation(
                type, 0, type, 0, type, topFromY, type, topToY
        );
        topAnim.setDuration(200);
        topAnim.setFillAfter(true);
        TranslateAnimation bottomAnim = new TranslateAnimation(
                type, 0, type, 0, type, bottomFromY, type, bottomToY
        );
        bottomAnim.setDuration(200);
        bottomAnim.setFillAfter(true);

        //当要回来时, 我们中间的两根线需要GONE掉
        if (isBack) {
            bottomAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {

                    mTopLine.setVisibility(View.GONE);
                    mBottomLine.setVisibility(View.GONE);
                }
            });
        }
        mTopLayout.startAnimation(topAnim);
        mBottomLayout.startAnimation(bottomAnim);

    }


}