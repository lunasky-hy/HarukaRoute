package com.lunaskyhy.harukaroute

import android.app.Application
import com.lunaskyhy.harukaroute.map.MapControllerProvider

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        MapControllerProvider.initialize(applicationContext)
    }
}

//1. MyObserver / MyObserverWithContext の目的
//  この Observer は、MapboxNavigationObserver または関連するインターフェース
//   （例：TripSessionStateObserver、RoutesObserver、LocationObserver など）を実装したクラスであり、
//   Mapbox Navigation の状態変化を監視するためのものです。
//
//たとえば以下のようなイベントを受け取るために使います：\
//  ナビゲーションの開始/停止 (TripSession の状態)\
//  現在位置の更新 (LocationObserver)\
//  ルートの更新 (RoutesObserver)\
//  音声案内イベント (VoiceInstructionsObserver)\
//  目的地到着イベント (ArrivalObserver)