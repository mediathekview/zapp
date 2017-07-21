package de.christinecoenen.code.zapp.mediathek.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MediathekFragment extends Fragment {

	public MediathekFragment() {
		// Required empty public constructor
	}

	public static MediathekFragment getInstance() {
		return new MediathekFragment();
	}

	public void navigateTo(Fragment fragment, String tag) {
		getChildFragmentManager()
			.beginTransaction()
			.replace(R.id.container, fragment)
			.addToBackStack(tag)
			.commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mediathek, container, false);
		ButterKnife.bind(this, view);

		if (savedInstanceState == null) {
			getChildFragmentManager()
				.beginTransaction()
				.add(R.id.container, MediathekListFragment.getInstance())
				.commit();
		}

		return view;
	}

}
