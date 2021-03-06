package com.milen.bluetoothapp.base.ui.pager.pages

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.milen.bluetoothapp.utils.shouldShow
import com.milen.bluetoothapp.view_models.MainViewModel
import kotlinx.android.synthetic.main.item_bluetooth_not_enabled.view.*

abstract class BasePageFragment : Fragment() {
    protected val viewModel : MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.getBluetoothAvailability().observe(viewLifecycleOwner, {
            val shouldShowNoBlueTooth = it == false
            view.no_bluetooth_layout?.shouldShow(shouldShowNoBlueTooth)
        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if(menuVisible){
            val shouldShowNoBlueTooth = !(viewModel.getBluetoothAdapter()?.isEnabled == true)
            view?.no_bluetooth_layout?.shouldShow(shouldShowNoBlueTooth)
        }
    }
}