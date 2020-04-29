package dev.haenara.smsmachinegun

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.telephony.SmsManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var shared : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        shared = getSharedPreferences("sms", Context.MODE_PRIVATE)

        edtPhone.setText(load("Phone", ""))
        edtPhone2.setText(load("Phone2", ""))
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
        save("Phone2", edtPhone2.text.toString())
        save("Message", edtMessage.text.toString())
        save("Date", edtDate.text.toString())
        save("Total", edtTotal.text.toString())
        save("Interval", edtInterval.text.toString())

        Thread {
            repeat(edtTotal.text.toString().toInt()) {
                sendSms(
                    getPhoneNumber(),
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

    var switch = true
    private fun getPhoneNumber(): String {
        return if (edtPhone2.text.isNullOrBlank()) {
            edtPhone.text.toString().trim()
        } else {
            switch = switch.not()
            if (switch) {
                edtPhone.text.toString().trim()
            } else {
                edtPhone2.text.toString().trim()
            }
        }
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
