package com.example.a05spatialite;

import java.util.Arrays;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ActivityMain
extends ActionBarActivity {

	private AppSpatialite app;
	private TextView tvLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		app = (AppSpatialite) getApplication();
		tvLabel = (TextView) findViewById(R.id.tvLabel);

		// baue einen String auf mit einigen Testergebnissen
		// um zu zeigen, daﬂ r‰umliche Abfragen funktionieren
		StringBuffer s = new StringBuffer()
		.append(app.db.queryVersions())
		.append("\n");
		String[] tabs = new String[] {"osm_points","osm_places","osm_railways"};
		for (String tab : tabs) {
			s.append(tab)
			.append(" extent = ")
			.append(Arrays.toString(app.db.queryExtent(tab)));
		}
		// gib den Probestring aus
		tvLabel.setText(s.toString());
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
