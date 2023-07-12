package com.yeqiu.awesomeandroid

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yeqiu.awesomeandroid.app.R
import com.yeqiu.awesomeandroid.app.databinding.ActivityMainBinding
import com.yeqiu.common.BaseBindActivity

class MainActivity : BaseBindActivity<ActivityMainBinding>() {

    override fun init() {


        binding.rvList.layoutManager = LinearLayoutManager(this)

        val list = mutableListOf<MainViewModel>()
        list.add(MainViewModel("录屏", ScreenRecordingActivity::class.java))
        list.add(MainViewModel("下载目录文件处理(兼容Android Q)", AndroidQFileActivity::class.java))
        list.add(MainViewModel("画板", BoardActivity::class.java))


        val mainListAdapter = MainListAdapter(list)
        binding.rvList.adapter = mainListAdapter

    }


   inner class MainListAdapter(private val list: List<MainViewModel>) :
        RecyclerView.Adapter<MainListAdapter.MainListViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainListViewHolder {

            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_main_item, null)

            return MainListViewHolder(view)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: MainListViewHolder, position: Int) {

            holder.tvName.text = list[position].name

            holder.tvName.setOnClickListener {
                startActivity(Intent(this@MainActivity, list[position].activity))
            }

        }


       inner  class MainListViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {

            val tvName by lazy { view.findViewById<TextView>(R.id.tv_name) }


        }

    }


    data class MainViewModel(val name: String, val activity: Class<out Activity>)


}


