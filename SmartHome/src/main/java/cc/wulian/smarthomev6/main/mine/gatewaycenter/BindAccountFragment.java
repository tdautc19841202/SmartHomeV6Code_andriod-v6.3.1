package cc.wulian.smarthomev6.main.mine.gatewaycenter;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cc.wulian.smarthomev6.R;
import cc.wulian.smarthomev6.main.mine.gatewaycenter.adapter.AccountListAdapter;
import cc.wulian.smarthomev6.main.application.BaseFragment;
import cc.wulian.smarthomev6.support.core.apiunit.AuthApiUnit;
import cc.wulian.smarthomev6.support.core.apiunit.bean.UserBean;
import cc.wulian.smarthomev6.support.customview.swipemenu.SwipeMenu;
import cc.wulian.smarthomev6.support.customview.swipemenu.SwipeMenuAdapter;
import cc.wulian.smarthomev6.support.customview.swipemenu.SwipeMenuCreator;
import cc.wulian.smarthomev6.support.customview.swipemenu.SwipeMenuItem;
import cc.wulian.smarthomev6.support.customview.swipemenu.SwipeMenuListView;
import cc.wulian.smarthomev6.support.tools.dialog.ProgressDialogManager;
import cc.wulian.smarthomev6.support.tools.dialog.WLDialog;
import cc.wulian.smarthomev6.support.utils.DisplayUtil;
import cc.wulian.smarthomev6.support.utils.ToastUtil;

/**
 * 作者: mamengchao
 * 时间: 2017/3/29 0029
 * 描述: 绑定账号
 * 联系方式: 805901025@qq.com
 */

public class BindAccountFragment extends BaseFragment {

    private static final String GET = "GET";
    private static final String DELETE = "DELETE";
    private SwipeMenuListView bindAccountListView;
    private AccountListAdapter accountListAdapter;
    private WLDialog.Builder builder;
    private WLDialog dialog;

    private AuthApiUnit authApiUnit;

    private List<UserBean> userBeanList;

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_bind_account;
    }

    @Override
    public void initView() {
        authApiUnit = new AuthApiUnit(mActivity);
        userBeanList = new ArrayList<>();

        bindAccountListView = (SwipeMenuListView) rootView.findViewById(R.id.bind_account_listView);

        accountListAdapter = new AccountListAdapter(mActivity, null);
        accountListAdapter.setMenuCreator(creatLeftDeleteItem());
        bindAccountListView.setAdapter(accountListAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)  {
        super.onViewCreated(view, savedInstanceState);

        initListeners();
        loadAllBindAccount();
    }

    private void initListeners() {
        // 左划删除
        accountListAdapter.setOnMenuItemClickListener(new SwipeMenuAdapter.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                UserBean userBean = accountListAdapter.getItem(position);
                String userid = preference.getUserID();
                if (!TextUtils.isEmpty(userid)){
                    if (TextUtils.equals(userid, userBean.uId)){
                        showDialog();
                    }else{
                        showDialog(userBean.uId);
                    }
                }
            }
        });
        bindAccountListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
            }
        });
    }

    /**
     * 创建左划删除item样式
     */
    private SwipeMenuCreator creatLeftDeleteItem() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu, int position) {
                SwipeMenuItem settingItem = new SwipeMenuItem(mActivity);
                settingItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                settingItem.setWidth(DisplayUtil.dip2Pix(mActivity, 90));
                settingItem.setTitle(R.string.AuthManager_Disbind);
                settingItem.setTitleSize(DisplayUtil.dip2Sp(mActivity, 5));
                settingItem.setTitleColor(mActivity.getResources().getColor(R.color.white));
                menu.addMenuItem(settingItem);
            }
        };
        return creator;
    }

    /**
     * 加载当前网关已授权的用户
     */
    private void loadAllBindAccount(){
        ProgressDialogManager.getDialogManager().showDialog(GET, mActivity, null, null, mActivity.getResources().getInteger(R.integer.http_timeout));
        String currentGateway = preference.getCurrentGatewayID();
        authApiUnit.doGetAllBindAccount(currentGateway, new AuthApiUnit.AuthApiCommonListener<List<UserBean>>() {
            @Override
            public void onSuccess(List<UserBean> bean) {
                userBeanList.clear();
                for (UserBean userBean:bean) {
                    if (TextUtils.isEmpty(userBean.phone) && TextUtils.isEmpty(userBean.email)){
                        continue;
                    }
                    userBeanList.add(userBean);
                }
                accountListAdapter.swapData(userBeanList);
                ProgressDialogManager.getDialogManager().dimissDialog(GET, 0);
            }

            @Override
            public void onFail(int code, String msg) {
                ProgressDialogManager.getDialogManager().dimissDialog(GET, 0);
                ToastUtil.show(msg);
            }
        });
    }

    /**
     * 解除绑定：待提供接口
     */
    private void unAuth(String granteeUid){
        ProgressDialogManager.getDialogManager().showDialog(DELETE, mActivity, null, null, mActivity.getResources().getInteger(R.integer.http_timeout));
        String currentGateway = preference.getCurrentGatewayID();
        authApiUnit.doUnAuthAccount(currentGateway, granteeUid, new AuthApiUnit.AuthApiCommonListener() {
            @Override
            public void onSuccess(Object bean) {
                ProgressDialogManager.getDialogManager().dimissDialog(DELETE, 0);
                ToastUtil.show(R.string.GatewaySetts_Disbind_Success);
                loadAllBindAccount();
            }

            @Override
            public void onFail(int code, String msg) {
                ProgressDialogManager.getDialogManager().dimissDialog(DELETE, 0);
                ToastUtil.show(msg);
            }
        });
    }

    private void showDialog(final String id) {
        builder = new WLDialog.Builder(mActivity);
        builder.setTitle(getString(R.string.Hint))
                .setCancelOnTouchOutSide(false)
                .setDismissAfterDone(false)
                .setMessage(getString(R.string.Gateway_User_Hint1))
                .setPositiveButton(this.getResources().getString(R.string.AuthManager_UnBind))
                .setNegativeButton(this.getResources().getString(R.string.Cancel))
                .setListener(new WLDialog.MessageListener() {
                    @Override
                    public void onClickPositive(View view, String msg) {
                        unAuth(id);
                        dialog.dismiss();
                    }

                    @Override
                    public void onClickNegative(View view) {
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void showDialog() {
        builder = new WLDialog.Builder(mActivity);
        builder.setTitle(getString(R.string.Hint))
                .setCancelOnTouchOutSide(false)
                .setDismissAfterDone(false)
                .setMessage(R.string.Gateway_User_Hint2)
                .setPositiveButton(this.getResources().getString(R.string.Sure))
                .setListener(new WLDialog.MessageListener() {
                    @Override
                    public void onClickPositive(View view, String msg) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onClickNegative(View view) {
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }
}
