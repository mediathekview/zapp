package de.christinecoenen.code.zapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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
