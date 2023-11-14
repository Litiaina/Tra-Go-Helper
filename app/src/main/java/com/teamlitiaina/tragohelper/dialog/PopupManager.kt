package com.teamlitiaina.tragohelper.dialog

import android.content.Context
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.viewbinding.ViewBinding

class PopupManager {
    companion object {
         fun displayInformationPopupWindow(context: Context, viewBinding: ViewBinding, focusable: Boolean, layoutLocation: Int): PopupWindow {
            val rootView = viewBinding.root
            val popupParent = FrameLayout(context)
            popupParent.addView(rootView)
            val popupWindow = PopupWindow(popupParent, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, focusable)
            popupWindow.showAtLocation(rootView, layoutLocation, 0, 0)
            return popupWindow
        }
    }
}