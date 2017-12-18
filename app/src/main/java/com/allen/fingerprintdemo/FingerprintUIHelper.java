package com.allen.fingerprintdemo;

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 继承指纹识别回调处理UI显示
 * Created by allen on 2017/12/15.
 */

public class FingerprintUIHelper extends FingerprintManagerCompat.AuthenticationCallback {

    private static final int STATUS_TEXT_CHANGE_DELAY = 1000;
    private static final int SUCCECC_CALLBACK_DELAY = 1000;

    private FingerprintManagerCompat mCompat;   //指纹管理器
    private CancellationSignal cancellationSignal;
    private ImageView mImageIcon;   //指纹图标
    private TextView mStatusText;  //指纹识别状态信息
    private TextView mChangeLoginText;  //切换登录方式
    private int enrollType; //操作类型

    /**
     * Create a FingerprintUIHelper object
     */
    public static class Builder {

        private FingerprintManagerCompat mCompat;
        private ImageView mImageIcon;
        private TextView mStatusText;
        private TextView mChangeLoginText;
        private int enrollType;

        public Builder withFingerManager(FingerprintManagerCompat mCompat) {
            this.mCompat = mCompat;
            return this;
        }

        public Builder withIconImage(ImageView imageView) {
            this.mImageIcon = imageView;
            return this;
        }

        public Builder withStatusText(TextView textView) {
            this.mStatusText = textView;
            return this;
        }

        public Builder withChangeLoginText(TextView textView) {
            this.mChangeLoginText = textView;
            return this;
        }

        public Builder withEnrollType(int enrollType) {
            this.enrollType = enrollType;
            return this;
        }

        public FingerprintUIHelper build() {
            return new FingerprintUIHelper(this);
        }

    }

    private FingerprintUIHelper(Builder builder) {
        this.mCompat = builder.mCompat;
        this.mImageIcon = builder.mImageIcon;
        this.mStatusText = builder.mStatusText;
        this.mChangeLoginText = builder.mChangeLoginText;
        this.enrollType = builder.enrollType;
        cancellationSignal = new CancellationSignal();
    }

    /**
     * 开启指纹识别监听
     *
     * @param cryptoObject CryptoObject对象
     */
    public void startListening(FingerprintManagerCompat.CryptoObject cryptoObject) {
        mCompat.authenticate(cryptoObject, 0, cancellationSignal, this, null);
    }

    /**
     * 关闭指纹识别监听
     */
    public void stopListening() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        super.onAuthenticationError(errMsgId, errString);
        showVerifyStatusUI(errString);
        if (callback != null) callback.onError();
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        showVerifyStatusUI("验证失败，请再试一次！");
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        super.onAuthenticationHelp(helpMsgId, helpString);
        showVerifyStatusUI(helpString);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        //设置指纹识别成功样式
        mStatusText.removeCallbacks(runnable);
        mImageIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mStatusText.setText(R.string.fingerprint_verify_success);
        mStatusText.setTextColor(mStatusText.getResources().getColor(R.color.success_color));
        //延迟回调
        mImageIcon.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (callback != null) callback.onSuccess();
            }
        }, SUCCECC_CALLBACK_DELAY);
    }

    /**
     * 设置验证指纹UI显示
     *
     * @param text 显示的文案
     */
    private void showVerifyStatusUI(CharSequence text) {
        if (enrollType == 2) { //验证指纹用来登录,显示切换登录方式按钮
            mChangeLoginText.setVisibility(View.VISIBLE);
        } else {
            mChangeLoginText.setVisibility(View.GONE);
        }
        mImageIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mStatusText.setText(text);
        //设置字体颜色
        mStatusText.setTextColor(mStatusText.getResources().getColor(R.color.warning_color));
        mStatusText.removeCallbacks(runnable);
        mStatusText.postDelayed(runnable, STATUS_TEXT_CHANGE_DELAY);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //延时1.5s后还原之前的样子
            mImageIcon.setImageResource(R.mipmap.finger_print_icon);
            mStatusText.setText("指纹识别");
            mStatusText.setTextColor(mStatusText.getResources().getColor(R.color.gray));
        }
    };

    /**
     * 设置验证指纹结果回调
     *
     * @param callback 验证指纹结果回调
     */
    public void setIVerifyCallback(IVerifyCallback callback) {
        this.callback = callback;
    }

    private IVerifyCallback callback;

    public interface IVerifyCallback {
        void onSuccess();

        void onError();
    }

}
