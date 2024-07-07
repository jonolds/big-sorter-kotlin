package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.v2.miniwriter.MiniWriter
import com.jonolds.bigsorter.v2.miniwriter.map


class MapPhase<P, R>(
    override var parent: Sender<P>?,
    override val senderClass: Class<R>,
    val mapper: (P) -> R,
): ThruPhase<P, R>() {

    override fun wrapChildWriter(childWriter: MiniWriter<R>): MiniWriter<P> =
        childWriter.map(receiverClass, mapper)

}