package com.jonolds.bigsorter.builders

import java.io.File
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit


@Suppress("UNCHECKED_CAST")
open class Builder4Base<T, S: Builder4Base<T, S>>(val b: Builder<T>) {


	fun maxFilesPerMerge(value: Int): S {
		check(value > 1) { "maxFilesPerMerge must be greater than 1" }
		b.maxFilesPerMerge = value
		return this as S
	}


	/**
	 * Sets the number of items in each file for the initial split. Default is 100_000.
	 *
	 * @param value the number of items in each file for the initial split
	 * @return this
	 */
	fun maxItemsPerFile(value: Int): S {
		check(value > 0) { "maxItemsPerFile must be greater than 0" }
		b.maxItemsPerFile = value
		return this as S
	}


	fun unique(value: Boolean = true): S {
		b.unique = value
		return this as S
	}


	fun initialSortInParallel(initialSortInParallel: Boolean = true): S {
		b.initialSortInParallel = initialSortInParallel
		return this as S
	}


	fun logger(logger: (String) -> Unit): S {
		b.logger = logger
		return this as S
	}


	fun loggerStdOut(): S = logger { msg: String ->
		println(ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS).format(Builder.DATE_TIME_PATTERN) + " " + msg)
	}


	fun bufferSize(bufferSize: Int): S {
		check(bufferSize > 0) { "bufferSize must be greater than 0" }
		b.bufferSize = bufferSize
		return this as S
	}


	fun tempDirectory(directory: File): S {
		b.tempDirectory = directory
		return this as S
	}
}