package com.loror.lororSubtitle.example

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.loror.lororSubtitle.example.databinding.ActivityMainBinding
import com.loror.subtitle.SubtitlesDecoder
import com.loror.subtitle.model.SubtitlesModel
import com.loror.subtitle.render.SubtitlesRender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val list = mutableListOf<SubtitlesModel>()
    private var cRender: SubtitlesRender? = null
    private val player = TimePlayer()
    private val handler = Handler(Looper.getMainLooper())
    private val update = object : Runnable {

        override fun run() {
            var hasAnimation = false
            if (list.isNotEmpty()) {
                val render = SubtitlesRender(list, player.current())
                if (render != cRender) {
                    binding.subtitle.showModels(render)
                    cRender = render
                    hasAnimation = render.isHasMove//返回字幕是否包含动画标签
                }
            }
            if (player.end()) {
                return
            }
            handler.postDelayed(this, if (hasAnimation) 30 else 200)//ass字幕支持动画，delay时间控制渲染帧率
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(update)
    }

    private fun getSubtitle(): Flow<List<SubtitlesModel>> = flow {
        Log.e("Subtitle", "decode")
        val decoder = SubtitlesDecoder()
        val manager = getResources().assets
        val inputStream = manager.open("test.ass")
        emit(decoder.decode(inputStream, "utf-8"))
    }

    private fun initView() {
        //设置视频比例，以实际视频大小为准
        binding.subtitle.render.setAspectRatio(1920, 1080)
        //设置字幕显示到视频范围
//        binding.subtitle.render.setLocateInVideo(true)
        runBlocking {
            getSubtitle()
                .catch {
                    it.printStackTrace()
                }
                .flowOn(Dispatchers.IO)
                .onEach {
                    Log.e("Subtitle", "result:${it.size}")
                    list.clear()
                    list.addAll(it)
                    handler.post(update)
                }
                .collect()
        }
    }
}
