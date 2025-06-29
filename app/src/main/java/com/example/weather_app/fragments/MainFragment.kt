package com.example.weather_app.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weather_app.DialogManager
import com.example.weather_app.MainViewModel
import com.example.weather_app.R
import com.example.weather_app.adapters.WeatherModel
import com.example.weather_app.adapters.VpAdapter
import com.example.weather_app.databinding.FragmentMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject


private const val API_KEY = "b8c98a14f49644988ae92245252006"
private const val DEFAULT_CITY = "Saint Petersburg"

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val model: MainViewModel by activityViewModels()

    private val fragmentList = listOf(HoursFragment.newInstance(), DaysFragment.newInstance())
    private lateinit var tabTitles: List<String>

    // Permission launchers
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        handlePermissionResult(isGranted)
    }

    private val locationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { checkLocationAndFetch() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabTitles = listOf(getString(R.string.hours), getString(R.string.days))
        initLocationClient()
        setupUI()
        checkLocationAndFetch()
    }

    override fun onResume() {
        super.onResume()
        if (this::binding.isInitialized && hasLocationPermission()) {
            checkLocationAndFetch()
        }
    }

    private fun initLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun setupUI() {
        setupViewPager()
        setupSyncButton()
        setupSearchButton()
        setupWeatherObserver()
    }

    private fun setupViewPager() {
        binding.vp.adapter = VpAdapter(requireActivity(), fragmentList)
        TabLayoutMediator(binding.tabLayout, binding.vp) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun setupSyncButton() {
        binding.ibSync.setOnClickListener {
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
            checkLocationAndFetch()
        }
    }

    private fun setupSearchButton() {
        binding.ibSearch.setOnClickListener {
            DialogManager.searchByNameDialog(requireContext()) { cityName ->
                requestWeatherData(cityName)
            }
        }
    }

    private fun setupWeatherObserver() {
        model.liveDataCurrent.observe(viewLifecycleOwner) { weather ->
            updateWeatherUI(weather)
        }
    }

    private fun checkLocationAndFetch() {
        when {
            !isGpsEnabled() -> showGpsDisabledAlert()
            !hasLocationPermission() -> requestLocationPermission()
            else -> fetchCurrentLocation()
        }
    }

    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            fetchCurrentLocation()
        } else {
            showPermissionDeniedMessage()
            requestWeatherData(DEFAULT_CITY)
        }
    }

    private fun fetchCurrentLocation() {
        if (!hasLocationPermission()) return

        val ct = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener { task ->
                task.result?.let { location ->
                    requestWeatherData("${location.latitude},${location.longitude}")
                } ?: run {
                    requestWeatherData(DEFAULT_CITY)
                }
            }
    }

    private fun requestWeatherData(location: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?" +
                "key=$API_KEY&q=$location&days=7&aqi=no&alerts=no"

        Volley.newRequestQueue(requireContext()).add(
            StringRequest(
                Request.Method.GET,
                url,
                { response -> handleWeatherResponse(response) },
                { error -> handleWeatherError(error) }
            )
        )
    }

    private fun handleWeatherResponse(response: String) {
        try {
            val jsonObject = JSONObject(response)
            val daysList = parseDaysWeatherData(jsonObject)
            parseCurrentWeatherData(jsonObject, daysList.first())
        } catch (e: Exception) {
            Log.e("WeatherError", "Failed to parse weather data", e)
            showToast("Failed to parse weather data")
        }
    }

    private fun handleWeatherError(error: Throwable) {
        Log.e("WeatherError", "API error", error)
        showToast("Error fetching weather: ${error.message}")
        requestWeatherData(DEFAULT_CITY)
    }

    private fun parseCurrentWeatherData(jsonObject: JSONObject, weatherItem: WeatherModel) {
        val current = jsonObject.getJSONObject("current")
        val condition = current.getJSONObject("condition")

        model.liveDataCurrent.value = WeatherModel(
            city = jsonObject.getJSONObject("location").getString("name"),
            time = current.getString("last_updated"),
            condition = condition.getString("text"),
            imageUrl = condition.getString("icon"),
            currentTemp = current.getString("temp_c"),
            maxTemp = weatherItem.maxTemp,
            minTemp = weatherItem.minTemp,
            hours = weatherItem.hours
        )
    }

    private fun parseDaysWeatherData(jsonObject: JSONObject): List<WeatherModel> {
        val daysList = mutableListOf<WeatherModel>()
        val locationName = jsonObject.getJSONObject("location").getString("name")
        val forecastDays = jsonObject.getJSONObject("forecast").getJSONArray("forecastday")

        for (i in 0 until forecastDays.length()) {
            val day = forecastDays.getJSONObject(i)
            val dayData = day.getJSONObject("day")
            val condition = dayData.getJSONObject("condition")

            daysList.add(WeatherModel(
                city = locationName,
                time = day.getString("date"),
                condition = condition.getString("text"),
                imageUrl = condition.getString("icon"),
                currentTemp = "",
                maxTemp = formatTemperature(dayData.getString("maxtemp_c")),
                minTemp = formatTemperature(dayData.getString("mintemp_c")),
                hours = day.getJSONArray("hour").toString()
            ))
        }

        model.liveDataList.value = daysList
        return daysList
    }

    private fun updateWeatherUI(weather: WeatherModel) {
        with(binding) {
            tvCity.text = weather.city
            tvData.text = weather.time
            tvCondition.text = weather.condition

            val currentTemp = if (weather.currentTemp.isNotEmpty()) "${weather.currentTemp}°C" else ""
            tvCurrentTemp.text = currentTemp.ifEmpty {
                "${weather.maxTemp}°C / ${weather.minTemp}°C"
            }

            Picasso.get()
                .load("https:${weather.imageUrl}")
                .into(imWeather)

            tvMaxMin.text = if (weather.currentTemp.isNotEmpty()) {
                "${weather.maxTemp}°C / ${weather.minTemp}°C"
            } else {
                ""
            }
        }
    }

    // Permission and location helpers
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun isGpsEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionRationaleDialog()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.location_permission_needed)
            .setMessage(R.string.location_permission_rationale)
            .setPositiveButton(R.string.ok) { _, _ ->
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                requestWeatherData(DEFAULT_CITY)
            }
            .show()
    }

    private fun showGpsDisabledAlert() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.gps_disabled)
            .setMessage(R.string.gps_required_message)
            .setPositiveButton(R.string.settings) { _, _ ->
                locationSettingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                requestWeatherData(DEFAULT_CITY)
            }
            .show()
    }

    private fun showPermissionDeniedMessage() {
        showToast(getString(R.string.location_permission_denied))
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun formatTemperature(temp: String) = temp.toFloat().toInt().toString()

    companion object {
        fun newInstance() = MainFragment()
    }
}