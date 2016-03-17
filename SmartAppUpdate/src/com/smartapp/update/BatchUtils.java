package com.smartapp.update;

import java.io.IOException;

import android.content.Context;

public class BatchUtils {
	static {
		System.loadLibrary("SmartAppUpdate");
	}

	private native static int patchApk(String oldApkPath, String newApkPath,
			String patchPath);

	public static int applyPatchToOwn(Context context, String newApkPath,
			String patchPath) throws IOException {
		String old = context.getApplicationInfo().sourceDir;
		return patchApk(old, newApkPath, patchPath);
	}
}
