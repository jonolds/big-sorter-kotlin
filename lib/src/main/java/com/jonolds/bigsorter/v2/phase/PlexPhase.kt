package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.v2.*
import com.jonolds.bigsorter.v2.miniwriter.MiniWriter
import com.jonolds.bigsorter.v2.miniwriter.MultiInputMiniWriter
import com.jonolds.bigsorter.v2.miniwriter.MultiOutputMiniWriter


class PlexInPhase<Q>(
    override var parent: Sender<Q>?,
    override val receiverClass: Class<Q>,
    val others: MutableList<Sender<Q>> = ArrayList()
) : ThruComponent<Q, Q>() {

    override val senderClass: Class<Q> = receiverClass

    init {
        others.forEach { it.child = this }
    }


    fun addOther(other: Sender<Q>): PlexInPhase<Q> {
        others.add(other)
        other.child = this
        return this
    }


    override fun wrapChildWriter(childWriter: MiniWriter<Q>): MiniWriter<Q> =
        MultiInputMiniWriter(others, childWriter, senderClass)

}



class PlexOutPhase<Q>(
    override var parent: Sender<Q>?,
    override val receiverClass: Class<Q>,
    val others: MutableList<Receiver<Q>> = ArrayList()
): ThruComponent<Q, Q>() {

    override val senderClass: Class<Q> get() = receiverClass

    init {
        others.forEach { it.parent = this }
    }


    override fun wrapChildWriter(childWriter: MiniWriter<Q>): MiniWriter<Q> =
        MultiOutputMiniWriter(listOf(childWriter) + others.map { it.getWriter() }, senderClass)

}


class PlexOutHandle<Q>(
    private val plexPhase: PlexOutPhase<Q>
) : Sender<Q>, Receiver<Q> {

    override var parent: Sender<Q>? = plexPhase

    override val senderClass: Class<Q> = parent!!.senderClass

    override val receiverClass: Class<Q> = parent!!.senderClass

    override var child: Receiver<Q>? = null

    override val context: DatMapContext = parent!!.context

    override var tag: String? = null


    fun mergeParentChild() {
        plexPhase.others.add(child!!)
        child!!.parent = plexPhase
    }


    override fun getWriter(): MiniWriter<Q> = TODO("Not yet implemented")
    override fun useFromBelow(miniWriter: MiniWriter<Q>) = TODO("Not yet implemented")

}


