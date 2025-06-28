package com.example.weather_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather_app.R
import com.example.weather_app.databinding.ListItemBinding
import com.squareup.picasso.Picasso

class WeatherAdapter :ListAdapter<WeatherModel, WeatherAdapter.WeatherViewHolder>(WeatherComparator()) {
    // 1. Шаблон для хранения ссылок на View (хранит ссылки на элемент разметки)
    class WeatherViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListItemBinding.bind(view)
        // 2. Заполнение элемента списка данными из модели
        fun bind(item: WeatherModel) = with(binding) {
            val maxMinTemp = "${item.maxTemp}°C/${item.minTemp}°C"
            val currentTemp = "${item.currentTemp}°C"
            tvDate.text = item.time
            tvCond.text = item.condition
            tvTemp.text = currentTemp
            Picasso.get().load("https:" + item.imageUrl).into(imStatus)

        }
    }

    // Сравнение элементов
    class WeatherComparator : DiffUtil.ItemCallback<WeatherModel>() {
        override fun areItemsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem // сравнение по id/уникальному элементу/ключу
        }
        override fun areContentsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem // сравнение по содержимому/данным
        }
    }

    // 3. RecyclerView вызывает метод для создания шаблона: Создание view == количество элементов в списке данных WeatherModel
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val  view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return WeatherViewHolder(view) // Создаём ViewHolder с разметкой
    }
    // 4. RecyclerView вызывает этот метод для заполнения данными
    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.bind(getItem(position)) // Передаём данные в bind()
    }
}