package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.ChannelReaderFactory
import com.jonolds.bigsorter.v2.DatMapContext
import com.jonolds.bigsorter.v2.miniwriter.MiniWriter
import com.jonolds.bigsorter.v2.pushFromFile
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


interface Sender<T>: Phase {

    var child: Receiver<T>?

    fun useFromBelow(miniWriter: MiniWriter<T>)

}


fun <T: Any, Z: Receiver<T>> Sender<T>.addChild(childToAdd: Z): Z {
    child = childToAdd
    return childToAdd
}





class MultiSourcePhase<P>(
    val mainSource: SourcePhase<P>,
    val otherSources: List<SourcePhase<P>>
) : SourcePhase<P> by mainSource {


    override var tag: String? = null

    override fun useFromBelow(miniWriter: MiniWriter<P>) {
        for (source in otherSources)
            source.push(miniWriter::writeBulk)
        mainSource.push(miniWriter::writeBulk)
        miniWriter.close()
    }

}


interface FileSourcePhase<T>: SourcePhase<T> {

    override val filenames: List<String>
    val channelReaderFactory: ChannelReaderFactory<T>

    override fun push(writeList: (List<T>) -> Unit) {

        val result = ArrayList<T>(defaultConfig.maxItemsPerPart)

        for (file in filenames.map { File(it) }) {
            pushFromFile(file, channelReaderFactory, defaultConfig.bufferSize,
                defaultConfig.maxItemsPerPart, result, writeList)
        }
        if (result.isNotEmpty())
            writeList(result)
    }
}


class FileInputPhase<T>(
    override val filenames: List<String>,
    override val channelReaderFactory: ChannelReaderFactory<T>,
    override val context: DatMapContext,
    override var child: Receiver<T>? = null,
): FileSourcePhase<T> {


    override var tag: String? = null

}

