package com.jonolds.bigsorter.v2.work

import com.jonolds.bigsorter.v2.phase.Sender
import com.jonolds.bigsorter.v2.phase.*
import java.io.File


abstract class Work {

    abstract val readFiles: List<File>

    abstract val writeFiles: List<File>

    abstract fun exec()

    abstract fun taskList(): List<WorkElement<*, *>>

}


class WorkProject(
    val workList: MutableList<Work>
): Work() {


    constructor(sinkPhase2: SinkPhase<*>): this(sinkPhase2.getWorkAbove())


    override val readFiles: List<File> get() = workList.flatMap { it.readFiles }

    override val writeFiles: List<File> get() = workList.flatMap { it.writeFiles }

    override fun exec() {
        for (work in workList)
            work.exec()
    }

    override fun taskList(): List<WorkElement<*, *>> = workList.flatMap { it.taskList() }

}


class WorkElement<SRC, SNK>(
    val source: SourcePhase<SRC>,
    val sink: SinkPhase<SNK>
): Work() {

    override val readFiles: List<File> get() = emptyList()

    override val writeFiles: List<File> get() = emptyList()

    override fun exec() {
        for (work in preWork)
            work.exec()

        source.processSource()

        for (work in postWork)
            work.exec()
    }



    val preWork: List<Work> by lazy {
        sourceDowntoSink()
            .filterIsInstance<PlexInPhase<*>>()
            .flatMap { it.others }
            .map { it.getWorkAbove() }
            .flatten()
    }


    val postWork: List<Work> by lazy {
        sourceDowntoSink()
            .filterIsInstance<PlexOutPhase<*>>()
            .flatMap { it.others }
            .map { it.getWorkBelow() }
            .flatten()
    }


    override fun taskList(): List<WorkElement<*, *>> = preWork.flatMap { it.taskList() } + this + postWork.flatMap { it.taskList() }

}


fun WorkElement<*, *>.sourceDowntoSink(): List<Phase> = generateSequence(source as Phase) {
    when (it) {
        sink -> null
        is Sender<*> -> it.child
        else -> null
    }
}.toList()



fun Phase.getWorkAbove(): MutableList<Work> {
    val boundariesUp = rangeToTop()
        .filterIsInstance<BoundaryPhase2>()

    return (boundariesUp.size-1 downTo 1)
        .map { i -> WorkElement(boundariesUp[i] as SourcePhase<*>, boundariesUp[i-1] as SinkPhase<*>) }
        .toMutableList()
}


fun Phase.getWorkBelow(): MutableList<Work> {
    val boundariesDown = rangeToBottom()
        .filterIsInstance<BoundaryPhase2>()

    return (0 until boundariesDown.size-1)
        .map { i -> WorkElement(boundariesDown[i] as SourcePhase<*>, boundariesDown[i+1] as SinkPhase<*>) }
        .toMutableList()
}