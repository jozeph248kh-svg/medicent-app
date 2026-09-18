package com.medreminder.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medreminder.app.data.Appointment
import com.medreminder.app.databinding.ItemAppointmentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppointmentAdapter(
    private val items: MutableList<Appointment>,
    private val onDelete: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.VH>() {

    inner class VH(val binding: ItemAppointmentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val appt = items[position]
        val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())
        holder.binding.textTitle.text = appt.title
        holder.binding.textDoctor.text = "دکتر: ${appt.doctorName}"
        holder.binding.textDateTime.text = "زمان: ${sdf.format(Date(appt.dateTimeMillis))}"
        holder.binding.textNotes.text = appt.notes
        holder.binding.btnDelete.setOnClickListener { onDelete(appt) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Appointment>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
