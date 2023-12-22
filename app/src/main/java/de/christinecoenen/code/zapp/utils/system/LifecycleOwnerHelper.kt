package de.christinecoenen.code.zapp.utils.system

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object LifecycleOwnerHelper {

	fun LifecycleOwner.launchOnCreated(
		block: suspend CoroutineScope.() -> Unit
	): Job {
		return launchOnLifecycle(Lifecycle.State.CREATED, block)
	}

	fun LifecycleOwner.launchOnResumed(
		block: suspend CoroutineScope.() -> Unit
	): Job {
		return launchOnLifecycle(Lifecycle.State.RESUMED, block)
	}

	@Suppress("MemberVisibilityCanBePrivate")
	fun LifecycleOwner.launchOnLifecycle(
		state: Lifecycle.State,
		block: suspend CoroutineScope.() -> Unit
	): Job {
		return this.lifecycleScope.launch {
			this@launchOnLifecycle.repeatOnLifecycle(state) {
				block()
			}
		}
	}

}
