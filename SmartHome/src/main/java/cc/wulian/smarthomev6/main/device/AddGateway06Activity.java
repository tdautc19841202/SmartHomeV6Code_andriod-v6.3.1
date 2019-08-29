package cc.wulian.smarthomev6.main.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import cc.wulian.smarthomev6.R;
import cc.wulian.smarthomev6.main.application.BaseTitleActivity;
import cc.wulian.smarthomev6.main.application.MainApplication;
import cc.wulian.smarthomev6.main.mine.gatewaycenter.GatewayBindActivity;
import cc.wulian.smarthomev6.support.tools.skin.SkinManager;
import cc.wulian.smarthomev6.support.tools.skin.SkinResouceKey;

/**
 * Created by 上海滩小马哥 on 2017/10/19.
 */

public class AddGateway06Activity extends BaseTitleActivity {

    private Button btn_next_step;
    private String deviceId;
    private String deviceType;
    private ImageView imageView;


    public static void start(Context context, String deviceId, String deviceType) {
        Intent intent = new Intent(context, AddGateway06Activity.class);
        intent.putExtra("deviceId", deviceId);
        intent.putExtra("deviceType", deviceType);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gateway_06, true);
    }

    @Override
    protected void initTitle() {
        super.initTitle();
        setToolBarTitle(getString(R.string.Device_Default_Name_GW06));
    }

    @Override
    protected void initView() {
        super.initView();
        btn_next_step = (Button) findViewById(R.id.btn_next_step);
        imageView = (ImageView) findViewById(R.id.iv_device);
    }

    @Override
    protected void updateSkin() {
        super.updateSkin();
        SkinManager skinManager = MainApplication.getApplication().getSkinManager();
        skinManager.setBackground(btn_next_step, SkinResouceKey.BITMAP_BUTTON_BG_S);
        skinManager.setTextColor(btn_next_step, SkinResouceKey.COLOR_BUTTON_TEXT);
    }

    @Override
    protected void initData() {
        super.initData();
        deviceId = getIntent().getStringExtra("deviceId");
        deviceType = getIntent().getStringExtra("deviceType");

        if (TextUtils.equals(deviceType, "GW06")) {
            setToolBarTitle(R.string.Device_Default_Name_GW06);
        } else if (TextUtils.equals(deviceType, "GW12")) {
            setToolBarTitle(R.string.Device_Default_Name_GW12);
            imageView.setImageResource(R.drawable.icon_gateway11_guide);
        }
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        btn_next_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GatewayBindActivity.start(AddGateway06Activity.this, deviceId, false);
                finish();
            }
        });
    }
}
