package com.jonolds.bigsorter.v2

import com.jonolds.bigsorter.ChannelReaderFactory
import com.jonolds.bigsorter.ChannelWriterFactory
import com.jonolds.bigsorter.serializers.ChannelSerializer
import com.jonolds.bigsorter.v2.phase.*
import com.jonolds.bigsorter.v2.work.WorkProject
import java.util.Comparator


/* DatMapContext */

inline fun <reified T> DatMapContext.input(
    channelReaderFactory: ChannelReaderFactory<T>,
    file: String,
    vararg files: String,
): FileInputPhase<T> = FileInputPhase(listOf(file) + files.toList(), channelReaderFactory, T::class.java, this)


inline fun <reified T> DatMapContext.input2(
    firstPhase: Sender<T>,
    vararg phases: Sender<T>
): Sender<T> =
    if (phases.isEmpty()) firstPhase
    else PlexInPhase(firstPhase, phases.toMutableList())



/* Output */

inline fun <reified T> Sender<T>.output(
    filename: String,
    writerFactory: ChannelWriterFactory<T>
): FileOutputPhase<T> =
    FileOutputPhase(filename, writerFactory, this).also{ child = it }




/* Map */

inline fun <reified A, reified B> Sender<A>.map2(
    noinline mapper: (A) -> B
): MapPhase<A, B> =
    MapPhase(this, B::class.java, mapper).also{ child = it }


inline fun <reified A, reified B> Sender<A>.mapWithContext(
    mapperFactory: () -> ((A) -> B)
): MapPhase<A, B> =
    MapPhase(this, B::class.java, mapperFactory()).also{ child = it }


inline fun <reified A, reified B, reified C> MapPhase<A, B>.map2(
    crossinline mapper: (B) -> C
): MapPhase<A, C> =
    MapPhase(parent, C::class.java) { mapper(mapper(it)) }.also { parent!!.child = it }



/* Filter */

inline fun <reified A> Sender<A>.filter2(
    noinline predicateFactory: (A) -> Boolean
): FilterPhase<A> =
    FilterPhase(this) { predicateFactory }.also{ child = it }


inline fun <reified A> Sender<A>.filterWithContext(
    noinline predicateFactory: () -> ((A) -> Boolean)
): FilterPhase<A> =
    FilterPhase(this, predicateFactory).also{ child = it }





/* Reduce */

inline fun <reified P> Sender<P>.reduce(
    noinline comparator: (lastValue: P, value: P) -> Boolean,
    noinline combiningFunction: (lastValue: P, value: P) -> P,
): ReducePhase<P> =
    ReducePhase(this, comparator, combiningFunction).also{ child = it }





/* Plex */

inline fun <reified T> Sender<T>.plexIn(senderFactory: () -> Sender<T>): PlexInPhase<T> =
    PlexInPhase(this, mutableListOf(senderFactory())).also{ child = it }

inline fun <reified T> PlexInPhase<T>.plexIn(senderFactory: (context: DatMapContext) -> Sender<T>): PlexInPhase<T> =
    addOther(senderFactory(context))

inline fun <reified T, reified U> Sender<T>.plexOut(noinline sinkFactory: Sender<T>.() -> SinkPhase<U>): PlexOutPhase<T> {
    val plex = PlexOutPhase(this, mutableListOf())
        .also{ child = it }
    return plex.plexOut2(sinkFactory)

}

inline fun <reified T, reified U> PlexOutPhase<T>.plexOut2(sinkFactory: Sender<T>.() -> SinkPhase<U>): PlexOutPhase<T> {
    sinkFactory(getOtherBuilder())
    return this
}






/* Sort */

inline fun <reified T> Sender<T>.sort(
    serializer: ChannelSerializer<T>,
    comparator: Comparator<in T>,
    outputPath: String? = null,
): SorterPhase<T> =
    SorterPhase(this, serializer, comparator, outputPath, this.context).also { child = it }




/* Control */

fun <T> SinkPhase<T>.execute() {

//    printStrFromTop(0)

    WorkProject(this)
        .exec()
}


internal fun Phase.print(numTabs: Int = 0) {

    println("${"\t".repeat(numTabs)}${javaClass.simpleName}")
    when(this) {
        is PlexOutPhase<*> -> others.forEach { it.printStrDownToSink(numTabs+1) }
        is PlexInPhase<*> -> others.forEach { it.printUpToSource(numTabs+1) }
    }

}

fun <T: Phase> T.printStrFromTop(numTabs: Int = 0): T {

    val phases = sequenceToTop().toList().reversed()

    phases.forEach { it.print(numTabs) }

    println()

    return this
}

internal fun Phase.printStrDownToSink(numTabs: Int = 0) {

    sequenceToBottom().toList().forEach {
        it.print(numTabs)
    }

}


internal fun Phase.printUpToSource(numTabs: Int = 0) {

    sequenceToTop().toList().forEach {
        it.print(numTabs)
    }

}






