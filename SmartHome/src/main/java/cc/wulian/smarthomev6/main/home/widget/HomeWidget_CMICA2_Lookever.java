package cc.wulian.smarthomev6.main.home.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import cc.wulian.smarthomev6.R;
import cc.wulian.smarthomev6.main.application.MainApplication;
import cc.wulian.smarthomev6.main.device.lookever.CameraLiveStreamActivity;
import cc.wulian.smarthomev6.main.device.lookever.LookeverDetailActivity;
import cc.wulian.smarthomev6.support.core.apiunit.DataApiUnit;
import cc.wulian.smarthomev6.support.core.apiunit.bean.LiveStreamAddressBean;
import cc.wulian.smarthomev6.support.core.apiunit.bean.LiveStreamInfoBean;
import cc.wulian.smarthomev6.support.core.device.Device;
import cc.wulian.smarthomev6.support.core.device.DeviceInfoDictionary;
import cc.wulian.smarthomev6.support.customview.RatioImageView;
import cc.wulian.smarthomev6.support.event.DeviceInfoChangedEvent;
import cc.wulian.smarthomev6.support.event.DeviceReportEvent;
import cc.wulian.smarthomev6.support.event.LastFrameEvent;
import cc.wulian.smarthomev6.support.tools.ImageLoaderTool;
import cc.wulian.smarthomev6.support.utils.FileUtil;

/**
 * 作者: chao
 * 时间: 2017/6/16
 * 描述: 随便看首页widget
 * 联系方式: 805901025@qq.com
 */

public class HomeWidget_CMICA2_Lookever extends RelativeLayout implements IWidgetLifeCycle {

    private TextView textName, textState;
    private ImageView home_view_cateye_play;
    private RatioImageView imageBg;

    private Device mDevice;
    private DataApiUnit dataApiUnit;

    public HomeWidget_CMICA2_Lookever(Context context) {
        super(context);
        initView(context);
    }

    @Override
    public void onBindViewHolder(HomeItemBean bean) {
        EventBus.getDefault().register(this);

        mDevice = MainApplication.getApplication().getDeviceCache().get(bean.getWidgetID());
        setName();
        setMode();

        String snapshotPath = FileUtil.getLastFramePath();
        String fileName = bean.getWidgetID() + ".jpg";
        showSnapshot(snapshotPath + "/" + fileName);
    }

    @Override
    public void onViewRecycled() {
        EventBus.getDefault().unregister(this);
    }

    private void initView(final Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.view_home_cmica2_lookever, null);
        addView(rootView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        textName = (TextView) rootView.findViewById(R.id.home_view_cateye_name);
        textState = (TextView) rootView.findViewById(R.id.home_view_cateye_state);
        imageBg = (RatioImageView) rootView.findViewById(R.id.home_view_cmicy1_eques_img_bg);
        home_view_cateye_play = (ImageView) rootView.findViewById(R.id.home_view_cateye_play);

        rootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                start(context, mDevice);
                LookeverDetailActivity.start(context, mDevice,false);
            }
        });
    }

    private void setState(Device device) {
        if (device != null) {
            if (device.isOnLine()) {
                // 上线
                textState.setText(getResources().getString(R.string.Device_Online));
                home_view_cateye_play.setImageResource(R.drawable.home_view_cateye_play_online);
            } else {
                //下线
                textState.setText(getResources().getString(R.string.Device_Offline));
                home_view_cateye_play.setImageResource(R.drawable.home_view_cateye_play_offline);
            }
        }
    }


    private void start(final Context context, final Device device) {
        dataApiUnit = new DataApiUnit(context);
        dataApiUnit.doGetLiveStreamData(device.devID, new DataApiUnit.DataApiCommonListener<LiveStreamInfoBean>() {
            @Override
            public void onSuccess(LiveStreamInfoBean bean) {
                if (bean.Open == 1) {
                    CameraLiveStreamActivity.start(context, device);
                } else {
                    LookeverDetailActivity.start(context, device,false);
                }
            }

            @Override
            public void onFail(int code, String msg) {

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLastFrameEvent(LastFrameEvent event) {
        if (mDevice.devID.equals(event.deviceId)) {
            showSnapshot(event.path);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceReport(DeviceReportEvent event) {
        if (event.device != null && mDevice != null) {
            if (TextUtils.equals(event.device.devID, mDevice.devID)) {
                mDevice = MainApplication.getApplication().getDeviceCache().get(mDevice.devID);
                setName();
                setMode();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceInfoChanged(DeviceInfoChangedEvent event) {
        if (event.deviceInfoBean != null && mDevice != null) {
            if (TextUtils.equals(event.deviceInfoBean.devID, mDevice.devID)) {
                if (event.deviceInfoBean.mode == 2) {
                    mDevice = MainApplication.getApplication().getDeviceCache().get(mDevice.devID);
                    setName();
                    setMode();
                }
            }
        }
    }

    private void setName() {
        textName.setText(DeviceInfoDictionary.getNameByDevice(mDevice));
    }

    private void setMode() {
        if (mDevice != null && mDevice.isOnLine()) {
            textState.setText(R.string.Device_Online);
        } else {
            textState.setText(R.string.Device_Offline);
        }
    }

    private void showSnapshot(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                imageBg.setImageBitmap(bitmap);
            }
        }
    }

}
