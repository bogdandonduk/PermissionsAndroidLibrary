package bogdandonduk.androidlibs.permissionsandroid

import android.view.View
import androidx.fragment.app.DialogFragment

class PermissionRequester {
    val rationaleModalsMap = mutableMapOf<String, Pair<(view: View, modal: DialogFragment) -> Unit, (view: View, modal: DialogFragment) -> Unit>>()
}