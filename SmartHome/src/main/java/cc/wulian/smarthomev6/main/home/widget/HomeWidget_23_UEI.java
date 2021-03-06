package cc.wulian.smarthomev6.main.home.widget;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cc.wulian.smarthomev6.R;
import cc.wulian.smarthomev6.entity.UeiConfig;
import cc.wulian.smarthomev6.main.application.MainApplication;
import cc.wulian.smarthomev6.main.device.device_23.AirConditioning.AirConditioningMainActivity;
import cc.wulian.smarthomev6.main.device.device_23.Device23Activity;
import cc.wulian.smarthomev6.main.device.device_23.Device23CategoryListActivity;
import cc.wulian.smarthomev6.main.device.device_23.custom.CustomActivity;
import cc.wulian.smarthomev6.main.device.device_23.tv.AudioRemoteMainActivity;
import cc.wulian.smarthomev6.main.device.device_23.tv.ProjectorRemoteMainActivity;
import cc.wulian.smarthomev6.main.device.device_23.tv.TvRemoteMainActivity;
import cc.wulian.smarthomev6.support.core.device.Device;
import cc.wulian.smarthomev6.support.core.device.DeviceInfoDictionary;
import cc.wulian.smarthomev6.support.core.mqtt.MQTTCmdHelper;
import cc.wulian.smarthomev6.support.core.mqtt.MQTTManager;
import cc.wulian.smarthomev6.support.core.mqtt.bean.GatewayConfigBean;
import cc.wulian.smarthomev6.support.event.DeviceInfoChangedEvent;
import cc.wulian.smarthomev6.support.event.DeviceReportEvent;
import cc.wulian.smarthomev6.support.event.GatewayConfigEvent;
import cc.wulian.smarthomev6.support.event.GetRoomListEvent;
import cc.wulian.smarthomev6.support.event.RoomInfoEvent;
import cc.wulian.smarthomev6.support.tools.Preference;
import cc.wulian.smarthomev6.support.utils.WLog;

import static cc.wulian.smarthomev6.main.device.device_23.tv.RemoteControlBrandActivity.SINGLE_CODE_PROJECTOR;

/**
 * Created by Veev on 2017/5/8
 * Tel: 18365264930
 * QQ: 2355738466
 * Function: 红外转发器 首页widget
 */

public class HomeWidget_23_UEI extends RelativeLayout implements IWidgetLifeCycle, View.OnClickListener {

    private static final String TAG = HomeWidget_23_UEI.class.getSimpleName();
    private Device mDevice;
    private HomeItemBean mHomeItemBean;

    private TextView textName, textState, textArea;

    private ImageView mImageFirst, mImageSecond, mImageThird;
    private LinearLayout mLinearFirst, mLinearSecond, mLinearThird, mLinearMore;
    private TextView mTextFirst, mTextSecond, mTextThird, mTextAdd;
    private ConstraintLayout mLayoutAdd;
    private View mRootView;

    private GatewayConfigBean mConfigBean;
    private List<UeiConfig> mControllerList = new ArrayList<>();

    private int mode;

    public HomeWidget_23_UEI(Context context) {
        super(context);

        initView(context);
    }

    public HomeWidget_23_UEI(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);

        initView(context);
    }

    private void initView(Context context) {
        mRootView = LayoutInflater.from(context).inflate(R.layout.view_home_23_uei, null);
        addView(mRootView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        textName = (TextView) mRootView.findViewById(R.id.widget_title_name);
        textArea = (TextView) mRootView.findViewById(R.id.widget_title_room);
        textState = (TextView) mRootView.findViewById(R.id.widget_title_state);

        mImageFirst = (ImageView) mRootView.findViewById(R.id.widget_uei_image_first);
        mImageSecond = (ImageView) mRootView.findViewById(R.id.widget_uei_image_second);
        mImageThird = (ImageView) mRootView.findViewById(R.id.widget_uei_image_third);

        mLinearFirst = (LinearLayout) mRootView.findViewById(R.id.widget_uei_linear_first);
        mLinearSecond = (LinearLayout) mRootView.findViewById(R.id.widget_uei_linear_second);
        mLinearThird = (LinearLayout) mRootView.findViewById(R.id.widget_uei_linear_third);
        mLinearMore = (LinearLayout) mRootView.findViewById(R.id.widget_uei_linear_more);

        mTextFirst = (TextView) mRootView.findViewById(R.id.widget_uei_text_first);
        mTextSecond = (TextView) mRootView.findViewById(R.id.widget_uei_text_second);
        mTextThird = (TextView) mRootView.findViewById(R.id.widget_uei_text_third);
        mTextAdd = (TextView) mRootView.findViewById(R.id.widget_uei_text_add);

        mLayoutAdd = (ConstraintLayout) mRootView.findViewById(R.id.widget_uei_root_add);

        mLinearFirst.setOnClickListener(this);
        mLinearSecond.setOnClickListener(this);
        mLinearThird.setOnClickListener(this);
        mLinearMore.setOnClickListener(this);
        mRootView.findViewById(R.id.widget_uei_view_add).setOnClickListener(this);

        DisplayMetrics mDisplayMetrics = getResources().getDisplayMetrics();
        int titleH = mDisplayMetrics.widthPixels - TypedValue.complexToDimensionPixelSize(30, mDisplayMetrics);
        textName.setMaxWidth(titleH / 2);
        textArea.setMaxWidth(titleH / 4);

        mRootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Device23Activity.start(getContext(), mHomeItemBean.getWidgetID(), false);
            }
        });
    }

    @Override
    public void onBindViewHolder(HomeItemBean bean) {
        EventBus.getDefault().register(this);
        if (bean == null) {
            return;
        }
        mHomeItemBean = HomeWidgetManager.get(bean.getWidgetID());
        mDevice = MainApplication.getApplication().getDeviceCache().get(bean.getWidgetID());
        mConfigBean = MainApplication.getApplication().getGatewayConfigCache().get(bean.getWidgetID(), "list");
        mode = mDevice.mode;
        // 设置在线离线的状态
        updateMode();

        setName();
        setRoomName();

        updateController();

        if (!HomeWidgetManager.hasInCache(mDevice)) {
            MainApplication.getApplication().getMqttManager().publishEncryptedMessage(
                    MQTTCmdHelper.createGatewayConfig(
                            Preference.getPreferences().getCurrentGatewayID(),
                            3,
                            MainApplication.getApplication().getLocalInfo().appID,
                            bean.getWidgetID(),
                            "list",
                            null,
                            null
                    ), MQTTManager.MODE_GATEWAY_FIRST
            );
        }
        HomeWidgetManager.add2Cache(mDevice);
    }

    private void updateController() {
        mConfigBean = MainApplication.getApplication().getGatewayConfigCache().get(mHomeItemBean.getWidgetID(), "list");

        int count = 0;
        if (mConfigBean == null || TextUtils.isEmpty(mConfigBean.v)) {
            // 没有遥控器
        } else {
            mControllerList = JSON.parseArray(mConfigBean.v, UeiConfig.class);
            count = mControllerList.size();
        }

        if (count > 3) {
            mLinearMore.setVisibility(VISIBLE);
        } else {
            mLinearMore.setVisibility(INVISIBLE);
        }

        count = count >= 3 ? 3 : count;

        mLinearFirst.setVisibility(INVISIBLE);
        mLinearSecond.setVisibility(INVISIBLE);
        mLinearThird.setVisibility(INVISIBLE);
        switch (count) {
            case 0:
                mLayoutAdd.setVisibility(VISIBLE);
                break;
            case 3:
                mLinearThird.setVisibility(VISIBLE);
                mTextThird.setText(mControllerList.get(2).getName());
                mImageThird.setImageResource(mControllerList.get(2).getWidgetImg(mDevice.isOnLine(),mControllerList.get(2).type, mControllerList.get(2).singleCode));
            case 2:
                mLinearSecond.setVisibility(VISIBLE);
                mTextSecond.setText(mControllerList.get(1).getName());
                mImageSecond.setImageResource(mControllerList.get(1).getWidgetImg(mDevice.isOnLine(),mControllerList.get(1).type, mControllerList.get(1).singleCode));
            case 1:
                mLinearFirst.setVisibility(VISIBLE);
                mTextFirst.setText(mControllerList.get(0).getName());
                mImageFirst.setImageResource(mControllerList.get(0).getWidgetImg(mDevice.isOnLine(),mControllerList.get(0).type, mControllerList.get(0).singleCode));
            default:
                mLayoutAdd.setVisibility(INVISIBLE);
                break;
        }
    }

    @Override
    public void onViewRecycled() {
        EventBus.getDefault().unregister(this);
    }

    private void setName() {
        if (mDevice == null) {
            textName.setText(DeviceInfoDictionary.getNameByTypeAndName(mHomeItemBean.getName(), mHomeItemBean.getType()));
        } else {
            textName.setText(DeviceInfoDictionary.getNameByTypeAndName(mDevice.type, mDevice.name));
        }
    }

    /**
     * 设置分区名
     */
    private void setRoomName() {
        String areaName = ((MainApplication) getContext().getApplicationContext()).getRoomCache().getRoomName(mDevice);
        textArea.setText(areaName);
    }

    /**
     * 更新上下线
     */
    private void updateMode() {
        switch (mode) {
            case 0:
            case 4:
            case 1:
                // 上线
                textState.setText(R.string.Device_Online);
                textState.setTextColor(getResources().getColor(R.color.colorPrimary));
//                mRootView.setEnabled(true);
                mTextAdd.setEnabled(true);
                break;
            case 2:
                // 离线
                textState.setText(R.string.Device_Offline);
                textState.setTextColor(getResources().getColor(R.color.newStateText));
//                mRootView.setEnabled(false);
                mTextAdd.setEnabled(false);
                break;
            case 3:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GatewayConfigEvent event) {
        WLog.i(TAG, "收到红外config了: " + event.bean);

        if (!TextUtils.equals(event.bean.d, mHomeItemBean.getWidgetID())) {
            return;
        }

        updateController();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceReport(DeviceReportEvent event) {
        if (event.device != null && mDevice != null) {
            if (TextUtils.equals(event.device.devID, mDevice.devID)) {
                mDevice = MainApplication.getApplication().getDeviceCache().get(mDevice.devID);
                // do something...
                mode = event.device.mode;
                updateMode();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceInfoChanged(DeviceInfoChangedEvent event) {
        if (event.deviceInfoBean != null && mDevice != null) {
            if (TextUtils.equals(event.deviceInfoBean.devID, mDevice.devID)) {
                if (event.deviceInfoBean.mode == 2) {
                    mDevice = MainApplication.getApplication().getDeviceCache().get(mDevice.devID);
                    textName.setText(DeviceInfoDictionary.getNameByTypeAndName(mDevice.type, event.deviceInfoBean.name));
                    setRoomName();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRoomInfoEvent(RoomInfoEvent event) {
        mDevice = MainApplication.getApplication().getDeviceCache().get(mDevice.devID);
        setRoomName();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetRoomListEvent(GetRoomListEvent event) {
        mDevice = MainApplication.getApplication().getDeviceCache().get(mDevice.devID);
        setRoomName();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.widget_uei_linear_more) {
            Device23Activity.start(getContext(), mHomeItemBean.getWidgetID(), false);
            return;
        }

        if (mode == 2) {
            return;
        }
        int index = -1;
        switch (v.getId()) {
            case R.id.widget_uei_linear_first:
                index = 0;
                break;
            case R.id.widget_uei_linear_second:
                index = 1;
                break;
            case R.id.widget_uei_linear_third:
                index = 2;
                break;
            case R.id.widget_uei_view_add:
                Device23CategoryListActivity.start(getContext(), mHomeItemBean.getWidgetID());
                break;
        }

        if (index != -1 && index < mControllerList.size()) {
            switch (mControllerList.get(index).type) {
                case "Z":
                    AirConditioningMainActivity.start(
                            v.getContext(),
                            mHomeItemBean.getWidgetID(),
                            mControllerList.get(index).getName(),
                            mControllerList.get(index).brand,
                            mControllerList.get(index).pick,
                            mControllerList.get(index).time
                    );
                    break;
                case "T":
                    if(SINGLE_CODE_PROJECTOR.equals(mControllerList.get(index).singleCode)){
                        ProjectorRemoteMainActivity.start(v.getContext(), mDevice.devID,
                                mControllerList.get(index).type,
                                mControllerList.get(index).getName(),
                                mControllerList.get(index).brand,
                                mControllerList.get(index).pick,
                                mControllerList.get(index).time);
                    }else{
                        TvRemoteMainActivity.start(v.getContext(), mDevice.devID,
                                mControllerList.get(index).type,
                                mControllerList.get(index).getName(),
                                mControllerList.get(index).brand,
                                mControllerList.get(index).pick,
                                mControllerList.get(index).time);
                    }
                    break;
                case "C":
                    TvRemoteMainActivity.start(v.getContext(), mDevice.devID,
                            mControllerList.get(index).type,
                            mControllerList.get(index).getName(),
                            mControllerList.get(index).brand,
                            mControllerList.get(index).pick,
                            mControllerList.get(index).time);
                    break;
                case "R,M":
                    AudioRemoteMainActivity.start(v.getContext(), mDevice.devID,
                            mControllerList.get(index).type,
                            mControllerList.get(index).getName(),
                            mControllerList.get(index).brand,
                            mControllerList.get(index).pick,
                            mControllerList.get(index).time);
                    break;
                case "CUSTOM":
                    CustomActivity.start(v.getContext(), mDevice.devID,
                            mControllerList.get(index).getName(),
                            mControllerList.get(index).brand,
                            mControllerList.get(index).pick,
                            mControllerList.get(index).time);
                    break;
            }
        }
    }

}
