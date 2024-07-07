package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.v2.DatMapContext
import com.jonolds.bigsorter.v2.miniwriter.MiniWriter


abstract class ThruPhase<P, C>: Receiver<P>, Sender<C> {

    override val receiverClass: Class<P> get() = parent!!.senderClass

    override var child: Receiver<C>? = null

    override var tag: String? = null

    override val context: DatMapContext get() = parent!!.context


    abstract fun wrapChildWriter(childWriter: MiniWriter<C>): MiniWriter<P>


    override fun useFromBelow(miniWriter: MiniWriter<C>) =
        parent!!.useFromBelow(wrapChildWriter(miniWriter))


    override fun getWriter(): MiniWriter<P> = wrapChildWriter(child!!.getWriter())

}



