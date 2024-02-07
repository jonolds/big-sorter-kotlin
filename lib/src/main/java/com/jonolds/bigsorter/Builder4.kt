package com.jonolds.bigsorter

import com.github.davidmoten.guavamini.Preconditions
import java.io.IOException
import java.io.OutputStream
import java.io.UncheckedIOException
import java.util.*
import java.util.function.Function


class Builder4<T>(b: Builder<T>) : Builder4Base<T, Builder4<T>>(b) {

	fun <S> outputMapper(
		writerFactory: OutputStreamWriterFactory<S>,
		mapper: Function<in T?, out S?>
	): Builder4<T> {
		Preconditions.checkArgument(b.outputWriterFactory.isEmpty)
		val factory =
			OutputStreamWriterFactory { out: OutputStream ->
				writerFactory.createWriter(out).map(mapper)
			}
		b.outputWriterFactory = Optional.of(factory)
		return this
	}

	// TODO add flatMap method, stream transforms?
	/**
	 * Sorts the input and writes the result to the given output file. If an
	 * [IOException] occurs then it is thrown wrapped in
	 * [UncheckedIOException].
	 */
	fun sort() {
		val sorter = Sorter(
			Sorter.inputs(b), b.serializer, b.output!!, b.comparator!!,
			b.maxFilesPerMerge, b.maxItemsPerFile, b.logger, b.bufferSize, b.tempDirectory,
			b.unique, b.initialSortInParallel, b.outputWriterFactory
		)
		try {
			sorter.sort()
		} catch (e: IOException) {
			b.output?.delete()
			throw UncheckedIOException(e)
		}
	}
}