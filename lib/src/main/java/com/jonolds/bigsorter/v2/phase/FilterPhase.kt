package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.v2.miniwriter.MiniWriter
import com.jonolds.bigsorter.v2.miniwriter.filter


abstract class FilterPhaseAbstract<P>: ThruPhase<P, P>() {

    abstract val predicateFactory: () -> ((P)->Boolean)


    override fun wrapChildWriter(childWriter: MiniWriter<P>): MiniWriter<P> =
        childWriter.filter(predicateFactory)

}

class FilterPhase<P>(
    override var parent: Sender<P>?,
    override val predicateFactory: () -> ((P)->Boolean),
): FilterPhaseAbstract<P>()




