package com.milen.bluetoothapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.data.entities.ConditionNames

class MedicalConditionsRecyclerAdapter(
    conditions: List<ConditionNames>
) :
    RecyclerView.Adapter<MedicalConditionsRecyclerAdapter.MedicalConditionHolder>() {
    private val conditions: List<ConditionNames> = conditions

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MedicalConditionHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.medical_condition_item_layout, parent, false)
        return MedicalConditionHolder(view)
    }

    override fun onBindViewHolder(
        holder: MedicalConditionHolder,
        position: Int
    ) {
        val condition: ConditionNames = conditions[position]
        holder.myExistingConditionTV?.text = condition.name
    }


    override fun getItemCount(): Int {
        return conditions.size
    }

    class MedicalConditionHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val rootView = itemView?.rootView
        val myExistingConditionTV = itemView?.findViewById<TextView>(R.id.myExistingConditionTV)

    }
}