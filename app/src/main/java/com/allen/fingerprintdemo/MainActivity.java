package com.allen.fingerprintdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.allen.fingerprintdemo.togglebutton.ToggleButton;
import com.allen.fingerprintlib.utils.FingerprintUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements ToggleButton.OnToggleChanged {

    @BindView(R.id.btn_symmetry)
    Button btnSymmetry;
    @BindView(R.id.btn_asymmetry)
    Button btnAsymmetry;
    @BindView(R.id.finger_print_button)
    ToggleButton fingerPrintButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        fingerPrintButton.setOnToggleChanged(this);
        boolean showFingerprintLayout = FingerprintUtil.getInstance().getFingerprintState(this);
        if (showFingerprintLayout) {
            btnAsymmetry.setVisibility(View.VISIBLE);
            btnSymmetry.setVisibility(View.VISIBLE);
            fingerPrintButton.toggleOn();
        } else {
            btnAsymmetry.setVisibility(View.GONE);
            btnSymmetry.setVisibility(View.GONE);
            fingerPrintButton.toggleOff();
        }
    }

    @OnClick({R.id.btn_symmetry, R.id.btn_asymmetry})
    public void onViewClicked(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.btn_symmetry:
                intent = new Intent(this, SymmetryActivity.class);
                break;
            case R.id.btn_asymmetry:
                intent = new Intent(this, AsymmetryActivity.class);
                break;
        }
        if (intent != null) startActivity(intent);
    }

    @Override
    public void onToggle(boolean on) {
        FingerprintUtil.getInstance().saveFingerprintState(this, on);
        if (on) {
            boolean supportFingerprint = FingerprintUtil.getInstance().isSupportFingerprint(this);
            if (supportFingerprint) {
                btnSymmetry.setVisibility(View.VISIBLE);
                btnAsymmetry.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "您的手机不支持指纹识别，赶紧换手机吧！", Toast.LENGTH_SHORT).show();
                fingerPrintButton.toggleOff();
                btnSymmetry.setVisibility(View.GONE);
                btnAsymmetry.setVisibility(View.GONE);
            }
        } else {
            btnSymmetry.setVisibility(View.GONE);
            btnAsymmetry.setVisibility(View.GONE);
        }
    }
}
