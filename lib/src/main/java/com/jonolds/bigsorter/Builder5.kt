package com.jonolds.bigsorter

import com.jonolds.bigsorter.Util.toRuntimeException
import java.util.stream.Stream


class Builder5<T>(b: Builder<T>) : Builder4Base<T, Builder5<T>>(b) {
	/**
	 * Sorts the input and writes the result to the output file. The items in the
	 * output file are returned as a Stream. The Stream must be closed (it is
	 * AutoCloseable) to avoid consuming unnecessary disk space with many calls.
	 * When the Stream is closed the output file is deleted. When a
	 * [java.io.IOException] occurs it is thrown wrapped in a
	 * [java.io.UncheckedIOException].
	 *
	 *
	 *
	 * Note that a terminal operation (like `.count()` for example) does NOT
	 * close the Stream. You should assign the Stream to a variable in a
	 * try-catch-with-resources block to ensure the output file is deleted.
	 *
	 * @return stream that on close deletes the output file of the sort
	 */
	fun sort(): Stream<T?> {


		try {
			b.output = Sorter.nextTempFile(b.tempDirectory)
			val sorter = Sorter(
				Sorter.inputs(b), b.serializer, b.output!!, b.comparator!!,
				b.maxFilesPerMerge, b.maxItemsPerFile, b.logger, b.bufferSize, b.tempDirectory,
				b.unique, b.initialSortInParallel, b.outputWriterFactory
			)
			sorter.sort()

			return b.serializer
				.createReaderFile(b.output!!)
				.stream()
				.onClose { b.output?.delete() }
		} catch (e: Throwable) {
			b.output!!.delete()
			throw toRuntimeException(e)
		}
	}
}