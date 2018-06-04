package com.megvii.livenessproject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.megvii.licensemanager.Manager;
import com.megvii.livenessdetection.Detector;
import com.megvii.livenessdetection.LivenessLicenseManager;
import com.megvii.livenesslib.LivenessActivity;
import com.megvii.livenesslib.util.ConUtil;
import com.megvii.livenesslib.util.SharedUtil;

import java.util.Locale;

import static android.os.Build.VERSION_CODES.M;

public class LoadingActivity extends Activity implements View.OnClickListener {

    private String uuid;
    private LinearLayout barLinear;
    private Button btn;
    private TextView WarrantyText;
    private ProgressBar WarrantyBar;
    private Button againWarrantyBtn;
    private SharedUtil mSharedUtil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);

        init();
        netWorkWarranty();
    }

    private void init() {
        mSharedUtil = new SharedUtil(this);
        uuid = ConUtil.getUUIDString(this);
        barLinear = (LinearLayout) findViewById(R.id.loading_layout_barLinear);
        WarrantyText = (TextView) findViewById(R.id.loading_layout_WarrantyText);
        WarrantyBar = (ProgressBar) findViewById(R.id.loading_layout_WarrantyBar);
        againWarrantyBtn = (Button) findViewById(R.id.loading_layout_againWarrantyBtn);
        againWarrantyBtn.setOnClickListener(this);
        btn = (Button) findViewById(R.id.loading_layout_livenessBtn);
        btn.setOnClickListener(this);
        TextView versionNameView = ((TextView) findViewById(R.id.loading_layout_version));
        versionNameView.setText(Detector.getVersion());
    }

    /**
     * 联网授权
     */
    private void netWorkWarranty() {
        btn.setVisibility(View.GONE);
        barLinear.setVisibility(View.VISIBLE);
        againWarrantyBtn.setVisibility(View.GONE);
        WarrantyText.setText(R.string.meglive_auth_progress);
        WarrantyBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Manager manager = new Manager(LoadingActivity.this);
                LivenessLicenseManager licenseManager = new LivenessLicenseManager(LoadingActivity.this);
                manager.registerLicenseManager(licenseManager);
                manager.takeLicenseFromNetwork(uuid);
                if (licenseManager.checkCachedLicense() > 0) {
                    //授权成功
                    mHandler.sendEmptyMessage(1);
                }else {
                    //授权失败
                    mHandler.sendEmptyMessage(2);
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String language_save = mSharedUtil.getStringValueByKey("language");
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (!language.equals(language_save))
            showLanguage(language);
    }

    protected void showLanguage(String language) {
        // 设置应用语言类
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        if (language.equals("zh")) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            config.locale = Locale.ENGLISH;
        }
        resources.updateConfiguration(config, dm);
        mSharedUtil.saveStringValue("language", language);
        freshView();
    }

    private void freshView() {
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.loading_layout_livenessBtn) {
            requestCameraPerm();
        } else if (id == R.id.loading_layout_againWarrantyBtn) {
            netWorkWarranty();
        }
    }

    private void requestCameraPerm() {
        if (android.os.Build.VERSION.SDK_INT >= M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                //进行权限请求
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        EXTERNAL_STORAGE_REQ_CAMERA_CODE);
            } else {
                enterNextPage();
            }
        } else {
            enterNextPage();
        }
    }

    private void enterNextPage() {
        startActivityForResult(new Intent(this, LivenessActivity.class), PAGE_INTO_LIVENESS);
    }

    public static final int EXTERNAL_STORAGE_REQ_CAMERA_CODE = 10;


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == EXTERNAL_STORAGE_REQ_CAMERA_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {// Permission Granted

                ConUtil.showToast(this, "获取相机权限失败");
            } else
                enterNextPage();
        }
    }


    private static final int PAGE_INTO_LIVENESS = 100;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAGE_INTO_LIVENESS && resultCode == RESULT_OK) {
//            String result = data.getStringExtra("result");
//            String delta = data.getStringExtra("delta");
//            Serializable images=data.getSerializableExtra("images");
            Bundle bundle=data.getExtras();
            ResultActivity.startActivity(this, bundle);
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    btn.setVisibility(View.VISIBLE);
                    barLinear.setVisibility(View.GONE);
                    break;
                case 2:
                    againWarrantyBtn.setVisibility(View.VISIBLE);
                    WarrantyText.setText(R.string.meglive_auth_failed);
                    WarrantyBar.setVisibility(View.GONE);
                    break;
            }
        }
    };
}