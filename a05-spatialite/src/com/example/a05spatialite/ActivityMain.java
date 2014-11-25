package com.example.a05spatialite;

import java.util.Arrays;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

public class ActivityMain
extends ActionBarActivity {

	private AppSpatialite app;
	private TextView tvLabel;
	private ImageView ivTest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		app = (AppSpatialite) getApplication();
		tvLabel = (TextView) findViewById(R.id.tvLabel);
		ivTest = (ImageView) findViewById(R.id.ivTest);

		// baue einen String auf mit einigen Testergebnissen
		// um zu zeigen, daß räumliche Abfragen funktionieren
		StringBuffer s = new StringBuffer()
		.append(app.db.queryVersions())
		.append("\n");
		String[] tabs = new String[] {"osm_points","osm_places","osm_railways"};
		for (String tab : tabs) {
			s.append(tab)
			.append(" extent = ")
			.append(Arrays.toString(app.db.queryExtent(tab)));
		}
		// gib den sql-Probestring aus
		tvLabel.setText(s.toString());

		// baue ein svg-bild auf
		ivTest.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		double[] extent = app.db.queryExtent(DataBaseHelper.TAB_RAILWAYS);
		double width = ivTest.getMeasuredWidth();
		double height = ivTest.getMeasuredHeight();
		double dx = -extent[0];
		double dy = -extent[1];
		double sx = width / (extent[2] - extent[0]);
		double sy = height / (extent[3] - extent[1]);
		String sv = app.db.queryRailwaysSVG(
			width,height,
			dx,dy,
			sx,sy
		);
		try {
			SVG svg = SVG.getFromString(sv);
			Drawable drawable = new PictureDrawable(svg.renderToPicture());
			ivTest.setImageDrawable(drawable);
		} catch(SVGParseException e) {
			e.printStackTrace();
		}

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
}
