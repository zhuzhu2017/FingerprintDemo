package com.allen.fingerprintlib.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.allen.fingerprintlib.callback.VerifyFingerprintCallback;

/**
 * 指纹识别工具类
 * Created by allen on 2017/12/13.
 */

public class FingerprintUtil {
    /*单例对象*/
    private static FingerprintUtil INSTANCE;

    /*私有构造器*/
    private FingerprintUtil() {
    }

    /*获取单例对象*/
    public static FingerprintUtil getInstance() {
        if (INSTANCE == null) {
            synchronized (FingerprintUtil.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FingerprintUtil();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 获取FingerprintManagerCompat对象
     *
     * @param context 上下文
     * @return FingerprintManagerCompat对象
     */
    private FingerprintManagerCompat getFingerprintManagerCompat(Context context) {
        if (context == null) return null;
        return FingerprintManagerCompat.from(context);
    }

    /**
     * 获取KeyguardManager对象
     *
     * @param context 上下文
     * @return KeyguardManager对象
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private KeyguardManager getKeyguardManager(Context context) {
        if (context == null) return null;
        return context.getSystemService(KeyguardManager.class);
    }

    /**
     * 验证是否支持指纹登录（默认API小于23都不支持）
     *
     * @param context  上下文
     * @param callback 验证结果回调
     */
    public void isSupportFingerprint(Context context, VerifyFingerprintCallback callback) {
        //默认API小于23的都不支持指纹登录
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManagerCompat compat = getFingerprintManagerCompat(context);
            //如果compat为空或者硬件不支持，就不用进行下面的判断了
            if (compat == null || !compat.isHardwareDetected()) {
                if (callback != null) callback.hardwareUnsupported();
                return;
            }
            KeyguardManager keyguardManager = getKeyguardManager(context);
            //是否设置手机锁
            boolean keyguardSecure = keyguardManager.isKeyguardSecure();
            //是否录入指纹
            boolean hasEnrolledFingerprints = compat.hasEnrolledFingerprints();
            if (keyguardSecure && hasEnrolledFingerprints) {    //手机锁和指纹都已经录入
                if (callback != null) callback.supportFingerprint();
            } else {   //只设置了手机锁，没有录入指纹（目前不可能出现设置了指纹却没有设置指纹锁的情况）
                if (callback != null) callback.notEnrolledFingerprints();
            }
        }
    }

}
