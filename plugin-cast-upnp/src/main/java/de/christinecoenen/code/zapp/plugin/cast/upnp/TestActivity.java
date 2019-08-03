package de.christinecoenen.code.zapp.plugin.cast.upnp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import de.christinecoenen.code.zapp.base.Constants;

public class TestActivity extends AppCompatActivity {

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		Toast.makeText(this, getIntent().getStringExtra(Constants.EXTRA_CAST_URL), Toast.LENGTH_SHORT).show();
	}
}
