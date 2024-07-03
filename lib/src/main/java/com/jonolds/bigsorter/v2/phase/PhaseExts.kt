package com.jonolds.bigsorter.v2.phase


fun Phase.sequenceToTop(): Sequence<Phase> = generateSequence(this) {
    if (it is Receiver<*>)
        it.parent
    else null
}

fun Phase.rangeToTop(): List<Phase> = sequenceToTop().toList()

fun Phase.greatestParent(): Phase = sequenceToTop().last()


fun Phase.sequenceToBottom(): Sequence<Phase> = generateSequence(this) {
    if (it is Sender<*>) it.child
    else null
}

fun Phase.rangeToBottom(): List<Phase> = sequenceToBottom().toList()

fun Phase.leastChild(): Phase = sequenceToBottom().last()



