package com.medreminder.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medreminder.app.data.Medication
import com.medreminder.app.databinding.ItemMedicationBinding

class MedicationAdapter(
    private val items: MutableList<Medication>,
    private val onMarkTaken: (Medication) -> Unit,
    private val onDelete: (Medication) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.VH>() {

    inner class VH(val binding: ItemMedicationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMedicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val med = items[position]
        holder.binding.textName.text = med.name
        holder.binding.textDosageNote.text = med.dosageNote
        holder.binding.textTimes.text = "زمان‌ها: ${med.timesCsv}"
        holder.binding.textStock.text = "موجودی: ${med.quantityRemaining} عدد" +
            if (med.quantityRemaining <= med.lowStockThreshold) "  ⚠️ موجودی کم" else ""

        holder.binding.btnMarkTaken.setOnClickListener { onMarkTaken(med) }
        holder.binding.btnDelete.setOnClickListener { onDelete(med) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Medication>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
