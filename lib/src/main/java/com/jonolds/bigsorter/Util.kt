package com.jonolds.bigsorter

import com.github.davidmoten.guavamini.Lists
import com.github.davidmoten.guavamini.Preconditions
import java.io.*
import java.util.function.Function

object Util {


	/**
	 * Writes common entries from both readers to the writer in sorted
	 * order.`readerA` and `readerB` must be reading already sorted
	 * data.
	 *
	 * @param <T>
	 * item type
	 * @param readerA
	 * reader of first file
	 * @param readerB
	 * reader of second file
	 * @param comparator
	 * comparator for item
	 * @param writer
	 * writer to which common entries are written to
	 * @throws IOException
	 * I/O exception
	</T> */
	@JvmStatic @Throws(IOException::class)
	fun <T> findSame(
		readerA: ReaderBS<out T>, readerB: ReaderBS<out T>,
		comparator: Comparator<in T>, writer: WriterBS<T>
	) {
		// return A intersection B
		var x = readerA.read()
		var y = readerB.read()
		while (x != null && y != null) {
			val compare = comparator.compare(x, y)
			if (compare == 0) {
				writer.write(x)
				// read next values
				x = readerA.read()
				y = readerB.read()
			} else if (compare < 0) {
				x = readerA.read()
			} else {
				y = readerB.read()
			}
		}
	}

	/**
	 * Writes common entries from both files to the output file in sorted
	 * order.`a` and `b` must already be sorted.
	 *
	 * @param <T>
	 * item type
	 * @param a
	 * first file
	 * @param b
	 * second file
	 * @param serializer
	 * item serializer
	 * @param comparator
	 * comparator for item
	 * @param output
	 * file to which common entries are written to
	 * @throws IOException
	 * I/O exception
	</T> */
	@JvmStatic @Throws(IOException::class)
	fun <T> findSame(
		a: File, b: File, serializer: SerializerBS<T>, comparator: Comparator<in T>,
		output: File
	) {
		serializer.createReaderFile(a).use { readerA ->
			serializer.createReaderFile(b).use { readerB ->
				serializer.createWriterFile(output).use { writer ->
					findSame(readerA, readerB, comparator, writer)
				}
			}
		}
	}

	/**
	 * Writes different entries (only those entries that are only present in one
	 * file) from both files to the output file in sorted order. `a` and
	 * `b` must already be sorted.
	 *
	 * @param <T>
	 * item type
	 * @param a
	 * first file
	 * @param b
	 * second file
	 * @param serializer
	 * item serializer
	 * @param comparator
	 * comparator for item
	 * @param output
	 * file to which common entries are written to
	 * @throws IOException
	 * I/O exception
	</T> */
	@JvmStatic @Throws(IOException::class)
	fun <T> findDifferent(
		a: File, b: File, serializer: SerializerBS<T>, comparator: Comparator<in T>,
		output: File
	) {
		serializer.createReaderFile(a).use { readerA ->
			serializer.createReaderFile(b).use { readerB ->
				serializer.createWriterFile(output).use { writer ->
					Util.findDifferent(readerA, readerB, comparator, writer)
				}
			}
		}
	}

	/**
	 * Writes different entries (only those entries that are only present in one
	 * input reader) from both readers to the writer in sorted order.
	 * `readerA` and `readerB` must be reading already sorted data.
	 *
	 * @param <T>
	 * item type
	 * @param readerA
	 * reader of first file
	 * @param readerB
	 * reader of second file
	 * @param comparator
	 * comparator for item
	 * @param writer
	 * writer to which common entries are written to
	 * @throws IOException
	 * I/O exception
	</T> */
	@JvmStatic @Throws(IOException::class)
	fun <T> findDifferent(
		readerA: ReaderBS<out T>, readerB: ReaderBS<out T>,
		comparator: Comparator<in T>, writer: WriterBS<T>
	) {
		// returns those elements in (A union B) \ (A intersection B)
		var x = readerA.read()
		var y = readerB.read()
		while (x != null && y != null) {
			val compare = comparator.compare(x, y)
			if (compare == 0) {
				x = readerA.read()
				y = readerB.read()
			} else if (compare < 0) {
				writer.write(x)
				x = readerA.read()
			} else {
				writer.write(y)
				y = readerB.read()
			}
		}
		while (x != null) {
			writer.write(x)
			x = readerA.read()
		}
		while (y != null) {
			writer.write(y)
			y = readerB.read()
		}
	}

	@JvmStatic
	@Throws(IOException::class)
	fun <T> findComplement(
		readerA: ReaderBS<out T>, readerB: ReaderBS<out T>,
		comparator: Comparator<in T>, writer: WriterBS<T>
	) {
		// returns those elements in A that are not present in B
		var x = readerA.read()
		var y = readerB.read()
		while (x != null && y != null) {
			val compare = comparator.compare(x, y)
			if (compare == 0) {
				x = readerA.read()
				y = readerB.read()
			} else if (compare < 0) {
				writer.write(x)
				x = readerA.read()
			} else {
				y = readerB.read()
			}
		}
		while (x != null) {
			writer.write(x)
			x = readerA.read()
		}
	}

	@JvmStatic
	@Throws(IOException::class)
	fun <T> findComplement(
		a: File, b: File, serializer: SerializerBS<T>, comparator: Comparator<in T>,
		output: File
	) {
		serializer.createReaderFile(a).use { readerA ->
			serializer.createReaderFile(b).use { readerB ->
				serializer.createWriterFile(output).use { writer ->
					findComplement(readerA, readerB, comparator, writer)
				}
			}
		}
	}

	@JvmStatic
	@Throws(IOException::class)
	fun <T> splitByCount(input: File, serializer: SerializerBS<T>, count: Long): List<File> {
		return splitByCount( //
			input,  //
			serializer,  //
			{ n: Int ->
				File(
					input.parentFile,
					input.name + "-" + n
				)
			},  //
			count
		)
	}

	@JvmStatic
	@Throws(IOException::class)
	fun <T> splitByCount(
		input: File, serializer: SerializerBS<T>, output: Function<Int, File>,
		count: Long
	): List<File> {
		return splitByCount<T>(listOf(input), serializer, output, count)
	}


	@JvmStatic
	@Throws(IOException::class)
	fun <T> splitByCount(
		input: List<File>, serializer: SerializerBS<T>, output: Function<Int, File>,
		count: Long
	): List<File> {
		Preconditions.checkArgument(count > 0, "count must be greater than 0")
		val list: MutableList<File> = Lists.newArrayList()
		var t: T?
		var fileNumber = 0
		var i: Long = 0
		var writer: WriterBS<T>? = null
		try {
			for (file in input) {
				serializer.createReaderFile(file).use { reader ->
					while ((reader.read().also { t = it }) != null) {
						if (writer == null) {
							fileNumber++
							val f = output.apply(fileNumber)
							list.add(f)
							writer = serializer.createWriterFile(f)
						}
						writer!!.write(t)
						i++
						if (i == count) {
							writer!!.close()
							writer = null
							i = 0
						}
					}
				}
			}
		} finally {
			if (writer != null) {
				writer!!.close()
			}
		}
		return list
	}

	@JvmStatic
	@Throws(IOException::class)
	fun <T> splitBySize(input: File, serializer: SerializerBS<T>, maxSize: Long): List<File> {
		return splitBySize( //
			input,  //
			serializer,  //
			{ n: Int ->
				File(
					input.parentFile,
					input.name + "-" + n
				)
			},  //
			maxSize
		)
	}

	@JvmStatic @Throws(IOException::class)
	fun <T> splitBySize(
		input: File, serializer: SerializerBS<T>, output: Function<Int, File>,
		maxSize: Long
	): List<File> {
		return splitBySize<T>(listOf(input), serializer, output, maxSize)
	}

	@JvmStatic @Throws(IOException::class)
	fun <T> splitBySize(
		input: List<File>, serializer: SerializerBS<T>, output: Function<Int, File>,
		maxSize: Long
	): List<File> {
		val bytes = ByteArrayOutputStream()
		val buffer = serializer.createWriter(bytes)
		val list: MutableList<File> = Lists.newArrayList()
		var t: T?
		var fileNumber = 0
		var n: Long = 0
		var writer: WriterBS<T>? = null
		try {
			for (file in input) {
				serializer.createReaderFile(file).use { reader ->
					while ((reader.read().also { t = it }) != null) {
						// check increase in size from writing t
						// by writing to buffer
						bytes.reset()
						buffer.write(t)
						buffer.flush()
						n += bytes.size().toLong()
						if (writer == null) {
							fileNumber++
							val f = output.apply(fileNumber)
							list.add(f)
							writer = serializer.createWriterFile(f)
							n = bytes.size().toLong()
						} else if (n > maxSize) {
							writer!!.close()
							fileNumber++
							val f = output.apply(fileNumber)
							list.add(f)
							writer = serializer.createWriterFile(f)
							n = bytes.size().toLong()
						}
						writer!!.write(t)
					}
				}
			}
		} finally {
			if (writer != null) {
				writer!!.close()
			}
		}
		return list
	}

	@JvmStatic
	fun close(c: Closeable) {
		try {
			c.close()
		} catch (e: IOException) {
			throw UncheckedIOException(e)
		}
	}

	@JvmStatic
	fun toRuntimeException(e: Throwable?): RuntimeException {
		return when (e) {
			is IOException -> {
				UncheckedIOException(e as IOException?)
			}

			is RuntimeException -> {
				e
			}

			else -> {
				RuntimeException(e)
			}
		}
	}

	@JvmStatic
	fun <S, T> convert(
		input: File, readerFactory: InputStreamReaderFactory<S>, out: File,
		writerFactory: OutputStreamWriterFactory<T>, mapper: Function<in S?, out T?>
	) {
		try {
			readerFactory.createReaderFile(input).use { r ->
				writerFactory.createWriterFile(out).use { w ->
					var s: S?
					while ((r.read().also { s = it }) != null) {
						w.write(mapper.apply(s))
					}
				}
			}
		} catch (e: IOException) {
			throw UncheckedIOException(e)
		}
	}

}