package com.milen.bluetoothapp.data.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class TrackerScores : Serializable {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("date")
    @Expose
    var date: String? = null

    @SerializedName("score")
    @Expose
    var score: Int? = null

    @SerializedName("score_image")
    @Expose
    var score_image: String? = null

    @SerializedName("score_name")
    @Expose
    var score_name: String? = null

}