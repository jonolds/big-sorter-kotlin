package com.jonolds.bigsorter.builders

import com.jonolds.bigsorter.*
import com.jonolds.bigsorter.Util.toInStream
import java.io.File
import java.io.InputStream
import java.util.function.Supplier


class Builder2<T>(val b: Builder<T>) {


	fun input(vararg inputs: InputStream): Builder3<T> = inStreams(inputs.map { Supplier{ NonClosingInputStream(it) } })

	fun input(file: File): Builder3<T> = input(listOf(file))

	fun input(files: List<File>): Builder3<T> = inStreams(files.map { Supplier { it.toInStream(b.bufferSize) } })



	private fun inStreams(inputs: List<Supplier<out InputStream>>): Builder3<T> {
		b.inputs.addAll(inputs.map { input -> Source(SourceType.SUPPLIER_INPUT_STREAM, input) })
		return Builder3(b)
	}





	fun inputItems(vararg items: T): Builder3<T> = inputItems(items.asList().iterator())

	fun inputItems(iterable: Iterable<T>): Builder3<T> = inputItems(iterable.iterator())

	private fun inputItems(iterator: Iterator<T>): Builder3<T> =
		readers(listOf(Supplier<ReaderBS<out T>> { ReaderFromIterator(iterator) }))


	private fun readers(readers: List<Supplier<out ReaderBS<out T>>>): Builder3<T> {
		b.inputs.addAll(readers.map { input -> Source(SourceType.SUPPLIER_READER, input) })
		return Builder3(b)
	}

}

