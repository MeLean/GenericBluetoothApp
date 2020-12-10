package com.milen.bluetoothapp.ui.pager

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.ui.pager.pages.MonitoringPageFragment
import com.milen.bluetoothapp.ui.pager.pages.ParedDevicesPageFragment
import com.milen.bluetoothapp.ui.pager.pages.RemoteControlPageFragment

class MainFragmentStateAdapter(activity: AppCompatActivity,private val pages : Array<Pages>) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return when (pages[position]) {
            Pages.PAGE_MONITORING -> MonitoringPageFragment()
            Pages.PAGE_PARED_DEVICES -> ParedDevicesPageFragment()
            Pages.PAGE_REMOTE_CONTROL -> RemoteControlPageFragment()
        }
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    @StringRes
    fun getStringResIdByPage(page: Pages): Int {
        return when (page) {
            Pages.PAGE_MONITORING -> R.string.page_monitoring
            Pages.PAGE_PARED_DEVICES -> R.string.page_pared_devices
            Pages.PAGE_REMOTE_CONTROL -> R.string.page_remote_control
        }
    }

    enum class Pages {
        PAGE_MONITORING,
        PAGE_PARED_DEVICES,
        PAGE_REMOTE_CONTROL
    }
}