package com.example.weather_app.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.squareup.picasso.Picasso
import android.provider.Settings
import com.example.weather_app.DialogManager


const val API_KEY = "b8c98a14f49644988ae92245252006"


class MainFragment : Fragment() {

    private lateinit var fLocationClient: FusedLocationProviderClient

    // обязательно учитываем порядок добавления фрагментов в адаптер
    private val fragmentList = listOf(HoursFragment.newInstance(), DaysFragment.newInstance())
    private val tabList = listOf("Hours", "Days")

    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding

    private val model: MainViewModel by activityViewModels()

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
        updateCurrentData()
    }

    override fun onResume() {
        super.onResume()
        getLocation()
    }


    private fun initPagerAdapter() = with(binding) {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = VpAdapter(activity as FragmentActivity, fragmentList)
        vp.adapter = adapter
        // Подключаем TabLayout к ViewPager
        TabLayoutMediator(tabLayout, vp) { tab, position ->
            tab.text = tabList[position]
        }.attach()
        ibSync.setOnClickListener {
            tabLayout.selectTab(tabLayout.getTabAt(0))//переключаемся на первую вкладку
            getLocation()
        }
        ibSearch.setOnClickListener {
            DialogManager.searchByNameDialog(requireContext(), object : DialogManager.Listener {
                override fun onClick(cityName: String) {
                    requestWeatherData(cityName)
                }
            })
        }
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
                day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
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
            val maxTempFormatted = "${it.maxTemp}°C"
            val minTempFormatted = "${it.minTemp}°C"
            val maxMinTemp = "$maxTempFormatted / $minTempFormatted"
            val currentTempFormatted =
                if (it.currentTemp.isNotEmpty()) "${it.currentTemp}°C" else ""
            tvCity.text = it.city
            tvData.text = it.time
            tvCondition.text = it.condition
            tvCurrentTemp.text = currentTempFormatted.ifEmpty {
                "$maxTempFormatted / $minTempFormatted"
            }
            Picasso.get().load("https:" + it.imageUrl).into(imWeather)
            tvMaxMin.text = if (it.currentTemp.isEmpty()) {
                ""
            } else {
                maxMinTemp
            }
        }
    }

    private fun getLocation() {
        // Проверяем включен ли GPS
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled =  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled) {
            // Показываем диалог для включения GPS
            showGpsDisabledAlert()
            requestWeatherData("Saint Petersburg") // Используем город по умолчанию
            return
        }

        // Проверяем разрешения
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Получаем местоположение
        val ct = CancellationTokenSource()
        fLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener {
                it.result?.let { location ->
                    requestWeatherData("${location.latitude},${location.longitude}")
                } ?: run {
                    requestWeatherData("Saint Petersburg") // Если местоположение не получено
                }
            }
    }

    private fun showGpsDisabledAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle("GPS отключен")
            .setMessage("Для определения вашего местоположения необходимо включить GPS. Включить сейчас?")
            .setPositiveButton("Да") { _, _ ->
                // Открываем настройки местоположения
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.dismiss()
                requestWeatherData("Saint Petersburg")
            }
            .setCancelable(false)
            .show()
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            MainFragment()
    }
}

