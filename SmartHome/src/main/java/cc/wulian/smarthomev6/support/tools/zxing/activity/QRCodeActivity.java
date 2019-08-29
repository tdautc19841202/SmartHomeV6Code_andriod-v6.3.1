/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.wulian.smarthomev6.support.tools.zxing.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.Result;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import cc.wulian.smarthomev6.R;
import cc.wulian.smarthomev6.main.application.BaseTitleActivity;
import cc.wulian.smarthomev6.main.device.AddDeviceActivity;
import cc.wulian.smarthomev6.main.device.AddGateway06Activity;
import cc.wulian.smarthomev6.main.device.AddGateway11Activity;
import cc.wulian.smarthomev6.main.device.camera_lc.config.AddLcCameraGuideActivity;
import cc.wulian.smarthomev6.main.device.config.AddDeviceByUidActivity;
import cc.wulian.smarthomev6.main.device.config.DeviceStartConfigActivity;
import cc.wulian.smarthomev6.main.device.gateway_mini.config.MiniGatewayActivity;
import cc.wulian.smarthomev6.main.device.gateway_wall.config.WallGatewayActivity;
import cc.wulian.smarthomev6.main.mine.gatewaycenter.GatewayBindActivity;
import cc.wulian.smarthomev6.support.core.apiunit.DeviceApiUnit;
import cc.wulian.smarthomev6.support.core.apiunit.bean.CheckV6SupportBean;
import cc.wulian.smarthomev6.support.core.apiunit.bean.LcSimpleInfoBean;
import cc.wulian.smarthomev6.support.core.device.DeviceInfoDictionary;
import cc.wulian.smarthomev6.support.tools.dialog.WLDialog;
import cc.wulian.smarthomev6.support.tools.zxing.camera.CameraManager;
import cc.wulian.smarthomev6.support.tools.zxing.decode.DecodeThread;
import cc.wulian.smarthomev6.support.tools.zxing.utils.BeepManager;
import cc.wulian.smarthomev6.support.tools.zxing.utils.CaptureActivityHandler;
import cc.wulian.smarthomev6.support.tools.zxing.utils.InactivityTimer;
import cc.wulian.smarthomev6.support.utils.ConstantsUtil;
import cc.wulian.smarthomev6.support.utils.DialogUtil;
import cc.wulian.smarthomev6.support.utils.ToastUtil;
import cc.wulian.smarthomev6.support.utils.UrlUtil;
import cc.wulian.smarthomev6.support.utils.WLog;


/**
 * 扫码界面
 */
public final class QRCodeActivity extends BaseTitleActivity implements SurfaceHolder.Callback, ICaptureActivity {

    private static final String TAG = QRCodeActivity.class.getSimpleName();
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 1;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;

    private SurfaceView scanPreview = null;
    private RelativeLayout scanContainer;
    private RelativeLayout scanCropView;
    private ImageView scanLine;
    private TextView mTextTips;

    private View layout_input;
    private String scanType;
    private int mGetCodeRequestCode;
    private String mTitle, mTips;
    private String cameraType = "";
    private String gwType = "";
    private String hisType;
    private boolean isBindDevice;
    private boolean isConfigWifi;
    private boolean unknownQR;

    private Rect mCropRect = null;
    private boolean isHasSurface = false;
    private boolean hasCameraPermission = false;
    public static String[] addDeviceWhiteList = {"GW01", "GW06", "GW08", "GW09", "GW10", "GW11", "GW12", "GW15",
            "CMICA1", "CMICA2", "CMICA3", "CMICA4", "CMICA5", "CMICY1", "IF02", "XW01", "Ok", "CG22", "CG23", "CG24", "CG25","CG26","CG27"};

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public static void getCode(Activity context, int requestCode, String title, String tips) {
        Intent intent = new Intent(context, QRCodeActivity.class);
        intent.putExtra("scanType", ConstantsUtil.GET_QR_CODE);
        intent.putExtra("title", title);
        intent.putExtra("tips", tips);
        intent.putExtra("requestCode", requestCode);
        context.startActivityForResult(intent, requestCode);
    }

    public static void start(Context context, String scanType) {
        Intent intent = new Intent(context, QRCodeActivity.class);
        intent.putExtra("scanType", scanType);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_qrcode, true);

        checkPermission();
    }

    @Override
    public boolean enableSwipeBack() {
        return false;
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                PERMISSION_CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_CAMERA)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{PERMISSION_CAMERA},
                        PERMISSION_CAMERA_REQUEST_CODE);

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{PERMISSION_CAMERA},
                        PERMISSION_CAMERA_REQUEST_CODE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            hasCameraPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasCameraPermission = true;
//                startWork();
            } else {
                // Permission Denied
                ToastUtil.show(R.string.Toast_Permission_Denied);
                finish();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void initView() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        scanPreview = (SurfaceView) findViewById(R.id.capture_preview);
        scanContainer = (RelativeLayout) findViewById(R.id.capture_container);
        scanCropView = (RelativeLayout) findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);
        layout_input = findViewById(R.id.layout_input);
        mTextTips = (TextView) findViewById(R.id.capture_text_tips);

        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation
                .RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.9f);
        animation.setDuration(4500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        scanLine.startAnimation(animation);
    }

    @Override
    protected void initListeners() {
        layout_input.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v == layout_input) {
            Intent intent = new Intent(this, AddDeviceByUidActivity.class);
            intent.putExtra("scanType", scanType);
            startActivityForResult(intent, 1);
//            this.finish();
        } else if (v.getId() == R.id.img_left) {
            if (TextUtils.equals(scanType, ConstantsUtil.GET_QR_CODE)) {
                finish();
            } else {
                startActivity(new Intent(this, AddDeviceActivity.class));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    String id = data.getStringExtra("id");
                    String type = data.getStringExtra("type");
                    if (!TextUtils.isEmpty(type) && DeviceInfoDictionary.isLcCamera(type)) {
                        AddLcCameraGuideActivity.start(QRCodeActivity.this, type, id, ConstantsUtil.DEVICE_SCAN_ENTRY);
                    } else {
                        setResult(RESULT_OK, new Intent().putExtra("type", type).putExtra("id", id));
                    }
                    finish();
                    break;
            }
        }
    }

    @Override
    protected void initTitle() {
        super.initTitle();
        mTitle = getIntent().getStringExtra("title");
        if (!TextUtils.isEmpty(mTitle)) {
            setToolBarTitle(mTitle);
        } else {
            setToolBarTitle(getString(R.string.AddDevice_AddDevice));
        }
    }

    @Override
    protected void initData() {
        super.initData();
        scanType = getIntent().getStringExtra("scanType");
        hisType = getIntent().getStringExtra("hisType");
        mGetCodeRequestCode = getIntent().getIntExtra("requestCode", 0);
        if (TextUtils.equals(scanType, ConstantsUtil.GET_QR_CODE)) {
            layout_input.setVisibility(View.GONE);
        }
        mTips = getIntent().getStringExtra("tips");
        if (!TextUtils.isEmpty(mTips)) {
            mTextTips.setText(mTips);
        }
//        cameraType = getIntent().getStringExtra("cameraType");
    }

    private void startWork() {

        // CameraManager must be initialized here(onResume), not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());

        handler = null;

        if (isHasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(scanPreview.getHolder());
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            scanPreview.getHolder().addCallback(this);
        }

        inactivityTimer.onResume();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isHasSurface) {
            scanPreview.getHolder().addCallback(this);
        }
        if (hasCameraPermission) {
            startWork();
        }
    }

    @Override
    protected void onPause() {
        if (hasCameraPermission) {
            if (handler != null) {
                handler.quitSynchronously();
                handler = null;
            }
            inactivityTimer.onPause();
            beepManager.close();
            cameraManager.closeDriver();
            if (!isHasSurface) {
                scanPreview.getHolder().removeCallback(this);
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    private boolean contains(String type) {
        for (int i = 0; i < addDeviceWhiteList.length; i++) {
            if (addDeviceWhiteList[i].equals(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            WLog.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!isHasSurface) {
            isHasSurface = true;
            if (hasCameraPermission) {
                initCamera(holder);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult The contents of the barcode.
     * @param bundle    The extras
     */
    public void handleDecode(Result rawResult, Bundle bundle) {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();
        final String deviceId = rawResult.getText();
        if (TextUtils.equals(scanType, ConstantsUtil.GET_QR_CODE)) {
            Intent intent = new Intent();
            intent.putExtra(ConstantsUtil.QR_CODE, deviceId);
            setResult(mGetCodeRequestCode, intent);
            finish();
            return;
        }
        if (UrlUtil.isWechatAlbumUrl(deviceId)) {
            String id = UrlUtil.getShortUrl(deviceId.split(UrlUtil.URL_PREFIX)[1]).toUpperCase();
            setResult(RESULT_OK, new Intent().putExtra("type", "GW15").putExtra("id", id));
            finish();
            return;
        }

        if (deviceId.contains("SN:") && deviceId.contains("DT:")) {
            final String id = deviceId.substring(4, 19);
            DeviceApiUnit deviceApiUnit = new DeviceApiUnit(this);
            deviceApiUnit.getDeviceSimpleInfo(id, new DeviceApiUnit.DeviceApiCommonListener<LcSimpleInfoBean>() {
                @Override
                public void onSuccess(LcSimpleInfoBean bean) {
                    String deviceType = bean.getDevices().getDeviceType();
                    String location = bean.getDevices().getLocation();
                    Log.i(TAG, "deviceType: " + deviceType + ",location = " + location);
                    if (!TextUtils.isEmpty(deviceType)) {
                        AddLcCameraGuideActivity.start(QRCodeActivity.this, deviceType, id, ConstantsUtil.DEVICE_SCAN_ENTRY);
                        finish();
                    }
                }

                @Override
                public void onFail(int code, String msg) {
                    ToastUtil.show(msg);
                }
            });
            return;

        }
        DeviceApiUnit deviceApiUnit = new DeviceApiUnit(this);
        deviceApiUnit.doCheckV6Support(deviceId, new DeviceApiUnit.DeviceApiCommonListener<CheckV6SupportBean>() {
            @Override
            public void onSuccess(CheckV6SupportBean bean) {
                List<CheckV6SupportBean.SupportDevice> supportList = bean.device;
                CheckV6SupportBean.SupportDevice gwDevice = null;
                CheckV6SupportBean.SupportDevice cameraDevice = null;
                CheckV6SupportBean.SupportDevice commonDevice = null;
                for (CheckV6SupportBean.SupportDevice device :
                        supportList) {
                    if (preference.isAuthGateway() && !contains(device.type)) {
                        ToastUtil.show(R.string.addDevice_toast_03);
                        return;
                    }
                    if (device.deviceFlag == 2) {
                        cameraDevice = device;
                        cameraType = cameraDevice.type;
                    } else if (device.deviceFlag == 1) {
                        gwDevice = device;
                        gwType = gwDevice.type;
                    } else if (device.deviceFlag == 3) {
                        commonDevice = device;
                    } else if (device.deviceFlag == 0) {
                        showScanTipsDialog(getString(R.string.Scancode_Unrecognized));
                    }
                }
                if (cameraDevice != null) {
                    if (cameraDevice.supportV6Flag != 0) {
                        judgeBindOrConfig(cameraDevice.deviceId);
                    } else {
                        showScanTipsDialog(getString(R.string.Scancode_Unrecognized));
                    }
                } else if (gwDevice != null) {
                    if (gwDevice.supportV6Flag != 0) {
                        jumpToGatewayConfig(gwDevice.deviceId);
                    } else {
                        showScanTipsDialog(getString(R.string.Scancode_Unrecognized));
                    }
                } else if (commonDevice != null) {
                    if (commonDevice.supportV6Flag != 0) {
                        setResult(RESULT_OK, new Intent().putExtra("type", commonDevice.type).putExtra("id", commonDevice.deviceId));
                        finish();
                    } else {
                        showScanTipsDialog(getString(R.string.Scancode_Unrecognized));
                    }
                }
            }

            @Override
            public void onFail(int code, String msg) {
                ToastUtil.show(msg);
            }
        });
    }


    /**
     * 跳网关设备的配网
     *
     * @param gwId
     */

    private void jumpToGatewayConfig(String gwId) {
        if (TextUtils.equals(gwType, "GW04")) {//mini 网关
            showMiniTipsDialog(gwId, getString(R.string.Minigateway_Scancode_Popup),
                    getString(R.string.Minigateway_Popup_Goconfiguration), getString(R.string.Minigateway_Popup_Alreadyconfigured));
        } else if (TextUtils.equals(gwType, "GW06") || TextUtils.equals(gwType, "GW12")) {//02增强型网关,网关02型(局域网)
            AddGateway06Activity.start(this, gwId, gwType);
            finish();
        } else if (TextUtils.equals(gwType, "GW08")) {//墙面网关
            showGWWallTipsDialog(gwId, getString(R.string.Gateway_Dialog_Chose_Tip),
                    getString(R.string.Minigateway_Popup_Goconfiguration), getString(R.string.Minigateway_Popup_Alreadyconfigured));
        } else if (TextUtils.equals(gwType, "GW11")) {//吸顶网关
            AddGateway11Activity.start(this, gwId, gwType);
            finish();
        } else {
            setResult(RESULT_OK, new Intent().putExtra("type", gwType).putExtra("id", gwId));
            finish();
        }
    }

    /**
     * 判断摄像机绑定还是wifi配置
     *
     * @param cameraId
     */
    private void judgeBindOrConfig(String cameraId) {
        if (!TextUtils.isEmpty(cameraType)) {
            switch (cameraType) {
                case "CMICA1":
                    isBindDevice = true;
                    startActivity(new Intent(this, DeviceStartConfigActivity.class).
                            putExtra("deviceId", cameraId).
                            putExtra("scanType", scanType).
                            putExtra("isBindDevice", true).
                            putExtra("deviceType", cameraType));
                    finish();
                    break;
                case "CMICA2":
                case "CMICA3":
                case "CMICA4":
                case "CMICA5":
                case "CMICA6":
                    judgeDifferentEntry(cameraId);
                    break;
                default:
                    showScanTipsDialog(getString(R.string.Scancode_Unrecognized));
                    break;

            }
        }
    }

    //根据不同的扫码入口判断是否弹框提示绑定摄像机或者网关
    private void judgeDifferentEntry(String cameraId) {
        if (scanType.equals(ConstantsUtil.DEVICE_SCAN_ENTRY)) {
            switch (cameraType) {
                case "CMICA2":
                    showScanTipsDialog(cameraId,
                            getString(R.string.Scancode_Lookever_SelectAddtype), getString(R.string.Lookever), getString(R.string.Lookever_Gateway));
                    break;
                case "CMICA3":
                case "CMICA6":
                    showScanTipsDialog(cameraId,
                            getString(R.string.Scancode_Penguin_SelectAddtype), getString(R.string.Penguin), getString(R.string.Penguin_Gateway));
                    break;
                case "CMICA4":
                    showScanTipsDialog(cameraId,
                            getString(R.string.Scan_Cylinacam_Tip), getString(R.string.Cylincam), getString(R.string.Cylincam_Gateway));
                    break;
                case "CMICA5":
                    showScanTipsDialog(cameraId,
                            getString(R.string.Scancode_Outdoor_SelectAddtype), getString(R.string.Device_Default_Name_CMICA5), getString(R.string.Outdoor_Camera_Gateway));
                    break;
            }
        } else if (TextUtils.equals(scanType, ConstantsUtil.CAMERA_ADD_ENTRY)) {
            if (TextUtils.equals(cameraType, "CMICA4")) {
                startActivity(new Intent(this, DeviceStartConfigActivity.class).
                        putExtra("deviceId", cameraId).putExtra("scanType", scanType).putExtra("isBindDevice", false).putExtra("deviceType", cameraType));
            } else {
                startActivity(new Intent(this, DeviceStartConfigActivity.class).
                        putExtra("deviceId", cameraId).putExtra("scanType", scanType).putExtra("isBindDevice", true).putExtra("deviceType", cameraType));
            }
            finish();
        }
    }


    /**
     * 摄像机扫描提示dialog
     *
     * @param deviceId
     * @param msg
     * @param posMsg
     * @param negMSg
     */
    private void showScanTipsDialog(final String deviceId, String msg, String posMsg, String negMSg) {
        DialogUtil.showConfigOrBindDialog(this, msg, posMsg, negMSg, new WLDialog.MessageListener() {
            @Override
            public void onClickPositive(View var1, String msg) {
                isBindDevice = true;
                if (TextUtils.equals("CMICA4", cameraType)) {
                    startActivity(new Intent(QRCodeActivity.this, DeviceStartConfigActivity.class).
                            putExtra("deviceId", deviceId).putExtra("scanType", scanType).putExtra("isBindDevice", false).putExtra("deviceType", cameraType));
                } else {
                    startActivity(new Intent(QRCodeActivity.this, DeviceStartConfigActivity.class).
                            putExtra("deviceId", deviceId).putExtra("scanType", scanType).putExtra("isBindDevice", true).putExtra("deviceType", cameraType));
                }
                finish();
            }

            @Override
            public void onClickNegative(View var1) {
                scanType = ConstantsUtil.BIND_CAMERA_GATEWAY_ENTRY;
                if (TextUtils.equals("CMICA4", cameraType)) {
                    startActivity(new Intent(QRCodeActivity.this, DeviceStartConfigActivity.class)
                            .putExtra("asGateway", ConstantsUtil.NEED_JUMP_BIND_GATEWAY_FLAG).putExtra("deviceId", deviceId).putExtra("scanType", scanType).putExtra("isBindDevice", false).putExtra("deviceType", cameraType));
                } else {
                    startActivity(new Intent(QRCodeActivity.this, DeviceStartConfigActivity.class)
                            .putExtra("asGateway", ConstantsUtil.NEED_JUMP_BIND_GATEWAY_FLAG).putExtra("deviceId", deviceId).putExtra("scanType", scanType).putExtra("isBindDevice", true).putExtra("deviceType", cameraType));
                }
                finish();
            }
        }).show();
    }

    /**
     * mini网关扫描提示dialog
     *
     * @param deviceId
     * @param msg
     * @param posMsg
     * @param negMSg
     */
    private void showMiniTipsDialog(final String deviceId, String msg, String posMsg, String negMSg) {
        DialogUtil.showConfigOrBindDialog(this, msg, posMsg, negMSg, new WLDialog.MessageListener() {
            @Override
            public void onClickPositive(View var1, String msg) {
                startActivity(new Intent(QRCodeActivity.this, MiniGatewayActivity.class).
                        putExtra("deviceId", deviceId));
                finish();
            }

            @Override
            public void onClickNegative(View var1) {
                GatewayBindActivity.start(QRCodeActivity.this, deviceId, true);
                finish();
            }
        }).show();
    }

    /**
     * 墙面网关扫描提示dialog
     *
     * @param deviceId
     * @param msg
     * @param posMsg
     * @param negMSg
     */
    private void showGWWallTipsDialog(final String deviceId, String msg, String posMsg, String negMSg) {
        DialogUtil.showConfigOrBindDialog(this, msg, posMsg, negMSg, new WLDialog.MessageListener() {
            @Override
            public void onClickPositive(View var1, String msg) {
                startActivity(new Intent(QRCodeActivity.this, WallGatewayActivity.class).
                        putExtra("deviceId", deviceId).putExtra("scanEntry", ConstantsUtil.DEVICE_SCAN_ENTRY));
                finish();
            }

            @Override
            public void onClickNegative(View var1) {
                GatewayBindActivity.start(QRCodeActivity.this, deviceId, true);
                finish();
            }
        }).show();
    }


    /**
     * 扫描提示dialog
     */
    private void showScanTipsDialog(String msg) {
        DialogUtil.showUnknownDeviceTips(this, new WLDialog.MessageListener() {
            @Override
            public void onClickPositive(View var1, String msg) {
                QRCodeActivity.this.finish();
            }

            @Override
            public void onClickNegative(View var1) {

            }
        }, msg).show();
    }


    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            WLog.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager, DecodeThread.ALL_MODE);
            }

            initCrop();
        } catch (IOException ioe) {
            WLog.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            WLog.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        // camera error
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("Camera error");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }

        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.show();
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

    public Rect getCropRect() {
        return mCropRect;
    }

    /**
     * 初始化截取的矩形区域
     */
    private void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().y;
        int cameraHeight = cameraManager.getCameraResolution().x;

        /** 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();

        /** 获取布局容器的宽高 */
        int containerWidth = scanContainer.getWidth();
        int containerHeight = scanContainer.getHeight();

        /** 计算最终截取的矩形的左上角顶点x坐标 */
        int x = cropLeft * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的左上角顶点y坐标 */
        int y = cropTop * cameraHeight / containerHeight;

        /** 计算最终截取的矩形的宽度 */
        int width = cropWidth * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的高度 */
        int height = cropHeight * cameraHeight / containerHeight;

        /** 生成最终的截取的矩形 */
        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (TextUtils.equals(scanType, ConstantsUtil.GET_QR_CODE)) {
            finish();
        } else {
            startActivity(new Intent(this, AddDeviceActivity.class));
        }
    }
}