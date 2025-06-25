package com.lunaskyhy.data

import com.lunaskyhy.data.model.Place

val PLACES = listOf(
    Place(
        0,
        "東京駅",
        "東京駅（とうきょうえき）は、東京都千代田区丸の内一丁目にある、東日本旅客鉄道（JR東日本）・東海旅客鉄道（JR東海）・東京地下鉄（東京メトロ）の駅である。",
        35.6738382,
        139.7565598,
    ),
    Place(
        1,
        "新宿駅",
        "新宿駅（しんじゅくえき）は、東京都新宿区・渋谷区にある、東日本旅客鉄道（JR東日本）・京王電鉄・小田急電鉄・東京地下鉄（東京メトロ）・東京都交通局（都営地下鉄）の駅である。",
        35.690921,
        139.700258
    ),
    Place(
        2,
        "名古屋駅",
        "名古屋駅（なごやえき）は、愛知県名古屋市中村区名駅にある、東海旅客鉄道（JR東海）・日本貨物鉄道（JR貨物）・名古屋臨海高速鉄道・名古屋市交通局（名古屋市営地下鉄）の駅である。",
        35.170915,
        136.8789566
    ),
)

class PlacesRepository {
    fun getPlaces(): List<Place> {
        return PLACES
    }

    fun getPlace(placeId: Int): Place? {
        return PLACES.find { it.id == placeId }
    }
}