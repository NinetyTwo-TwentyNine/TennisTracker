package com.example.tennistracker.UI

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.tennistracker.R
import com.example.tennistracker.ViewModel.TennisViewModel
import com.example.tennistracker.data.Constants.APP_TENNIS_MAX_RADIAN
import com.example.tennistracker.data.Constants.APP_TENNIS_MAX_SPEED
import com.example.tennistracker.data.Constants.APP_TENNIS_MAX_STRENGTH
import com.example.tennistracker.data.TennisHit
import com.example.tennistracker.databinding.FragmentTrackingBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate

@RequiresApi(Build.VERSION_CODES.S)
@Suppress("DEPRECATION")
class TrackingFragment : Fragment() {
    private lateinit var binding: FragmentTrackingBinding
    private val tennisViewModel: TennisViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupData()

        val strengthEntries: ArrayList<PieEntry> = arrayListOf()
        strengthEntries.add(PieEntry(tennisViewModel.getCurrentStrength(), "Current"))
        strengthEntries.add(PieEntry((APP_TENNIS_MAX_STRENGTH - tennisViewModel.getCurrentStrength()), ""))
        setupChart(binding.piechartStrength, PieData(PieDataSet(strengthEntries, "").also { it.colors = mutableListOf(R.color.purple_500, R.color.black) }))

        val speedEntries: ArrayList<PieEntry> = arrayListOf()
        speedEntries.add(PieEntry(tennisViewModel.getCurrentSpeed(), "Current"))
        speedEntries.add(PieEntry((APP_TENNIS_MAX_SPEED - tennisViewModel.getCurrentSpeed()), ""))
        setupChart(binding.piechartSpeed, PieData(PieDataSet(speedEntries, "").also { it.colors = mutableListOf(R.color.purple_200, R.color.black) }))

        val radianEntries: ArrayList<PieEntry> = arrayListOf()
        radianEntries.add(PieEntry(tennisViewModel.getCurrentRadian(), "Current"))
        radianEntries.add(PieEntry((APP_TENNIS_MAX_RADIAN - tennisViewModel.getCurrentRadian()), ""))
        setupChart(binding.piechartRadian, PieData(PieDataSet(radianEntries, "").also { it.colors = mutableListOf(R.color.purple_700, R.color.black) }))

        val barDataList: ArrayList<BarEntry> = arrayListOf()
        tennisViewModel.getHitData().forEach { item ->
            barDataList.add(BarEntry((barDataList.size+1).toFloat(), item.getSpeed()))
        }
        val barDataSet = BarDataSet(barDataList, "")
        barDataSet.colors = ColorTemplate.COLORFUL_COLORS.toMutableList()
        barDataSet.valueTextColor = R.color.white
        barDataSet.valueTextSize = 16F
        binding.barchartSpeed.description.isEnabled = false
        binding.barchartSpeed.data = BarData(barDataSet)
        binding.barchartSpeed.getAxis(barDataSet.axisDependency).setStartAtZero(true)
        binding.barchartSpeed.setVisibleYRange(0F, APP_TENNIS_MAX_SPEED.toFloat(), barDataSet.axisDependency)
        binding.barchartSpeed.xAxis.granularity = 1F
        binding.barchartSpeed.notifyDataSetChanged()
    }

    private fun setupData() {
        tennisViewModel.addHit(TennisHit(speed = 20F, strength = 37F, radian = 129F))
        tennisViewModel.addHit(TennisHit(speed = 30F, strength = 30F, radian = 135F) )
        tennisViewModel.addHit(TennisHit(speed = 25F, strength = 44F, radian = 131F) )
        tennisViewModel.addHit(TennisHit(speed = 40F, strength = 35F, radian = 130F) )
    }

    private fun setupChart(chart: PieChart, data: PieData) {
        chart.data = data
        chart.description.isEnabled = false
        chart.setDrawCenterText(false)
        chart.setDrawEntryLabels(false)
        chart.setDrawRoundedSlices(false)
        chart.setDrawSlicesUnderHole(false)
        chart.isDrawHoleEnabled = true
        chart.legend.isEnabled = false
        chart.animateY(1000)
        chart.notifyDataSetChanged()
    }
}