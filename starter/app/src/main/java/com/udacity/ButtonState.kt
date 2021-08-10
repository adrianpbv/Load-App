package com.udacity


sealed class ButtonState {
    object Done : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}