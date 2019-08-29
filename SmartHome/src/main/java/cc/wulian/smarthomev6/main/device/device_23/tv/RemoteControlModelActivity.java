package cc.wulian.smarthomev6.main.device.device_23.tv;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import cc.wulian.smarthomev6.support.customview.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uei.control.ACEService;

import java.util.ArrayList;

import cc.wulian.smarthomev6.R;
import cc.wulian.smarthomev6.main.application.BaseTitleActivity;
import cc.wulian.smarthomev6.main.application.MainApplication;
import cc.wulian.smarthomev6.support.core.apiunit.DataApiUnit;
import cc.wulian.smarthomev6.support.core.apiunit.bean.BrandBean;
import cc.wulian.smarthomev6.support.core.apiunit.bean.CodeLibraryBean;
import cc.wulian.smarthomev6.support.tools.EndlessRecyclerOnScrollListener;
import cc.wulian.smarthomev6.support.tools.skin.SkinManager;
import cc.wulian.smarthomev6.support.tools.skin.SkinResouceKey;
import cc.wulian.smarthomev6.support.utils.KeyboardUtil;
import cc.wulian.smarthomev6.support.utils.ToastUtil;
import cc.wulian.smarthomev6.support.utils.WLog;

/**
 * created by huxc  on 2017/11/2.
 * func：电机遥控机型号列表页
 * email: hxc242313@qq.com
 */

public class RemoteControlModelActivity extends BaseTitleActivity {
    private View layout_brand_header;
    private LinearLayout mLinearSearch;
    private EditText mEditSearch;
    private TextView mTextCancel, mTextNoResult;
    private Button btnDownMatchCode;
    private boolean isSearching = false;
    private String deviceID;
    private String ueiType;
    private String singleCode;
    private String mUeiUserID, brandName, localName, mCreatedTime, mName, mPickCode;
    private String searchText;
    DataApiUnit dataApiUnit;
    private SwipeRefreshLayout mSwipe;
    private BandModelListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private int page;
    private EndlessRecyclerOnScrollListener loadMoreListener;
    private ArrayList<CodeLibraryBean.ModelCodeBean> codeList;
    private ArrayList<CodeLibraryBean.ModelCodeBean> modelList;
    private ArrayList<CodeLibraryBean.ModelCodeBean> allModelList;


    /**
     * 品牌型号
     *
     * @param brandName 品牌名
     */
    public static void start(Context context, String deviceID, String ueiType, String brandName, String localName) {
        Intent intent = new Intent(context, RemoteControlModelActivity.class);
        intent.putExtra("deviceID", deviceID);
        intent.putExtra("ueiType", ueiType);
        intent.putExtra("brandName", brandName);
        if (localName != null) {
            intent.putExtra("localName", localName);
        }
        context.startActivity(intent);
    }

    public static void start(Context context, String deviceID, String ueiType, String singleCode, String brandName, String localName) {
        Intent intent = new Intent(context, RemoteControlModelActivity.class);
        intent.putExtra("deviceID", deviceID);
        intent.putExtra("ueiType", ueiType);
        intent.putExtra("singleCode", singleCode);
        intent.putExtra("brandName", brandName);
        if (localName != null) {
            intent.putExtra("localName", localName);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_remote_control_type, true);
    }

    @Override
    protected void initTitle() {
        super.initTitle();
        setToolBarTitle(R.string.Infraredrelay_Addremote_Selectmodel);
    }

    @Override
    protected void initView() {
        super.initView();
        mRecyclerView = (RecyclerView) findViewById(R.id.device_23_recycler);
        layout_brand_header = findViewById(R.id.layout_brand_header);
        mLinearSearch = (LinearLayout) findViewById(R.id.device_23_linear_search);
        mTextCancel = (TextView) findViewById(R.id.device_23_text_cancel);
        mTextNoResult = (TextView) findViewById(R.id.device_23_text_noResult);
        mEditSearch = (EditText) findViewById(R.id.device_23_edit_search);
        btnDownMatchCode = (Button) findViewById(R.id.btn_match_code_download);
        mSwipe = (SwipeRefreshLayout) findViewById(R.id.model_swipe);
    }

    @Override
    protected void updateSkin() {
        super.updateSkin();
        SkinManager skinManager = MainApplication.getApplication().getSkinManager();
        skinManager.setBackground(layout_brand_header, SkinResouceKey.BITMAP_NAV_BOTTOM_BACKGROUND);
        skinManager.setBackground(btnDownMatchCode, SkinResouceKey.BITMAP_BUTTON_BG_S);
        skinManager.setTextColor(btnDownMatchCode, SkinResouceKey.COLOR_BUTTON_TEXT);
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        mLinearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchViewClick();
            }
        });
        mTextCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTextClick(v);
            }
        });
        btnDownMatchCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatchingModelActivity.start(RemoteControlModelActivity.this, deviceID, brandName, ueiType, singleCode, localName);
            }
        });

        mEditSearch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Drawable drawable = mEditSearch.getCompoundDrawables()[2];
                if (drawable == null) {
                    // don't have end drawable
                    return false;
                }

                // 点击了 输入框中 右边的 x
                if (event.getX() > mEditSearch.getWidth()
                        - mEditSearch.getPaddingRight()
                        - drawable.getIntrinsicWidth()) {
                    mEditSearch.setText("");
                }
                return false;
            }
        });

        mEditSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                }
                return false;
            }
        });

        mEditSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                WLog.i(TAG, "before: " + s + ", count: " + count);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                WLog.i(TAG, "on: " + s + ", count: " + count);
                searchText = s.toString();
                page = 0;
                modelList.clear();
                doGetBrandCodeModel(s.toString(), page + "");
            }

            @Override
            public void afterTextChanged(Editable s) {
                WLog.i(TAG, "after: " + s);

            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        deviceID = getIntent().getStringExtra("deviceID");
        brandName = getIntent().getStringExtra("brandName");
        ueiType = getIntent().getStringExtra("ueiType");
        singleCode = getIntent().getStringExtra("singleCode");
        localName = getIntent().getStringExtra("localName");
        allModelList = new ArrayList<>();
        modelList = new ArrayList<>();
        mUeiUserID = ACEService.ACEncryptUserId(deviceID + System.currentTimeMillis());
        mAdapter = new BandModelListAdapter(deviceID, brandName, ueiType, singleCode, localName);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        loadMoreListener = new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                // 加载更多...
                doGetBrandCodeModel(searchText, (currentPage - 1) + "");
            }
        };
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 0, 3);
            }
        });
        dataApiUnit = new DataApiUnit(this);
        getBrandCodeList();
    }

    /**
     * 获取码库编号
     */
    private void getBrandCodeList() {
        dataApiUnit.doGetBrandCodeList(ueiType, brandName, mUeiUserID, new DataApiUnit.DataApiCommonListener<BrandBean.CodeListBean>() {
            @Override
            public void onSuccess(BrandBean.CodeListBean bean) {
                CodeLibraryBean codeLibraryBean = new CodeLibraryBean();
                codeLibraryBean.codes = bean.codes;
                codeList = new ArrayList<CodeLibraryBean.ModelCodeBean>();
                for (int i = 0; i < codeLibraryBean.codes.size(); i++) {
                    CodeLibraryBean.ModelCodeBean modelCodeBean = new CodeLibraryBean.ModelCodeBean();
                    modelCodeBean.code = bean.codes.get(i);
                    codeList.add(i, modelCodeBean);
                }
                mAdapter.setData(codeList);

            }

            @Override
            public void onFail(int code, String msg) {

            }
        });

    }


    /**
     * 查询某种品牌下的型号
     *
     * @param model
     * @param pageNum
     */
    private void doGetBrandCodeModel(final String model, final String pageNum) {
        dataApiUnit.doGetBrandCodeType(ueiType, mUeiUserID, brandName, model, pageNum,null,null, new DataApiUnit.DataApiCommonListener<CodeLibraryBean>() {
            @Override
            public void onSuccess(CodeLibraryBean bean) {
                mRecyclerView.addOnScrollListener(loadMoreListener);
                page = bean.pageNum;
                page++;
                WLog.i(TAG, "page: " + page);
                if (bean.codes != null && bean.models != null) {
                    for (int i = 0; i < bean.codes.size(); i++) {
                        CodeLibraryBean.ModelCodeBean modelCodeBean = new CodeLibraryBean.ModelCodeBean();
                        modelCodeBean.code = bean.codes.get(i);
                        modelCodeBean.model = bean.models.get(i);
                        modelList.add(i, modelCodeBean);
                    }
                    WLog.i(TAG, "modelListSize: " + modelList.size());
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mTextNoResult.setVisibility(View.INVISIBLE);
                    mAdapter.setData(modelList);
                } else {
                    if (TextUtils.equals(pageNum, "0")) {
                        mTextNoResult.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onFail(int code, String msg) {

            }
        });
    }

    /**
     * 取消TextView点击事件
     */
    private void cancelTextClick(View v) {
        isSearching = false;
        mAdapter.setData(codeList);
        mLinearSearch.setVisibility(View.VISIBLE);
        mEditSearch.setVisibility(View.INVISIBLE);
        mTextCancel.setVisibility(View.INVISIBLE);
        mTextNoResult.setVisibility(View.INVISIBLE);
        mEditSearch.clearFocus();
        // 强制隐藏键盘
        KeyboardUtil.hideKeyboard(RemoteControlModelActivity.this, v);
    }

    /**
     * 点击 searchView
     */
    private void searchViewClick() {
        isSearching = true;

        mLinearSearch.setVisibility(View.INVISIBLE);
        mEditSearch.setVisibility(View.VISIBLE);
        mTextCancel.setVisibility(View.VISIBLE);
        mEditSearch.setFocusable(true);
        mEditSearch.setFocusableInTouchMode(true);
        mEditSearch.requestFocus();
        // 强制打开键盘
        KeyboardUtil.showKeyboard(RemoteControlModelActivity.this);
        mEditSearch.setText("");
    }

}
