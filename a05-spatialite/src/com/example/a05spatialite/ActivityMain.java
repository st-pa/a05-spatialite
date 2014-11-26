package com.example.a05spatialite;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

public class ActivityMain
extends ActionBarActivity implements OnClickListener {

	private static final String SVG_TARGET_PATH = "svg.svg";

	private AppSpatialite app;
	private TextView tvLabel;
	private ImageView ivTest;
	private Button btTest;

	private int height;
	private int width;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		app = (AppSpatialite) getApplication();
		context = (Context) this;
		tvLabel = (TextView) findViewById(R.id.tvLabel);
		ivTest = (ImageView) findViewById(R.id.ivTest);
		btTest = (Button) findViewById(R.id.btTest);
		btTest.setOnClickListener(this);
		ViewTreeObserver vto = ivTest.getViewTreeObserver();
		vto.addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				clickedBtTest();
				ivTest.getViewTreeObserver().removeOnPreDrawListener(this);
				height = ivTest.getHeight();
				width = ivTest.getWidth();
				Toast.makeText(
						context,
						String.format("width:%d, height:%d",width,height),
						Toast.LENGTH_LONG
				).show();
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v == btTest) {
			clickedBtTest();
		}
	}

	private void clickedBtTest() {
		// baue ein svg-bild auf
		ivTest.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		double[] extent = app.db.queryExtent(DataBaseHelper.TAB_RAILWAYS);
		double width = 256;
		double height = 256;
//		double width = ivTest.getWidth();
//		double height = ivTest.getHeight();
		Toast.makeText(this,String.format("width:%d, height:%d",ivTest.getWidth(),ivTest.getHeight()),Toast.LENGTH_LONG).show();
		double dx = -extent[0];
		double dy = -extent[1];
		double sx = width / (extent[2] - extent[0]);
		double sy = height / (extent[1] - extent[3]);
		String sv = app.db.queryRailwaysSVG(
			width,height, // gewünschte ausgabegröße
			dx,dy, // geometrieen verschieben
			sx,sy // geometrieen skalieren
		);

		// write to file
		Log.v("Main","export file " + SVG_TARGET_PATH);
		app.textToFile(sv,SVG_TARGET_PATH);

		// render to drawable
		Log.v("Main","render SVG");
		try {
			SVG svg = SVG.getFromString(sv);
			Drawable drawable = new PictureDrawable(svg.renderToPicture());
			ivTest.setImageDrawable(drawable);
			Log.v("Main","all done rendering");
			Toast.makeText(this,String.format("width:%d, height:%d",ivTest.getWidth(),ivTest.getHeight()),Toast.LENGTH_LONG).show();
		} catch(SVGParseException e) {
			e.printStackTrace();
			Toast.makeText(this,"Fehler im try-Block",Toast.LENGTH_LONG).show();
		}
	}
}
