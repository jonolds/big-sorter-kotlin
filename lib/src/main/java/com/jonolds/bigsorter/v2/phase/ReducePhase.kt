package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.v2.miniwriter.MiniWriter


class ReducePhase<P> constructor(
    override var parent: Sender<P>?,
    override val receiverClass: Class<P>,
    val comparator: (lastValue: P, value: P) -> Boolean,
    val combiningFunction: (lastValue: P, value: P) -> P,
): ThruComponent<P, P>() {

    override val senderClass: Class<P> get() = receiverClass

    override fun wrapChildWriter(childWriter: MiniWriter<P>): MiniWriter<P> =
        childWriter.reduce(comparator, combiningFunction)


}