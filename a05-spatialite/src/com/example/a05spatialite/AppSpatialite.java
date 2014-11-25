package com.example.a05spatialite;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class AppSpatialite
extends Application {

	protected DataBaseHelper db;

	@Override
	public void onCreate() {
		toast(this,"App.onCreate");
		super.onCreate();
		db = new DataBaseHelper(this);
		try {
			db.createDataBase();
		} catch (IOException ioe) {
			throw new Error("Unable to create database");
		}
		try {
			db.openDataBase();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTerminate() {
		toast(this,"App.onTerminate");
		db.close();
	}

	/**
	 * Zeigt einen kurzen Toast.
	 * @param context
	 * @param text
	 */
	protected static void toast(Context context,String text) {
		Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
	}

	public void textToFile(String text, String target) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(
				new OutputStreamWriter( 
					openFileOutput(target,Context.MODE_PRIVATE)
				)
			);
			bw.write(text); 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
