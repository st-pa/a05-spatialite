package com.example.a05spatialite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jsqlite.Database;
import jsqlite.Stmt;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class DataBaseHelper
extends SQLiteOpenHelper {

	// The Android's default system path of your application database.
	private static String DB_PATH = "/data/data/com.example.a05spatialite/databases/";
	private static String DB_NAME = "geodb.sqlite";

	private Database db;
	private final Context context;

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 * 
	 * @param context
	 */
	public DataBaseHelper(Context context) {
		super(context, DB_NAME, null, 1);
		this.context = context;
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	public void createDataBase() throws IOException {
		AppSpatialite.toast(context,"createDB");
		boolean dbExist = checkDataBase();
		if (dbExist) {
			// do nothing - database already exist
		} else {
			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are gonna be able to overwrite that
			// database with our database.
			this.getReadableDatabase();
			try {
				copyDataBase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {
		AppSpatialite.toast(context,"checkDB");
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			// database does't exist yet.
		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 */
	private void copyDataBase() throws IOException {
		AppSpatialite.toast(context,"copyDB");
		// Open your local db as the input stream
		InputStream myInput = context.getAssets().open(DB_NAME);
		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;
		// Open the empty db as the output stream
		System.out.println(Environment.getDataDirectory());
		
		OutputStream myOutput = new FileOutputStream(outFileName);
		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	public void openDataBase() throws Exception {
		AppSpatialite.toast(context,"openDB");
		// Open the database
		String myPath = DB_PATH + DB_NAME;
//		db = Database.openDatabase(myPath, null,SQLiteDatabase.OPEN_READONLY);
		File spatialDbFile = new File(myPath);
		db = new jsqlite.Database();
		db.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
				| jsqlite.Constants.SQLITE_OPEN_CREATE);
	}

	@Override
	public synchronized void close() {
		AppSpatialite.toast(context,"closeDB");
		if (db != null)
			try {
				db.close();
			} catch (jsqlite.Exception e) {
				e.printStackTrace();
			}
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	// Add your public helper methods to access and get content from the database.
	// You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
	// to you to create adapters for your views.

	public String queryVersions() {
		StringBuilder sb = new StringBuilder();
		sb.append("Check versions...\n");
		try {
			Stmt stmt01 = db.prepare("SELECT spatialite_version();");
			if (stmt01.step()) {
				sb.append("\t").append("SPATIALITE_VERSION: " + stmt01.column_string(0));
				sb.append("\n");
			}
	
			stmt01 = db.prepare("SELECT proj4_version();");
			if (stmt01.step()) {
				sb.append("\t").append("PROJ4_VERSION: " + stmt01.column_string(0));
				sb.append("\n");
			}
	
			stmt01 = db.prepare("SELECT geos_version();");
			if (stmt01.step()) {
				sb.append("\t").append("GEOS_VERSION: " + stmt01.column_string(0));
				sb.append("\n");
			}
			stmt01.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sb.append("Done...\n");
		return sb.toString();
	}
}