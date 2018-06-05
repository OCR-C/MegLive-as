package com.example.livenessproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.megvii.livenesslib.LivenessActivity;
import com.megvii.livenesslib.util.ConUtil;
import com.megvii.livenessproject.R;
import com.megvii.licensemanager.Manager;
import com.megvii.livenessdetection.Detector;
import com.megvii.livenessdetection.LivenessLicenseManager;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by binghezhouke on 14-7-25.
 */
public class LoadingActivity extends Activity implements View.OnClickListener {

	private String uuid;
	private LinearLayout barLinear;
	private Button btn;
	private TextView WarrantyText;
	private ProgressBar WarrantyBar;
	private Button againWarrantyBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading_layout);

		init();
		netWorkWarranty();
	}

	private void init() {
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
		WarrantyText.setText("正在联网授权中...");
		WarrantyBar.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Manager manager = new Manager(LoadingActivity.this);
				LivenessLicenseManager licenseManager = new LivenessLicenseManager(LoadingActivity.this);
				manager.registerLicenseManager(licenseManager);

				manager.takeLicenseFromNetwork(uuid);
				if (licenseManager.checkCachedLicense() > 0)
					mHandler.sendEmptyMessage(1);
				else
					mHandler.sendEmptyMessage(2);
			}
		}).start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.loading_layout_livenessBtn) {
			startActivityForResult(new Intent(this, LivenessActivity.class), PAGE_INTO_LIVENESS);
		} else if (id == R.id.loading_layout_againWarrantyBtn) {
			netWorkWarranty();
		}
	}

	private static final int PAGE_INTO_LIVENESS = 100;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PAGE_INTO_LIVENESS && resultCode == RESULT_OK) {
			String result = data.getStringExtra("result");
			ResultActivity.startActivity(this, result);
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
				WarrantyText.setText("联网授权失败！请检查网络或找服务商");
				WarrantyBar.setVisibility(View.GONE);
				break;
			}
		}
	};
}