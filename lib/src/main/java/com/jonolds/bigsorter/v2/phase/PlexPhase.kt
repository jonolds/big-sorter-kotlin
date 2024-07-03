package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.v2.*
import com.jonolds.bigsorter.v2.miniwriter.MiniWriter
import com.jonolds.bigsorter.v2.miniwriter.MultiInputMiniWriter
import com.jonolds.bigsorter.v2.miniwriter.MultiOutputMiniWriter


class PlexInPhase<Q>(
    override var parent: Sender<Q>?,
    val others: MutableList<Sender<Q>> = ArrayList()
) : ThruPhase<Q, Q>() {

    init {
        others.forEach { it.child = this }
    }


    fun addOther(other: Sender<Q>): PlexInPhase<Q> {
        other.child = this
        others.add(other)
        return this
    }


    override fun wrapChildWriter(childWriter: MiniWriter<Q>): MiniWriter<Q> =
        MultiInputMiniWriter(others, childWriter)

}



class PlexOutPhase<Q>(
    override var parent: Sender<Q>?,
    val others: MutableList<Receiver<Q>> = ArrayList()
): ThruPhase<Q, Q>() {


    init {
        others.forEach { it.parent = this }
    }


    fun addOther(other: Receiver<Q>): PlexOutPhase<Q> {
        other.parent = this
        others.add(other)
        return this
    }


    fun getOtherBuilder(): Sender<Q> = object : Sender<Q> {

        override var child: Receiver<Q>?
            get() = TODO("Not yet implemented")
            set(value) {
                addOther(value!!)
            }

        override fun useFromBelow(miniWriter: MiniWriter<Q>) = TODO("Not yet implemented")

        override val context: DatMapContext get() = this@PlexOutPhase.context

        override var tag: String?
            get() = this@PlexOutPhase.tag
            set(value) { value?.let { this@PlexOutPhase.tag(it) } }

    }


    override fun wrapChildWriter(childWriter: MiniWriter<Q>): MiniWriter<Q> =
        MultiOutputMiniWriter(listOf(childWriter) + others.map { it.getWriter() })

}


