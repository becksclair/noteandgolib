package com.heliasar.noteandgolib.data;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import com.heliasar.tools.Utils;
import android.media.ExifInterface;
import android.os.Environment;

public class Storage {
	public static final String PHOTO_MAKER = "NoteAndGo";

	public static final String DEFAULT_PHOTO_NAME = "temp.jpg";
	public static final String PHOTOCACHE_NAME = "cache.dat";
	public static final String PHOTOS_STORAGE_LOCATION = Environment.getExternalStorageDirectory().toString() + "/data/noteandgo/photos";
	public static final String PHOTO_FILE_ABSOLUTE_PATH = PHOTOS_STORAGE_LOCATION + "/" + DEFAULT_PHOTO_NAME;
	public static final String PHOTOCACHE_FILE_ABSOLUTE_PATH = PHOTOS_STORAGE_LOCATION + "/" + PHOTOCACHE_NAME;

	public static final int CAMERA_PIC_REQUEST = 1337;

	public static void createPhotosStorage() {
		File newStorage = new File(PHOTOS_STORAGE_LOCATION);
		File noIndexFile = new File(PHOTOS_STORAGE_LOCATION + "/.nomedia");

		if (!newStorage.isDirectory()) {
			newStorage.mkdirs();
		}

		if (!noIndexFile.isFile()) {
			try {
				noIndexFile.createNewFile();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static File getPhotosStorageLocation() {
		return new File(PHOTOS_STORAGE_LOCATION);
	}

	public static File createPhotoFile() {
		File newFile = new File(PHOTO_FILE_ABSOLUTE_PATH);

		try {
			if (!newFile.isFile()) {
				newFile.createNewFile();
				newFile.setReadable(true, false);
			} else {
				newFile.delete();
				newFile.createNewFile();
				newFile.setReadable(true, false);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newFile;
	}

	public static File createCacheFile() {
		File newFile = new File(PHOTOCACHE_FILE_ABSOLUTE_PATH);

		try {
			if (!newFile.isFile()) {
				newFile.createNewFile();
			} else {
				newFile.delete();
				newFile.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newFile;
	}
	
	public static boolean deleteCacheFile() {
		File newFile = new File(PHOTOCACHE_FILE_ABSOLUTE_PATH);

		if (!newFile.isFile()) {
			return true;
		}
		return newFile.delete();
	}

	public static File getCacheFile() {
		return new File(PHOTOCACHE_FILE_ABSOLUTE_PATH);
	}

	public static void renameFile(String newTitle) {

		class RenameThread implements Runnable {
			String newTitle;

			public void run() {
				File newFile = new File(PHOTO_FILE_ABSOLUTE_PATH);

				try {
					ExifInterface exi = new ExifInterface(
							PHOTO_FILE_ABSOLUTE_PATH);
					exi.setAttribute(ExifInterface.TAG_MAKE, PHOTO_MAKER);
					exi.saveAttributes();

					Utils.l(newTitle);

					newFile.renameTo(new File(PHOTOS_STORAGE_LOCATION, newTitle
							+ ".jpg"));

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		RenameThread thread = new RenameThread();

		if (newTitle != "") {
			thread.newTitle = newTitle;
		} else {
			Calendar cal = Calendar.getInstance();

			int day = cal.get(Calendar.DAY_OF_MONTH);
			int month = cal.get(Calendar.MONTH);
			int year = cal.get(Calendar.YEAR);
			int hr = cal.get(Calendar.HOUR_OF_DAY);
			int min = cal.get(Calendar.MINUTE);
			int sec = cal.get(Calendar.SECOND);

			thread.newTitle = new String(String.valueOf(day)
					+ String.valueOf(month) + String.valueOf(year) + "-"
					+ String.valueOf(hr) + String.valueOf(min)
					+ String.valueOf(sec));
		}
		thread.run();
	}
}
