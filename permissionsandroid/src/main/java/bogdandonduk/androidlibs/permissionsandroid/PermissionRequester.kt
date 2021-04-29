package bogdandonduk.androidlibs.permissionsandroid

import android.view.View
import androidx.fragment.app.DialogFragment

class PermissionRequester {
    val rationaleModalsMap = mutableMapOf<String, Pair<() -> Unit, () -> Unit>>()
}