package com.example.test_plugin

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import com.alibaba.fastjson.JSONObject
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.IOException

/** TestPlugin */
class TestPlugin : FlutterPlugin, MethodCallHandler, ActivityAware
//    , RecognitionListener
{
    val TAG = "vosk"
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private var activity: Activity? = null
    lateinit var model: Model
    private var speechService: SpeechService? = null

    val recognitionListener = object : RecognitionListener {
        override fun onPartialResult(p0: String?) {
            Log.i(TAG,"onPartialResult:$p0")
        }

        override fun onResult(p0: String?) {
            Log.i(TAG,"onResult:$p0")
//            text = p0.toString()
//            if (p0?.length ?: 0 > text.length) {
//                text = p0.toString()
//            }
        }

        override fun onFinalResult(p0: String?) {
            Log.i(TAG,"onFinalResult:$p0")
//            if (p0?.length ?: 0 > text.length) {
//                text = p0.toString()
//            }
            val jsonObject = JSONObject.parseObject(p0)
            val msg = jsonObject.getString("text")

            methodCallResult?.let {
                it.success(msg)
            }
            methodCallResult = null
        }

        override fun onError(p0: Exception?) {
            Log.e(TAG,"onError:$p0")
        }

        override fun onTimeout() {
            Log.e(TAG,"onTimeout")
        }
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "test_plugin")
        channel.setMethodCallHandler(this)
    }

    var methodCallResult:Result?=null

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method == "initModel") {
            initModel(null)
            result.success("")
        }else if (call.method == "startRecognize") {
            startRecognize()
            result.success("")
        } else if (call.method == "stopRecognize") {
            stopRecognize()
            methodCallResult = result
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    var initModel = false
    private fun startRecognize() {
        Log.i(TAG,"录音开始")
        if (initModel) {
            recognizeMicrophone()
        } else {
            initModel {
                Log.i(TAG,"模型文件初始化成功")
                recognizeMicrophone()
            }
        }
    }

    private fun stopRecognize(){
        speechService?.stop()
        speechService = null
        Log.i(TAG,"录音结束")
    }

    private fun recognizeMicrophone() {
//        text = ""
        if (speechService != null) {
            speechService?.stop()
            speechService = null
        } else {
            try {
                val rec = Recognizer(model, 16000.0f)
                speechService = SpeechService(rec, 16000.0f)
                speechService?.startListening(recognitionListener)
            } catch (e: IOException) {
                println(e.message)
            }
        }
    }


    private fun initModel(initializationCompletedCallback: (() -> Unit)?) {
        StorageService.unpack(activity, "model-cn", "model",
            {
                this.model = it
                initializationCompletedCallback?.invoke()
            },
            { exception: IOException ->
                Log.e(TAG,"Failed to unpack the model" + exception.message)
            })
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        this.onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        this.onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

//    var text = ""
}
