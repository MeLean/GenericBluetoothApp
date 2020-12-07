package com.milen.bluetoothapp.ui.pager

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.milen.bluetoothapp.ui.pager.pages.ParedDevicesPageFragment
import com.milen.bluetoothapp.ui.pager.pages.RemoteControlPageFragment
import com.milen.bluetoothapp.ui.pager.pages.SettingsPageFragment

class MainFragmentStateAdapter(activity: AppCompatActivity,private val pages : Array<Pages>) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return when(pages[position]){
            Pages.PAGE_SETTINGS -> SettingsPageFragment()
            Pages.PAGE_PARED_DEVICES -> ParedDevicesPageFragment()
            Pages.PAGE_REMOTE_CONTROL -> RemoteControlPageFragment()
        }
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    enum class Pages{
        PAGE_SETTINGS,
        PAGE_PARED_DEVICES,
        PAGE_REMOTE_CONTROL
    }
}