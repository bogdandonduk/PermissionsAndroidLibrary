package bogdandonduk.androidlibs.permissionsandroid

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.Manifest.permission.*
import android.Manifest.permission_group.*
import android.content.DialogInterface
import android.view.View
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentActivity
import bogdandonduk.androidlibs.bottomsheetmodalsandroid.BottomSheetModalsService
import bogdandonduk.androidlibs.bottomsheetmodalsandroid.anatomy.ButtonItem
import bogdandonduk.androidlibs.bottomsheetmodalsandroid.anatomy.TextItem
import bogdandonduk.androidlibs.permissionsandroid.PermissionsNamesExtensionVocabulary.delimiter
import bogdandonduk.androidlibs.permissionsandroid.PermissionsNamesExtensionVocabulary.DO_NOT_ASK_AGAIN
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.random.Random

object PermissionsService {
    private const val LIBRARY_PREFIX = "prefs_bogdandonduk.androidlibs.permissionsandroid"

    private var sentToAppSettings = false

    private const val PACKAGE_SCHEME = "package"

    private const val DO_NOT_ASK_AGAIN_PREFIX =
        LIBRARY_PREFIX + delimiter + DO_NOT_ASK_AGAIN + delimiter

    private fun getPreferences(context: Context) =
        context.getSharedPreferences(
            LIBRARY_PREFIX + delimiter + context.packageName,
            Context.MODE_PRIVATE
        )

    @SuppressLint("InlinedApi")
    fun checkPermissions(
        activity: Activity,
        cleanForApiLevel: Boolean = false,
        vararg permissions: String
    ): MutableMap<String, Boolean> {
        val checkResultsMap = mutableMapOf<String, Boolean>()

        permissions.forEach {
            when (it) {
                STORAGE -> {
                    checkResultsMap[READ_EXTERNAL_STORAGE] =
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            activity.checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        else true

                    checkResultsMap[WRITE_EXTERNAL_STORAGE] =
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            activity.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        else true
                }

                READ_EXTERNAL_STORAGE -> {
                    checkResultsMap[READ_EXTERNAL_STORAGE] =
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            activity.checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        else true
                }

                WRITE_EXTERNAL_STORAGE -> {
                    checkResultsMap[WRITE_EXTERNAL_STORAGE] =
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            activity.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        else true
                }

                MANAGE_EXTERNAL_STORAGE -> {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                        checkResultsMap[MANAGE_EXTERNAL_STORAGE] = Environment.isExternalStorageManager()
                    else if(!cleanForApiLevel)
                        checkResultsMap[MANAGE_EXTERNAL_STORAGE] = false
                }

                ACCEPT_HANDOVER -> {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        checkResultsMap[ACCEPT_HANDOVER] = activity.checkSelfPermission(ACCEPT_HANDOVER) == PackageManager.PERMISSION_GRANTED
                    else if(!cleanForApiLevel)
                        checkResultsMap[ACCEPT_HANDOVER] = false
                }

                else -> {
                    checkResultsMap[it] =
                        activity.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
                }
            }
        }

        return checkResultsMap
    }

    fun openAppSettings(activity: Activity) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts(PACKAGE_SCHEME, activity.packageName, null)

            activity.startActivity(this)

            sentToAppSettings = true
        }
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

    fun closeOnDenial(modal: DialogInterface, activity: Activity? = null) {
        modal.dismiss()
        activity?.finish()
    }

    fun requestPermissions(activity: FragmentActivity, rationaleModalBuildHelper: RationaleModalBuildHelper, vararg rationalePermissionItems: RationalePermissionItem) : PermissionsRequestItem? =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions(activity, cleanForApiLevel = true, permissions = mutableListOf<String>().apply{
                rationalePermissionItems.forEach {
                    add(it.permission)
                }
            }.toTypedArray())
                .filter {
                    !it.value
                }.run {
                    if(isNotEmpty()) {
                        val requestCode: Int = Random.nextInt(0, 999)
                        var rationaleTag: String? = null
                        var postActions: MutableMap<String, PermissionPostRequestRationaleAction>? = null

                        val rationalePermissions = mutableListOf<String>()
                        val requestPermissions = mutableListOf<String>()

                        forEach { it ->
                            if(activity.shouldShowRequestPermissionRationale(it.key) || getPreferences(activity).getBoolean(DO_NOT_ASK_AGAIN_PREFIX + it.key, false))
                                rationalePermissions.add(it.key)

                            when(it.key) {
                                MANAGE_EXTERNAL_STORAGE -> {
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        if(postActions == null) postActions = mutableMapOf()

                                        if(postActions!!.containsKey(MANAGE_EXTERNAL_STORAGE)) {
                                            val manageStorageRationaleTag = "permissions_rationale-${Random.nextInt(1000, 1999)}"

                                            postActions!![MANAGE_EXTERNAL_STORAGE] =
                                                PermissionPostRequestRationaleAction(
                                                    MANAGE_EXTERNAL_STORAGE,
                                                    manageStorageRationaleTag,
                                                ) {
                                                    BottomSheetModalsService.startBuildingSimpleModal(manageStorageRationaleTag)
                                                        .setBackgroundColor(rationaleModalBuildHelper.backgroundColor)
                                                        .setTitle(TextItem(null, rationaleModalBuildHelper.title, rationaleModalBuildHelper.titleColor))
                                                        .setTextItems(mutableListOf<TextItem>().apply {
                                                            rationalePermissionItems.forEach {
                                                                if(it.permission == MANAGE_EXTERNAL_STORAGE)
                                                                    add(TextItem(null, it.permissionRationaleTitle + ": " + it.permissionRationaleMessage, it.textColor))
                                                            }
                                                        })
                                                        .setPositiveButton(ButtonItem(rationaleModalBuildHelper.positiveButtonText, rationaleModalBuildHelper.positiveButtonTextColor) { _: View, bottomSheetDialogFragment: BottomSheetDialogFragment ->
                                                            closeOnDenial(bottomSheetDialogFragment as DialogInterface)

                                                            openAppSettingsForManageStorage(activity)
                                                        })
                                                        .setNegativeButton(ButtonItem(rationaleModalBuildHelper.negativeButtonText, rationaleModalBuildHelper.negativeButtonTextColor) { _: View, bottomSheetDialogFragment: BottomSheetDialogFragment ->
                                                            closeOnDenial(bottomSheetDialogFragment as DialogInterface)
                                                        })
                                                        .show(fragmentManager = activity.supportFragmentManager)
                                                }
                                        }
                                    }
                                }
                                else -> {
                                    with(it.key) {
                                        if(!requestPermissions.contains(this))
                                            requestPermissions.add(this)
                                    }
                                }
                            }
                        }

                        if(rationalePermissions.isNotEmpty()) {
                            BottomSheetModalsService.startBuildingSimpleModal("permissions_rationale-$requestCode".apply { rationaleTag = this })
                                .setBackgroundColor(rationaleModalBuildHelper.backgroundColor)
                                .setTitle(TextItem(null, rationaleModalBuildHelper.title, rationaleModalBuildHelper.titleColor))
                                .setTextItems(mutableListOf<TextItem>().apply {
                                    rationalePermissionItems.forEach {
                                        add(TextItem(null, it.permissionRationaleTitle + ": " + it.permissionRationaleMessage, it.textColor))
                                    }
                                })
                                .setPositiveButton(ButtonItem(rationaleModalBuildHelper.positiveButtonText, rationaleModalBuildHelper.positiveButtonTextColor) { _: View, bottomSheetDialogFragment: BottomSheetDialogFragment ->
                                    closeOnDenial(bottomSheetDialogFragment as DialogInterface)

                                    activity.requestPermissions(
                                        requestPermissions.toTypedArray(),
                                        requestCode
                                    )
                                })
                                .setNegativeButton(ButtonItem(rationaleModalBuildHelper.negativeButtonText, rationaleModalBuildHelper.negativeButtonTextColor) { _: View, bottomSheetDialogFragment: BottomSheetDialogFragment ->
                                    closeOnDenial(bottomSheetDialogFragment as DialogInterface)
                                })
                                .show(fragmentManager = activity.supportFragmentManager)
                        } else
                            if(requestPermissions.isNotEmpty())
                                activity.requestPermissions(
                                    requestPermissions.toTypedArray(),
                                    requestCode
                                )

                        PermissionsRequestItem(requestCode, rationaleTag, postActions)
                    } else null
                }
        } else null

    fun handlePermissionsRequestResult(
        activity: Activity,
        responseRequestCode: Int,
        requestCode: Int,
        grantResults: IntArray,
        permissions: Array<String>
    ) : PermissionsSplitCollection {
        val allowedPermissions = mutableListOf<String>()
        val deniedPermissions = mutableListOf<String>()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && responseRequestCode == requestCode) {
            permissions.forEachIndexed { i: Int, s: String ->
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    getPreferences(activity).edit().remove(DO_NOT_ASK_AGAIN_PREFIX + s).apply()

                    allowedPermissions.add(s)
                } else {
                    if(!activity.shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE))
                        getPreferences(activity).edit().putBoolean(DO_NOT_ASK_AGAIN_PREFIX + s, true).apply()

                    deniedPermissions.add(s)
                }

            }
        } else {
            permissions.forEach {
                allowedPermissions.add(it)
            }
        }

        return PermissionsSplitCollection(allowedPermissions, deniedPermissions)
    }

    fun handleReturnFromAppSettings(activity: Activity, vararg permissionsToCheck: PermissionCheckAction) =
        if(sentToAppSettings) {
            sentToAppSettings = false

            val allowedPermissions = mutableListOf<String>()
            val deniedPermissions = mutableListOf<String>()

            permissionsToCheck.forEach {
                if((it.specialCheckAction != null && it.specialCheckAction.invoke()) || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(it.permission) == PackageManager.PERMISSION_GRANTED))
                    allowedPermissions.add(it.permission)
                else
                    deniedPermissions.add(it.permission)
            }

            PermissionsSplitCollection(allowedPermissions, deniedPermissions)
        } else null

    class RationaleModalBuildHelper(
        @ColorInt var backgroundColor: Int,
        var title: String,
        @ColorInt var titleColor: Int,
        var positiveButtonText: String,
        @ColorInt var positiveButtonTextColor: Int,
        var negativeButtonText: String,
        @ColorInt var negativeButtonTextColor: Int = positiveButtonTextColor
    )

    class RationalePermissionItem(val permission: String, var permissionRationaleTitle: String, var permissionRationaleMessage: String, @ColorInt var textColor: Int)

    class PermissionPostRequestRationaleAction(val permission: String, var rationaleTag: String? = null, val action: () -> Unit)

    class PermissionCheckAction(val permission: String, val specialCheckAction: (() -> Boolean)? = null)

    data class PermissionsSplitCollection(val allowedPermissions: MutableList<String>, val deniedPermissions: MutableList<String>)

    class PermissionsRequestItem(val requestCode: Int?, val rationaleTag: String?, val postActionsForPermissionsMap: MutableMap<String, PermissionPostRequestRationaleAction>?)
}
