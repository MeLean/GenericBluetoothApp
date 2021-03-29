package com.milen.bluetoothapp.data.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class TrackerDetails : Serializable {
    var uniqueid = 0

    @SerializedName("title_part")
    @Expose
    var title_part: String? = null

    @SerializedName("graph_color")
    @Expose
    var graphColor: String? = null

    @SerializedName("tracker_name")
    @Expose
    var trackerName: String? = null

    @SerializedName("tracker_id")
    @Expose
    var trackerId: Int? = null

    @SerializedName("score_names")
    @Expose
    var scoreNames: List<String>? = null

    @SerializedName("results")
    @Expose
    var results: List<TrackerScores>? = null


}
