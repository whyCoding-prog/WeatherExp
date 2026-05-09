package com.example.weatherexpect.Tool

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherexpect.Data.Response.Data.Location
import com.example.weatherexpect.R

class CityAdapter : ListAdapter<Location, CityAdapter.ViewHolder>(DiffCallback()) {

    private var itemClickListener: ((Location) -> Unit)? = null

    fun setOnItemClickListener(listener: (Location) -> Unit) {
        itemClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cityName: TextView = itemView.findViewById(R.id.tv_city_name)

        fun bind(location: Location) {
            // 根据和风天气API返回的实际字段调整
            val displayText = "${location.name}, ${location.adm2 ?: ""}, ${location.adm1 ?: ""}"
            cityName.text = displayText
            itemView.setOnClickListener { itemClickListener?.invoke(location) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_city, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Location>() {
        override fun areItemsTheSame(oldItem: Location, newItem: Location) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Location, newItem: Location) =
            oldItem == newItem
    }
}