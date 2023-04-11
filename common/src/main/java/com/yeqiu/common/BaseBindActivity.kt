package com.yeqiu.common

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BaseBindActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VB
    private val context: Activity by lazy {
        this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val type = javaClass.genericSuperclass
            if (type is ParameterizedType) {
                val clazz = type.actualTypeArguments[0] as Class<VB>
                val method = clazz.getMethod("inflate", LayoutInflater::class.java)
                binding = method.invoke(null, layoutInflater) as VB
                setContentView(binding.root)
                init()
            } else {
                log("ViewBinding 初始化失败")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            log("ViewBinding 初始化失败 e = ${e.message}")
        }


    }


    abstract fun init()
}