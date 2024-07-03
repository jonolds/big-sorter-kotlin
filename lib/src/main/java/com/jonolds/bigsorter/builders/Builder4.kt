package com.jonolds.bigsorter.builders

import com.jonolds.bigsorter.*
import java.io.IOException
import java.io.UncheckedIOException
import java.util.*


class Builder4<T>(b: Builder<T>) : Builder4Base<T, Builder4<T>>(b) {

	fun <S> outputMapper(
		writerFactory: FileWriterFactory<S>,
		mapper: (T?) -> S?
	): Builder4<T> {
		check(b.outputWriterFactory.isEmpty)
		b.outputWriterFactory = Optional.of(writerFactory.mapper(mapper))
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
			inputs = b.inputs,
			serializer = b.serializer,
			output = FileWithElemCount(b.output!!.path),
			comparator = b.comparator!!,
			maxFilesPerMerge = b.maxFilesPerMerge,
			maxItemsPerPart = b.maxItemsPerFile,
			log = b.logger,
			bufferSize = b.bufferSize,
			tempDirectory = b.tempDirectory,
			unique = b.unique,
			initialSortInParallel = b.initialSortInParallel,
			outputWriterFactory = b.outputWriterFactory,
		).sort()
	} catch (e: IOException) {
		b.output?.delete()
		throw UncheckedIOException(e)
	}


	fun sortBulk() = try {
		SorterBulk(
			inputs = b.inputs,
			serializer = b.serializer,
			output = FileWithElemCount(b.output!!.path),
			comparator = b.comparator!!,
			maxFilesPerMerge = b.maxFilesPerMerge,
			maxItemsPerPart = b.maxItemsPerFile,
			log = b.logger,
			bufferSize = b.bufferSize,
			tempDirectory = b.tempDirectory,
			unique = b.unique,
			initialSortInParallel = b.initialSortInParallel,
			outputWriterFactory = b.outputWriterFactory,
		).sort()
	} catch (e: IOException) {
		b.output?.delete()
		throw UncheckedIOException(e)
	}


	fun sortBulk2() = try {
		SorterBulk2(
			inputs = b.inputs,
			serializer = b.serializer,
			output = FileWithElemCount(b.output!!.path),
			comparator = b.comparator!!,
			maxFilesPerMerge = b.maxFilesPerMerge,
			maxItemsPerPart = b.maxItemsPerFile,
			log = b.logger,
			bufferSize = b.bufferSize,
			tempDirectory = b.tempDirectory,
			unique = b.unique,
			initialSortInParallel = b.initialSortInParallel,
			outputWriterFactory = b.outputWriterFactory,
		).sort()
	} catch (e: IOException) {
		b.output?.delete()
		throw UncheckedIOException(e)
	}


}