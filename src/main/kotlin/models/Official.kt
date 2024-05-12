package models

data class Official(
    var St_vlaka: String,
    var St: Int,
    var Postaja_SZ_OD: String,
    val Postaja_SZ_DO: String,
    var Relacija: String,
    var St_postaje: String,
    var Postaja: String,
    var Odhod: String,
    var Koordinate: String,
    var Vrsta: Int,
    var Zamuda_cas: Int,
    var Predviden_prihod: Int,
    var Vrsta_vlaka: String,
    var Rang: String
)
