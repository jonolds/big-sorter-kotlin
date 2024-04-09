@file:Suppress("DuplicatedCode")

package com.jonolds.bigsorter


import com.jonolds.bigsorter.Util.convert
import com.jonolds.bigsorter.Util.toInStream
import com.jonolds.bigsorter.builders.Builder
import com.jonolds.bigsorter.builders.Builder2
import com.jonolds.bigsorter.internal.FastList
import com.jonolds.bigsorter.serializers.SerializerBS
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.DecimalFormat
import java.util.*


/*  NotThreadSafe
The class is not considered thread safe because calling the sort() method on the same Sorter object simultaneously from
different threads could break things. Admittedly that would be a pretty strange thing to do! In short, create a new Sorter
and sort() in one thread, don't seek to reuse the same Sorter object. */
class Sorter<T>(
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
	val outputWriterFactory: Optional<WriterFactory<T?>>,
) {

	var count: Long = 0

	fun log(msg: String, vararg objects: Any?) = log?.invoke(String.format(msg, *objects))


	/////////////////////
	// Main sort routine
	/////////////////////
	fun sort(): File {

		tempDirectory.mkdirs()

		val time = System.currentTimeMillis()
		count = 0
		val sortedFiles: FastList<File> = splitAndSort(time)

		// TODO write final merge to final output to avoid possible copying at the end
		// (and apply outputWriterfactory on the fly if present)
		val result = merge(sortedFiles)

		if (outputWriterFactory.isPresent)
			convert(result, serializer, output, outputWriterFactory.get()) { x: T? -> x }
		else
			Files.move(result.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING)

		log("sort of " + count + " records completed in " + (System.currentTimeMillis() - time) / 1000.0 + "s")
		return output
	}

	fun splitAndSort(time: Long): FastList<File> {

		val sortedFiles: FastList<File> = FastList()
		log("starting sort")
		log("unique = $unique")

		var i = 0
		val list = FastList<T>()


		for (supplier in inputs!!) {
			supplier().use { reader ->

				while (true) {
					val t = reader.read()
					if (t != null) {
						list.add(t)
						i++
					}
					if (t == null || i == maxItemsPerPart) {
						i = 0
						if (!list.isEmpty()) {
							val f = sortAndWriteToFile(list)
							sortedFiles.add(f)
							list.clear()
						}
					}
					if (t == null)
						break
				}
			}
		}
		log("completed initial split and sort, starting merge, elapsed time=" + (System.currentTimeMillis() - time) / 1000.0 + "s")
		return sortedFiles
	}



	fun merge(files: FastList<File>): File {
		// merge the files in chunks repeatedly until only one remains
		// TODO make a better guess at the chunk size so groups are more even
		var currentFiles = files
		try {

			while (currentFiles.size > 1) {

				val nextRound = FastList<File>()
				var i = 0

				while (i < currentFiles.size) {
					val merged = mergeGroup(currentFiles.subList(i, minOf(currentFiles.size, i + maxFilesPerMerge)))
					nextRound.add(merged)
					i += maxFilesPerMerge
				}

				currentFiles = nextRound
			}

			val result: File

			if (currentFiles.isEmpty()) {
				output.delete()
				output.createNewFile()
				result = output
			}
			else
				result = currentFiles[0]

			return result
		} catch (e: IOException) {
			throw UncheckedIOException(e)
		}
	}

	fun mergeGroup(list: List<File>): File {
		log("merging %s files", list.size)
		if (list.size == 1)
			return list[0]
		val states: MutableList<FileState<T>> = FastList()
		for (f in list) {
			val st = createState(f)
			// note that st.value will be present otherwise the file would be empty
			// and an empty file would not be passed to this method
			states.add(st)
		}

		val output = Util.nextTempFile(tempDirectory)

		serializer.createFileWriter(output, bufferSize).use { writer ->

			val q = PriorityQueue<FileState<T>> { x, y -> comparator.compare(x.currentValue, y.currentValue) }

			q.addAll(states)

			var last: T? = null

			while (!q.isEmpty()) {
				val state = q.poll()
				if (!unique || last == null || comparator.compare(state.currentValue, last) != 0) {
					writer.write(state.currentValue)
					last = state.currentValue
				}
				state.currentValue = state.reader.readAutoClosing()

				if (state.currentValue != null)
					q.offer(state)
				else
					state.delete() // delete intermediate files
			}
		}
		return output
	}

	fun createState(file: File): FileState<T> {
		val reader = serializer.createFileReader(file, bufferSize)
		val t = reader.readAutoClosing()
		return FileState(file.path, reader, t)
	}

	fun sortAndWriteToFile(list: FastList<T>): File {
		val file = Util.nextTempFile(tempDirectory)
		val t = System.currentTimeMillis()

		if (initialSortInParallel)
			list.parallelSort(comparator)
		else
			list.sortWith(comparator)

		writeToFile(list, file)

		count += list.size.toLong()
		log(
			"total=%s, sorted %s records to file %s in %ss",
			count, list.size, file.name, DecimalFormat("0.000").format((System.currentTimeMillis() - t) / 1000.0)
		)
		return file
	}

	fun writeToFile(list: List<T>, f: File) = serializer.createFileWriter(f, bufferSize).use { writer ->
		var last: T? = null

		for (t in list) {
			if (!unique || last == null || comparator.compare(t, last) != 0) {
				writer.write(t)
				last = t
			}
		}
	}

	companion object {

		fun <T> serializer(serializer: SerializerBS<T>): Builder<T> = Builder(serializer)

		fun serializerLinesUtf8(): Builder<String> = serializer(SerializerBS.linesUtf8())

		fun serializerLines(charset: Charset?): Builder<String> = serializer(SerializerBS.lines(charset!!))

		fun lines(charset: Charset?): Builder2<String> =
			serializer(SerializerBS.lines(charset!!)).comparator(Comparator.naturalOrder<String>())

		fun linesUtf8(): Builder2<String> =
			serializer(SerializerBS.linesUtf8()).comparator(Comparator.naturalOrder<String>())

	}


}