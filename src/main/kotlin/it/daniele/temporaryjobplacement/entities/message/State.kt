package it.daniele.temporaryjobplacement.entities.message

enum class State {
    RECEIVED{
        fun readMessage(): State{
            return READ
        }
    },
    READ{
        fun discardMessage(): State{
            return DISCARTED
        }
        fun processMessage(): State{
            return PROCESSING
        }
        fun completeMessage(): State{
            return DONE
        }
        fun failMessage(): State{
            return FAILED
        }
    },
    DISCARTED,
    PROCESSING{
        fun completeMessage(): State{
            return DONE
        }
        fun failMessage(): State{
            return FAILED
        }
    },
    DONE,
    FAILED

}