package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.v2.miniwriter.MiniWriter


class ReducePhase<P> (
    override var parent: Sender<P>?,
    val comparator: (lastValue: P, value: P) -> Boolean,
    val combiningFunction: (lastValue: P, value: P) -> P,
): ThruPhase<P, P>() {


    override fun wrapChildWriter(childWriter: MiniWriter<P>): MiniWriter<P> =
        childWriter.reduce(comparator, combiningFunction)


}