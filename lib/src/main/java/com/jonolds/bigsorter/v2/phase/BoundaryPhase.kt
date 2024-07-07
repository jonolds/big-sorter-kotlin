package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.v2.miniwriter.MiniWriter


interface BoundaryPhase2

interface SourcePhase<T>: Sender<T>, BoundaryPhase2 {

    val filenames: List<String>?

    fun push(writeList: (List<T>) -> Unit)

    override fun useFromBelow(miniWriter: MiniWriter<T>) {
        push(miniWriter::writeBulk)
        miniWriter.close()
    }

    fun processSource() {
        val writer = child?.getWriter() ?: return
        useFromBelow(writer)
    }

}

interface SinkPhase<T>: Receiver<T>, BoundaryPhase2 {

    val filenames: List<String>

    fun processSink() {
        parent!!.useFromBelow(getWriter())
    }

}

interface DualBoundaryPhase<P, C>:  SinkPhase<P>, SourcePhase<C> {

}



