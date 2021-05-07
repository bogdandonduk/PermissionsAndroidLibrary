package bogdandonduk.androidlibs.permissionsandroidlibrary

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import bogdandonduk.androidlibs.permissionsandroid.PermissionsService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView.setOnClickListener {
            Log.d("TAG", "onCreate: " + PermissionsService.checkPermissions(
                activity = this,
                permissions = arrayOf(
                    Manifest.permission_group.STORAGE,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCEPT_HANDOVER,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            ))

            PermissionsService.requestPermissions(
                activity = this,
                permissions = arrayOf(
                    Manifest.permission_group.STORAGE,
//                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCEPT_HANDOVER,
//                    Manifest.permission.ACCEPT_HANDOVER
                )
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}