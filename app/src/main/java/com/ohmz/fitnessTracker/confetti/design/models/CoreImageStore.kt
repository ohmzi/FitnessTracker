package com.ohmz.tictactoe.confetti.design.models

interface CoreImageStore<T> {
    fun storeImage(image: T): Int

    fun getImage(id: Int): T?
}
