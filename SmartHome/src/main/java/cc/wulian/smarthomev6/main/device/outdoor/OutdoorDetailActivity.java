package cc.wulian.smarthomev6.main.device.outdoor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.wulian.sdk.android.ipc.rtcv2.IPCController;
import com.wulian.sdk.android.ipc.rtcv2.IPCMsgApiType;
import com.wulian.sdk.android.ipc.rtcv2.IPCMsgController;
import com.wulian.sdk.android.ipc.rtcv2.IPCResultCallBack;
import com.wulian.sdk.android.ipc.rtcv2.message.IPCCallStateMsgEvent;
import com.wulian.sdk.android.ipc.rtcv2.message.IPCOnReceivedMsgEvent;
import com.wulian.sdk.android.ipc.rtcv2.message.IPCVideoFrameMsgEvent;
import com.wulian.sdk.android.ipc.rtcv2.message.IPCcameraXmlMsgEvent;
import com.wulian.sdk.android.ipc.rtcv2.message.messagestate.MsgCallState;
import com.wulian.sdk.android.ipc.rtcv2.message.messagestate.MsgReceivedType;
import com.wulian.sdk.android.ipc.rtcv2.utils.IPCGetFrameFunctionType;
import com.wulian.webrtc.ViEAndroidGLES20;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.wulian.smarthomev6.R;
import cc.wulian.smarthomev6.entity.DefinitionBean;
import cc.wulian.smarthomev6.entity.DeviceDetailMsg;
import cc.wulian.smarthomev6.main.application.BaseTitleActivity;
import cc.wulian.smarthomev6.main.application.MainApplication;
import cc.wulian.smarthomev6.main.device.cateye.album.AlbumGridActivity;
import cc.wulian.smarthomev6.main.device.cylincam.utils.IotUtil;
import cc.wulian.smarthomev6.main.device.lookever.LookeverReplayHardActivity;
import cc.wulian.smarthomev6.main.device.outdoor.bean.Preset;
import cc.wulian.smarthomev6.main.device.outdoor.setting.OutdoorSettingActivity;
import cc.wulian.smarthomev6.main.home.scene.SceneListDialogActivity;
import cc.wulian.smarthomev6.main.message.alarm.ICamAlarmActivity;
import cc.wulian.smarthomev6.support.core.apiunit.DeviceApiUnit;
import cc.wulian.smarthomev6.support.core.apiunit.ICamCloudApiUnit;
import cc.wulian.smarthomev6.support.core.apiunit.bean.icam.ICamDeviceBean;
import cc.wulian.smarthomev6.support.core.apiunit.bean.icam.ICamGetSipInfoBean;
import cc.wulian.smarthomev6.support.core.device.Device;
import cc.wulian.smarthomev6.support.core.device.DeviceInfoDictionary;
import cc.wulian.smarthomev6.support.customview.AngleMeter;
import cc.wulian.smarthomev6.support.customview.BrightnessSetPop;
import cc.wulian.smarthomev6.support.customview.CylincamYuntaiButton;
import cc.wulian.smarthomev6.support.customview.CylincamYuntaiButtonLandscape;
import cc.wulian.smarthomev6.support.customview.DefinitionChoosePop;
import cc.wulian.smarthomev6.support.customview.PinchLayout;
import cc.wulian.smarthomev6.support.customview.popupwindow.PresetPopupWindow;
import cc.wulian.smarthomev6.support.event.DeviceReportEvent;
import cc.wulian.smarthomev6.support.event.LastFrameEvent;
import cc.wulian.smarthomev6.support.event.NetworkInfoEvent;
import cc.wulian.smarthomev6.support.tools.CameraGestureListener;
import cc.wulian.smarthomev6.support.tools.dialog.ProgressDialogManager;
import cc.wulian.smarthomev6.support.tools.dialog.WLDialog;
import cc.wulian.smarthomev6.support.utils.CameraUtil;
import cc.wulian.smarthomev6.support.utils.DisplayUtil;
import cc.wulian.smarthomev6.support.utils.FileUtil;
import cc.wulian.smarthomev6.support.utils.LanguageUtil;
import cc.wulian.smarthomev6.support.utils.SizeUtil;
import cc.wulian.smarthomev6.support.utils.ToastUtil;
import cc.wulian.smarthomev6.support.utils.VibratorUtil;
import cc.wulian.smarthomev6.support.utils.WLog;
import cc.wulian.smarthomev6.support.utils.XmlHandler;

/**
 * Created by hxc on 2017/7/4.
 * 企鹅机详情界面
 */

public class OutdoorDetailActivity extends BaseTitleActivity {

    private static final String TAG = "OutdoorDetailActivity";
    private static final String KEY_ICAM_DEVICE_BEAN = "icam_device_bean";
    protected final String PROCESS = "icamProcess";
    private static final String QUERY = "QUERY";

    private static final String PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private static final int PERMISSION_REQUEST_CODE = 1;

    private static final int SHOWSPEED_INTERVAL = 3000;// 速度间隔为3秒

    private DeviceApiUnit deviceApiUnit;
    private ICamCloudApiUnit iCamCloudApiUnit;
    private ICamDeviceBean iCamDeviceBean;
    private ICamGetSipInfoBean iCamGetSipInfoBean;
    private String deviceId;
    private String uniqueDeviceId;
    //    private String sdomain;
    private boolean isPreset = false;
    private int registerExpTime = 0;
    private float mDensity;
    private int mHiddenViewMeasuredHeight;

    private PinchLayout layout_video_container;
    private FrameLayout main_container;
    private CylincamYuntaiButton yt_penguin;
    private CylincamYuntaiButtonLandscape yt_penguin_landscape;
    private ImageView iv_arrow;
    private View layout_video_loading, layout_video_reload, layout_video_offline;
    private PresetPopupWindow presetPopupWindow;
    private TextView tv_network_speed, tv_hold_speek;
    private ViEAndroidGLES20 view_video;
    private AngleMeter angleMeter;
    private SoundPool soundPool;
    private int snapshot_sound_id;
    private boolean presetPressed = false;

    private View btn_snapshot;
    private ImageView btn_sound_switch, btn_alarmlist, btn_replay, btn_brightness, btn_preset, btn_scene, iv_snapshot, btn_fullscreen;
    private ImageView iv_hold_speek;
    private TextView btn_definition;
    private DefinitionChoosePop definitionChoosePop;
    private WLDialog mDialog;
    private WLDialog deleteDialog;
    private BrightnessSetPop brightnessSetPop;
    private GestureDetector gestureDetector;
    private int definitionValue = 3;
    private int brightnessValue = 50;
    private int minWidth, maxWidth;
    private int widthRatio = 16, heightRatio = 9;
    private String presetMsg;
    private boolean isShowLandscapeView = true;
    private Handler handler = new Handler(Looper.getMainLooper());

    private Bitmap saveLastBitmap;//保存当前图库图片的引用，方便更换的时候回收

    //横竖屏切换相关view
    private View layout_portrait, layout_portrait_bottom, layout_landscape;
    private TextView btn_definition_landscape, tv_network_speed_landscape;
    private ImageView btn_sound_switch_landscape, btn_snapshot_landscape, iv_hold_speek_landscape, btn_fullscreen_landscape;
    private FrameLayout layoutBrightness;
    private Button btnIknown;
    private ImageView ivBrightness;

    public static boolean hasInit = false;
    private boolean isPause = false;
    private boolean isRadioOpen = false;
    private boolean isPlayAndRecord = false;
    private boolean isFirstCreate = false;
    private boolean isControling = false;
    private boolean isLandscape = false;
    private boolean isShowLimitsDialog = false;
    private boolean isQueryHistory = false;
    private boolean canAdjustBrightness = false;

    private long saveReceivedDataSize = 0;
    private static final int YUNTAI_CONTROL = 1;
    private CylincamYuntaiButton.Direction curDirection;
    private CylincamYuntaiButtonLandscape.Direction curDirectionLandscape;
    private Runnable autoPullRunnable;

    private TranslateAnimation animation;
    /**
     * 0 loading，1 断开，2 播放, 3 离线
     */
    private int videoPlayState;

    public boolean isShared = false;
    Handler ytHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case YUNTAI_CONTROL:
                    curDirection = (CylincamYuntaiButton.Direction) msg.obj;
                    switch (curDirection) {
                        case left:
                            yuntai_left();
                            break;
                        case right:
                            yuntai_right();
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    Handler ytHandlerLandscape = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case YUNTAI_CONTROL:
                    curDirectionLandscape = (CylincamYuntaiButtonLandscape.Direction) msg.obj;
                    switch (curDirectionLandscape) {
                        case left:
                            yuntai_left();
                            break;
                        case right:
                            yuntai_right();
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public static void start(Context context, Device bean,boolean isInService) {
        Intent intent = new Intent(context, OutdoorDetailActivity.class);
        ICamDeviceBean iCamDeviceBean = new ICamDeviceBean();
        iCamDeviceBean.did = bean.devID;
        iCamDeviceBean.online = 1;

        if (bean.devID.startsWith("CG") && bean.devID.length() >= 11) {
            iCamDeviceBean.uniqueDeviceId = bean.devID.substring(0, 11);
        } else {
            iCamDeviceBean.uniqueDeviceId = bean.devID;
        }
        try {
            JSONObject jsonObject = new JSONObject(bean.data);
            boolean online = jsonObject.getBoolean("onLine");
            iCamDeviceBean.nick = jsonObject.getString("name");
            if (online) {
                iCamDeviceBean.online = 1;
            } else {
                iCamDeviceBean.online = 0;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(isInService){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(KEY_ICAM_DEVICE_BEAN, iCamDeviceBean);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_outdoor_detail, true);
        EventBus.getDefault().register(this);
        CameraUtil.setHasVideoActivityRunning(true);
        WLog.i("android.os.Build.MODEL", Build.MODEL);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    @Override
    public boolean enableSwipeBack() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
        showLastSnapshot();
        if (videoPlayState != 2 && !isShowLimitsDialog) {
            getSipInfo();
        }
    }

    @Override
    protected void onPause() {
        if (videoPlayState == 2) {
            IPCController.getRenderFrame("hello", IPCGetFrameFunctionType.FRAME_MAIN_THUNBNAIL);
        }
        isPause = true;
        super.onPause();
    }

    @Override
    protected void onStop() {
        isQueryHistory = false;
        if (isPlayAndRecord) {
            IPCController.stopPlayAndRecordAudioAsync(null);
            isPlayAndRecord = false;
        }
        tv_network_speed.setText("0KB/s");
        tv_network_speed_landscape.setText("0KB/s");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        handler.removeCallbacksAndMessages(null);
        IPCController.closeAllVideoAsync(new IPCResultCallBack() {
            @Override
            public void getResult(int i) {
//                IPCController. (new IPCResultCallBack() {
//                    @Override
//                    public void getResult(int i) {
//                    }
//                });
            }
        });
        IPCController.setRender("", null);
        layout_video_container.removeView(view_video);
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        CameraUtil.setHasVideoActivityRunning(false);
        super.onDestroy();
    }

    private void stopWork() {
        IPCController.closeAllVideoAsync(new IPCResultCallBack() {
            @Override
            public void getResult(int i) {
                if (i == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (videoPlayState != 3) {//离线不改变状态
                                videoPlayState = 1;
                            }
                            handler.removeCallbacksAndMessages(null);
                            tv_network_speed.setText("0KB/s");
                            tv_network_speed_landscape.setText("0KB/s");
                        }
                    });
                }
            }
        });
    }

    private void getSipInfo() {
        if (iCamGetSipInfoBean == null) {
            iCamCloudApiUnit.doGetSipInfo(deviceId, true, new ICamCloudApiUnit.IcamApiCommonListener<ICamGetSipInfoBean>() {
                @Override
                public void onSuccess(ICamGetSipInfoBean bean) {
                    if (bean != null) {
                        iCamGetSipInfoBean = bean;
                        initSip();
                    }
                }

                @Override
                public void onFail(int code, String msg) {

                }
            });
        } else {
            initSip();
            WLog.i(TAG, "getSipInfo: initSip");
        }
    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                PERMISSION_RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{PERMISSION_RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        } else {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            isShowLimitsDialog = true;
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permission Denied
                ToastUtil.singleCenter(R.string.Toast_Permission_Denied);
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void initTitle() {
        super.initTitle();
        setToolBarTitleAndRightImg("zhende jia de ", R.drawable.icon_cateye_setting);
    }

    @Override
    protected void initView() {
        main_container = (FrameLayout) findViewById(R.id.main_container);
        layout_video_container = (PinchLayout) findViewById(R.id.layout_video_container);
        layout_video_loading = findViewById(R.id.layout_video_loading);
        layout_video_reload = findViewById(R.id.layout_video_reload);
        layout_video_offline = findViewById(R.id.layout_video_offline);
        yt_penguin = (CylincamYuntaiButton) findViewById(R.id.yt_penguin);
        yt_penguin_landscape = (CylincamYuntaiButtonLandscape) findViewById(R.id.yt_penguin_landscape);
        iv_arrow = (ImageView) findViewById(R.id.iv_arrows);
        angleMeter = (AngleMeter) findViewById(R.id.anglemeter);
        angleMeter.setMaxAngle("100°");
        view_video = new ViEAndroidGLES20(this);
        DisplayMetrics displayMetrics = SizeUtil.getScreenSize(getApplicationContext());
//        int deviceHeight = displayMetrics.heightPixels;
//        int cameraPreviewHeight = deviceHeight * 7 / 9;// 根据布局中的上下比例
//        int cameraPreviewWidth = (int) ((float) cameraPreviewHeight
//                / heightRatio * widthRatio);
//        minWidth = displayMetrics.widthPixels;
//        maxWidth = cameraPreviewWidth;
        int deviceWidth = displayMetrics.widthPixels;
        int cameraPreviewWidth = deviceWidth;// 根据布局中的上下比例
        int cameraPreviewHeight = (int) ((float) cameraPreviewWidth
                * heightRatio / widthRatio);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(cameraPreviewWidth, cameraPreviewHeight);
        layoutParams.gravity = Gravity.CENTER;
        layout_video_container.addView(view_video, 0, layoutParams);
        view_video.setKeepScreenOn(true);

        tv_network_speed = (TextView) findViewById(R.id.tv_network_speed);
        tv_hold_speek = (TextView) findViewById(R.id.tv_hold_speek);
        btn_snapshot = findViewById(R.id.btn_snapshot);
        iv_snapshot = (ImageView) findViewById(R.id.iv_snapshot);
        btn_sound_switch = (ImageView) findViewById(R.id.btn_sound_switch);
        btn_alarmlist = (ImageView) findViewById(R.id.btn_alarmlist);
        btn_replay = (ImageView) findViewById(R.id.btn_replay);
        btn_brightness = (ImageView) findViewById(R.id.btn_brightness);
        btn_preset = (ImageView) findViewById(R.id.btn_preset);
        btn_scene = (ImageView) findViewById(R.id.btn_scene);
        iv_hold_speek = (ImageView) findViewById(R.id.iv_hold_speek);
        btn_definition = (TextView) findViewById(R.id.btn_definition);
        btn_fullscreen = (ImageView) findViewById(R.id.btn_fullscreen);
        btn_definition.setAlpha(0.5f);
        btn_fullscreen.setAlpha(0.5f);

        //横竖屏切换相关view
        layout_portrait = findViewById(R.id.layout_portrait);
        layout_portrait_bottom = findViewById(R.id.layout_portrait_bottom);
        layout_landscape = findViewById(R.id.layout_landscape);
        btn_definition_landscape = (TextView) findViewById(R.id.btn_definition_landscape);
        tv_network_speed_landscape = (TextView) findViewById(R.id.tv_network_speed_landscape);
        btn_sound_switch_landscape = (ImageView) findViewById(R.id.btn_sound_switch_landscape);
        btn_snapshot_landscape = (ImageView) findViewById(R.id.btn_snapshot_landscape);
        iv_hold_speek_landscape = (ImageView) findViewById(R.id.iv_hold_speek_landscape);
        btn_fullscreen_landscape = (ImageView) findViewById(R.id.btn_fullscreen_landscape);
        tv_network_speed_landscape.setAlpha(0.5f);
        layoutBrightness = (FrameLayout) findViewById(R.id.layout_brightness_tips);
        ivBrightness = (ImageView) findViewById(R.id.iv_brightness_tip);
        btnIknown = (Button) findViewById(R.id.btn_i_known);
    }

    @Override
    protected void initData() {
        Bundle bd = getIntent().getExtras();
        if (bd == null) {
            return;
        }
        isFirstCreate = true;
        iCamDeviceBean = (ICamDeviceBean) bd.getSerializable(KEY_ICAM_DEVICE_BEAN);
        deviceId = iCamDeviceBean.did;
        uniqueDeviceId = iCamDeviceBean.uniqueDeviceId;
        deviceApiUnit = new DeviceApiUnit(this);
        iCamCloudApiUnit = new ICamCloudApiUnit(this);
        mDensity = getResources().getDisplayMetrics().density;
        mHiddenViewMeasuredHeight = (int) (mDensity * 50 + 0.5);

        Device device = MainApplication.getApplication().getDeviceCache().get(deviceId);
        if (device != null) {
            setTitleText(DeviceInfoDictionary.getNameByTypeAndName(device.type, device.name));
            if (device.isOnLine()) {
                updateLoadingState(0);
            } else {
                updateLoadingState(3);
            }
            //共享设备
            isShared = device.isShared;
        }
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        snapshot_sound_id = soundPool.load(this, R.raw.snapshot, 1);

        setRadioOpen(isRadioOpen);

        checkPermission();
        autoPullRunnable = new Runnable() {
            @Override
            public void run() {
                pullDownAnimation();
            }
        };
        handler.postDelayed(autoPullRunnable, 1000);

        btn_definition.setText(DefinitionBean.getNameResByValue(definitionValue));
        showSnapshot();

        gestureDetector = new GestureDetector(this,new CameraGestureListener(this, new CameraGestureListener.MyGestureListener() {
            @Override
            public void OnBrightChanged(float brightness) {
                if(canAdjustBrightness){
                    setBrightness(brightness);
                }
            }

            @Override
            public void onSingleTouchConfirmed() {
                if (isLandscape) {
                    hideOrShowLandscapeView();
                }
            }
        }));
    }

    @Override
    protected void initListeners() {
        btn_snapshot.setOnClickListener(this);
        iv_snapshot.setOnClickListener(this);
        btn_sound_switch.setOnClickListener(this);
        btn_alarmlist.setOnClickListener(this);
        btn_replay.setOnClickListener(this);
        btn_brightness.setOnClickListener(this);
        btn_preset.setOnClickListener(this);
        btn_scene.setOnClickListener(this);
        layout_video_reload.setOnClickListener(this);
        btn_definition.setOnClickListener(this);
        btn_fullscreen.setOnClickListener(this);
        btnIknown.setOnClickListener(this);

        btn_definition_landscape.setOnClickListener(this);
        btn_sound_switch_landscape.setOnClickListener(this);
        btn_snapshot_landscape.setOnClickListener(this);
        btn_fullscreen_landscape.setOnClickListener(this);
        iv_arrow.setOnClickListener(this);
        iv_hold_speek.setOnClickListener(this);
        iv_hold_speek_landscape.setOnClickListener(this);
        yt_penguin.setOnDirectionLisenter(new MyDirection());
        yt_penguin.setOnDirectionLisenter(new MyDirection());
        yt_penguin_landscape.setOnDirectionLisenter(new MyDirectionLandscape());
        layout_landscape.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);// 手势双击
                return true;// 自定义方向判断
            }
        });
        iv_hold_speek.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (videoPlayState == 2) {//播放状态才能点击
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            iv_hold_speek.setImageResource(R.drawable.icon_hold_speek_on);
                            tv_hold_speek.setText(R.string.Cateye_In_Call);
                            IPCController.recordAudioAsync(new IPCResultCallBack() {
                                @Override
                                public void getResult(int i) {
                                    WLog.i("recordAudioAsync resalt:" + i);
                                    VibratorUtil.holdSpeakVibration();
                                }
                            });
                        }
                        break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL: {
                            iv_hold_speek.setImageResource(R.drawable.icon_hold_speek);
                            tv_hold_speek.setText(R.string.CateEye_Detail_Hold_Speek);
                            IPCController.stopRecordAudioAsync(new IPCResultCallBack() {
                                @Override
                                public void getResult(int i) {
                                    WLog.i("stopRecordAudioAsync resalt:" + i);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            setRadioOpen(isRadioOpen);
                                        }
                                    });
                                }
                            });
                        }
                        break;
                    }
                }
                return true;
            }
        });

        iv_hold_speek_landscape.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        iv_hold_speek_landscape.setImageResource(R.drawable.btn_hold_fullscreen_pre);
                        IPCController.recordAudioAsync(new IPCResultCallBack() {
                            @Override
                            public void getResult(int i) {
                                WLog.i("recordAudioAsync resalt:" + i);
                                VibratorUtil.holdSpeakVibration();
                            }
                        });
                    }
                    break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        iv_hold_speek_landscape.setImageResource(R.drawable.btn_hold_fullscreen);
                        IPCController.stopRecordAudioAsync(new IPCResultCallBack() {
                            @Override
                            public void getResult(int i) {
                                WLog.i("stopRecordAudioAsync resalt:" + i);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        setRadioOpen(isRadioOpen);
                                    }
                                });
                            }
                        });
                    }
                    break;
                }
                return true;
            }
        });

        layout_video_container.setOnChildViewLocationChangedListener(new PinchLayout.OnChildViewLocationChangedListener() {
            @Override
            public void childViewMoveScaleX(float ratio) {
                angleMeter.refreshAngle(ratio);
            }

            @Override
            public void minSize(boolean isMinSize) {
                angleMeter.setVisibility(isMinSize ? View.GONE : View.VISIBLE);
            }
        });
    }

    class MyDirection implements CylincamYuntaiButton.OnDirectionLisenter {
        @Override
        public void directionLisenter(CylincamYuntaiButton.Direction direction) {
            yuntai_stop();
            ytHandler.removeMessages(YUNTAI_CONTROL);
            if (direction != CylincamYuntaiButton.Direction.none) {
                ytHandler.sendMessageDelayed(
                        Message.obtain(ytHandler, YUNTAI_CONTROL, direction),
                        500);
            }
        }
    }

    class MyDirectionLandscape implements CylincamYuntaiButtonLandscape.OnDirectionLisenter {
        @Override
        public void directionLisenter(CylincamYuntaiButtonLandscape.Direction direction) {
            yuntai_stop();
            ytHandlerLandscape.removeMessages(YUNTAI_CONTROL);
            if (direction != CylincamYuntaiButtonLandscape.Direction.none) {
                ytHandlerLandscape.sendMessageDelayed(
                        Message.obtain(ytHandlerLandscape, YUNTAI_CONTROL, direction),
                        500);
            }
        }
    }


    @Override
    public void onClickView(View v) {
//        super.onClick(v);
        if (videoPlayState != 3) {//离线不能点击
            if (v == btn_snapshot || v == btn_snapshot_landscape) {
                if (videoPlayState == 2) {
                    IPCController.getRenderFrame("hello", IPCGetFrameFunctionType.FRAME_PLAY_THUMBNAIL);
                    btn_snapshot.setEnabled(false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btn_snapshot.setEnabled(true);
                        }
                    }, 1000);
                }
            } else if (v == btn_sound_switch || v == btn_sound_switch_landscape) {
                if (videoPlayState == 2) {
                    setRadioOpen(!isRadioOpen);
                }
            } else if (v == btn_replay) {
                isQueryHistory = true;
                IPCMsgController.MsgQueryStorageStatus(uniqueDeviceId, iCamGetSipInfoBean.deviceDomain);//查询储状态信息
                ProgressDialogManager.getDialogManager().showDialog(QUERY, this, null, null, 10000);
            } else if (v == btn_brightness) {
                if (brightnessSetPop == null) {
                    brightnessSetPop = new BrightnessSetPop(this, new BrightnessSetPop.OnValueChangedListener() {
                        @Override
                        public void onValueChanged(int value) {
                            brightnessValue = value;
                            IPCMsgController.MsgConfigCSC(uniqueDeviceId,
                                    iCamGetSipInfoBean.deviceDomain, brightnessValue, 50, 50, 50);
                        }

                        @Override
                        public void onDismiss() {
                            btn_definition.setVisibility(View.VISIBLE);
                        }
                    });
                }
                btn_definition.setVisibility(View.GONE);
                brightnessSetPop.showUpRise(btn_brightness, brightnessValue);
            } else if (v == btn_definition) {
                if (definitionChoosePop == null) {
                    definitionChoosePop = new DefinitionChoosePop(this, new DefinitionChoosePop.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(DefinitionBean bean) {
                            definitionValue = bean.value;
                            btn_definition.setText(bean.name);
                            btn_definition_landscape.setText(bean.name);
                            IPCMsgController.MsgConfigEncode(uniqueDeviceId,
                                    iCamGetSipInfoBean.deviceDomain, definitionValue);//1,2,3
                        }
                    });
                }
                definitionChoosePop.showUpRise(btn_definition, definitionValue);
            } else if (v == btn_definition_landscape) {
                if (definitionValue < 3) {
                    definitionValue += 1;
                } else {
                    definitionValue = 1;
                }
                btn_definition.setText(DefinitionBean.getNameResByValue(definitionValue));
                btn_definition_landscape.setText(DefinitionBean.getNameResByValue(definitionValue));
                IPCMsgController.MsgConfigEncode(uniqueDeviceId,
                        iCamGetSipInfoBean.deviceDomain, definitionValue);//1,2,3
            } else if (v == btn_preset) {
                this.presetPressed = !presetPressed;
                if (presetPressed) {
                    btn_preset.setImageResource(R.drawable.icon_button_collect_selected);
                    if (presetPopupWindow == null) {
                        presetPopupWindow = new PresetPopupWindow(this, uniqueDeviceId, iCamGetSipInfoBean.deviceDomain);
                        presetPopupWindow.setOnClickListener(listener);
                    }
                    presetPopupWindow.refresh();
                    presetPopupWindow.showParent(layout_portrait_bottom);
                } else {
                    btn_preset.setImageResource(R.drawable.icon_button_collect);
                    if (presetPopupWindow != null) {
                        presetPopupWindow.dismiss();
                    }
                }

            } else if (v == layout_video_reload) {
                updateLoadingState(0);
                IPCController.closeAllVideoAsyncRefresh(new IPCResultCallBack() {
                    @Override
                    public void getResult(int i) {
                        if (i == 0) {
                            WLog.i(PROCESS, "关闭视频流并重置呼叫" + i);
                        }
                    }
                });
                makeCall();
            }
        }
        if (v == iv_snapshot) {
            Intent intent = new Intent(this, AlbumGridActivity.class);
            intent.putExtra("devId", deviceId);
            startActivity(intent);
        } else if (v == btn_alarmlist) {
//            stopWork();
            ICamAlarmActivity.start(this, deviceId, iCamGetSipInfoBean.deviceDomain, "CMICA5");
        } else if (v.getId() == R.id.img_right) {
            if (iCamDeviceBean != null) {
//                stopWork();
                Intent intent = new Intent(this, OutdoorSettingActivity.class);
                iCamDeviceBean.nick = mainApplication.getDeviceCache().get(deviceId).name;
                iCamDeviceBean.sdomain = iCamGetSipInfoBean.deviceDomain;
                intent.putExtra("ICamDeviceBean", iCamDeviceBean);
                startActivityForResult(intent, 1);
            }
        } else if (v == iv_arrow) {
            if (yt_penguin.getVisibility() == View.GONE) {
                handler.removeCallbacks(autoPullRunnable);
                pullDownAnimation();
            } else {
                pushupAnimation();
            }
        } else if (v == btn_fullscreen_landscape) {
            exitFullscreen();
        } else if (v == btn_fullscreen) {
            performFullscreen();
        }else if (v == btnIknown) {
            canAdjustBrightness = true;
            setLandscapeViewEnable(true);
            layoutBrightness.setVisibility(View.GONE);
        } else if (v == btn_scene) {
            startActivity(new Intent(this, SceneListDialogActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            this.finish();
        }
    }

    public PresetPopupWindow.PrePositionListener listener = new PresetPopupWindow.PrePositionListener() {
        @Override
        public void roateToPosition(int position) {
            IPCMsgController.MsgControlLocatePreset(uniqueDeviceId, iCamGetSipInfoBean.deviceDomain, position);
        }

        @Override
        public void addPosition(int position) {
            showSavePrePositionDialog(position);
        }

        @Override
        public void removePosition(int position, String name) {
            showDeletePresetDialog(position, name);
        }
    };

    private void showSavePrePositionDialog(final int position) {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }

        WLDialog.Builder builder = new WLDialog.Builder(this);
        builder.setTitle(this.getResources().getString(R.string.Save_Position))
                .setCancelOnTouchOutSide(false)
                .setEditTextHint(R.string.Position_Name)
                .setPositiveButton(this.getResources().getString(R.string.Sure))
                .setNegativeButton(this.getResources().getString(R.string.Cancel))
                .setListener(new WLDialog.MessageListener() {
                    @Override
                    public void onClickPositive(View view, String msg) {
                        presetMsg = msg;
                        File file = new File(FileUtil.getPrePositionPath() + "/" + uniqueDeviceId);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        isPreset = true;
                        preference.saveCylincamPrePosition(uniqueDeviceId, position, position + "");
                        IPCController.getRenderFrame(iCamGetSipInfoBean.deviceDomain, IPCGetFrameFunctionType.FRAME_PLAY_THUMBNAIL);
                        IPCMsgController.MsgControlPreset(uniqueDeviceId, iCamGetSipInfoBean.deviceDomain, position, msg);
                        presetPopupWindow.refresh();
                    }

                    @Override
                    public void onClickNegative(View view) {
                        mDialog.dismiss();
                    }

                });
        mDialog = builder.create();
        mDialog.show();
    }

    private void showDeletePresetDialog(final int position, final String name) {
        if (deleteDialog != null && deleteDialog.isShowing()) {
            return;
        }
        WLDialog.Builder builder = new WLDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_Preset_Position))
                .setCancelOnTouchOutSide(false)
                .setPositiveButton(this.getResources().getString(R.string.Sure))
                .setNegativeButton(this.getResources().getString(R.string.Cancel))
                .setListener(new WLDialog.MessageListener() {
                    @Override
                    public void onClickPositive(View view, String msg) {
                        File file = new File(FileUtil.getPrePositionPath() + "/" + uniqueDeviceId);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        IotUtil.delFilePassWay(FileUtil.getPrePositionPath() + "/" + uniqueDeviceId, name + ".jpg");
                        preference.saveCylincamPrePosition(uniqueDeviceId, position, null);

                        IPCMsgController.MsgControlDeletePresets(uniqueDeviceId, iCamGetSipInfoBean.deviceDomain, String.valueOf(position));
                        presetPopupWindow.refresh();
                    }

                    @Override
                    public void onClickNegative(View view) {
                        deleteDialog.dismiss();
                    }
                });
        deleteDialog = builder.create();
        deleteDialog.show();
    }

    private void hideOrShowLandscapeView() {
        if (isShowLandscapeView) {
            btn_sound_switch_landscape.setVisibility(View.GONE);
            btn_snapshot_landscape.setVisibility(View.GONE);
            iv_hold_speek_landscape.setVisibility(View.GONE);
            btn_fullscreen_landscape.setVisibility(View.GONE);
            yt_penguin_landscape.setVisibility(View.GONE);
            btn_definition_landscape.setVisibility(View.GONE);
            tv_network_speed_landscape.setVisibility(View.VISIBLE);
            isShowLandscapeView = false;
        } else {
            btn_sound_switch_landscape.setVisibility(View.VISIBLE);
            btn_snapshot_landscape.setVisibility(View.VISIBLE);
            iv_hold_speek_landscape.setVisibility(View.VISIBLE);
            btn_fullscreen_landscape.setVisibility(View.VISIBLE);
            yt_penguin_landscape.setVisibility(View.VISIBLE);
            btn_definition_landscape.setVisibility(View.VISIBLE);
            tv_network_speed_landscape.setVisibility(View.VISIBLE);
            isShowLandscapeView = true;
        }
    }

    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;
        }
        getWindow().setAttributes(lp);
    }

    private void initSip() {
        if (MainApplication.getApplication().hasRegisterSipAccount &&
                TextUtils.equals(preference.getCurrentSipSuid(), iCamGetSipInfoBean.suid)) {
            WLog.i(PROCESS, "已经注册sip账号，直接呼叫");
            makeCall();
        } else if (MainApplication.getApplication().hasInitSip &&
                !MainApplication.getApplication().hasRegisterSipAccount) {
            WLog.i(PROCESS, "已经初始化sip，但未注册sip账号");
            startRegister();
        } else if (MainApplication.getApplication().hasInitSip && !TextUtils.equals(preference.getCurrentSipSuid(), iCamGetSipInfoBean.suid)) {
            WLog.i(PROCESS, "摄像机和锁之间切换，需要重新注册sip");
            startRegister();
        } else {
            IPCResultCallBack initRTCAsyncCallback = new IPCResultCallBack() {
                @Override
                public void getResult(int result) {
                    hasInit = result == 0 ? true : false;
                    WLog.i(PROCESS, "未初始化也未注册，此刻开始初始化" + hasInit);
                    if (hasInit) {
                        MainApplication.getApplication().hasInitSip = hasInit;
                    }
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startRegister();
                        }
                    }, 500);
                }
            };
            IPCController.initRTCAsync(initRTCAsyncCallback);
        }
    }


    private void startRegister() {
        IPCResultCallBack ipcResultCallBack = new IPCResultCallBack() {
            @Override
            public void getResult(int i) {
                boolean isRegisterAccount = i == 0 ? true : false;
                WLog.i(PROCESS, "注册sip账号: " + isRegisterAccount);
                if (isRegisterAccount) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            preference.saveCurrentSipSuid(iCamGetSipInfoBean.suid);
                            MainApplication.getApplication().hasRegisterSipAccount = true;
                            WLog.i(PROCESS, "注册成功后呼叫");
                            makeCall();
                        }
                    }, 500);
                }
            }
        };
        String suid = iCamGetSipInfoBean.suid;
        String userSipPwd = iCamGetSipInfoBean.spassword;
        String sdomain = iCamGetSipInfoBean.sipDomain;
        IPCController.registerAccountAsync(ipcResultCallBack, suid, userSipPwd, sdomain);
    }


    private void setRender() {
        if (isFirstCreate) {
            isFirstCreate = false;
            WLog.i(PROCESS, "设置渲染器");
            IPCController.setRender("", view_video);
            IPCController.setRenderFlag(iCamGetSipInfoBean.deviceDomain);
        }
    }


    private void makeCall() {
        if (videoPlayState != 3) {//离线不启动视频
            setRender();
            updateLoadingState(0);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    IPCMsgController.MsgConfigEncode(uniqueDeviceId,
                            iCamGetSipInfoBean.deviceDomain, definitionValue);//1,2,3
                    IPCMsgController.MsgConfigCSC(uniqueDeviceId,
                            iCamGetSipInfoBean.deviceDomain, brightnessValue, 50, 50, 50);
                    IPCController.makeCallAsync(new IPCResultCallBack() {
                        @Override
                        public void getResult(int i) {
                            WLog.i(PROCESS, "发起视频呼叫结果: " + i);
                            if (i != 0 && i != 4) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateLoadingState(1);
                                    }
                                });
                            } else if (i == 4 && registerExpTime < 5) {
                                WLog.i(PROCESS, "账号注册异常重新注册" + registerExpTime);
                                registerExpTime++;
                                startRegister();
                            } else if (i == 4 && registerExpTime == 5) {
                                WLog.i(PROCESS, "账号注册异常重新注册次数超过5次需手动刷新" + registerExpTime);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateLoadingState(1);
                                    }
                                });
                                registerExpTime = 0;
                            }
                        }
                    }, uniqueDeviceId, iCamGetSipInfoBean.deviceDomain);
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(IPCcameraXmlMsgEvent event) {
        if (event.getCode() != 0) {
            SipDataReturn(false, event.getApiType(), event.getMessage(),
                    event.getDestURI(), String.valueOf(event.getCode()));
            Log.i("sip", "fail---" + "apiType = " + event.getApiType() + "msg = " + event.getMessage());
        } else {
            SipDataReturn(true, event.getApiType(), event.getMessage(),
                    event.getDestURI(), String.valueOf(event.getCode()));
            Log.i("sip", "success---" + "apiType = " + event.getApiType() + "msg = " + event.getMessage());
        }
    }

    protected void SipDataReturn(boolean isSuccess, IPCMsgApiType apiType,
                                 String xmlData, String from, String code) {
        if (isSuccess) {
            Log.i("hxc", apiType + "");
            switch (apiType) {
                case QUERY_DEVICE_DESCRIPTION_INFO:
                    DeviceDetailMsg detailMsg = XmlHandler
                            .getDeviceDetailMsg(xmlData);
                    if (detailMsg != null) {
//                        setDeviceInformation(detailMsg);
                    }
                    break;
                case QUERY_STORAGE_STATUS:
                    ProgressDialogManager.getDialogManager().dimissDialog(QUERY, 0);
                    if (!isPause && isQueryHistory) {
                        Pattern pstatus = Pattern
                                .compile("<storage.*status=\"(\\d)\"\\s+.*/?>(</storage>)?");
                        Matcher matchers = pstatus.matcher(xmlData);
                        if (matchers.find()) {
                            String status = matchers.group(1).trim();
                            if ("1".equals(status)) {
                                ToastUtil.singleCenter(R.string.No_SD_Look_Back);
                            } else if ("2".equals(status)) {
//                                stopWork();
//                                LookeverReplayActivity.start(this, iCamGetSipInfoBean.suid, iCamGetSipInfoBean.deviceDomain, iCamDeviceBean);
                                IPCController.changeReplay(uniqueDeviceId, iCamGetSipInfoBean.deviceDomain);
                                LookeverReplayHardActivity.start(this, iCamGetSipInfoBean.suid, iCamGetSipInfoBean.deviceDomain, iCamDeviceBean);

                            }
                        }
                    }
                    break;
                case QUERY_PRESET:
                    List<Preset> presetsList = new ArrayList<>();
                    Preset currentPreset = null;
                    String device_id = "";
                    XmlPullParser parser = Xml.newPullParser();
                    try {
                        parser.setInput(new StringReader(xmlData));
                        int eventType = parser.getEventType();// 获取事件类型
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            switch (eventType) {
                                case XmlPullParser.START_DOCUMENT:// 文档开始事件,可以进行数据初始化处理
                                    break;
                                case XmlPullParser.START_TAG://开始读取某个标签
                                    //通过getName判断读到哪个标签，然后通过nextText()获取文本节点值，或通过getAttributeValue(i)获取属性节点值
                                    String name = parser.getName();
                                    if (name.equalsIgnoreCase("uri")) {

                                        String uri = parser.nextText();
                                        device_id = uri.split("@")[0];

                                    } else if (name.equalsIgnoreCase("desc")) {
                                        currentPreset = new Preset();
                                        currentPreset.setPreset(new Integer(parser.getAttributeValue(null, "id")));
                                        currentPreset.setDesc(parser.nextText());
                                        currentPreset.setDevice_id(device_id);
                                        presetsList.add(currentPreset);
                                    }
                                    break;
                                case XmlPullParser.END_TAG:// 结束元素事件
                                    break;
                            }

                            eventType = parser.next();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    presetPopupWindow.updateData(presetsList);
                    isPreset = false;
                    break;
            }
        }
    }

    private Runnable requestSpeedTask = new Runnable() {
        @Override
        public void run() {
            IPCController.getCallSpeedInfo();
            handler.postDelayed(this, SHOWSPEED_INTERVAL);
        }
    };

    private void showSpeed(String speedInfo) {
        if (!TextUtils.isEmpty(speedInfo)) {
            long dataSize = 0;
            long delatDataSize = 0;
            try {
                dataSize = Long.parseLong(speedInfo);
                delatDataSize = dataSize - saveReceivedDataSize;
                delatDataSize = (delatDataSize > 0 ? delatDataSize : 0)
                        / (SHOWSPEED_INTERVAL / 1000);
                saveReceivedDataSize = dataSize;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            long speed = delatDataSize / 1000;
            tv_network_speed.setText("" + (speed > 0 ? speed : 0) + "KB/s");
            tv_network_speed_landscape.setText("" + (speed > 0 ? speed : 0) + "KB/s");
        }
    }

    private void setRadioOpen(boolean isOpen) {
        this.isRadioOpen = isOpen;
        if (isOpen) {
            btn_sound_switch.setImageResource(R.drawable.icon_cateye_sound_on);
            btn_sound_switch_landscape.setImageResource(R.drawable.btn_sound_fullscreen_on);
            IPCController.playAudioAsync(new IPCResultCallBack() {
                @Override
                public void getResult(int i) {
                    WLog.i("playAudioAsync resalt:" + i);
                }
            });
        } else {
            btn_sound_switch.setImageResource(R.drawable.icon_cateye_sound_off);
            btn_sound_switch_landscape.setImageResource(R.drawable.btn_sound_fullscreen_off);
            IPCController.stopPlayAudioAsync(new IPCResultCallBack() {
                @Override
                public void getResult(int i) {
                    WLog.i("stopPlayAudioAsync resalt:" + i);
                }
            });
        }
    }

    /**
     * 视频背景显示上一次退出时的截图
     */
    private void showSnapshot() {
        String snapshotPath = FileUtil.getLastFramePath();
        String fileName = deviceId + ".jpg";
        String path = snapshotPath + "/" + fileName;
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                view_video.setBackground(new BitmapDrawable(getResources(), bitmap));
                return;
            }
        }
        view_video.setBackgroundResource(R.drawable.camera_default_bg1);
    }

    private void showLastSnapshot() {
        String savePath = FileUtil.getSnapshotPath() + "/" + deviceId;
        File savePathFile = new File(savePath);
        String[] bmpFiles = savePathFile.list();
        if (bmpFiles != null && bmpFiles.length > 0) {
            String bmpFile = bmpFiles[bmpFiles.length - 1];
            Bitmap bitmap = BitmapFactory.decodeFile(savePath + "/" + bmpFile);
            iv_snapshot.setImageBitmap(bitmap);
        } else {
            iv_snapshot.setImageResource(R.drawable.icon_image_gallery);
        }
    }

    private void saveBitmap(final Bitmap bitmap) {
        if (bitmap != null) {
            String savePath = FileUtil.getSnapshotPath() + "/" + deviceId;
            File savePathFile = new File(savePath);
            if (!savePathFile.exists()) {
                savePathFile.mkdirs();
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmsss");
            String time = simpleDateFormat.format(System.currentTimeMillis());
            String fileName = time + ".jpg";
            FileUtil.saveBitmapToJpeg(bitmap, savePath, fileName);
            DisplayUtil.snapAnimotion(this, main_container, view_video, iv_snapshot, bitmap, new DisplayUtil.onCompleteListener() {
                @Override
                public void onComplete() {
                    iv_snapshot.setImageBitmap(bitmap);
                }
            });
            if (soundPool != null) {
                soundPool.play(snapshot_sound_id, 1.0f, 1.0f, 0, 0, 1);
            }
        }
    }

    private void savePresetBitmap(final Bitmap bitmap) {
        if (bitmap != null) {
            String savePath = FileUtil.getPrePositionPath() + "/" + uniqueDeviceId;
            File savePathFile = new File(savePath);
            if (!savePathFile.exists()) {
                savePathFile.mkdirs();
            }
            String fileName = presetMsg + ".jpg";
            FileUtil.saveBitmapToJpeg(bitmap, savePath, fileName);
        }
    }

    /**
     * 设置loadingview 状态
     *
     * @param state 0 loading，1 断开，2 播放,3 离线
     */
    private void updateLoadingState(int state) {
        videoPlayState = state;
        if (state == 0) {
            layout_video_reload.setVisibility(View.GONE);
            layout_video_loading.setVisibility(View.VISIBLE);
            layout_video_offline.setVisibility(View.GONE);
        } else if (state == 1) {
            layout_video_reload.setVisibility(View.VISIBLE);
            layout_video_loading.setVisibility(View.GONE);
            layout_video_offline.setVisibility(View.GONE);
        } else if (state == 2) {
            layout_video_reload.setVisibility(View.GONE);
            layout_video_loading.setVisibility(View.GONE);
            layout_video_offline.setVisibility(View.GONE);
        } else if (state == 3) {
            layout_video_reload.setVisibility(View.GONE);
            layout_video_loading.setVisibility(View.GONE);
            layout_video_offline.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 进入全屏
     */
    private void performFullscreen() {
        boolean isFirstFullScreen = preference.getIsFrisCameraFullScreen();
        if (isFirstFullScreen) {
            preference.setIsFrisCameraFullScreen(false);
            if (!LanguageUtil.isChina()) {
                ivBrightness.setImageResource(R.drawable.icon_brightness_tip_en);
                btnIknown.setBackgroundResource(R.drawable.icon_i_know_en);
            } else {
                ivBrightness.setImageResource(R.drawable.icon_brightness_tip_cn);
                btnIknown.setBackgroundResource(R.drawable.icon_i_know_cn);
            }
            setLandscapeViewEnable(false);
            layoutBrightness.setVisibility(View.VISIBLE);
        } else {
            canAdjustBrightness = true;
            layoutBrightness.setVisibility(View.GONE);
        }
        layout_video_container.setScaleEnable(false);
        isLandscape = true;
        getmToolBarHelper().setToolBarVisible(false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(params);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        layout_portrait.setVisibility(View.GONE);
        layout_portrait_bottom.setVisibility(View.GONE);
        layout_landscape.setVisibility(View.VISIBLE);

//        DisplayMetrics displayMetrics = SizeUtil.getScreenSize(getApplicationContext());
//        int cameraPreviewHeight = (int) ((float) displayMetrics.widthPixels
//                * heightRatio / widthRatio);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(displayMetrics.widthPixels, cameraPreviewHeight);
//        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//        view_video.setLayoutParams(layoutParams);
//        view_video.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                gestureDetector.onTouchEvent(event);// 手势双击
//                return true;
//            }
//        });

        if (brightnessSetPop != null && brightnessSetPop.isShowing()) {
            brightnessSetPop.dismiss();
        }
        if (definitionChoosePop != null && definitionChoosePop.isShowing()) {
            definitionChoosePop.dismiss();
        }

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 全屏


    }

    /**
     * 退出全屏
     */
    private void exitFullscreen() {
        layout_video_container.setScaleEnable(true);
        isLandscape = false;
        getmToolBarHelper().setToolBarVisible(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setAttributes(params);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        layout_portrait.setVisibility(View.VISIBLE);
        layout_portrait_bottom.setVisibility(View.VISIBLE);
        layout_landscape.setVisibility(View.GONE);

        layout_video_container.setChildViewLocationCenter();

//        DisplayMetrics displayMetrics = SizeUtil.getScreenSize(getApplicationContext());
////        int deviceHeight = displayMetrics.heightPixels;
////        int cameraPreviewHeight = deviceHeight * 7 / 9;// 根据布局中的上下比例
////        int cameraPreviewWidth = (int) ((float) cameraPreviewHeight
////                / heightRatio * widthRatio);
//        int deviceWidth = displayMetrics.widthPixels;
//        int cameraPreviewWidth = deviceWidth;// 根据布局中的上下比例
//        int cameraPreviewHeight = (int) ((float) cameraPreviewWidth
//                * heightRatio / widthRatio);
//        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view_video.getLayoutParams();
//        lp.width = cameraPreviewWidth;
//        lp.height = cameraPreviewHeight;
//        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
//        view_video.setLayoutParams(lp);
    }

    private void setLandscapeViewEnable(boolean flag){
        iv_hold_speek_landscape.setEnabled(flag);
        btn_definition_landscape.setEnabled(flag);
        btn_fullscreen_landscape.setEnabled(flag);
        btn_sound_switch_landscape.setEnabled(flag);
        btn_snapshot_landscape.setEnabled(flag);
    }

    @Override
    public void onBackPressed() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            exitFullscreen();
        } else {
            super.onBackPressed();
        }
    }

    private int retryCount = 0;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(IPCCallStateMsgEvent event) {
        MsgCallState callState = MsgCallState.getMsgCallState(event
                .getCallState());
        switch (callState) {
            case STATE_ESTABLISHED:
                WLog.i(PROCESS, "##建立连接了");
//                isConncted = true;
//                ll_linking_video_refresh.setVisibility(View.GONE);
                break;
            case STATE_TERMINATED:
                WLog.i(PROCESS, "##挂断了");
                tv_network_speed.setText("0KB/s");
                tv_network_speed_landscape.setText("0KB/s");
                if (retryCount < 2) {
                    updateLoadingState(0);
                    WLog.i(PROCESS, "sdk挂断重呼");
                    IPCController.closeAllVideoAsyncRefresh(new IPCResultCallBack() {
                        @Override
                        public void getResult(int i) {
                            if (i == 0) {
                                WLog.i(PROCESS, "关闭视频流并重置呼叫" + i);
                                makeCall();
                            }
                        }
                    });
                    retryCount += 1;
                } else {
                    updateLoadingState(1);
                }
//                reCallVideo();
                handler.removeCallbacks(requestSpeedTask);
                break;
            case STATE_VIDEO_INCOMING:
                updateLoadingState(2);
//                isVideoComing = true;
                WLog.i(PROCESS, "##视频流来了");
                view_video.setBackground(null);
                retryCount = 0;
                handler.removeCallbacks(requestSpeedTask);
                handler.postDelayed(requestSpeedTask, SHOWSPEED_INTERVAL);
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(IPCOnReceivedMsgEvent event) {
        switch (MsgReceivedType.getMsgReceivedTypeByID(event.getRtcType())) {
            case HANDLE_RTC_CALL_SPEED_TYPE:
//                WLog.i("##摄像头速率");
                showSpeed(event.getMessage());
                break;
            case HANDLE_RTC_CALL_DQ_TYPE:
                WLog.i("##处理DQ信息");
                WLog.i("DQ信息-->" + event.getMessage());
//                dq_message = event.getMessage();
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(IPCVideoFrameMsgEvent event) {
        WLog.d("PML", "End time is:" + System.currentTimeMillis());
        IPCGetFrameFunctionType type = event.getType();
        switch (type) {
            case FRAME_MAIN_THUNBNAIL:
                Bitmap bitmap = event.getmVideoBitmap();
                if (bitmap != null && !bitmap.isRecycled()) {
                    String savePath = FileUtil.getLastFramePath();
                    String fileName = deviceId + ".jpg";
                    File savePathFile = new File(savePath);
                    if (!savePathFile.exists()) {
                        savePathFile.mkdirs();
                    }
                    FileUtil.saveBitmapToJpeg(event.getmVideoBitmap(), savePath, fileName);
                    EventBus.getDefault().post(new LastFrameEvent(deviceId, savePath + "/" + fileName));
//                    bitmap.recycle();
                }
                break;
            case FRAME_PLAY_THUMBNAIL:

                WLog.i("#Thread-->" + Thread.currentThread().getName());
                WLog.i("收到抓拍图片");
                if (event.getmVideoBitmap() == null) {
                    WLog.i("抓拍图片为空");
                } else {
                    if (isPreset) {
                        savePresetBitmap(event.getmVideoBitmap());
                    } else {
                        saveBitmap(event.getmVideoBitmap());
                    }
                }
                break;

        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceReport(DeviceReportEvent event) {
        if (event.device != null && deviceId != null) {
            if (TextUtils.equals(event.device.devID, deviceId)) {
                if (event.device.isOnLine()) {
                    if (videoPlayState == 3) {//离线变为在线，重新连接
                        updateLoadingState(0);
                        initSip();
                    }
                } else if (event.device.mode == 3) {
                    finish();
                } else {
                    updateLoadingState(3);
                }
                setTitleText(DeviceInfoDictionary.getNameByTypeAndName(event.device.type, event.device.name));
            }
        }
    }

    private void pullDownAnimation() {
        iv_hold_speek.setVisibility(View.GONE);
        tv_hold_speek.setVisibility(View.GONE);
        yt_penguin.setVisibility(View.VISIBLE);
        animation = new TranslateAnimation(0, 0, -200, 0);
        animation.setDuration(1000);//设置动画持续时间
        yt_penguin.startAnimation(animation);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                iv_arrow.setImageResource(R.drawable.arrow_up_white);
            }
        }, 1000);
    }

    private void pushupAnimation() {
        yt_penguin.setVisibility(View.GONE);
        iv_hold_speek.setVisibility(View.VISIBLE);
        tv_hold_speek.setVisibility(View.VISIBLE);
        animation = new TranslateAnimation(0, 0, 200, 0);
        animation.setDuration(1000);//设置动画持续时间
        iv_hold_speek.startAnimation(animation);
        tv_hold_speek.startAnimation(animation);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                iv_arrow.setImageResource(R.drawable.arrow_down_white);
            }
        }, 1000);
    }

    public void yuntai_stop() {
        stopMove();
    }

    public void yuntai_left() {
        if (isControling) {
            return;
        }
        isControling = true;
        IPCMsgController.MsgControlPTZMovement(-1, 0);
    }


    public void yuntai_right() {
        if (isControling) {
            return;
        }
        isControling = true;
        IPCMsgController.MsgControlPTZMovement(1, 0);
    }

    private void stopMove() {
        if (isControling) {
            isControling = false;
            IPCMsgController.MsgControlPTZMovement(0, 0);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkInfoEvent(NetworkInfoEvent event) {
        if (event.networkInfo.isConnected()) {
            if (videoPlayState != 2 && !isShowLimitsDialog) {
                getSipInfo();
            }
        } else {
            updateLoadingState(1);
        }
    }
}
