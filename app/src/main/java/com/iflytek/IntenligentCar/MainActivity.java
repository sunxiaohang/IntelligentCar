package com.iflytek.IntenligentCar;
/***
 * AlphaAnimation渐变透明度动画效果
 * RotateAnimation画面转移旋转动画效果
 * ScaleAnimation简便尺寸伸缩动画效果
 * AnimationSet动画容器
 * TranslateAnimation画面转换位置移动动画
 * Animation.AnimationListenter
 * {
 * onAnimationEnd()
 * onAnimationRepeat()
 * onAnimationStart()
 * }
 */

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.telecom.Connection;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by m1320 on 2016/9/4.
 */
public class MainActivity extends Activity implements SensorEventListener {
    private FloatingActionButton add;
    private FloatingActionButton autocontrol;
    private FloatingActionButton handcontrol;
    private FloatingActionButton voicecontrol;
    private boolean fabstatus = true;
    private SensorManager sensorManager;
    private ImageView position_circle;
    private float[] gravity = new float[3];
    private LinearLayout.LayoutParams layoutParams;
    private boolean display_status = true;
    private RelativeLayout rl;
    private WebView video_display;
    private String video_display_url = "http://192.168.1.88:8080/?action=stream";
    private Button btn_up;
    private Button btn_down;
    private Button btn_left;
    private Button btn_right;
    private Button btn_scalel;
    private Button btn_scales;
    private Button btn_turnl;
    private Button btn_turnr;
    private boolean up = true;
    private boolean down = true;
    private boolean left = true;
    private boolean right = true;
    private boolean stop = true;
    private FloatingActionButton loadvideo;
    private FloatingActionButton stopvideo;
    private Button btn_voiceControl;
    private Button btn_voiceChat;
    private final String requestAddress="http://op.juhe.cn/robot/index?info=";
    private final String key="&key=12efe116cb1528ac08e5d4b36c8d503d";
    /***
     * 创建语音识别对象
     */
    private static String TAG = "IatDemo";
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    //输出提示信息
    private StringBuffer mResultText;
    private Toast mToast;
    int ret=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mResultText=new StringBuffer();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /***
         * 初始化语音识别
         */
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener);//语音识别监听器
        mIatDialog = new RecognizerDialog(this,mInitListener);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        /***
         * 控制按键事件监听
         */
        btn_up = (Button) findViewById(R.id.btn_up);
        btn_down = (Button) findViewById(R.id.btn_down);
        btn_left = (Button) findViewById(R.id.btn_left);
        btn_right = (Button) findViewById(R.id.btn_right);
        btn_scalel = (Button) findViewById(R.id.btn_scacleL);
        btn_scales = (Button) findViewById(R.id.btn_scacleS);
        btn_turnl = (Button) findViewById(R.id.btn_turnl);
        btn_turnr = (Button) findViewById(R.id.btn_turnr);
        btn_voiceChat = (Button) findViewById(R.id.btn_voiceChat);
        btn_voiceControl = (Button) findViewById(R.id.btn_voiceControl);
        /***
         * 向左旋转按钮setOnTouchListener监听
         */
        btn_turnl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        SendControlCode sc = new SendControlCode(111);
                        sc.execute();
                        break;
                    case MotionEvent.ACTION_UP:
                        SendControlCode sc1 = new SendControlCode(115);
                        sc1.execute();
                        break;
                }
                return false;
            }
        });
        /***
         * 向右旋转setOnTouchListener监听
         */
        btn_turnr.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        SendControlCode sc = new SendControlCode(112);
                        sc.execute();
                        break;
                    case MotionEvent.ACTION_UP:
                        SendControlCode sc1 = new SendControlCode(116);
                        sc1.execute();
                        break;
                }
                return false;
            }
        });
        /***
         * 前进setOnTouchListener事件监听
         */
        btn_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        SendControlCode sc = new SendControlCode(1);
                        sc.execute();
                        break;
                    case MotionEvent.ACTION_UP:
                        SendControlCode sc1 = new SendControlCode(5);
                        sc1.execute();
                        break;
                }
                return false;
            }
        });
        /***
         * 后退setOnTouchListener监听
         */
        btn_down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        SendControlCode sc = new SendControlCode(2);
                        sc.execute();
                        break;
                    case MotionEvent.ACTION_UP:
                        SendControlCode sc1 = new SendControlCode(6);
                        sc1.execute();
                        break;
                }
                return false;
            }
        });
        /***
         * 舵机向左旋转setOnTouchListener监听
         */
        btn_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        SendControlCode sc = new SendControlCode(3);
                        sc.execute();
                        break;
                    case MotionEvent.ACTION_UP:
                        SendControlCode sc1 = new SendControlCode(7);
                        sc1.execute();
                        break;
                }
                return false;
            }
        });
        /***
         * 舵机向右旋转setOnTouchListener监听
         */
        btn_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        SendControlCode sc = new SendControlCode(4);
                        sc.execute();
                        break;
                    case MotionEvent.ACTION_UP:
                        SendControlCode sc1 = new SendControlCode(8);
                        sc1.execute();
                        break;
                }
                return false;
            }
        });
        /***
         * camera舵机收缩setOnTouchListener监听
         */
        btn_scalel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        SendControlCode sc = new SendControlCode(113);
                        sc.execute();
                        break;
                    case MotionEvent.ACTION_UP:
                        SendControlCode sc1 = new SendControlCode(117);
                        sc1.execute();
                        break;
                }
                return false;
            }
        });
        /****
         * camera舵机伸展setOnTouchListener监听
         */
        btn_scales.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        SendControlCode sc = new SendControlCode(114);
                        sc.execute();
                        break;
                    case MotionEvent.ACTION_UP:
                        SendControlCode sc1 = new SendControlCode(118);
                        sc1.execute();
                        break;
                }
                return false;
            }
        });
        /***
         * 语音控制按钮点击事件监听
         */
        btn_voiceControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        /***
         * 语音交流按钮点击事件监听
         */
        btn_voiceChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( null == mIat ){
                    showTip( "创建对象失败，请确认 libmsc.so 放置正确，\n 且有调用 createUtility 进行初始化" );
                    return;
                }
                mResultText.delete(0,mResultText.length());
//                mIat.stopListening();
                getVoiceContent();
//                System.out.println(result+"***************");

            }
        });

        /***
         * 视频传输显示
         */
        video_display = (WebView) findViewById(R.id.video_display);
//        video_display.getSettings().setUseWideViewPort(true);
//        video_display.getSettings().setLoadWithOverviewMode(true);
        //video_display.loadUrl(video_display_url);
        /***
         * 视频加载停止控制
         *
         */
        loadvideo = (FloatingActionButton) findViewById(R.id.loadvideo);
        stopvideo = (FloatingActionButton) findViewById(R.id.stopvideo);
        loadvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendControlCode sc = new SendControlCode(110);
                sc.execute();
                loadvideo.setVisibility(View.GONE);
                stopvideo.setVisibility(View.VISIBLE);
                CustomAnimation ca = new CustomAnimation();
                stopvideo.startAnimation(ca);
                video_display.loadUrl(video_display_url);
            }
        });
        stopvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendControlCode sc = new SendControlCode(119);
                sc.execute();
                stopvideo.setVisibility(View.GONE);
                loadvideo.setVisibility(View.VISIBLE);
                CustomAnimation ca = new CustomAnimation();
                loadvideo.startAnimation(ca);
            }
        });
        /***
         * 自控控制，手动控制切换
         */
        add = (FloatingActionButton) findViewById(R.id.addfunction);
        autocontrol = (FloatingActionButton) findViewById(R.id.phone);
        handcontrol = (FloatingActionButton) findViewById(R.id.mail);
        voicecontrol = (FloatingActionButton) findViewById(R.id.phones);
        position_circle = (ImageView) findViewById(R.id.position_circle);
        layoutParams = (LinearLayout.LayoutParams) position_circle.getLayoutParams();
        /***
         * 获取sendorManager
         */
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabstatus == true) {
                    fabstatus = false;
                    ObjectAnimator.ofFloat(autocontrol, "translationX", 0.0F, -200.0F).setDuration(400).start();
                    ObjectAnimator.ofFloat(handcontrol, "translationX", 0.0F, -400.0F).setDuration(600).start();
                    ObjectAnimator.ofFloat(voicecontrol, "translationX", 0.0F, -600.0F).setDuration(800).start();
                    RotateAnimation ra = new RotateAnimation(0, 225, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    ra.setDuration(600);
                    ra.setFillAfter(true);
                    add.startAnimation(ra);
                } else {
                    fabstatus = true;
                    ObjectAnimator.ofFloat(autocontrol, "translationX", -200.0F, 0.0F).setDuration(400).start();
                    ObjectAnimator.ofFloat(handcontrol, "translationX", -400.0F, 0.0F).setDuration(600).start();
                    ObjectAnimator.ofFloat(voicecontrol, "translationX", -600.0F, 0.0F).setDuration(800).start();
                    RotateAnimation ra = new RotateAnimation(255, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    ra.setDuration(600);
                    ra.setFillAfter(true);
                    add.startAnimation(ra);
                }
            }

        });
        /***
         * 手自动切换setOnTouchListener监听
         */
        autocontrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendControlCode sc = new SendControlCode(777);
                sc.execute();
                if (display_status == true) {
                    sensorManager.registerListener(MainActivity.this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_FASTEST);
                    display_status = false;
                    rl = (RelativeLayout) findViewById(R.id.hand_control_panel);
                    ScaleAnimation sa = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    sa.setDuration(1000);
                    rl.startAnimation(sa);
                    /***
                     * 动画状态监听
                     */
                    sa.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            rl.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    LinearLayout ll = (LinearLayout) findViewById(R.id.auto_control_panel);
                    ll.setVisibility(View.VISIBLE);
                    RelativeLayout rll = (RelativeLayout) findViewById(R.id.voice_control_panel);
                    rll.setVisibility(View.GONE);
                }
            }
        });
        handcontrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendControlCode sc = new SendControlCode(666);
                sc.execute();
                if (display_status == false) {
                    sensorManager.unregisterListener(MainActivity.this);
                    display_status = true;
                    RelativeLayout rl = (RelativeLayout) findViewById(R.id.hand_control_panel);
                    rl.setVisibility(View.VISIBLE);
                    ScaleAnimation sa = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    sa.setDuration(1000);
                    rl.startAnimation(sa);
                    LinearLayout ll = (LinearLayout) findViewById(R.id.auto_control_panel);
                    ll.setVisibility(View.GONE);
                    RelativeLayout rll = (RelativeLayout) findViewById(R.id.voice_control_panel);
                    rll.setVisibility(View.GONE);
                }
            }
        });
        voicecontrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendControlCode sc = new SendControlCode(555);
                sc.execute();
                sensorManager.unregisterListener(MainActivity.this);
                display_status = true;
                RelativeLayout rl = (RelativeLayout) findViewById(R.id.voice_control_panel);
                rl.setVisibility(View.VISIBLE);
                ScaleAnimation sa = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                sa.setDuration(1000);
                rl.startAnimation(sa);
                LinearLayout ll = (LinearLayout) findViewById(R.id.auto_control_panel);
                RelativeLayout rll = (RelativeLayout) findViewById(R.id.hand_control_panel);
                ll.setVisibility(View.GONE);
                rll.setVisibility(View.GONE);
            }
        });
    }

    /***
     * 获取回答内容
     * @param result
     */
    private void requestMessage(String result) {
        HttpURLConnection connection=null;
        try {
            URL url=new URL(requestAddress+result+key);
            connection= (HttpURLConnection) url.openConnection();
            InputStream in=connection.getInputStream();
            BufferedReader reader=new BufferedReader(new InputStreamReader(in));
            String temp=null;
            StringBuffer stringBuffer=new StringBuffer();
            while ((temp=reader.readLine())!=null){
                stringBuffer.append(temp);
            }
            String resolveResult=stringBuffer.toString();
            resolveResult=resolveResult.substring(50,resolveResult.length());
            resolveResult=resolveResult.substring(0,resolveResult.indexOf('"'));
            System.out.println(resolveResult+"机器人的回复");
            composeVoice(resolveResult);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(connection!=null)connection.disconnect();
        }
    }

    /***
     * 合成语音
     * @param resolveResult
     */
    private void composeVoice(String resolveResult) {
        TextToSpeech tts=new TextToSpeech(MainActivity.this,resolveResult);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.auto_control_panel);
        int offsetX = (ll.getWidth() - autocontrol.getWidth()) / 2;
        int offsetY = (ll.getHeight() - autocontrol.getHeight()) / 2;
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_GRAVITY://加速度传感器
                gravity[0] = sensorEvent.values[0];
                gravity[1] = sensorEvent.values[1];
                gravity[2] = sensorEvent.values[2];
                offsetX = (int) (offsetX - 50 * gravity[0]);
                offsetY = (int) (offsetY + 30 * gravity[1]);
                /***
                 * 状态码
                 */
                if (gravity[0] > 1) {
                    if (left) {
                        SendControlCode sc2 = new SendControlCode(8);
                        sc2.execute();//左
                        SendControlCode sc = new SendControlCode(3);
                        sc.execute();//左
                        left = false;
                        right = true;
                        stop = true;
                    }
                } else if (gravity[0] < -1) {
                    if (right) {
                        SendControlCode sc2 = new SendControlCode(7);
                        sc2.execute();//左
                        SendControlCode sc = new SendControlCode(4);
                        sc.execute();//右
                        right = false;
                        left = true;
                        stop = true;
                    }
                } else {
                    if (stop) {
                        SendControlCode sc = new SendControlCode(7);
                        sc.execute();
                        stop = false;
                        left = true;
                        right = true;
                    }
                }
                if (gravity[1] < -1.5) {
                    if (up) {
                        SendControlCode sc2 = new SendControlCode(6);
                        sc2.execute();//上
                        SendControlCode sc = new SendControlCode(1);
                        sc.execute();//上
                        up = false;
                        down = true;
                        stop = true;
                    }
                } else if (gravity[1] > 1.5) {
                    if (down) {
                        SendControlCode sc2 = new SendControlCode(5);
                        sc2.execute();//上
                        SendControlCode sc = new SendControlCode(2);
                        sc.execute();//下
                        down = false;
                        up = true;
                        stop = true;
                    }
                } else {
                    if (stop) {
                        SendControlCode sc = new SendControlCode(6);
                        sc.execute();
                        stop = false;
                        up = true;
                        down = true;
                    }
                }
                layoutParams.leftMargin = offsetX;
                layoutParams.topMargin = offsetY;
                position_circle.setLayoutParams(layoutParams);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
//    private void showTip(final String str)
//    {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mToast.setText(str);
//                mToast.show();
//            }
//        });
//    }
    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };
    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParser.parseIatResult(results.getResultString());
            mResultText.append(text);
            if(isLast) {
                String result=mResultText.toString();
                if(result!=null)requestMessage(result);
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }
    };

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, "recognizer result：" + results.getResultString());
            String text = JsonParser.parseIatResult(results.getResultString());
            mResultText.append(text);
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

    };

    private void showTip(final String str)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.setText(str);
                mToast.show();
            }
        });
    }
    public void setParam(){
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        String lag = "mandarin";
        // 设置引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        }else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT,lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( null != mIat ){
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }
    public String getVoiceContent(){
        mIatResults.clear();
        // 设置参数
        setParam();
        boolean isShowDialog = false;
        if (isShowDialog) {
            // 显示听写对话框
            mIatDialog.setListener(mRecognizerDialogListener);
            mIatDialog.show();
            showTip(getString(R.string.text_begin));
        } else {
            // 不显示听写对话框
            ret = mIat.startListening(mRecognizerListener);
            if (ret != ErrorCode.SUCCESS) {
                showTip("听写失败,错误码：" + ret);
            } else {
                showTip(getString(R.string.text_begin));
            }
        }
        return mResultText.toString();
    }
}




