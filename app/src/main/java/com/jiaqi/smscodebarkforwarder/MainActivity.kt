package com.jiaqi.smscodebarkforwarder

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var serverInput: EditText
    private lateinit var deviceKeyInput: EditText
    private lateinit var keywordsInput: EditText
    private lateinit var enabledSwitch: Switch
    private lateinit var statusText: TextView
    private lateinit var logsText: TextView
    private lateinit var testButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        loadConfig()
        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refreshStatus()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        root.addView(title("短信验证码转发"))

        root.addView(label("Bark server 或完整 Bark URL"))
        serverInput = EditText(this).apply {
            hint = "${AppPrefs.DEFAULT_SERVER} 或 ${AppPrefs.DEFAULT_SERVER}/你的key"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
        }
        root.addView(serverInput, matchWrapParams())

        root.addView(label("Bark device_key"))
        deviceKeyInput = EditText(this).apply {
            hint = "如果上面粘贴完整 URL，这里可以留空"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            setSingleLine(true)
        }
        root.addView(deviceKeyInput, matchWrapParams())

        root.addView(label("验证码关键词（每行一个，也可用逗号分隔）"))
        keywordsInput = EditText(this).apply {
            hint = "默认包含中英文常见词，可按需添加其他语言"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 4
            gravity = android.view.Gravity.TOP
        }
        root.addView(keywordsInput, matchWrapParams())

        enabledSwitch = Switch(this).apply {
            text = "启用转发"
        }
        root.addView(enabledSwitch, matchWrapParams())

        val saveButton = Button(this).apply {
            text = "保存"
            setOnClickListener {
                saveConfig()
                AppLog.add(this@MainActivity, "配置已保存")
                refreshStatus()
            }
        }
        root.addView(saveButton, matchWrapParams())

        val permissionButton = Button(this).apply {
            text = "请求短信权限"
            setOnClickListener {
                requestPermissions(arrayOf(Manifest.permission.RECEIVE_SMS), REQUEST_SMS_PERMISSION)
            }
        }
        root.addView(permissionButton, matchWrapParams())

        testButton = Button(this).apply {
            text = "发送测试"
            setOnClickListener {
                saveConfig()
                sendTestPush()
            }
        }
        root.addView(testButton, matchWrapParams())

        statusText = TextView(this).apply {
            textSize = 15f
            setPadding(0, dp(14), 0, dp(8))
        }
        root.addView(statusText, matchWrapParams())

        root.addView(label("最近日志"))
        logsText = TextView(this).apply {
            textSize = 14f
            setTextIsSelectable(true)
        }
        root.addView(logsText, matchWrapParams())

        val scrollView = ScrollView(this).apply {
            addView(root)
        }
        setContentView(scrollView)
    }

    private fun loadConfig() {
        val config = AppPrefs.read(this)
        serverInput.setText(config.server)
        deviceKeyInput.setText(config.deviceKey)
        keywordsInput.setText(config.verificationKeywords.joinToString("\n"))
        enabledSwitch.isChecked = config.enabled
    }

    private fun saveConfig() {
        AppPrefs.save(
            context = this,
            server = serverInput.text.toString(),
            deviceKey = deviceKeyInput.text.toString(),
            enabled = enabledSwitch.isChecked,
            verificationKeywords = keywordsInput.text.toString(),
        )
    }

    private fun sendTestPush() {
        testButton.isEnabled = false
        Thread {
            val result = SmsForwarder.sendTest(applicationContext)
            if (result.ok) {
                AppLog.add(applicationContext, "测试推送成功：${result.message}")
            } else {
                AppLog.add(applicationContext, "测试推送失败：${result.message}")
            }

            runOnUiThread {
                testButton.isEnabled = true
                refreshStatus()
            }
        }.start()
    }

    private fun refreshStatus() {
        val config = AppPrefs.read(this)
        val endpoint = BarkEndpoint.resolve(config.server, config.deviceKey)
        val permissionText = if (hasSmsPermission()) "已允许" else "未允许"
        val enabledText = if (config.enabled) "已开启" else "已关闭"
        val keyText = if (endpoint.deviceKey.isBlank()) "未填写" else "已填写"

        statusText.text = "短信权限：$permissionText\n转发：$enabledText\nDevice key：$keyText\n关键词：${config.verificationKeywords.size} 个\n请求地址：${endpoint.pushUrl}"
        logsText.text = AppLog.read(this).ifBlank { "暂无日志" }
    }

    private fun hasSmsPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun title(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 24f
            setPadding(0, 0, 0, dp(16))
        }
    }

    private fun label(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setPadding(0, dp(12), 0, dp(4))
        }
    }

    private fun matchWrapParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val REQUEST_SMS_PERMISSION = 1001
    }
}
