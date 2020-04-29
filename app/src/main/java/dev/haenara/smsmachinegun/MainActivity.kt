package dev.haenara.smsmachinegun

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import androidx.appcompat.app.AlertDialog
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var shared : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        shared = getSharedPreferences("sms", Context.MODE_PRIVATE)

        edtPhone.setText(load("Phone", ""))
        edtMessage.setText(load("Message", "TEST"))
        edtDate.setText(load("Date", "yyyyMMdd HH:mm:ss"))
        edtTotal.setText(load("Total", "3"))
        edtInterval.setText(load("Interval", "1000"))

        btnSend.setOnClickListener { requestPermission(
            success = {
                send()
            }
        ) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AlertDialog.Builder(this)
            .setTitle("$requestCode")
            .setMessage("$resultCode ${data?.getStringExtra("ub_result")}")
            .create()
            .show()
    }

    private fun requestPermission(success : ()->Unit) {
        TedPermission.with(this)
            .setPermissionListener(object : PermissionListener{
                override fun onPermissionGranted() {
                    success()
                }
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {}
            })
            .setDeniedMessage("설정에서 필수 권한을 허용해주시기 바랍니다.")
            .setPermissions(Manifest.permission.SEND_SMS)
            .check()
    }
    private fun send(){
        save("Phone", edtPhone.text.toString())
        save("Message", edtMessage.text.toString())
        save("Date", edtDate.text.toString())
        save("Total", edtTotal.text.toString())
        save("Interval", edtInterval.text.toString())

        Thread {
            repeat(edtTotal.text.toString().toInt()) {
                sendSms(
                    edtPhone.text.toString(),
                    if (edtCount.isChecked) {
                        "[$it] ${createMessage()}"
                    } else {
                        createMessage()
                    }
                )
                Thread.sleep(edtInterval.text.toString().toLong())
            }
        }.start()
    }

    private fun sendSms(phone: String, message: String) {
        val manager = SmsManager.getDefault()
        manager.sendTextMessage(phone, null, message, null, null)
    }

    private fun createMessage(): String {
        return "${edtMessage.text} ${date(edtDate.text.toString())}".trim()
    }

    private fun date(formatString: String) : String {
        return SimpleDateFormat(formatString).format(Date())
    }

    private fun save(key : String, value: String) {
        shared.edit().putString(key, value).apply()
    }

    private fun load(key : String, default : String = "") : String {
        return shared.getString(key,  default) ?: default
    }
}
