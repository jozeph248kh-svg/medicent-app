package com.medreminder.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medreminder.app.data.DailyTask
import com.medreminder.app.databinding.ItemTaskBinding

class TaskAdapter(
    private val items: MutableList<DailyTask>,
    private val onToggleActive: (DailyTask, Boolean) -> Unit,
    private val onDelete: (DailyTask) -> Unit
) : RecyclerView.Adapter<TaskAdapter.VH>() {

    inner class VH(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = items[position]
        holder.binding.textTitle.text = task.title
        holder.binding.textTime.text = "ساعت: ${task.time}"
        holder.binding.switchActive.setOnCheckedChangeListener(null)
        holder.binding.switchActive.isChecked = task.active
        holder.binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
            onToggleActive(task, isChecked)
        }
        holder.binding.btnDelete.setOnClickListener { onDelete(task) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<DailyTask>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
