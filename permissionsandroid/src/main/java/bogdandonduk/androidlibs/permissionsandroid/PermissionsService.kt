package bogdandonduk.androidlibs.permissionsandroid

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.DialogFragment
import bogdandonduk.androidlibs.permissionsandroid.PermissionsNamesExtensionVocabulary.STORAGE
import bogdandonduk.androidlibs.permissionsandroid.PermissionsNamesExtensionVocabulary.READ_EXTERNAL_STORAGE
import bogdandonduk.androidlibs.permissionsandroid.PermissionsNamesExtensionVocabulary.WRITE_EXTERNAL_STORAGE
import bogdandonduk.androidlibs.permissionsandroid.PermissionsNamesExtensionVocabulary.delimiter

object PermissionsService {
    private const val LIBRARY_PREFIX = "prefs_bogdandonduk.androidlibs.permissionsandroid"

    private var sentToAppSettings = false

    private const val PACKAGE_SCHEME = "package"

    private val codesMap = mutableMapOf(
        STORAGE to 1,
        READ_EXTERNAL_STORAGE to 11,
        WRITE_EXTERNAL_STORAGE to 12
    )

    private const val DO_NOT_ASK_AGAIN_PREFIX = LIBRARY_PREFIX + delimiter + "doNotAskAgain" + delimiter

    private fun getPreferences(context: Context) =
        context.getSharedPreferences(LIBRARY_PREFIX + context.packageName, Context.MODE_PRIVATE)


    fun checkStorage(activity: Activity) : Boolean =
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                    activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                else
                    Environment.isExternalStorageManager()
            else true

    fun openAppSettings(activity: Activity) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts(PACKAGE_SCHEME, activity.packageName, null)

            activity.startActivity(this)

            sentToAppSettings = true
        }
    }

    fun closeOnDenial(modal: DialogFragment, activity: Activity? = null) {
        modal.dismiss()
        activity?.finish()
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun openAppSettingsForManageStorage(activity: Activity) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).run {
                data = Uri.fromParts(PACKAGE_SCHEME, activity.packageName, null)

                activity.startActivity(this)
            }

            sentToAppSettings = true
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun requestStorage(activity: Activity, deniedRationaleAction: (() -> Unit)?, doNotAskAgainRationaleAction: (() -> Unit)?, api30manageStoragePermissionRequestRationaleAction: (() -> Unit)?, alreadyGrantedOrLessThanApi23Action: () -> Unit) {
        if(!checkStorage(activity))
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    if(!Environment.isExternalStorageManager())
                        api30manageStoragePermissionRequestRationaleAction?.invoke()
                    else
                        alreadyGrantedOrLessThanApi23Action.invoke()
                else
                    if(activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                        if(activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) || activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                            deniedRationaleAction?.invoke()
                        else if(getPreferences(activity).getBoolean(DO_NOT_ASK_AGAIN_PREFIX + STORAGE, false))
                            doNotAskAgainRationaleAction?.invoke()
                        else
                            activity.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), codesMap[STORAGE]!!)
                    else
                        alreadyGrantedOrLessThanApi23Action.invoke()
            else
                alreadyGrantedOrLessThanApi23Action.invoke()
        else
            alreadyGrantedOrLessThanApi23Action.invoke()
    }

    fun requestStorageUnwrapped(activity: Activity) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                openAppSettingsForManageStorage(activity)
            else
                activity.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), codesMap[STORAGE]!!)
    }

    @SuppressLint("NewApi")
    fun handleStorageRequestResult(activity: Activity, requestCode: Int, grantResults: IntArray, grantedAction: (() -> Unit)? = null, deniedAction: (() -> Unit)? = null) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val granted = {
                getPreferences(activity).edit().remove(DO_NOT_ASK_AGAIN_PREFIX + STORAGE).apply()

                grantedAction?.invoke()
            }

            val denied = {
                if(!activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) || !activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    getPreferences(activity).edit().putBoolean(DO_NOT_ASK_AGAIN_PREFIX + STORAGE, true).apply()

                deniedAction?.invoke()
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                if(Environment.isExternalStorageManager())
                    granted.invoke()
                else
                    denied.invoke()
            else
                if(requestCode == codesMap[STORAGE])
                    if(grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                        granted.invoke()
                    else
                        denied.invoke()
        }
    }

    fun handleReturnFromAppSettings(activity: Activity, checkPermissionAction: (activity: Activity) -> Boolean, deniedAction: (() -> Unit)?, grantedAction: (() -> Unit)? = null) {
        if(sentToAppSettings) {
            sentToAppSettings = false

            if(checkPermissionAction.invoke(activity))
                grantedAction?.invoke()
            else
                deniedAction?.invoke()
        }
    }
}