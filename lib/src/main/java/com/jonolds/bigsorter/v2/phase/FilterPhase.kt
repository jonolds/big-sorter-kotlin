package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.v2.miniwriter.MiniWriter
import com.jonolds.bigsorter.v2.miniwriter.filter


abstract class FilterPhaseAbstract<P>: ThruComponent<P, P>() {

    abstract val predicateFactory: () -> ((P)->Boolean)


    override val senderClass: Class<P> get() = receiverClass

    override fun wrapChildWriter(childWriter: MiniWriter<P>): MiniWriter<P> =
        childWriter.filter(predicateFactory)

}

class FilterPhase<P> constructor(
    override var parent: Sender<P>?,
    override val receiverClass: Class<P>,
    override val predicateFactory: () -> ((P)->Boolean),
): FilterPhaseAbstract<P>()


inline fun <reified P> filterPhase(
    parent: Sender<P>?,
    noinline predicateFactory: () -> ((P)->Boolean),
): FilterPhase<P> = FilterPhase(parent, P::class.java, predicateFactory)



