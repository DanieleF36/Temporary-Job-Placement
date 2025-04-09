package it.daniele.temporaryjobplacement.entities.message

enum class State {
    RECEIVED,
    READ,
    DISCARTED,
    PROCESSING,
    DONE,
    FAILED;

    fun checkNewState(newState: State): Boolean{
        return when(this){
            RECEIVED -> newState == READ
            READ -> newState == DONE || newState == DISCARTED || newState == PROCESSING || newState == FAILED
            DISCARTED -> false
            PROCESSING -> newState == DONE || newState == FAILED
            DONE -> false
            FAILED -> false
        }
    }
}