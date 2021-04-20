package bogdandonduk.androidlibs.permissionsandroid

import android.Manifest.permission.*
import android.Manifest.permission_group.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object PermissionsService {
    private const val LIBRARY_PREFIX = "prefs_bogdandonduk.androidlibs.permissionsandroid"

    private var instance: PermissionsService? = null

    private var sentToAppSettings = false

    private val codesMap = mutableMapOf(
        STORAGE to 1,
        READ_EXTERNAL_STORAGE to 11,
        WRITE_EXTERNAL_STORAGE to 12
    )

    private fun getPreferences(context: Context) =
        context.getSharedPreferences(LIBRARY_PREFIX + context.packageName, Context.MODE_PRIVATE)

    fun checkStorageGroup(context: Context) : Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (context.checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        } else true
    }

    fun checkStorageRead(context: Context) : Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (context.checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        } else true
    }

    fun checkStorageWrite(context: Context) : Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        } else true
    }
}