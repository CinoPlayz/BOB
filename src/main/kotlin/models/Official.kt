package models

data class Official(
    val St_vlaka: String,
    val St: Int,
    val Postaja_SZ_OD: String,
    val Postaja_SZ_DO: String,
    val Relacija: String,
    val St_postaje: String,
    val Postaja: String,
    val Odhod: String,
    val Koordinate: String,
    val Vrsta: Int,
    val Zamuda_cas: Int,
    val Predviden_prihod: Int,
    val Vrsta_vlaka: String,
    val Rang: String
)
