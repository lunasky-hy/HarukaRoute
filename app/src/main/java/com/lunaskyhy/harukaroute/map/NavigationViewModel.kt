package com.lunaskyhy.harukaroute.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.navigation.Navigator

class NavigationViewModel : ViewModel() {
    // Navigator のインスタンスを保持
    private var navigator: Navigator? = null

    // ルートのPolylineやナビ情報などをStateで公開
    var routePolyline by mutableStateOf<List<LatLng>>(emptyList())
    var nextTurnInfo by mutableStateOf<String?>(null)

    fun startNavigation() {
        // ... Navigatorを初期化して、ナビを開始 ...
        // navigator.setRouteChangedListener { ... } でルート情報を取得して routePolyline を更新
        // navigator.addNavInfoListener { navInfo -> ... } でナビ情報を取得して nextTurnInfo を更新
    }
}