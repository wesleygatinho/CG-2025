package br.edu.historiaviva.ui

object Routes {
    const val Welcome = "welcome"
    const val Gallery = "gallery"
    const val Detail = "detail/{id}"
    const val Ar = "ar/{id}"

    fun detail(id: String) = "detail/$id"
    fun ar(id: String) = "ar/$id"
}

