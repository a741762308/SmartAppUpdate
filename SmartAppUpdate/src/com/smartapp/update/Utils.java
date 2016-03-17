package com.smartapp.update;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

public class Utils {
	/**
	 * 获取已安装Apk文件的源Apk文件 如：/data/app/com.smartapp.update.SmartAppUpdate.apk
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static String getSourceApkPath(Context context, String packageName) {
		if (TextUtils.isEmpty(packageName))
			return null;
		try {
			ApplicationInfo appInfo = context.getPackageManager()
					.getApplicationInfo(packageName, 0);
			return appInfo.sourceDir;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 安装Apk
	 * 
	 * @param context
	 * @param apkPath
	 */
	public static void installApk(Context context, String apkPath) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + apkPath),
				"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	/**
	 * 获取未安装Apk的签名
	 * 
	 * @param apkPath
	 * @return
	 */
	public static String getUnInstalledApkSignature(Context context,
			String apkPath) {
		String PATH_PackageParser = "android.content.pm.PackageParser";
		try {
			/*
			 * Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
			 * Class<?>[] typeArgs = new Class[1]; typeArgs[0] = String.class;
			 * Constructor<?> pkgParserCt =
			 * pkgParserCls.getConstructor(typeArgs); Object[] valueArgs = new
			 * Object[1]; valueArgs[0] = apkPath; Object pkgParser =
			 * pkgParserCt.newInstance(valueArgs);
			 * 
			 * DisplayMetrics metrics = new DisplayMetrics();
			 * metrics.setToDefaults();
			 * 
			 * typeArgs = new Class[4]; typeArgs[0] = File.class; typeArgs[1] =
			 * String.class; typeArgs[2] = DisplayMetrics.class; typeArgs[3] =
			 * Integer.TYPE;
			 * 
			 * Method pkgParser_parsePackageMtd =
			 * pkgParserCls.getDeclaredMethod( "parsePackage", typeArgs);
			 * valueArgs = new Object[4]; valueArgs[0] = new File(apkPath);
			 * valueArgs[1] = apkPath; valueArgs[2] = metrics; valueArgs[3] =
			 * PackageManager.GET_SIGNATURES; Object pkgParserPkg =
			 * pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
			 * 
			 * typeArgs = new Class[2]; typeArgs[0] = pkgParserPkg.getClass();
			 * typeArgs[1] = Integer.TYPE;
			 * 
			 * Method pkgParser_collectCertificatesMtd = pkgParserCls
			 * .getDeclaredMethod("collectCertificates", typeArgs); valueArgs =
			 * new Object[2]; valueArgs[0] = pkgParserPkg; valueArgs[1] =
			 * PackageManager.GET_SIGNATURES;
			 * pkgParser_collectCertificatesMtd.invoke(pkgParser, valueArgs);
			 * 
			 * Field packageInfoFld = pkgParserPkg.getClass().getDeclaredField(
			 * "mSignatures"); Signature[] info = (Signature[])
			 * packageInfoFld.get(pkgParserPkg); return info[0].toCharsString();
			 */

			Class clazz = Class.forName(PATH_PackageParser);
			Object packageParser = getParserObject(clazz);

			Object packag = getPackage(context, clazz, packageParser, apkPath);

			Method collectCertificatesMethod = clazz.getMethod(
					"collectCertificates",
					Class.forName("android.content.pm.PackageParser$Package"),
					int.class);
			collectCertificatesMethod.invoke(packageParser, packag,
					PackageManager.GET_SIGNATURES);
			Signature mSignatures[] = (Signature[]) packag.getClass()
					.getField("mSignatures").get(packag);

			System.out.println("size:" + mSignatures.length);

			Signature apkSignature = mSignatures.length > 0 ? mSignatures[0]
					: null;
			return apkSignature.toCharsString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static Object getParserObject(Class clazz)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException {
		return Build.VERSION.SDK_INT >= 21 ? clazz.getConstructor()
				.newInstance() : clazz.getConstructor(String.class)
				.newInstance("");
	}

	private static Object getPackage(Context c, Class clazz, Object instance,
			String path) throws Exception {
		Object pkg = null;
		if (Build.VERSION.SDK_INT >= 21) {
			Method method = clazz.getMethod("parsePackage", File.class,
					int.class);
			pkg = method.invoke(instance, new File(path), 0x0004);
		} else {
			Method method = clazz.getMethod("parsePackage", File.class,
					String.class, DisplayMetrics.class, int.class);
			pkg = method.invoke(instance, new File(path), null, c
					.getResources().getDisplayMetrics(), 0x0004);
		}

		return pkg;
	}

	/**
	 * 获取已安装apk签名
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static String getInstalledApkSignature(Context context,
			String packageName) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> apps = pm
				.getInstalledPackages(PackageManager.GET_SIGNATURES);

		Iterator<PackageInfo> iter = apps.iterator();
		while (iter.hasNext()) {
			PackageInfo packageinfo = iter.next();
			String thisName = packageinfo.packageName;
			if (thisName.equals(packageName)) {
				return packageinfo.signatures[0].toCharsString();
			}
		}

		return null;
	}
}
