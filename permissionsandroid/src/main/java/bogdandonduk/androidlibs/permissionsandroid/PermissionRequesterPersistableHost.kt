package bogdandonduk.androidlibs.permissionsandroid

interface PermissionRequesterPersistableHost {
    val requestedPermissionsCodesMap: MutableMap<String, Int>
}