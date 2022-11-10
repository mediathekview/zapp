package de.christinecoenen.code.zapp.app.personal

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.personal.adapter.DownloadListAdapter
import de.christinecoenen.code.zapp.app.personal.adapter.HeaderAdapater
import de.christinecoenen.code.zapp.databinding.PersonalFragmentBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PersonalFragment : Fragment(), MenuProvider {

	private var _binding: PersonalFragmentBinding? = null
	private val binding: PersonalFragmentBinding get() = _binding!!

	private val viewModel: PersonalViewModel by viewModel()

	private lateinit var outerAdapter: ConcatAdapter
	private lateinit var downloadsAdapter: DownloadListAdapter


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		downloadsAdapter = DownloadListAdapter(lifecycleScope)

		outerAdapter = ConcatAdapter(
			HeaderAdapater(R.string.activity_main_tab_downloads),
			downloadsAdapter
		)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = PersonalFragmentBinding.inflate(inflater, container, false)

		binding.list.adapter = outerAdapter

		lifecycleScope.launch {
			viewModel.downloadsFlow.collect {
				downloadsAdapter.setShows(it)
			}
		}

		requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.activity_main_toolbar, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false
}
