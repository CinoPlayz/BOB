package models

data class VlakiSi(var success: Boolean, var data: List<VlakSiTrain>)

data class VlakSiTrain(var train_cache: VlakiSiTrainCache, var train_data: VlakiSiTrainData , var coordinates: Coordinates)

data class VlakiSiTrainCache(var composition: List<VlakiSiTrainComposition>, var delay: Int, var is_bus: Boolean)

data class VlakiSiTrainComposition(var timestamp: String, var source: String, var composition: List<VlakiSiTrainCompositionInner>)

data class VlakiSiTrainCompositionInner(var kind: String, var uicNumber: String)

data class VlakiSiTrainTimes(
    var stop_sequence: Int,
    var stop_name: String,
    var arrival_time: String?,
    var departure_time: String?
)

data class VlakiSiTrainData(
    var train_number: String,
    var train_type: String,
    var train_name: String,
    var train_times: List<VlakiSiTrainTimes>,
    var train_start_time: String,
    var train_end_time: String,
    var train_common_name: String,
    var current_stop_sequence: Int,
    var next_stop_sequence: Int
)

