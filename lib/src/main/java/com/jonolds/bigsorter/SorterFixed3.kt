package com.jonolds.bigsorter

import com.jonolds.bigsorter.Util.createOrReplaceWithCount
import com.jonolds.bigsorter.internal.Array2
import com.jonolds.bigsorter.serializers.SerializerBS
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.time.measureTime

@Suppress("UNCHECKED_CAST", "TYPE_MISMATCH_WARNING_FOR_INCORRECT_CAPTURE_APPROXIMATION")
class SorterFixed3<T>(

	val inputs: List<() -> ReaderBS<out T>>? = null,
	val serializer: SerializerBS<T>,
	val output: FileWithElemCount,
	val comparator: Comparator<in T>,
	val maxFilesPerMerge: Int,
	val maxItemsPerPart: Int,
	val log: ((String) -> Unit)?,
	val bufferSize: Int,
	val tempDirectory: File,
	val unique: Boolean,
	val initialSortInParallel: Boolean,
	val outputWriterFactory: Optional<FileWriterFactory<T?>>,
) {

	var count: Long = 0

	fun log(msg: String, vararg objects: Any?) = log?.invoke(String.format(msg, *objects))


	/////////////////////
	// Main sort routine
	/////////////////////



	fun sort(): File {

		tempDirectory.mkdirs()

		count = 0
		val sortedFiles: MutableList<FileWithElemCount>

		val timeFirstSort = measureTime {
			sortedFiles = splitAndSort()
		}
		log("completed initial split and sort, starting merge, elapsed time=$timeFirstSort")

		val timeTotal = timeFirstSort + measureTime {

			// TODO write final merge to final output to avoid possible copying at the end
			// (and apply outputWriterfactory on the fly if present)
			val result = merge(sortedFiles)

			if (outputWriterFactory.isPresent)
				Util.convert(result, serializer, output, outputWriterFactory.get()) { x: T? -> x }
			else
				Files.move(result.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING)
		}


		log("sort of $count records completed in $timeTotal")
		return output
	}


	private fun splitAndSort(): MutableList<FileWithElemCount> {

		val sortedFiles: MutableList<FileWithElemCount> = ArrayList(500)
		log("starting sort")
		log("unique = $unique")



		val list = Array2(serializer.makeArray(maxItemsPerPart))

		var currentCount = 0

		for (supplier in inputs!!) {
			supplier().use { reader ->

				reader as ReaderBS<T>

				reader.readBulkArray(maxItemsPerPart-currentCount, list)


				while (list.size-currentCount > 0) {
					currentCount = list.size
					if (currentCount == maxItemsPerPart) {
						sortedFiles.add(sortAndWriteToFile(list))
						list.clear()
						currentCount = 0
					}
					reader.readBulkArray(maxItemsPerPart-currentCount, list)
				}


			}
		}
		if (!list.isEmpty())
			sortedFiles.add(sortAndWriteToFile(list))

		return sortedFiles
	}




	private fun merge(files: List<FileWithElemCount>): FileWithElemCount{
		// merge the files in chunks repeatedly until only one remains
		// TODO make a better guess at the chunk size so groups are more even

		try {
			var currentFiles = files
			while (currentFiles.size > 1) {

				val nextRound: MutableList<FileWithElemCount> = ArrayList(currentFiles.size/2)

				for (subList in currentFiles.chunked(maxFilesPerMerge))
					nextRound.add(mergeGroup(subList))

				currentFiles = nextRound
			}

			return currentFiles.firstOrNull() ?: output.createOrReplaceWithCount()
		} catch (e: IOException) {
			throw UncheckedIOException(e)
		}
	}


	private fun mergeGroup(files: List<FileWithElemCount>): FileWithElemCount {

		log("merging %s files", files.size)

		if (files.size == 1)
			return files[0]

		val states: List<FileState<T>> = files.map { FileState(it, serializer.createFileReader(it, bufferSize)) }

		val output = Util.nextTempFileWithElemCount(tempDirectory)

		val totalElems = files.sumOf { it.elemCount }
		var count = 0

		serializer.createFileWriter(output, bufferSize).use { writer ->

			val q = PriorityQueue<FileState<T>> { x, y -> comparator.compare(x.currentValue, y.currentValue) }

			q.addAll(states)

			var last: T? = null

			for (i in 0 until totalElems) {
				val state = q.poll()
				if (!unique || last == null || comparator.compare(state.currentValue, last) != 0) {
					writer.write(state.currentValue)
					last = state.currentValue
					count++
				}
				state.currentValue = state.reader.read()

				if (state.currentValue != null)
					q.offer(state)
				else {
					state.reader.close()
					state.delete() // delete intermediate files
				}
			}
		}

		output.elemCount = count
		return output
	}


	private fun sortAndWriteToFile(list: Array2<T>): FileWithElemCount {
		val file = Util.nextTempFileWithElemCount(tempDirectory, "-partA", list.size)

		val time = measureTime {

			if (initialSortInParallel)
				Arrays.parallelSort(list.arr, 0, list.size, comparator)
			else
				Arrays.sort(list.arr, 0, list.size, comparator)

			writeToFileBulk(list, file)

			count += list.size.toLong()
		}

		log("total=%s, sorted %s records to file %s in %ss", count, list.size, file.name, time)
		return file
	}


	private fun writeToFileBulk(list: Array2<T>, f: FileWithElemCount) = serializer.createFileWriter(f, bufferSize).use { writer ->

		if (list.isEmpty())
			return@use

		if (!unique) {
			writer.writeBulkArray(list)
			return@use
		}

		var start = 0
		var count = 0
		for (i in 1 until list.size) {

			if (comparator.compare(list.arr[i], list.arr[i-1]) == 0) {
				if (start < i) {
					writer.writeBulkArray(list, start, i)
					count+=(i-start)
				}
				start = i+1
			}

		}
		if (start < list.size)
			writer.writeBulkArray(list, start, list.size)

		count+=list.size-start
		f.elemCount = count
		writer.flush()
	}


}