# Android字幕渲染Library-LororSubtitle

[![License](https://img.shields.io/badge/License%20-Apache%202-337ab7.svg)](https://www.apache.org/licenses/LICENSE-2.0)

## 支持srt、ass/ssa大部分标签渲染

## Studio中引入项目

```
dependencies {
    compile 'com.github.Loror:LororSubtitle:1.0.0'
}

allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

## 解码示例

```
    private fun getSubtitle(): Flow<List<SubtitlesModel>> = flow {
        val decoder = SubtitlesDecoder()
        val manager = getResources().assets
        val inputStream = manager.open("test.ass")
        emit(decoder.decode(inputStream, "utf-8"))
    }
```

## 显示示例

```
    private val list = mutableListOf<SubtitlesModel>()
    private var cRender: SubtitlesRender? = null
    private var time = 0
    private val handler = Handler(Looper.getMainLooper())
    private val update = object : Runnable {

        override fun run() {
            if (list.isNotEmpty()) {
                val render = SubtitlesRender(list, time)
                if (render != cRender) {
                    binding.subtitle.showModels(render)
                    cRender = render
                }
            }
            time += 30
            if (time > 10 * 60 * 1000) {
                return
            }
            handler.postDelayed(this, 30)
        }

    }
    
    private fun initView() {
        //设置视频比例，以实际视频大小为准
        binding.subtitle.render.setAspectRatio(1920f / 1080)
        //设置字幕显示到视频范围
        binding.subtitle.render.setLocateInVideo(true)
        runBlocking {
            getSubtitle()
                .catch {
                    it.printStackTrace()
                }
                .flowOn(Dispatchers.IO)
                .onEach {
                    list.clear()
                    list.addAll(it)
                    handler.post(update)
                }
                .collect()
        }
    }
```

License
-------

    Copyright 2024 Loror

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
