package com.milen.bluetoothapp.ui.pager

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.ui.pager.pages.MonitoringPageFragment
import com.milen.bluetoothapp.ui.pager.pages.ParedDevicesPageFragment
import com.milen.bluetoothapp.ui.pager.pages.RemoteControlPageFragment

class MainFragmentStateAdapter(activity: AppCompatActivity,private val pages : Array<Page>) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return when (pages[position]) {
            Page.PAGE_MONITORING -> MonitoringPageFragment()
            Page.PAGE_PARED_DEVICES -> ParedDevicesPageFragment()
            Page.PAGE_REMOTE_CONTROL -> RemoteControlPageFragment()
        }
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    @StringRes
    fun getStringResIdByPage(page: Page): Int {
        return when (page) {
            Page.PAGE_MONITORING -> R.string.page_monitoring
            Page.PAGE_PARED_DEVICES -> R.string.page_pared_devices
            Page.PAGE_REMOTE_CONTROL -> R.string.page_remote_control
        }
    }

    enum class Page {
        PAGE_MONITORING,
        PAGE_PARED_DEVICES,
        PAGE_REMOTE_CONTROL
    }
}