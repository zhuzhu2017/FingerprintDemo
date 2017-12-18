package com.allen.fingerprintdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.allen.fingerprintlib.callback.VerifyFingerprintCallback;
import com.allen.fingerprintlib.utils.FingerprintUtil;

import java.lang.ref.SoftReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 非对称机密算法实现指纹识别
 * Created by allen on 2017/12/14.
 */

public class AsymmetryActivity extends AppCompatActivity implements VerifyFingerprintCallback {
    @BindView(R.id.btn_begin)
    Button btnBegin;
    @BindView(R.id.tv_result)
    TextView tvResult;

    private SoftReference<AsymmetryActivity> softCtx;
    private FingerprintDialogFragment dialogFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asymmetry);
        ButterKnife.bind(this);
        softCtx = new SoftReference<>(this);
        dialogFragment = new FingerprintDialogFragment();
    }

    @OnClick(R.id.btn_begin)
    public void onViewClicked() {
        //验证是否支持指纹识别
        FingerprintUtil.getInstance().isSupportFingerprint(softCtx.get(), this);
    }

    /**
     * 硬件不支持
     */
    @Override
    public void hardwareUnsupported() {

    }

    /**
     * 支持指纹识别
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void supportFingerprint() {
        /*已经录入指纹，进行指纹识别操作*/
        FingerprintManagerCompat.CryptoObject cryptoObject = FingerprintUtil.getInstance()
                .asymmetryFingerprintVerify(Constants.KEY_NAME);
        dialogFragment.setCryptoObject(cryptoObject);
        dialogFragment.show(getFragmentManager(), Constants.DIALOG_FRAGMENT_TAG);
    }

    /**
     * 没有录入指纹
     */
    @Override
    public void notEnrolledFingerprints() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("提示")
                .setMessage("您还没有录入指纹，点击确定按钮设置")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                }).show();
    }
}
