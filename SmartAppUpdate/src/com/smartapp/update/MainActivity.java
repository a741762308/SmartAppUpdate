package com.smartapp.update;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	// 合成成功
	private static final int SUCCESS = 1;
	// 合成的APK签名和已安装的签名不一致
	private static final int FAIL_SING = -1;
	// 合成失败
	private static final int FAIL_ERROR = -2;
	// 获取源文件失败
	private static final int FAIL_GET_SOURCE = -3;
	public static final String PATH = Environment.getExternalStorageDirectory()
			+ File.separator;
	public static final String NEW_APK_PATH = PATH + "SmartAppUpdate.apk";
	public static final String PATCH_PATH = PATH + "SmartAppUpdate.patch";
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		beforeUpdate();
//		afterUpdate();
	}
	/**
	 * 升级前的界面
	 */
	private void beforeUpdate() {
		setContentView(R.layout.activity_old_main);
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("doing..");
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		findViewById(R.id.update).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new UpdateApkTask().execute();
			}
		});
	}

	/**
	 * 升级后的界面
	 */
	private void afterUpdate() {
		setContentView(R.layout.activity_new_main);
	}

	private class UpdateApkTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected Integer doInBackground(String... params) {
			String oldApkSource = Utils.getSourceApkPath(MainActivity.this,
					getPackageName());
			if (!TextUtils.isEmpty(oldApkSource)) {
				int patchResult = -1;
				try {
					patchResult = BatchUtils.applyPatchToOwn(MainActivity.this,
							NEW_APK_PATH, PATCH_PATH);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (patchResult == 0) {
					String signatureNew = Utils
							.getUnInstalledApkSignature(MainActivity.this,NEW_APK_PATH);
					String signatureSource = Utils.getInstalledApkSignature(
							MainActivity.this, getPackageName());
					if (!TextUtils.isEmpty(signatureNew)
							&& !TextUtils.isEmpty(signatureSource)
							&& signatureNew.equals(signatureSource)) {
						return SUCCESS;
					} else {
						return FAIL_SING;
					}
				} else {
					return FAIL_ERROR;
				}
			} else {
				return FAIL_GET_SOURCE;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			switch (result) {
			case SUCCESS:
				showShortToast("新apk已合成成功：" + NEW_APK_PATH);
				Utils.installApk(MainActivity.this, NEW_APK_PATH);
				break;
			case FAIL_SING:
				showShortToast("新apk已合成失败，签名不一致");
				break;
			case FAIL_ERROR:
				showShortToast("新apk已合成失败");
				break;
			case FAIL_GET_SOURCE:
				showShortToast("无法获取packageName为" + getPackageName()
						+ "的源apk文件，只能整包更新了！");
				break;
			}
		}
	}
	
	private void showShortToast(final String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

}
