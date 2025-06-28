package com.example.weather_app.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weather_app.adapters.VpAdapter
import com.example.weather_app.databinding.FragmentMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONObject
import com.android.volley.Request
import com.example.weather_app.MainViewModel
import com.example.weather_app.adapters.WeatherModel
import com.squareup.picasso.Picasso

const val API_KEY = "b8c98a14f49644988ae92245252006"


class MainFragment : Fragment() {
    // обязательно учитываем порядок добавления фрагментов в адаптер
    private val fragmentList = listOf(HoursFragment.newInstance(), DaysFragment.newInstance())
    private val tabList = listOf("Hours", "Days")
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding

    private val model : MainViewModel by activityViewModels ()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        initPagerAdapter()
        requestWeatherData("Saint Petersburg")
        updateCurrentData()
    }

    private fun initPagerAdapter() = with(binding) {
        val adapter = VpAdapter(activity as FragmentActivity, fragmentList)
        vp.adapter = adapter
        // Подключаем TabLayout к ViewPager
        TabLayoutMediator(tabLayout, vp) { tab, position ->
            tab.text = tabList[position]
        }.attach()
    }


    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermission() {
        if (!isPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    private fun requestWeatherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "&q=" +
                city +
                "&days=7" +
                "&aqi=no&alerts=no"

        val queue = Volley.newRequestQueue(context)

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                parseWeatherData(response)

            },
            { error ->
                Toast.makeText(
                    context,
                    "API error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
        queue.add(request)
    }

    private fun parseWeatherData(response: String) {
        val jsonObject = JSONObject(response)
        val daysList = parseDaysWeatherData(jsonObject)
        parseCurrentWeatherData(jsonObject, daysList[0])

    }


    // Заполнение карточки текущими данными
    private fun parseCurrentWeatherData(jsonObject: JSONObject, weatherItem: WeatherModel) {
        val itemWeather = WeatherModel(
            jsonObject.getJSONObject("location").getString("name"),
            jsonObject.getJSONObject("current").getString("last_updated"),
            jsonObject.getJSONObject("current")
                .getJSONObject("condition").getString("text"),
            jsonObject.getJSONObject("current")
                .getJSONObject("condition").getString("icon"),
            jsonObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            weatherItem.hours

        )
        // Заполнили карточку текущими данными и передаём данные в LiveData
        model.liveDataCurrent.value = itemWeather
        Log.d("MyLog", "Time: ${itemWeather.hours}")

    }


    private fun parseDaysWeatherData(jsonObject: JSONObject): List<WeatherModel> {
        val daysList = mutableListOf<WeatherModel>()
        val daysArray = jsonObject.getJSONObject("forecast").getJSONArray("forecastday")
        val name = jsonObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val dayItem = WeatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                "",
                day.getJSONObject("day").getString("maxtemp_c"),
                day.getJSONObject("day").getString("mintemp_c"),
                day.getJSONArray("hour").toString()

            )
            daysList.add(dayItem)
        }
        model.liveDataList.value = daysList
        return daysList
    }

    private fun updateCurrentData() = with(binding) {
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            Log.d("MyLog", "New data received: $it")
            val maxMinTemp = "${it.maxTemp}°C/${it.minTemp}°C"
            val currentTemp = "${it.currentTemp}°C"
            tvCity.text = it.city
            tvData.text = it.time
            tvCondition.text = it.condition
            tvCurrentTemp.text = currentTemp
            Picasso.get().load("https:" + it.imageUrl).into(imWeather)
            tvMaxMin.text = maxMinTemp

        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            MainFragment()
    }
}

