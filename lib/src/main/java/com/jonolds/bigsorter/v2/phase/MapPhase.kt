package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.v2.miniwriter.MiniWriter
import com.jonolds.bigsorter.v2.miniwriter.map



abstract class MapPhaseAbstract<P, R>: ThruComponent<P, R>() {


    abstract val mapperFactory: () -> ((P) -> R)


    override fun wrapChildWriter(childWriter: MiniWriter<R>): MiniWriter<P> =
        childWriter.map(receiverClass, mapperFactory())

}

class MapPhase<P, R>(
    override var parent: Sender<P>?,
    override val receiverClass: Class<P>,
    override var senderClass: Class<R>,
    val mapper: (P) -> R,
): MapPhaseAbstract<P, R>() {

    override val mapperFactory: () -> (P) -> R get() = { mapper }
}


class MapContextPhase<P, R>(
    override var parent: Sender<P>?,
    override val receiverClass: Class<P>,
    override var senderClass: Class<R>,
    override val mapperFactory: () -> ((P) -> R)
): MapPhaseAbstract<P, R>()


inline fun <reified P, reified R> mapPhase(
    parent: Sender<P>?,
    crossinline mapper: (P) -> R
): MapPhase<P, R> = MapPhase(
    parent = parent,
    receiverClass = P::class.java,
    senderClass = R::class.java,
    mapper = { mapper(it) }
)

inline fun <reified P, reified R> mapContextPhase(
    parent: Sender<P>?,
    noinline mapperFactory: () -> ((P) -> R)
): MapContextPhase<P, R> = MapContextPhase(
    parent = parent,
    receiverClass = P::class.java,
    senderClass = R::class.java,
    mapperFactory = mapperFactory
)