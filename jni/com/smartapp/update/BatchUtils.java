package com.smartapp.update;

public class BatchUtils {
	private native static int patchApk(String oldApkPath, String newApkPath,
			String patchPath);
}
