package bogdandonduk.androidlibs.permissionsandroid

interface PermissionRequester {
    val requestedPermissions: MutableMap<Int, Boolean>

    fun requestPermissions()
}