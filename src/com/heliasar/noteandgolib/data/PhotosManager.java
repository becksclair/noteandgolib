package com.heliasar.noteandgolib.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Vector;

import com.heliasar.toolkit.BitmapUtils;
import com.heliasar.toolkit.ToolkitUtils;
import com.heliasar.tools.Utils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class PhotosManager {

	public ArrayList<Photo> cache = new ArrayList<Photo>();
	private PhotosAdapter adapter;
	private SherlockFragmentActivity activity;

	public PhotosManager(PhotosAdapter baseAdapter, SherlockFragmentActivity act) {
		adapter = baseAdapter;
		activity = act;
	}

	public void cachePhotos() {
		new PhotosCacher().execute();
	}

	@SuppressWarnings("unchecked")
	public void restoreCache() {
		class RestoreThread implements Runnable {
			SherlockFragmentActivity activity;

			public void run() {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						activity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
					}
				});
				
				Utils.l("Starting to restore cache");
				try {
					File cacheFile = Storage.getCacheFile();

					if (cacheFile.exists() && cacheFile.length() > 1) {
						FileInputStream is = new FileInputStream(cacheFile);
						ObjectInputStream in = new ObjectInputStream(is);
						cache = (ArrayList<Photo>) in.readObject();
						in.close();

						Utils.l("Cache loaded from store");
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								adapter.notifyDataSetChanged();
							}
						});
					} else {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								cachePhotos();
							}
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						activity.setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
					}
				});
				Utils.l("Finished restoring cache");
			}
		}
		RestoreThread thread = new RestoreThread();
		thread.activity = activity;
		new Thread(thread).start();
	}

	private void saveCache() {
		class SaveThread implements Runnable {
			SherlockFragmentActivity activity;

			public void run() {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						activity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
					}
				});
				
				try {
					File cacheFile = Storage.createCacheFile();
					FileOutputStream os = new FileOutputStream(cacheFile);
					ObjectOutputStream out = new ObjectOutputStream(os);
					out.writeObject(cache);
					out.close();

				} catch (Exception e) {
					e.printStackTrace();
					Storage.deleteCacheFile();
				}
				
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						activity.setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
					}
				});
			}
		}
		SaveThread thread = new SaveThread();
		thread.activity = activity;
		new Thread(thread).start();
	}
	
	public void emptyCache() {
		cache.clear();
		adapter.notifyDataSetChanged();
	}

	public void deletePhoto(int position) {
		class DeleteThread implements Runnable {
			int position;
			SherlockFragmentActivity activity;

			public void run() {
				Photo photo = cache.get(position);
				if (photo.fileRef.delete()) {
					cache.remove(position);
					saveCache();
					
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							adapter.notifyDataSetChanged();
						}
					});
				}
			}
		}

		DeleteThread thread = new DeleteThread();
		thread.position = position;
		thread.activity = activity;
		new Thread(thread).start();
	}

	public boolean renamePhoto(int position, String newName) {
		Photo photo = cache.get(position);
		if (photo.fileRef.renameTo(new File(Storage.PHOTOS_STORAGE_LOCATION, newName + ".jpg"))) {

			photo.fileRef = new File(Storage.PHOTOS_STORAGE_LOCATION, newName + ".jpg");
			saveCache();
			adapter.notifyDataSetChanged();
			return true;
		} else {
			return false;
		}
	}

	private class PhotosCacher extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			final int IMAGE_MAX_WIDTH = (int) Utils.PixelToDp(230f);
			final int IMAGE_MAX_HEIGHT = (int) Utils.PixelToDp(140f);

			File storageDir = Storage.getPhotosStorageLocation();
			File[] containedFiles = storageDir.listFiles();

			BitmapFactory.Options decOptions = new Options();
			BitmapFactory.Options options = new Options();
			Bitmap bitmap;

			cache.removeAll(cache);

			if (containedFiles != null) {
				for (File file : containedFiles) {
					if (!file.isFile()) continue;

					int orientation = ExifInterface.ORIENTATION_NORMAL;
					int exifOrientation = 0;
					
					try {
						ExifInterface exif = new ExifInterface(file.getAbsolutePath());
						exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
						orientation = ToolkitUtils.orientationToDegrees(exifOrientation);
						
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					
					decOptions.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(file.getAbsolutePath(), decOptions);

					final int scaleFactor = Math.max(decOptions.outWidth / IMAGE_MAX_WIDTH,
							decOptions.outHeight / IMAGE_MAX_HEIGHT);

					options.inSampleSize = scaleFactor;
					bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
					if (bitmap == null) continue;
					
					Photo photo = new Photo();
					Matrix m = new Matrix();
					m.postRotate(orientation);
					
					bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
					
					Vector<Integer> dim = BitmapUtils.getScaleDimensions(
							bitmap.getWidth(), bitmap.getHeight(), (int) (IMAGE_MAX_WIDTH * 1.5), (int) (IMAGE_MAX_HEIGHT * 1.5));
					
					bitmap = Bitmap.createScaledBitmap(bitmap, dim.get(0), dim.get(1), false);
					photo.image = BitmapUtils.cropToCenter(bitmap, IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT);
					bitmap.recycle();

					photo.fileRef = file;
					cache.add(photo);
					publishProgress();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			adapter.notifyDataSetChanged();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
			Utils.l("Start loading photos");
		}

		@Override
		protected void onPostExecute(Void result) {
			saveCache();
			Utils.l("Finished Loading photos");
			super.onPostExecute(result);
		}

	}

}
