package com.example.weather_app.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather_app.MainViewModel
import com.example.weather_app.adapters.WeatherAdapter
import com.example.weather_app.adapters.WeatherModel
import com.example.weather_app.databinding.FragmentHourBinding
import org.json.JSONArray


class HoursFragment : Fragment() {
    private lateinit var binding: FragmentHourBinding
    private lateinit var adapter: WeatherAdapter
    private val model: MainViewModel by activityViewModels()


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
        model.liveDataCurrent.observe(viewLifecycleOwner) {
           Log.d("MyLog", "New data received: ${it.hours}")
            adapter.submitList(getHoursList(it))
        }
    }

    private fun initRcView() = with(binding) {
        // 1. Настраиваем расположение элементов (вертикальный список)
        rcView.layoutManager = LinearLayoutManager(activity)
        // 2. Создаём адаптер
        adapter = WeatherAdapter()
        // 3. Привязываем адаптер к RecyclerView
        rcView.adapter = adapter
        // 4. Передаём данные в адаптер

    }

    private fun getHoursList(item: WeatherModel): List<WeatherModel> {
        val hoursArray = JSONArray(item.hours)
        val list = mutableListOf<WeatherModel>()
        for (i in 0 until hoursArray.length()) {
            val hourItam = WeatherModel(
                item.city,
                hoursArray.getJSONObject(i).getString("time"),
                hoursArray.getJSONObject(i).getJSONObject("condition").getString("text"),
                hoursArray.getJSONObject(i).getJSONObject("condition").getString("icon"),
                hoursArray.getJSONObject(i).getString("temp_c"),
                item.maxTemp,
                item.minTemp,
                ""
            )
            list.add(hourItam)
        }
        return list
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            HoursFragment()
    }
}
