package models

data class VlakiSi(val success: Boolean, val data: List<VlakSiTrain>)

data class VlakSiTrain(val train_cache: VlakiSiTrainCache, val train_data: VlakiSiTrainData , val coordinates: Coordinates)

data class VlakiSiTrainCache(val composition: List<VlakiSiTrainComposition>, val delay: Int, val is_bus: Boolean)

data class VlakiSiTrainComposition(val timestamp: String, val source: String, val composition: List<VlakiSiTrainCompositionInner>)

data class VlakiSiTrainCompositionInner(val kind: String, val uicNumber: String)

data class VlakiSiTrainTimes(
    val stop_sequence: Int,
    val stop_name: String,
    val arrival_time: String?,
    val departure_time: String?
)

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

