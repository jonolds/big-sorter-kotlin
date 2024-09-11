package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.*
import com.jonolds.bigsorter.Util.createOrReplace
import com.jonolds.bigsorter.serializers.ChannelSerializer
import com.jonolds.bigsorter.v2.DatMapContext
import com.jonolds.bigsorter.v2.fastlist.FastArrayList
import com.jonolds.bigsorter.v2.miniwriter.MiniWriter
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.measureTime


class SorterPhase<T> constructor(
    override var parent: Sender<T>?,
    val serializer: ChannelSerializer<T>,
    val comparator: Comparator<in T>,
    var namedOutputPath: String? = null,
    inputContext: DatMapContext? = null,
) : DualBoundaryPhase<T, T>, FileSourcePhase<T> {


    override val receiverClass: Class<T> = serializer.clazz

    override val senderClass: Class<T> get() = receiverClass

    override var child: Receiver<T>? = null

    override var tag: String? = null

    var unique: Boolean = true
    var initialSortInParallel: Boolean = true



    override val context: DatMapContext by lazy { inputContext ?: parent!!.context }

    fun log(msg: String, vararg objects: Any?) = context::log

    private val output: File by lazy { namedOutputPath?.let {
        File(it).createOrReplace() } ?: context.nextTempFile()
    }

    override val filenames: List<String> by lazy { listOf(output.path) }


    override fun getWriter(): MiniWriter<T> = object : MiniWriter<T>() {

        override val receiverClass: Class<T> = this@SorterPhase.receiverClass


        val sortedFiles: MutableList<FileWithElemCount> = ArrayList(500)

        init {

            log("starting sort")
            log("unique = $unique")

            defaultConfig.tempDirectory.mkdirs()

        }


        override fun writeBulk(list: List<T>) {
            if (list is FastArrayList<T>)
                sortedFiles.add(sortAndWriteToFile(list))
            else
                sortedFiles.add(sortAndWriteToFile(FastArrayList(list, receiverClass)))
        }

        override fun close() {

            val time = measureTime {

                val result = merge(sortedFiles)

                Files.move(result.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }


        }


    }


    var count = 0L

    private fun merge(files: List<FileWithElemCount>): File {
        // merge the files in chunks repeatedly until only one remains
        // TODO make a better guess at the chunk size so groups are more even

        try {
            var currentFiles = files
            while (currentFiles.size > 1) {

                val nextRound: MutableList<FileWithElemCount> = ArrayList(currentFiles.size/2)

                for (subList in currentFiles.chunked(defaultConfig.maxFilesPerMerge))
                    nextRound.add(mergeGroup(subList))

                currentFiles = nextRound
            }

            return currentFiles.firstOrNull() ?: output.createOrReplace()
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }


    private fun mergeGroup(files: List<FileWithElemCount>): FileWithElemCount {

        log("merging %s files", files.size)

        if (files.size == 1)
            return files[0]

        val states: List<FileState<T>> = files.mapIndexed { i, file -> FileState(file, serializer.createFileReader(file, defaultConfig.bufferSize), i) }

        val output = Util.nextTempFileWithElemCount(defaultConfig.tempDirectory)

        var totalElems = files.sumOf { it.elemCount }
        var count = 0


        serializer.createFileWriter(output, defaultConfig.bufferSize).use { writer ->

            val q = PriorityQueue<FileState<T>> { x, y -> comparator.compare(x.currentValue, y.currentValue) }

            q.addAll(states)

            var last: T? = null


            while (totalElems > 0) {

                val numToDo = minOf(totalElems, defaultConfig.maxItemsPerPart)

                val result = FastArrayList(numToDo, receiverClass)


                for (i in 0 until numToDo) {
                    val state = q.poll()
                    if (!unique || last == null || comparator.compare(state.currentValue, last) != 0) {
                        result.add(state.currentValue!!)
                        last = state.currentValue
                    }
                    state.currentValue = state.reader.read()

                    if (state.currentValue != null)
                        q.offer(state)
                    else {
                        state.reader.close()
                    }

                }

                writer.writeBulk(result)

                count+=result.size
                totalElems-=numToDo

            }
        }

//        serializer.createFileWriter(output, defaultConfig.bufferSize).use { writer ->
//
//            val q = PriorityQueue<FileState<T>> { x, y -> comparator.compare(x.currentValue, y.currentValue) }
//
//            q.addAll(states)
//
//            var last: T? = null
//
//            for (i in 0 until totalElems) {
//                val state = q.poll()
//                if (!unique || last == null || comparator.compare(state.currentValue, last) != 0) {
//                    writer.write(state.currentValue)
//                    last = state.currentValue
//                    count++
//                }
//                state.currentValue = state.reader.read()
//
//                if (state.currentValue != null)
//                    q.offer(state)
//                else {
//                    state.reader.close()
//                }
//            }
//        }

//		println("totalElems=$totalElems  count=$count")
        output.elemCount = count
        return output
    }

    private fun sortAndWriteToFile(list: FastArrayList<T>): FileWithElemCount {
        val file = context.nextTempFileWithElemCount(defaultConfig.tempDirectory, "-partA", list.size)

        val time = measureTime {

            //TODO Fix parallel sort
            val sorted = if (initialSortInParallel) list.parallelSort(comparator)
                else list.sortedWith(comparator)

            writeToFileBulk(sorted, file)

            count += list.size.toLong()
        }

        log("total=%s, sorted %s records to file %s in %ss", count, list.size, file.name, time)
        return file
    }

    private fun writeToFileBulk(list: List<T>, fileWithCount: FileWithElemCount) = serializer.createFileWriter(fileWithCount, defaultConfig.bufferSize).use { writer ->

        if (list.isEmpty())
            return@use

        if (!unique) {
            writer.writeBulk(list)
            return@use
        }

        var start = 0
        var count = 0
        for (i in 1 until list.size) {

            if (comparator.compare(list[i], list[i-1]) == 0) {
                if (start < i) {
                    writer.writeBulk(list, start, i)
                    count+=(i-start)
                }
                start = i+1
            }

        }
        if (start < list.size)
            writer.writeBulk(list, start, list.size)

        count+=list.size-start
        fileWithCount.elemCount = count
        writer.flush()
    }


    override val channelReaderFactory: ChannelReaderFactory<T> get() = serializer

}


fun <T> SorterPhase<T>.unique(value: Boolean): SorterPhase<T> {
    unique = value
    return this
}

fun <T> SorterPhase<T>.initialSortInParallel(value: Boolean): SorterPhase<T> {
    initialSortInParallel = value
    return this
}