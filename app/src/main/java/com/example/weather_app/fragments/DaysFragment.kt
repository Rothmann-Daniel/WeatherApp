package com.example.weather_app.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather_app.MainViewModel
import com.example.weather_app.R
import com.example.weather_app.adapters.WeatherAdapter
import com.example.weather_app.adapters.WeatherModel
import com.example.weather_app.databinding.FragmentDaysBinding

class DaysFragment : Fragment(), WeatherAdapter.OnItemClickListener {

private lateinit var binding: FragmentDaysBinding
private lateinit var adapter: WeatherAdapter
private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDaysBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcViewDays()
        model.liveDataList.observe(viewLifecycleOwner) {
            adapter.submitList(it.subList(1, it.size))
        }
    }

    private fun initRcViewDays() = with(binding) {
        // 1. Создаём адаптер
        adapter = WeatherAdapter(this@DaysFragment)
        // 2. Настраиваем расположение элементов (вертикальный список)
        rcViewDays.layoutManager = LinearLayoutManager(activity)
        // 3. Привязываем адаптер к RecyclerView
        rcViewDays.adapter = adapter
    }

    companion object {

        @JvmStatic
        fun newInstance() = DaysFragment()
    }

    override fun onItemClick(item: WeatherModel) {
      model.liveDataCurrent.value = item
    }
}