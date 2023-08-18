package de.christinecoenen.code.zapp.app.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.SearchFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment(), MenuProvider {

	private var _binding: SearchFragmentBinding? = null
	private val binding: SearchFragmentBinding get() = _binding!!

	private val viewModel: SearchViewModel by viewModel()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = SearchFragmentBinding.inflate(inflater, container, false)

		requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.search_fragment, menu)
	}

	override fun onPrepareMenu(menu: Menu) {
		val searchMenu = menu.findItem(R.id.menu_search)
		val searchView = searchMenu.actionView as SearchView

		searchView.setIconifiedByDefault(false)
		searchMenu.expandActionView()

		searchMenu.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
			override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
				return true
			}

			override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
				findNavController().popBackStack()
				return true
			}
		})
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		return false
	}

}
