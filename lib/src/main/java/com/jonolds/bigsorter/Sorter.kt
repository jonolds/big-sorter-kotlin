package com.jonolds.bigsorter


import com.jonolds.bigsorter.Util.convert
import com.jonolds.bigsorter.Util.toInStream
import com.jonolds.bigsorter.builders.Builder
import com.jonolds.bigsorter.builders.Builder2
import com.jonolds.bigsorter.internal.FastList
import com.jonolds.bigsorter.serializers.SerializerBS
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.DecimalFormat
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

/*  NotThreadSafe
The class is not considered thread safe because calling the sort() method on the same Sorter object simultaneously from
different threads could break things. Admittedly that would be a pretty strange thing to do! In short, create a new Sorter
and sort() in one thread, don't seek to reuse the same Sorter object. */
class Sorter<T> constructor(
	val inputs: List<Supplier<out ReaderBS<out T>>>,
	val serializer: SerializerBS<T>,
	val output: File,
	val comparator: Comparator<in T>,
	val maxFilesPerMerge: Int,
	val maxItemsPerPart: Int,
	val log: Consumer<in String>?,
	val bufferSize: Int,
	val tempDirectory: File,
	val unique: Boolean,
	val initialSortInParallel: Boolean,
	val outputWriterFactory: Optional<OutputStreamWriterFactory<T?>>
) {

	var count: Long = 0

	fun log(msg: String, vararg objects: Any?) = log?.accept(String.format(msg, *objects))


	/////////////////////
	// Main sort routine
	/////////////////////
	fun sort(): File {

		tempDirectory.mkdirs()

		val time = System.currentTimeMillis()
		count = 0
		val sortedFiles: MutableList<File> = splitAndSort(time)

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

	fun splitAndSort(time: Long): MutableList<File> {

		val sortedFiles: MutableList<File> = FastList()
		log("starting sort")
		log("unique = $unique")

		var i = 0
		val list = FastList<T>()


		for (supplier in inputs) {
			supplier.get().use { reader ->

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



	fun sortBulk(): File {

		tempDirectory.mkdirs()

		val time = System.currentTimeMillis()
		count = 0
		val sortedFiles: MutableList<File> = splitAndSortBulk(time)

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

	fun splitAndSortBulk(time: Long): MutableList<File> {

		val sortedFiles: MutableList<File> = FastList()
		log("starting sort")
		log("unique = $unique")

		var i = 0
		val list = FastList<T>()


		for (supplier in inputs) {
			supplier.get().use { reader ->

				var t = reader.read()
				while (t != null) {
					list.add(t)
					if (i++ == maxItemsPerPart) {
						sortedFiles.add(sortAndWriteToFile(list))
						list.clear()
						i = 0
					}
					t = reader.read()
				}
			}
		}
		if (!list.isEmpty())
			sortedFiles.add(sortAndWriteToFile(list))

		log("completed initial split and sort, starting merge, elapsed time=" + (System.currentTimeMillis() - time) / 1000.0 + "s")
		return sortedFiles
	}




	fun merge(files: List<File>): File {
		// merge the files in chunks repeatedly until only one remains
		// TODO make a better guess at the chunk size so groups are more even
		var currentFiles = files
		try {

			while (currentFiles.size > 1) {

				val nextRound: MutableList<File> = FastList()
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
		val states: MutableList<State<T>> = FastList()
		for (f in list) {
			val st = createState(f)
			// note that st.value will be present otherwise the file would be empty
			// and an empty file would not be passed to this method
			states.add(st)
		}

		val output = Util.nextTempFile(tempDirectory)

		BufferedOutputStream(FileOutputStream(output), bufferSize).use { out ->

			serializer.createWriter(out).use { writer ->

				val q = PriorityQueue<State<T>> { x, y -> comparator.compare(x.value, y.value) }

				q.addAll(states)

				var last: T? = null

				while (!q.isEmpty()) {
					val state = q.poll()
					if (!unique || last == null || comparator.compare(state.value, last) != 0) {
						writer.write(state.value)
						last = state.value
					}
					state.value = state.reader.readAutoClosing()

					if (state.value != null)
						q.offer(state)
					else
						state.file.delete() // delete intermediate files
				}
			}
		}
		return output
	}

	fun createState(file: File): State<T> {
		val inStr = file.toInStream(bufferSize)
		val reader = serializer.createReader(inStr)
		val t = reader.readAutoClosing()
		return State(file, reader, t)
	}

	fun sortAndWriteToFile(list: FastList<T>): File {
		val file = Util.nextTempFile(tempDirectory)
		val t = System.currentTimeMillis()

		if (initialSortInParallel) list.parallelSort(comparator)
		else list.sortWith(comparator)

		writeToFile(list, file)

		count += list.size.toLong()
		log(
			"total=%s, sorted %s records to file %s in %ss",
			count, list.size, file.name, DecimalFormat("0.000").format((System.currentTimeMillis() - t) / 1000.0)
		)
		return file
	}

	fun writeToFile(list: List<T>, f: File) {

		BufferedOutputStream(FileOutputStream(f), bufferSize).use { out ->
			serializer.createWriter(out).use { writer ->
				var last: T? = null

				for (t in list) {
					if (!unique || last == null || comparator.compare(t, last) != 0) {
						writer.write(t)
						last = t
					}
				}
			}
		}

	}

	companion object {

		fun <T> serializer(serializer: SerializerBS<T>): Builder<T> = Builder(serializer)

		fun <T> serializerLinesUtf8(): Builder<String> = serializer(SerializerBS.linesUtf8())

		fun <T> serializerLines(charset: Charset?): Builder<String> = serializer(SerializerBS.lines(charset!!))

		fun <T> lines(charset: Charset?): Builder2<String> =
			serializer(SerializerBS.lines(charset!!)).comparator(Comparator.naturalOrder<String>())

		fun <T> linesUtf8(): Builder2<String> =
			serializer(SerializerBS.linesUtf8()).comparator(Comparator.naturalOrder<String>())

	}


}