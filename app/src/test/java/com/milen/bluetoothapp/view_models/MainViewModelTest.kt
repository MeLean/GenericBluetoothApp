package com.milen.bluetoothapp.view_models

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.milen.bluetoothapp.data.sharedPreferences.ApplicationSharedPreferences
import com.milen.bluetoothapp.services.MyBluetoothService
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class MainViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var tested: MainViewModel
    @Mock
    private lateinit var sharedPreferences: ApplicationSharedPreferences
    @Mock
    private lateinit var bluetoothService: MyBluetoothService

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        tested = MainViewModel(sharedPreferences, bluetoothService)
    }

    @Test
    fun shouldSetScrollToPageToMonitoring() {
        whenViewModelScrollToPage(MainFragmentStateAdapter.Page.PAGE_MONITORING)
        thenScrollToPageIs(MainFragmentStateAdapter.Page.PAGE_MONITORING)
    }

    @Test
    fun shouldSetScrollToPageToRemoteControl() {
        whenViewModelScrollToPage(MainFragmentStateAdapter.Page.PAGE_REMOTE_CONTROL)
        thenScrollToPageIs(MainFragmentStateAdapter.Page.PAGE_REMOTE_CONTROL)
    }

    @Test
    fun shouldEnableBluetoothAvailability() {
        whenViewModelSetBluetoothAvailability(true)
        thenBluetoothAvailabilityIs(true)
    }

    @Test
    fun shouldDisableBluetoothAvailability() {
        whenViewModelSetBluetoothAvailability(false)
        thenBluetoothAvailabilityIs(false)
    }

    @Test
    fun shouldStartBluetoothServiceWhenEnableBluetooth() {
        whenViewModelSetBluetoothAvailability(true)
        thenBluetoothServiceStart()
    }

    @Test
    fun shouldStopBluetoothServiceWhenDisableBluetooth() {
        whenViewModelSetBluetoothAvailability(true)
        thenBluetoothServiceStop()
    }

    private fun thenScrollToPageIs(
        expectedPage: MainFragmentStateAdapter.Page
    ) {
        val actualPage = tested.getShouldScrollToPage().value
        assertThat(actualPage).isSameAs(expectedPage)
    }

    private fun whenViewModelScrollToPage(page: MainFragmentStateAdapter.Page) {
        tested.setShouldScrollToPage(page)
    }

    private fun whenViewModelSetBluetoothAvailability(isEnabled: Boolean) {
        tested.setBluetoothAvailability(isEnabled)
    }

    private fun thenBluetoothAvailabilityIs(isEnabled: Boolean) {
        val actualResult = tested.getBluetoothAvailability().value
        assertThat(actualResult).isSameAs(isEnabled)
    }

    private fun thenBluetoothServiceStart() {
        verify(bluetoothService).startService()
    }

    private fun thenBluetoothServiceStop() {
        verify(bluetoothService).stopService()
    }

}