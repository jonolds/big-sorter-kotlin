package com.jonolds.bigsorter.v2.miniwriter

import com.jonolds.bigsorter.v2.phase.Sender
import java.util.*

class MultiInputMiniWriter<T> constructor(
    extras: List<Sender<T>>,
    val writer: MiniWriter<T>,
    override val receiverClass: Class<T>
) : MiniWriter<T>() {

    val stack = Stack<Sender<T>>()
        .also { it.addAll(extras) }

    override fun writeBulk(list: List<T>) = writer.writeBulk(list)

    override fun close() {
        if (stack.isNotEmpty())
            return stack.pop().useFromBelow(this)
        writer.close()
    }

}