package cc.wulian.smarthomev6.main.device.lookever.setting;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wulian.sdk.android.ipc.rtcv2.IPCMsgController;
import com.wulian.sdk.android.ipc.rtcv2.message.IPCcameraXmlMsgEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.wulian.smarthomev6.R;
import cc.wulian.smarthomev6.main.application.BaseTitleActivity;
import cc.wulian.smarthomev6.main.application.MainApplication;
import cc.wulian.smarthomev6.support.callback.IcamMsgEventHandler;
import cc.wulian.smarthomev6.support.core.apiunit.bean.icam.ICamDeviceBean;
import cc.wulian.smarthomev6.support.tools.dialog.ProgressDialogManager;
import cc.wulian.smarthomev6.support.tools.dialog.WLDialog;
import cc.wulian.smarthomev6.support.tools.skin.SkinManager;
import cc.wulian.smarthomev6.support.tools.skin.SkinResouceKey;
import cc.wulian.smarthomev6.support.utils.CameraUtil;
import cc.wulian.smarthomev6.support.utils.ConfigUtil;
import cc.wulian.smarthomev6.support.utils.ConstantsUtil;
import cc.wulian.smarthomev6.support.utils.DialogUtil;
import cc.wulian.smarthomev6.support.utils.ToastUtil;
import cc.wulian.smarthomev6.support.utils.WLog;

/**
 * Created by hxc on 2017/6/7.
 * func:随便看安全防护界面
 */

public class LookEverSafeProtectActivity extends BaseTitleActivity implements IcamMsgEventHandler, View.OnClickListener {
    private ImageView ivSafeProtectIcon;
    private TextView tvSafeProtectTips1;
    private TextView tvSafeProtectTips2;
    private TextView tvProtectTime;
    private LinearLayout llSafeProtectTime;
    private Button btnResetProtect;
    private Button btnStopProtect;
    private Button btnProtect;

    private String deviceID;
    private String uniqueDeviceId;
    private String sipDomain;

    private ICamDeviceBean iCamDeviceBean;
    private SharedPreferences sp;
    public static final int REQUESTCODE_SETTING = 1;
    public static final String STOP_PROTECT = "stop_protect";
    private static final String QUERY = "query";
    private WLDialog mStopDialog;
    private int sipokcount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loookever_safe_protect, true);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initTitle() {
        super.initTitle();
        setToolBarTitle(getResources().getString(R.string.Device_Category_Safety));
    }

    @Override
    protected void initView() {
        super.initView();
        ivSafeProtectIcon = (ImageView) findViewById(R.id.iv_safe_protect_icon);
        tvSafeProtectTips1 = (TextView) findViewById(R.id.tv_safe_protect_tips1);
        tvSafeProtectTips2 = (TextView) findViewById(R.id.tv_safe_protect_tips2);
        tvProtectTime = (TextView) findViewById(R.id.tv_protect_time);
        llSafeProtectTime = (LinearLayout) findViewById(R.id.ll_safe_protect_time);
        btnResetProtect = (Button) findViewById(R.id.btn_reset_protect);
        btnStopProtect = (Button) findViewById(R.id.btn_stop_protect);
        btnProtect = (Button) findViewById(R.id.btn_protect);
    }

    @Override
    protected void initData() {
        super.initData();
        iCamDeviceBean = (ICamDeviceBean) getIntent().getSerializableExtra("ICamDeviceBean");
        deviceID = iCamDeviceBean.did;
        uniqueDeviceId = iCamDeviceBean.uniqueDeviceId;
        sipDomain = iCamDeviceBean.sdomain;
        sp = getSharedPreferences(ConfigUtil.SP_CONFIG, MODE_PRIVATE);
        initWebData();
    }

    @Override
    protected void updateSkin() {
        super.updateSkin();
        SkinManager skinManager = MainApplication.getApplication().getSkinManager();
        skinManager.setBackground(btnProtect, SkinResouceKey.BITMAP_BUTTON_BG_S);
        skinManager.setBackground(btnStopProtect, SkinResouceKey.BITMAP_BUTTON_BG_S);
        skinManager.setTextColor(btnProtect, SkinResouceKey.COLOR_BUTTON_TEXT);
        skinManager.setTextColor(btnStopProtect, SkinResouceKey.COLOR_BUTTON_TEXT);
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        ivSafeProtectIcon.setOnClickListener(this);
        tvSafeProtectTips1.setOnClickListener(this);
        tvSafeProtectTips2.setOnClickListener(this);
        tvProtectTime.setOnClickListener(this);
        llSafeProtectTime.setOnClickListener(this);
        btnResetProtect.setOnClickListener(this);
        btnProtect.setOnClickListener(this);
        btnStopProtect.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_stop_protect:
                showStopConfirmDialog();
                break;
            case R.id.btn_reset_protect:
            case R.id.btn_protect:
                startActivityForResult((new Intent(this,
                        LookEverProtectSettingActivity.class)).putExtra("ICamDeviceBean",
                        iCamDeviceBean), REQUESTCODE_SETTING);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        sp = getSharedPreferences(ConfigUtil.SP_CONFIG, MODE_PRIVATE);
        switch (requestCode) {
            case REQUESTCODE_SETTING:
                if (resultCode == RESULT_OK) {
                    WLog.i("safeProtect", "----onActivityResult--->");
                    updateView();
                }
                break;

            default:
                break;
        }
    }

    public void initWebData() {
        // 查询移动布防
        progressDialogManager.showDialog(QUERY, this, null, null, 10000);
        IPCMsgController.MsgQueryMovementDetectionInfo(
                uniqueDeviceId, sipDomain);
        IPCMsgController.MsgQueryLinkageArmingInfo(uniqueDeviceId,
                sipDomain);
    }

    private void updateView() {
        boolean isMoveEnable = sp.getBoolean(deviceID
                + ConfigUtil.IS_MOVE_DETECTION, false);
        WLog.i("hxctest", "updateview = " + isMoveEnable);
        String weekday = sp.getString(deviceID
                + ConfigUtil.MOVE_WEEKDAY, "");
        String time = sp.getString(deviceID
                + ConfigUtil.MOVE_TIME, "");
        WLog.i("safeProtect", "isMoveEnable = " + isMoveEnable);
        WLog.i("safeProtect", "weekday = " + weekday + ",time = " + time);
        if (isMoveEnable) {
            if (TextUtils.isEmpty(weekday) || TextUtils.isEmpty(time)) {
                tvSafeProtectTips1.setVisibility(View.VISIBLE);
                tvSafeProtectTips1.setText(getString(R.string.Safety_Protection_Title));
                tvSafeProtectTips2.setVisibility(View.VISIBLE);
                ivSafeProtectIcon.setImageResource(R.drawable.icon_safe_protect_normal);
                llSafeProtectTime.setVisibility(View.GONE);
                btnResetProtect.setVisibility(View.GONE);
                btnStopProtect.setVisibility(View.GONE);
                btnProtect.setVisibility(View.VISIBLE);
            } else {
                ivSafeProtectIcon.setImageResource(R.drawable.icon_safe_protect_set);
                tvSafeProtectTips1.setText(getResources().getString(R.string.Opened_Safety_Protection));
                tvSafeProtectTips1.setVisibility(View.VISIBLE);
                tvSafeProtectTips2.setVisibility(View.GONE);
                llSafeProtectTime.setVisibility(View.VISIBLE);
                btnProtect.setVisibility(View.GONE);
                btnResetProtect.setVisibility(View.VISIBLE);
                btnStopProtect.setVisibility(View.VISIBLE);
                String weekday_move = CameraUtil.convertWeekday(LookEverSafeProtectActivity.this, weekday);
                String time_move = CameraUtil.convertTime(this, time);
                tvProtectTime.setText(weekday_move + time_move);
            }
        } else {
            tvSafeProtectTips1.setVisibility(View.VISIBLE);
            tvSafeProtectTips1.setText(getString(R.string.Safety_Protection_Title));
            tvSafeProtectTips2.setVisibility(View.VISIBLE);
            ivSafeProtectIcon.setImageResource(R.drawable.icon_safe_protect_normal);
            llSafeProtectTime.setVisibility(View.GONE);
            btnResetProtect.setVisibility(View.GONE);
            btnStopProtect.setVisibility(View.GONE);
            btnProtect.setVisibility(View.VISIBLE);
        }
    }

    private void showStopConfirmDialog() {
        Resources rs = getResources();
        mStopDialog = DialogUtil.showCommonDialog(this,
                rs.getString(R.string.Hint),
                rs.getString(R.string.Stop_Protection_Tip), rs.getString(R.string.Sure), rs.getString(R.string.Cancel),
                new WLDialog.MessageListener() {
                    @Override
                    public void onClickPositive(View var1, String msg) {
                        mStopDialog.dismiss();
                        stopProtect();
                    }

                    @Override
                    public void onClickNegative(View var1) {
                        mStopDialog.dismiss();
                    }
                });

        mStopDialog.show();
    }

    private void stopProtect() {
        String moveArea = sp.getString(deviceID
                + ConfigUtil.MOVE_AREA, ";");
        IPCMsgController.MsgConfigMovementDetection(uniqueDeviceId,
                sipDomain, false, 50, moveArea.split(";"));
        IPCMsgController.MsgConfigLinkageArming(uniqueDeviceId,
                sipDomain, false, null);
        ProgressDialogManager.getDialogManager().showDialog(STOP_PROTECT, this, null, null, 10000);
        ConstantsUtil.START_PROTECT_AREA = false;
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(IPCcameraXmlMsgEvent event) {
        if (event.getCode() != 0) {
            Log.i("SettingSipMSg", "fail---" + "apiType = " + event.getApiType() + "msg = " + event.getMessage());
            switch (event.getApiType()) {
                case QUERY_MOVEMENT_DETECTION_INFO:
                case QUERY_LINKAGE_ARMING_INFO:
                    ToastUtil.show(getString(R.string.Config_Query_Device_Fail));
                    break;
                case CONFIG_LINKAGE_ARMING:
                case CONFIG_MOVEMENT_DETECTION:
                    ToastUtil.show(getString(R.string.Setting_Fail));
                    break;
            }
        } else {
            switch (event.getApiType()) {
                case QUERY_MOVEMENT_DETECTION_INFO:
                    progressDialogManager.dimissDialog(QUERY, 0);
                    String[] results1 = CameraUtil.getMotionArea(event.getMessage(), 4);
                    StringBuilder sb = new StringBuilder();
                    for (String s : results1) {
                        sb.append(s).append(";");
                    }
                    boolean isMoveEnable = "true".equals(CameraUtil.getParamFromXml(
                            event.getMessage(), "enable")) ? true : false;
                    int sen = 0;
                    try {
                        sen = Integer.parseInt(CameraUtil.getParamFromXml(event.getMessage(),
                                "sensitivity"));
                    } catch (NumberFormatException e) {
                        sen = 50;
                    }
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt(deviceID
                            + ConfigUtil.MOVE_SENSITIVITY, sen);// 灵敏度
                    editor.putBoolean(deviceID
                            + ConfigUtil.IS_MOVE_DETECTION, isMoveEnable);// 启用
                    WLog.i("hxctest", "QUERY_MOVEMENT_DETECTION_INFO = " + isMoveEnable);
                    editor.putString(deviceID + ConfigUtil.MOVE_AREA,
                            sb.toString().equals("") ? ";" : sb.toString());// 区域
                    editor.commit();

                    updateView();
                    break;
                case QUERY_LINKAGE_ARMING_INFO:
                    // TODO 数据返回，进行存储，需判断跨夜时间段
                    int eventType = 0;
                    try {
                        eventType = Integer.parseInt(CameraUtil.getParamFromXml(event.getMessage(),
                                "eventType"));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (eventType == 1 || eventType == 2) {// 1 移动布防 2 遮挡布防
                        String config_weekday, config_time;
                        if (eventType == 1) {
                            config_weekday = deviceID
                                    + ConfigUtil.MOVE_WEEKDAY;
                            config_time = deviceID
                                    + ConfigUtil.MOVE_TIME;
                        } else {
                            config_weekday = deviceID
                                    + ConfigUtil.COVER_WEEKDAY;
                            config_time = deviceID
                                    + ConfigUtil.COVER_TIME;
                        }

                        String weekTime = CameraUtil.getWeekFromXmlData(event.getMessage());
                        SharedPreferences.Editor weekAndTimeEditor = sp.edit();
                        weekAndTimeEditor.putString(config_weekday, weekTime);
                        Pattern p = Pattern
                                .compile("<time start=\"(.+)\" end=\"(.+)\">0</time>");
                        Pattern p2 = Pattern
                                .compile("<time start=\"(.+)\" end=\"(.+)\">1</time>");
                        Matcher matcher = p.matcher(event.getMessage());
                        Matcher matcher2 = p2.matcher(event.getMessage());
                        String savaTime = "";
                        if (matcher.find()) {
                            String startTime[] = matcher.group(1).trim().split(":");
                            String endTime[] = matcher.group(2).trim().split(":");
                            savaTime = startTime[0] + "," + startTime[1] + ","
                                    + endTime[0] + "," + endTime[1];
                        }
                        if (matcher2.find()) {
                            String startTime2[] = matcher2.group(1).trim()
                                    .split(":");
                            String endTime2[] = matcher2.group(2).trim().split(":");
                            savaTime += "," + startTime2[0] + "," + startTime2[1]
                                    + "," + endTime2[0] + "," + endTime2[1];
                        }
                        if (savaTime.equals("")) {
                            weekAndTimeEditor.putString(config_time, "");
                        } else {
                            String[] time = savaTime.split(",");
                            // 始终存取四段
                            if (time.length > 4 && time.length == 8) {
                                savaTime = time[0] + "," + time[1] + "," + time[6]
                                        + "," + time[7];
                            }
                            weekAndTimeEditor.putString(config_time, savaTime);
                        }
                        weekAndTimeEditor.commit();
                        updateView();
                    }
                    break;
                case CONFIG_MOVEMENT_DETECTION:
                    if (CameraUtil.getParamFromXml(event.getMessage(), "status").equalsIgnoreCase(
                            "ok")) {
                        // CustomToast.show(this, "1");
                        sipokcount++;
                    } else {
                        ToastUtil.show(getString(R.string.Setting_Fail));
                    }
                    break;
                case CONFIG_LINKAGE_ARMING:
                    if (CameraUtil.getParamFromXml(event.getMessage(), "status").equalsIgnoreCase(
                            "ok")) {
                        sipokcount++;
                    } else {
                        ToastUtil.show(getString(R.string.Setting_Fail));
                    }
                    break;
            }
            if (sipokcount == 2) {
                ToastUtil.show(R.string.Setting_Success);
                sipokcount = 0;
                SharedPreferences.Editor editor = sp.edit();
                if (!ConstantsUtil.START_PROTECT_AREA) {
                    editor.putBoolean(deviceID
                            + ConfigUtil.IS_MOVE_DETECTION, false);
                    WLog.i("hxctest", "STOP_PROTECT = " + false);
                    editor.commit();
                }
                progressDialogManager.dimissDialog(STOP_PROTECT, 0);
                updateView();
            }
        }
    }

}
