package com.example.weather_app.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class VpAdapter (fragmentActivity: FragmentActivity, private val fragmentList: List<Fragment>): FragmentStateAdapter (fragmentActivity) {
    // Количество фрагментов два: Hours/Days
    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
       return fragmentList[position]
    }
}