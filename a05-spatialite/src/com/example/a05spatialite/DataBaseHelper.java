package com.example.a05spatialite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Vector;

import jsqlite.Database;
import jsqlite.Stmt;
import jsqlite.TableResult;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DataBaseHelper
extends SQLiteOpenHelper {

	protected static final String DB_PATH = "/data/data/com.example.a05spatialite/databases/";
	protected static final String DB_NAME = "geodb.sqlite";
	protected static final String TAB_RAILWAYS = "osm_railways";
	protected static final String TAB_POINTS = "osm_points";
	protected static final String TAB_PLACES = "osm_places";

	private Database db;
	private final Context context;

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
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

	/**
	 * gibt den Extent der Geometrie der gegebenen Tabelle zurück
	 * als array von vier doubles in der Form {x0,y0,x1,y1} oder
	 * <code>null</code> falls Fehler, wobei p0(x0|y0) die linke
	 * obere und p1(x1|y1) die rechte untere ecke ist.
	 * @param tab {@link String}
	 * @return double[]
	 */
	public double[] queryExtent(String tab) {
		double[] r = {};
		StringBuffer query = new StringBuffer()
		.append("SELECT\n")
		.append("\tX(PointN(ExteriorRing(Extent(\"geometry\")),1)),\n")
		.append("\tY(PointN(ExteriorRing(Extent(\"geometry\")),1)),\n")
		.append("\tX(PointN(ExteriorRing(Extent(\"geometry\")),3)),\n")
		.append("\tY(PointN(ExteriorRing(Extent(\"geometry\")),3))\n")
		.append("FROM \"")
		.append(tab)
		.append("\"");
		try {
			Stmt stmt = db.prepare(query.toString());
			if (stmt.step()) {
				r = new double[] {
					stmt.column_double(0),
					stmt.column_double(1),
					stmt.column_double(2),
					stmt.column_double(3)
				};
			}
			Log.v("DB",tab + "'s extent = " + Arrays.toString(r));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * @param width breite der ausgabegraphik
	 * @param height höhe der ausgabegraphik
	 * @param dx x-verschiebung, sollte minus x-wert extent-ecke sein
	 * @param dy y-verschiebung, sollte minus y-wert extent-ecke sein
	 * @param sx x-skalierung, sollte ausgabebreite geteilt durch extentbreite sein
	 * @param sy y-skalierung, sollte ausgabehöhe geteilt durch extenthöhe sein
	 * @return
	 */
	public String queryRailwaysSVG(
		double width, double height,
		double dx, double dy,
		double sx, double sy
	) {
		// sql-abfrage aufbauen
		StringBuffer query = new StringBuffer()
		.append("SELECT AsSVG(\n")
		.append("\tScaleCoordinates(\n")
		.append("\t\tShiftCoordinates(\n")
		.append("\t\t\t\"geometry\",\n")
		.append("\t\t\t")
		.append(Double.toString(dx))
		.append(",")
		.append(Double.toString(dy))
		.append("\n\t\t),\n\t\t")
		.append(Double.toString(sx))
		.append(",")
		.append(Double.toString(sy))
		.append("\n\t)\n")
		.append(")\nFROM \"")
		.append(TAB_RAILWAYS)
		.append("\" ")
		.append("WHERE length(\"name\") > 0")
		;
		// svg-header aufbauen
		StringBuffer svg = new StringBuffer()
		.append("<?xml version=\"1.0\" standalone=\"no\" ?>")
		.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n")
		.append("<svg width=\"")
		.append(Double.toString(width))
		.append("\" height=\"")
		.append(Double.toString(height))
		.append("\" xmlns=\"http://www.w3.org/2000/svg\" ")
		.append("xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
		// sql-abfrage starten
		Log.v("DB","query railways geometry: " + query.toString());
		try {
			TableResult tr = db.get_table(query.toString());
			// schleife durch alle gelieferten geometrieen
			if (tr != null && tr.nrows > 0) {
				Vector<String[]> rows = tr.rows;
				for (String[] row : rows) {
					/* <polyline fill="lightgray" stroke="red" stroke-width="5px" points="..."/> */
					svg
					.append("<path d=\"")
					.append(row[0])
					.append("\" fill=\"none\" stroke=\"black\" />\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// svg-footer abschließen
		svg.append("</svg>");
		Log.v("DB","done with query railways geometry");
		return svg.toString();
	}
}