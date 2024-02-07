package com.jonolds.bigsorter

import com.github.davidmoten.guavamini.Lists
import com.github.davidmoten.guavamini.Preconditions
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Supplier
import java.util.stream.Collectors



class Builder2<T>(val b: Builder<T>) {

	fun input(charset: Charset, vararg strings: String): Builder3<T> {

		val list = Arrays.stream(strings)
			.map { string: String -> ByteArrayInputStream(string.toByteArray(charset)) }
			.map { bis: ByteArrayInputStream? -> Supplier<InputStream> { bis } }
			.collect(Collectors.toList())
		return inputStreams(list)
	}

	fun input(vararg strings: String): Builder3<T> {
		return input(StandardCharsets.UTF_8, *strings)
	}

	fun input(vararg inputs: InputStream): Builder3<T> {
		val list: MutableList<Supplier<InputStream>> = Lists.newArrayList()
		for (inStr in inputs)
			list.add(Supplier { NonClosingInputStream(inStr) })
		return inputStreams(list)
	}

	fun input(input: Supplier<out InputStream>): Builder3<T> {
		return inputStreams(listOf(input))
	}

	fun input(vararg files: File): Builder3<T> {
		return input(files.toList())
	}

	fun input(files: List<File>): Builder3<T> = inputStreams(
		files.stream()
			.map { file: File -> this.supplier(file) }
			.collect(Collectors.toList())
	)

	fun inputStreams(inputs: List<Supplier<out InputStream>>): Builder3<T> {
		for (input in inputs) {
			b.inputs.add(Source(SourceType.SUPPLIER_INPUT_STREAM, input))
		}
		return Builder3(b)
	}

	fun readers(readers: List<Supplier<out ReaderBS<out T>>>): Builder3<T> {
		Preconditions.checkNotNull(readers)
		for (input in readers) {
			b.inputs.add(Source(SourceType.SUPPLIER_READER, input))
		}
		return Builder3(b)
	}

	@SafeVarargs
	fun inputItems(vararg items: T): Builder3<T> {
		return inputItems(items.asList())
	}

	fun inputItems(iterable: Iterable<T>): Builder3<T> {
		val supplier: Supplier<out ReaderBS<out T>> = Supplier<ReaderBS<out T>> { ReaderFromIterator(iterable.iterator()) }
		return readers(listOf(supplier))
	}

	fun inputItems(iterator: Iterator<T>): Builder3<T> {
		val supplier: Supplier<out ReaderBS<out T>> = Supplier<ReaderBS<out T>> { ReaderFromIterator(iterator) }
		return readers(listOf(supplier))
	}

	private fun supplier(file: File): Supplier<InputStream> {
		return Supplier {
			try {
				return@Supplier Sorter.openFile(file, b.bufferSize)
			} catch (e: FileNotFoundException) {
				throw UncheckedIOException(e)
			}
		}
	}
}

