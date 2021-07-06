package de.christinecoenen.code.zapp

import org.junit.After
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest


abstract class AutoCloseKoinTest : KoinTest {

	@After
	fun tearDown() {
		stopKoin()
	}

}
