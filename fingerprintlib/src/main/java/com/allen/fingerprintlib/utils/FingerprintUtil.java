package com.allen.fingerprintlib.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.allen.fingerprintlib.FingerprintConstants;
import com.allen.fingerprintlib.callback.VerifyFingerprintCallback;

import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * 指纹识别工具类
 * ①检验指纹识别是否可用
 * ②
 * Created by allen on 2017/12/13.
 */

public class FingerprintUtil {
    /*单例对象*/
    private static FingerprintUtil INSTANCE;
    /*指纹验证管理器*/
    private FingerprintManagerCompat compat;

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
     * 保存指纹开启关闭状态
     *
     * @param context 上下文
     * @param opened  是否开启指纹验证
     */
    public void saveFingerprintState(Context context, boolean opened) {
        if (context == null || !isSupportFingerprint(context)) return;
        SharedPreferences sp = context.getSharedPreferences(FingerprintConstants.SP_FINGERPRINT, Context.MODE_PRIVATE);
        sp.edit().putBoolean(FingerprintConstants.SP_STATE, opened).apply();
    }

    /**
     * 获取指纹识别开启关闭状态
     *
     * @param context 上下文
     * @return 是否打开指纹识别
     */
    public boolean getFingerprintState(Context context) {
        if (context == null) return false;
        SharedPreferences sp = context.getSharedPreferences(FingerprintConstants.SP_FINGERPRINT, Context.MODE_PRIVATE);
        return sp.getBoolean(FingerprintConstants.SP_STATE, false);
    }

    /**
     * 获取FingerprintManagerCompat对象
     *
     * @param context 上下文
     * @return FingerprintManagerCompat对象
     */
    public FingerprintManagerCompat getFingerprintManagerCompat(Context context) {
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
        if (context == null) return;
        boolean supportFingerprint = isSupportFingerprint(context);
        if (supportFingerprint) { //硬件支持指纹识别
            //是否录入指纹
            boolean hasEnrolledFingerprints = compat.hasEnrolledFingerprints();
            if (hasEnrolledFingerprints) {    //密码锁和指纹都已经录入
                if (callback != null) callback.supportFingerprint();
            } else {   //只设置了密码锁，没有录入指纹（目前不可能出现设置了指纹却没有设置密码锁的情况）
                if (callback != null) callback.notEnrolledFingerprints();
            }
        } else {
            if (callback != null) callback.hardwareUnsupported();
        }
    }

    /**
     * 只校验是否硬件支持指纹识别
     *
     * @param context 上下文
     * @return 是否支持指纹识别
     */
    public boolean isSupportFingerprint(Context context) {
        //API版本低于23默认不支持指纹登录
        if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false;
        compat = getFingerprintManagerCompat(context);
        //如果硬件不支持，就不用进行下面的判断了
        return compat.isHardwareDetected();
    }

    /**
     * 是否设置密码锁
     *
     * @param context 上下文
     * @return 是否设置密码锁
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isKeyguardSecure(Context context) {
        if (context == null) return false;
        //验证是否设置密码锁
        KeyguardManager keyguardManager = getKeyguardManager(context);
        return keyguardManager != null && keyguardManager.isKeyguardSecure();
    }

    /**
     * 生成Key
     *
     * @param keyName key名称
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createKey(String keyName) {
        try {
            //创建KeyGenerator对象
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // 设置需要用户验证
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 对称算法实现指纹识别
     *
     * @param keyName key
     * @return CryptoObject对象，供开启指纹传感器使用
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public FingerprintManagerCompat.CryptoObject symmetryFingerprintVerify(String keyName) {
        //根据key名称生成key文件
        createKey(keyName);
        FingerprintManagerCompat.CryptoObject cryptoObject = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(keyName, null);
            Cipher mCipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            mCipher.init(Cipher.ENCRYPT_MODE, key);
            cryptoObject = new FingerprintManagerCompat.CryptoObject(mCipher);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cryptoObject;
    }

    /**
     * 获取KeyPairGenerator对象
     *
     * @return KeyPairGenerator对象
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private KeyPairGenerator getKeyPairGenerator() {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generator;
    }

    /**
     * 非对称算法实现指纹识别
     *
     * @param keyName key
     * @return CryptoObject对象，供开启指纹传感器使用
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public FingerprintManagerCompat.CryptoObject asymmetryFingerprintVerify(String keyName) {
        //创建 KeyPairGenerator 对象
        KeyPairGenerator keyPairGenerator = getKeyPairGenerator();
        FingerprintManagerCompat.CryptoObject cryptoObject = null;
        try {
            //生成密钥对
            keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_SIGN)
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                    .setUserAuthenticationRequired(true)
                    .build());
            keyPairGenerator.generateKeyPair();
            //使用私钥签名产生Cipher
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.load(null);
            PrivateKey key = (PrivateKey) keyStore.getKey(keyName, null);
            Signature mSignature = Signature.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            mSignature.initSign(key);
            cryptoObject = new FingerprintManagerCompat.CryptoObject(mSignature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cryptoObject;
    }

}
