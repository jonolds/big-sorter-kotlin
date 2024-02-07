package com.jonolds.bigsorter

import com.github.davidmoten.guavamini.annotations.VisibleForTesting
import com.jonolds.bigsorter.Util.convert
import com.jonolds.bigsorter.internal.ArrayList
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.DecimalFormat
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.stream.Collectors
import kotlin.math.min

// is java.util.ArrayList but with an extra parallelSort method that is more memory efficient
// that can be achieved outside the class
// NotThreadSafe
// The class is not considered thread safe because calling the sort() method on the same Sorter object simultaneously from
// different threads could break things. Admittedly that would be a pretty strange thing to do! In short, create a new Sorter
// and sort() in one thread, don't seek to reuse the same Sorter object.

@Suppress("UNCHECKED_CAST", "unused")
class Sorter<T>(
	val inputs: List<Supplier<out ReaderBS<out T>>>,
	val serializer: SerializerBS<T>,
	val output: File,
	val comparator: Comparator<in T>,
	val maxFilesPerMerge: Int,
	maxItemsPerFile: Int,
	val log: Consumer<in String>?,
	val bufferSize: Int,
	tempDirectory: File,
	val unique: Boolean,
	val initialSortInParallel: Boolean,
	val outputWriterFactory: Optional<OutputStreamWriterFactory<T?>>
) {

	init {
		Sorter.tempDirectory = tempDirectory
	}

	val maxItemsPerPart: Int = maxItemsPerFile
	var count: Long = 0

	fun log(msg: String, vararg objects: Any?) {
		if (log != null) {
			val s = String.format(msg, *objects)
			log.accept(s)
		}
	}


	/////////////////////
	// Main sort routine
	/////////////////////
	@Throws(IOException::class)
	fun sort(): File {
		tempDirectory.mkdirs()

		// read the input into sorted small files
		val time = System.currentTimeMillis()
		count = 0
		val files: MutableList<File> = ArrayList()
		log("starting sort")
		log("unique = $unique")

		var i = 0
		val list = ArrayList<T>()
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
							files.add(f)
							list.clear()
						}
					}
					if (t == null) {
						break
					}
				}
			}
		}
		log("completed initial split and sort, starting merge, elapsed time=" + (System.currentTimeMillis() - time) / 1000.0 + "s")

		// TODO write final merge to final output to avoid possible copying at the end
		// (and apply outputWriterfactory on the fly if present)
		val result = merge(files)
		if (outputWriterFactory.isPresent) {
			convert(
				result, serializer, output, outputWriterFactory.get()
			) { x: T? -> x }
		} else {
			Files.move(
				result.toPath(),
				output.toPath(),
				StandardCopyOption.REPLACE_EXISTING
			)
		}
		log("sort of " + count + " records completed in " + (System.currentTimeMillis() - time) / 1000.0 + "s")
		return output
	}

	@VisibleForTesting
	fun merge(files: List<File>): File {
		// merge the files in chunks repeatedly until only one remains
		// TODO make a better guess at the chunk size so groups are more even
		var currentFiles = files
		try {
			while (currentFiles.size > 1) {
				val nextRound: MutableList<File> = ArrayList()
				var i = 0
				while (i < currentFiles.size) {
					val merged = mergeGroup(
						currentFiles.subList(i, min(currentFiles.size.toDouble(), (i + maxFilesPerMerge).toDouble()).toInt())
					)
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
			} else {
				result = currentFiles[0]
			}
			return result
		} catch (e: IOException) {
			throw UncheckedIOException(e)
		}
	}

	@Throws(IOException::class)
	fun mergeGroup(list: List<File>): File {
		log("merging %s files", list.size)
		if (list.size == 1) {
			return list[0]
		}
		val states: MutableList<State<T>> = ArrayList()
		for (f in list) {
			val st = createState(f)
			// note that st.value will be present otherwise the file would be empty
			// and an empty file would not be passed to this method
			states.add(st)
		}
		val output = nextTempFile()
		BufferedOutputStream(FileOutputStream(output), bufferSize).use { out ->
			serializer.createWriter(out).use { writer ->
				val q = PriorityQueue { x: State<T>, y: State<T> ->
						comparator.compare(
							x.value,
							y.value
						)
					}
				q.addAll(states)
				var last: T? = null
				while (!q.isEmpty()) {
					val state = q.poll()
					if (!unique || last == null || comparator.compare(state.value, last) != 0) {
						writer.write(state.value)
						last = state.value
					}
					state.value = state.reader.readAutoClosing()
					if (state.value != null) {
						q.offer(state)
					} else {
						// delete intermediate files
						state.file.delete()
					}
				}
			}
		}
		return output
	}

	@Throws(IOException::class)
	fun createState(f: File): State<T> {
		val inStr = openFile(f, bufferSize)
		val reader = serializer.createReader(inStr)
		val t = reader.readAutoClosing()!!
		return State(f, reader, t)
	}

	class State<T>(val file: File, var reader: ReaderBS<T>, var value: T?)

	@Throws(IOException::class)
	fun sortAndWriteToFile(list: ArrayList<T>): File {
		val file = nextTempFile()
		val t = System.currentTimeMillis()
		if (initialSortInParallel) {
			list.parallelSort(comparator)
		} else {
			list.sortWith(comparator)
		}
		writeToFile(list, file)
		val df = DecimalFormat("0.000")
		count += list.size.toLong()
		log(
			"total=%s, sorted %s records to file %s in %ss",
			count,
			list.size,
			file.name,
			df.format((System.currentTimeMillis() - t) / 1000.0)
		)
		return file
	}

	@Throws(IOException::class)
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

		lateinit var tempDirectory: File

		fun <T> serializer(serializer: SerializerBS<T>): Builder<T> {
			return Builder(serializer)
		}

		fun <T> serializerLinesUtf8(): Builder<String> {
			return serializer(SerializerBS.linesUtf8())
		}

		fun <T> serializerLines(charset: Charset?): Builder<String> {
			return serializer(SerializerBS.lines(charset!!))
		}

		fun <T> lines(charset: Charset?): Builder2<String> {
			return serializer(SerializerBS.lines(charset!!)).comparator(Comparator.naturalOrder<String>())
		}

		fun <T> linesUtf8(): Builder2<String> {
			return serializer(SerializerBS.linesUtf8()).comparator(Comparator.naturalOrder<String>())
		}


		fun <T> inputs(b: Builder<T>): List<Supplier<out ReaderBS<out T>>> {
			return b.inputs
				.stream()
				.map { source: Source ->
					if (source.type == SourceType.SUPPLIER_INPUT_STREAM) {
						return@map Supplier<ReaderBS<out T>> {
							b.transform
								.apply(inputStreamReader<T>(b, source))
						}
					} else { // Supplier of a Reader
						return@map Supplier<ReaderBS<out T>> {
							b.transform
								.apply((source.source as Supplier<ReaderBS<T>>).get())
						}
					}
				}.collect(Collectors.toList())
		}

		fun <T> inputStreamReader(b: Builder<T>, source: Source): ReaderBS<T> {
			val rf = b.inputReaderFactory.orElse(b.serializer)
			return rf.createReader((source.source as Supplier<out InputStream>).get())
		}

		@Throws(FileNotFoundException::class)
		fun openFile(file: File, bufferSize: Int): InputStream {
			return BufferedInputStream(FileInputStream(file), bufferSize)
		}

		@JvmOverloads
		@Throws(IOException::class)
		fun nextTempFile(tempDirectory: File = this.tempDirectory): File {
			return Files.createTempFile(tempDirectory.toPath(), "big-sorter", "").toFile()
		}

	}


}