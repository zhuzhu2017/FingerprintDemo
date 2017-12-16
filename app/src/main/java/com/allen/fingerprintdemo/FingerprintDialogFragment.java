package com.allen.fingerprintdemo;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.allen.fingerprintlib.utils.FingerprintUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 指纹识别弹窗
 * Created by allen on 2017/12/16.
 */

public class FingerprintDialogFragment extends DialogFragment {

    @BindView(R.id.fingerprint_icon)
    ImageView fingerprintIcon;
    @BindView(R.id.fingerprint_status)
    TextView fingerprintStatus;
    @BindView(R.id.fingerprint_container)
    LinearLayout fingerprintContainer;
    @BindView(R.id.tv_change_login)
    TextView tvChangeLogin;
    @BindView(R.id.tv_cancel)
    TextView tvCancel;
    @BindView(R.id.ll_buttonPanel)
    LinearLayout llButtonPanel;
    Unbinder unbinder;

    private Activity ctx;
    private FingerprintManagerCompat.CryptoObject cryptoObject;
    private int enrollType;
    private FingerprintUIHelper uiHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ctx == null) {
            ctx = getActivity();
        }
        //配置FragmentDialog
        setCancelable(false);   //设置点击返回不可取消
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(Constants.FINGERPRINT_DIALOG_TITLE);
        //添加视图
        View view = inflater.inflate(R.layout.dialog_fingerprint, container, false);
        unbinder = ButterKnife.bind(this, view);
        uiHelper = new FingerprintUIHelper.Builder()
                .withFingerManager(FingerprintUtil.getInstance().getFingerprintManagerCompat(ctx))
                .withIconImage(fingerprintIcon)
                .withStatusText(fingerprintStatus)
                .withChangeLoginText(tvChangeLogin)
                .withEnrollType(enrollType)
                .build();
        return view;
    }

    /**
     * onResume中开启指纹传感器识别
     */
    @Override
    public void onResume() {
        super.onResume();
        uiHelper.startListening(cryptoObject);
    }

    /**
     * onPause中停止指纹传感器识别
     */
    @Override
    public void onPause() {
        super.onPause();
        uiHelper.stopListening();
    }

    /**
     * 设置CryptoObject对象
     *
     * @param cryptoObject FingerprintManagerCompat.CryptoObject对象
     */
    public void setCryptoObject(FingerprintManagerCompat.CryptoObject cryptoObject) {
        this.cryptoObject = cryptoObject;
    }

    /**
     * 设置添加指纹后续处理动作（如果不是需要与后台交互的可以不设置）
     *
     * @param enrollType 处理类型，1表示绑定，2表示登录
     */
    public void setEnrollType(int enrollType) {
        this.enrollType = enrollType;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.tv_change_login, R.id.tv_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_change_login:
                Intent intent = new Intent(ctx, LoginActivity.class);
                startActivity(intent);
                dismiss();
                ctx.finish();
                break;
            case R.id.tv_cancel:
                dismiss();
                break;
        }
    }
}
