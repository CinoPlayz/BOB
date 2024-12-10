package models

import kotlinx.serialization.Serializable

@Serializable
data class VlakiSi(val success: Boolean, val data: List<VlakSiTrain>)

@Serializable
data class VlakSiTrain(val train_cache: VlakiSiTrainCache, val train_data: VlakiSiTrainData , val coordinates: Coordinates)

@Serializable
data class VlakiSiTrainCache(val delay: Int, val is_bus: Boolean)

@Serializable
data class VlakiSiTrainCompositionInner(val kind: String, val uicNumber: String)

@Serializable
data class VlakiSiTrainTimes(
    val stop_sequence: Int,
    val stop_name: String,
    val arrival_time: String?,
    val departure_time: String?
)

@Serializable
data class VlakiSiTrainData(
    val train_number: String,
    val train_type: String,
    val train_name: String,
    val train_times: List<VlakiSiTrainTimes>,
    val train_start_time: String,
    val train_end_time: String,
    val train_common_name: String,
    val current_stop_sequence: Int,
    val next_stop_sequence: Int
)

