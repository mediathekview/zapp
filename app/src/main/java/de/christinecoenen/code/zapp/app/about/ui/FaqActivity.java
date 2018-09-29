package de.christinecoenen.code.zapp.app.about.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import de.christinecoenen.code.zapp.R;

public class FaqActivity extends AppCompatActivity {

	public static Intent getStartIntent(Context context) {
		return new Intent(context, FaqActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_faq);
	}
}
