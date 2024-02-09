package com.jonolds.bigsorter.builders

import com.jonolds.bigsorter.OutputStreamWriterFactory
import com.jonolds.bigsorter.Sorter
import java.io.IOException
import java.io.UncheckedIOException
import java.util.*
import java.util.function.Function


class Builder4<T>(b: Builder<T>) : Builder4Base<T, Builder4<T>>(b) {

	fun <S> outputMapper(
		writerFactory: OutputStreamWriterFactory<S>,
		mapper: Function<in T?, out S?>
	): Builder4<T> {
		check(b.outputWriterFactory.isEmpty)
		val factory = OutputStreamWriterFactory { out -> writerFactory.createWriter(out).map(mapper) }
		b.outputWriterFactory = Optional.of(factory)
		return this
	}


	// TODO add flatMap method, stream transforms?
	/**
	 * Sorts the input and writes the result to the given output file. If an
	 * [IOException] occurs then it is thrown wrapped in
	 * [UncheckedIOException].
	 */
	fun sort() = try {
		Sorter(
			inputs = b.buildInputSuppliers(),
			serializer = b.serializer,
			output = b.output!!,
			comparator = b.comparator!!,
			maxFilesPerMerge = b.maxFilesPerMerge,
			maxItemsPerPart = b.maxItemsPerFile,
			log = b.logger,
			bufferSize = b.bufferSize,
			tempDirectory = b.tempDirectory,
			unique = b.unique,
			initialSortInParallel = b.initialSortInParallel,
			outputWriterFactory = b.outputWriterFactory
		).sort()
	} catch (e: IOException) {
		b.output?.delete()
		throw UncheckedIOException(e)
	}


	fun sortBulk() = try {
		Sorter(
			inputs = b.buildInputSuppliers(),
			serializer = b.serializer,
			output = b.output!!,
			comparator = b.comparator!!,
			maxFilesPerMerge = b.maxFilesPerMerge,
			maxItemsPerPart = b.maxItemsPerFile,
			log = b.logger,
			bufferSize = b.bufferSize,
			tempDirectory = b.tempDirectory,
			unique = b.unique,
			initialSortInParallel = b.initialSortInParallel,
			outputWriterFactory = b.outputWriterFactory
		).sortBulk()
	} catch (e: IOException) {
		b.output?.delete()
		throw UncheckedIOException(e)
	}


}