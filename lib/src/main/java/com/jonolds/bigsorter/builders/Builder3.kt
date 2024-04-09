package com.jonolds.bigsorter.builders

import com.jonolds.bigsorter.ReaderBS
import java.io.File


@Suppress("UNCHECKED_CAST")
class Builder3<T>(val b: Builder<T>) {



	fun map(mapper: (T) -> T): Builder3<T> {
		val currentTransform = b.transform
		return transform { reader -> currentTransform(reader).mapper(mapper) }
	}


	fun transform(transform: (ReaderBS<T>) -> ReaderBS<out T>): Builder3<T> {
		val currentTransform = b.transform
		b.transform = { reader -> transform(currentTransform(reader) as ReaderBS<T>) }
		return this
	}


	fun output(output: File): Builder4<T> {
		b.output = output
		return Builder4(b)
	}

	fun outputAsStream(): Builder5<T> = Builder5(b)
}