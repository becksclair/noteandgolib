package com.heliasar.noteandgolib.data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

class BitmapDataObject implements Serializable {
	private static final long serialVersionUID = -8719394331164860967L;
	public byte[] imageByteArray;
}

public class Photo implements Serializable {

	private static final long serialVersionUID = 4247846054160218972L;
	public Bitmap image;
	public File fileRef;

	public String getName() {
		String filename = fileRef.getName();
		return filename.substring(0, filename.length() - 4);
	}
	
	public String getDate() {
		// TODO: Return the creation date of the file
		return null;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(fileRef);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		image.compress(Bitmap.CompressFormat.PNG, 60, stream);
		BitmapDataObject bitmapDataObject = new BitmapDataObject();
		bitmapDataObject.imageByteArray = stream.toByteArray();
		
		out.writeObject(bitmapDataObject);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.fileRef = (File) in.readObject();
		BitmapDataObject bitmapDataObject = (BitmapDataObject) in.readObject();
		image = BitmapFactory.decodeByteArray(bitmapDataObject.imageByteArray, 0, bitmapDataObject.imageByteArray.length);
	}
	
}

