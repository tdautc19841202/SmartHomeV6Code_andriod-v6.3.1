package cc.wulian.smarthomev6.main.device.camera_lc.config;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cc.wulian.smarthomev6.R;
import cc.wulian.smarthomev6.entity.LcConfigWifiModel;
import cc.wulian.smarthomev6.entity.SpannableBean;
import cc.wulian.smarthomev6.main.application.MainApplication;
import cc.wulian.smarthomev6.main.application.WLFragment;
import cc.wulian.smarthomev6.main.device.camera_lc.setting.LcCameraSettingActivity;
import cc.wulian.smarthomev6.main.device.camera_lc.setting.LcInformationActivity;
import cc.wulian.smarthomev6.main.device.config.WifiListDialog;
import cc.wulian.smarthomev6.support.tools.dialog.WLDialog;
import cc.wulian.smarthomev6.support.tools.skin.SkinManager;
import cc.wulian.smarthomev6.support.tools.skin.SkinResouceKey;
import cc.wulian.smarthomev6.support.utils.DialogUtil;
import cc.wulian.smarthomev6.support.utils.StringUtil;
import cc.wulian.smarthomev6.support.utils.ToastUtil;

/**
 * Created by hxc on 2017/7/6.
 * func:wifi密码输入界面
 */

public class LcInputWifiFragment extends WLFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private Context context;
    private TextView tvWifiName;
    private TextView tvWifiChange;
    private Button btnWireConfig;
    private EditText etWifiPwd;
    private Button btnNextStep;
    private CheckBox cbNoWiFiPwd;
    private CheckBox cbPwdShow;
    private RelativeLayout rlWifiPwdInput;
    private WifiManager wifiManager;
    private ScanResult scanResult;
    private LcConfigWifiModel configData;
    private WLDialog dialog;
    private FragmentManager manager;
    private FragmentTransaction ft;
    private LcGetReadyFragment getReadyFragment;

    private static final String PERMISSION_ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int PERMISSION_REQUEST_CODE = 1;


    public static LcInputWifiFragment newInstance(LcConfigWifiModel configData) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("configData", configData);
        LcInputWifiFragment deviceInputWifiFragment = new LcInputWifiFragment();
        deviceInputWifiFragment.setArguments(bundle);
        return deviceInputWifiFragment;
    }

    @Override
    public int layoutResID() {
        return R.layout.activity_lc_input_wifi;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        manager = getFragmentManager();
        Bundle bundle = getArguments();
        configData = (LcConfigWifiModel) bundle.getSerializable("configData");
    }

    @Override
    public void initView(View v) {
        mTvTitle.setText(getString(R.string.Config_WiFi));
        btnNextStep = (Button) v.findViewById(R.id.btn_next_step);
        cbNoWiFiPwd = (CheckBox) v.findViewById(R.id.no_wifi_pwd_checkbox);
        rlWifiPwdInput = (RelativeLayout) v.findViewById(R.id.rl_wifi_pwd_input);
        tvWifiName = (TextView) v.findViewById(R.id.tv_wifi_name);
        btnWireConfig = (Button) v.findViewById(R.id.btn_wire_connect);
        etWifiPwd = (EditText) v.findViewById(R.id.et_wifi_pwd);
        etWifiPwd.setTypeface(Typeface.DEFAULT);
        etWifiPwd.setTransformationMethod(new PasswordTransformationMethod());
        cbPwdShow = (CheckBox) v.findViewById(R.id.cb_wifi_pwd_show);
        tvWifiChange = (TextView) v.findViewById(R.id.tv_wifi_change);
        StringUtil.addColorOrSizeorEvent(tvWifiChange, getResources().getString(R.string.Cateye_Connect_Tip2), new SpannableBean[]{new SpannableBean(getResources().getColor(R.color.v6_green), 14, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        })});
    }

    @Override
    protected void updateSkin() {
        super.updateSkin();
        SkinManager skinManager = MainApplication.getApplication().getSkinManager();
        skinManager.setBackground(btnNextStep, SkinResouceKey.BITMAP_BUTTON_BG_S);
        skinManager.setTextColor(btnNextStep, SkinResouceKey.COLOR_BUTTON_TEXT);
    }

    @Override
    protected void initTitle(View v) {
        super.initTitle(v);
        setLeftImg(R.drawable.icon_back);
    }

    @Override
    public void initListener() {
        super.initListener();
        tvWifiName.setOnClickListener(this);
        btnWireConfig.setOnClickListener(this);
        btnNextStep.setOnClickListener(this);
        cbNoWiFiPwd.setOnCheckedChangeListener(this);
        cbPwdShow.setOnCheckedChangeListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        checkPermission();
    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(context, PERMISSION_ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, PERMISSION_ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{PERMISSION_ACCESS_COARSE_LOCATION, PERMISSION_ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        wifiManager = (WifiManager) MainApplication.getApplication().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String wifiName = wifiInfo.getSSID().replace("\"", "");
                tvWifiName.setText(wifiName);
                etWifiPwd.setText(preference.getWifiPassword(wifiName));
                consumeWifiScanResultList();
            }
        }
    }


    public void consumeWifiScanResultList() {
        List<ScanResult> list = wifiManager.getScanResults();
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        for (ScanResult result : list) {
            if (result.BSSID.equals(wifiInfo.getBSSID())) {
                scanResult = result;
                break;
            }
        }
    }

    public void showDialog() {
        WifiListDialog dialog = new WifiListDialog(context, new WifiListDialog.OnWifiSelectedListener() {
            @Override
            public void onGatewaySelected(ScanResult bean) {
                scanResult = bean;
                tvWifiName.setText(bean.SSID);
                etWifiPwd.setText(preference.getWifiPassword(bean.SSID));
            }
        });
        dialog.show();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_next_step) {
            if (scanResult != null) {
                String wifiName = scanResult.SSID;
                String wifiPassword = etWifiPwd.getText().toString();
                configData.setWifiName(wifiName);
                configData.setWifiPwd(wifiPassword);
                if (wifiPassword.length() < 8 && !cbNoWiFiPwd.isChecked()) {
                    ToastUtil.show(getString(R.string.WiFi_Password_Eight));
                } else {
                    preference.setWifiNameAndPassword(wifiName, wifiPassword);
                    ft = manager.beginTransaction();
                    getReadyFragment = LcGetReadyFragment.newInstance(configData);
                    ft.replace(android.R.id.content, getReadyFragment, LcGetReadyFragment.class.getName());
                    ft.addToBackStack(null);
                    ft.commit();
                }
            } else {
                dialog = DialogUtil.showCommonDialog(getActivity(), "",
                        getString(R.string.Get_WiFi_Location), getString(R.string.Open_Btn), getString(R.string.Cancel), new WLDialog.MessageListener() {
                            @Override
                            public void onClickPositive(View var1, String msg) {
                                Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, 0);
                                dialog.dismiss();
                            }

                            @Override
                            public void onClickNegative(View var1) {
                                dialog.dismiss();
                            }
                        });
                dialog.show();

            }
        } else if (id == R.id.tv_wifi_name) {
            showDialog();
        } else if (id == R.id.btn_wire_connect) {
            configData.setWiredConnect(true);
            configData.setWifiConnect(false);
            AddLcCameraGuideActivity.start(getActivity(), configData);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.no_wifi_pwd_checkbox) {
            if (isChecked) {
                rlWifiPwdInput.setVisibility(View.GONE);
            } else {
                rlWifiPwdInput.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.cb_wifi_pwd_show) {
            if (isChecked) {

                etWifiPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etWifiPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            CharSequence charsequence = etWifiPwd.getText();
            if (charsequence instanceof Spannable) {
                Spannable spanText = (Spannable) charsequence;
                Selection.setSelection(spanText, charsequence.length());
            }
        }
    }
}
