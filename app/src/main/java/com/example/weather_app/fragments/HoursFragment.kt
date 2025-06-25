package com.example.weather_app.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather_app.R
import com.example.weather_app.adapters.WeatherAdapter
import com.example.weather_app.adapters.WeatherModel
import com.example.weather_app.databinding.FragmentHourBinding

class HoursFragment : Fragment() {
    private lateinit var binding: FragmentHourBinding
    private lateinit var adapter: WeatherAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHourBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
    }

    private fun initRcView() = with(binding) {
        // 1. Настраиваем расположение элементов (вертикальный список)
        rcView.layoutManager = LinearLayoutManager(activity)
        // 2. Создаём адаптер
        adapter = WeatherAdapter()
        // 3. Привязываем адаптер к RecyclerView
        rcView.adapter = adapter
        // 4. Передаём данные в адаптер
        val list = listOf(
            WeatherModel("SPB", "22:00", "SUNNY","","25°C","","",""),
            WeatherModel("SPB", "23:00", "SUNNY","","20°C","","",""),
            WeatherModel("SPB", "00:00", "SUNNY","","19°C","","",""),
            )
        adapter.submitList(list)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            HoursFragment()
    }
}
