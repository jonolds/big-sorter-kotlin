package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.v2.miniwriter.MiniWriter

@Suppress("UNCHECKED_CAST")
class OpContainer<A, D>(
    override var parent: Sender<A>?,
    val first: ThruPhase<A, *>,
    val last: ThruPhase<*, D>
): ThruPhase<A, D>() {

    override val receiverClass: Class<A> = first.receiverClass
    override val senderClass: Class<D> = last.senderClass

    fun <C> append(newLast: ThruPhase<D, C>): OpContainer<A, C> {
        last.setChild(newLast)
        return OpContainer(
            parent = parent,
            first = first,
            last = newLast
        )
    }


    fun <C, E> replaceLast(newLast: ThruPhase<C, E>): OpContainer<A, E> {
        if (last == first)
            return opContainer(
                parent = parent,
                first = newLast as ThruPhase<A, E>
            )


        (last.parent!! as Sender<C>).setChild(newLast)
        return OpContainer(
            parent = parent,
            first = first,
            last = newLast
        )
    }

    override fun wrapChildWriter(childWriter: MiniWriter<D>): MiniWriter<A> {

        var currWriter: MiniWriter<*> = childWriter

        var currPhase: Phase? = last


        fun <P, C> wrapChild(phase: ThruPhase<P, C>, writer: MiniWriter<*>): MiniWriter<P> =
            phase.wrapChildWriter(writer as MiniWriter<C>)

        while (currPhase is ThruPhase<*, *>) {

            currWriter = wrapChild(currPhase, currWriter)
            currPhase = currPhase.parent

        }

        return currWriter as MiniWriter<A>
    }

}


fun <A, B> opContainer(
    parent: Sender<A>?,
    first: ThruPhase<A, B>
): OpContainer<A, B> = OpContainer(
    parent = parent,
    first = first,
    last = first
)