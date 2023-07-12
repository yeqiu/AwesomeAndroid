package com.yeqiu.awesomeandroid

import android.graphics.Color
import android.view.View
import android.widget.Toast
import com.yeqiu.awesomeandroid.app.databinding.ActivityBoradBinding
import com.yeqiu.board.pen.NormalPen
import com.yeqiu.common.BaseBindActivity

class BoardActivity : BaseBindActivity<ActivityBoradBinding>() {
    override fun init() {

        binding.bvBoard.setAlpha(5)
        binding.bvBoard.setPenStyle(Color.RED,10)

        binding.btSize.setOnClickListener {

            binding.bvBoard.setNewPen(NormalPen())
            binding.bvBoard.setPenStyle(Color.BLACK, 10)

        }
        binding.btEraser.setOnClickListener {

            binding.bvBoard.openEraser()
            binding.bvBoard.setPenStyle(Color.BLACK, 50)

        }
        binding.btClear.setOnClickListener {

            binding.bvBoard.clear()

        }
        binding.btRevocation.setOnClickListener {

            binding.bvBoard.revocation()

        }
        binding.btSave.setOnClickListener {

            Toast.makeText(this@BoardActivity,"即将显示bitmap，无法响应绘制",Toast.LENGTH_SHORT).show()

            if (binding.ivImg.visibility == View.VISIBLE) {
                binding.bvBoard.visibility = View.VISIBLE
                binding.ivImg.visibility = View.GONE
            } else {
                binding.bvBoard.visibility = View.GONE
                binding.ivImg.visibility = View.VISIBLE

                val bitmap = binding.bvBoard.getBitmap()
                binding.ivImg.setImageBitmap(bitmap)

            }
        }
    }



}