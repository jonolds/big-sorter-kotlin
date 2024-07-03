package com.jonolds.bigsorter.builders

import com.jonolds.bigsorter.FileReaderFactory
import com.jonolds.bigsorter.InputTransform
import java.io.File


class Builder2<T>(val b: Builder<T>) {


//	fun input(vararg inputs: InputStream): Builder3<T> = inStreams(inputs.map { { NonClosingInputStream(it) } })

	fun input(file: File, inputTransform: InputTransform<Any, T>? = null): Builder3<T> =
		input(listOf(file), inputTransform)


	fun input(files: List<File>, inputTransform: InputTransform<Any, T>? = null): Builder3<T> {
		val factory = inputTransform?.transformed() ?: b.serializer
		b.inputs.addAll(files.map { f -> { factory.createFileReader(f) } })
		return Builder3(b)
	}


	fun input(files: List<File>, inputReaderFactory: FileReaderFactory<T>?): Builder3<T> {
		val factory = inputReaderFactory ?: b.serializer
		b.inputs.addAll(files.map { f -> { factory.createFileReader(f) } })
		return Builder3(b)
	}



//	private fun inStreams(inputs: List<() -> InputStream>, inputTransform: InputTransform<Any, T>? = null): Builder3<T> {
//		val factory = inputTransform?.transformed() ?: b.serializer
//		b.inputs.addAll(inputs.map { input -> { factory.createStreamReader(input as InputStream) } })
//		return Builder3(b)
//	}



}

