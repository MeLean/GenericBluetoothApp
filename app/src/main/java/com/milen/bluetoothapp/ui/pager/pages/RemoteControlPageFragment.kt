package com.milen.bluetoothapp.ui.pager.pages

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.base.ui.BasePageFragment
import com.milen.bluetoothapp.utils.EMPTY_STRING
import kotlinx.android.synthetic.main.fragment_remote_control_page.*
import kotlinx.android.synthetic.main.fragment_remote_control_page.view.*

class RemoteControlPageFragment : BasePageFragment(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_remote_control_page, container, false)

        initViews(view)

        initOnChangeListeners(view)

        initOnClickListeners(view)

        return view
    }


    private fun initViews(view: View) {
        viewModel.getLastCommand().observe(viewLifecycleOwner,
            { command ->
                view.remote_sent_edit_text.setText(command)
            }
        )

        viewModel.getCustomCommandsAutoCompleteSet().observe(viewLifecycleOwner,
            { strSet ->
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1, strSet.toTypedArray()
                )
                view.remote_custom_edit_text?.setAdapter(adapter)
                if(strSet.isNotEmpty()){
                    view.remote_custom_edit_text.setText(strSet.last())
                }

            }
        )

        viewModel.getUpValue().observe(viewLifecycleOwner,
            { value -> remote_up_edit_text.setText(value) }
        )

        viewModel.getDownValue().observe(viewLifecycleOwner,
            { value -> remote_down_edit_text.setText(value) }
        )

        viewModel.getLeftValue().observe(viewLifecycleOwner,
            { value -> remote_left_edit_text.setText(value) }
        )

        viewModel.getRightValue().observe(viewLifecycleOwner,
            { value -> remote_right_edit_text.setText(value) }
        )
    }

    private fun initOnChangeListeners(view: View) {
        view.remote_custom_edit_text.addTextChangedListener { editable: Editable? ->
            editable.let {
                viewModel.addCustomCommand(it.toString())
            }
        }

        view.remote_up_edit_text.addTextChangedListener { editable: Editable? ->
            editable.let {
                viewModel.setUpValue(it.toString())
            }
        }

        view.remote_down_edit_text.addTextChangedListener { editable: Editable? ->
            editable.let {
                viewModel.setDownValue(it.toString())
            }
        }

        view.remote_left_edit_text.addTextChangedListener { editable: Editable? ->
            editable.let {
                viewModel.setLeftValue(it.toString())
            }
        }

        view.remote_right_edit_text.addTextChangedListener { editable: Editable? ->
            editable.let {
                viewModel.setRightValue(it.toString())
            }
        }
    }

    private fun initOnClickListeners(view: View) {
        view.btn_send_custom.setOnClickListener(this)
        view.btn_up.setOnClickListener(this)
        view.btn_down.setOnClickListener(this)
        view.btn_left.setOnClickListener(this)
        view.btn_right.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_send_custom ->
                viewModel.sentCommand(getStringFromEditText(remote_custom_edit_text))
            R.id.btn_up ->
                viewModel.sentCommand(getStringFromEditText(remote_up_edit_text))
            R.id.btn_down ->
                viewModel.sentCommand(getStringFromEditText(remote_down_edit_text))
            R.id.btn_left ->
                viewModel.sentCommand(getStringFromEditText(remote_left_edit_text))
            R.id.btn_right ->
                viewModel.sentCommand(getStringFromEditText(remote_right_edit_text))
        }
    }

    private fun getStringFromEditText(editText: EditText?) =
        editText?.text?.toString() ?: EMPTY_STRING
}

