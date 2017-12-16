package com.allen.fingerprintlib.callback;

import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * 验证是否支持指纹验证接口
 * Created by allen on 2017/12/13.
 */

public interface VerifyFingerprintCallback {
    /**
     * 硬件不支持
     */
    void hardwareUnsupported();

    /**
     * 支持指纹验证
     */
    void supportFingerprint();

    /**
     * 没有录入指纹
     */
    void notEnrolledFingerprints();

}
