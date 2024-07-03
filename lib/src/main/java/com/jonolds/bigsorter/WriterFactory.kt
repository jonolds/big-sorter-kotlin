package com.jonolds.bigsorter

import com.jonolds.bigsorter.Util.outChannel
import com.jonolds.bigsorter.Util.toOutStream
import java.io.File
import java.io.OutputStream
import java.nio.channels.FileChannel


interface FileWriterFactory<T> {


	fun createFileWriter(file: File, bufferSize: Int): WriterBS<T>


	fun <S> mapper(mapper: (S?) -> T?): FileWriterFactory<S>

}


fun interface ChannelWriterFactory<T>: FileWriterFactory<T> {


	fun createChannelWriter(outCh: FileChannel, bufferSize: Int?): WriterBS<T>


	override fun createFileWriter(file: File, bufferSize: Int): WriterBS<T> =
		createChannelWriter(file.outChannel(), bufferSize)


	override fun <S> mapper(mapper: (S?) -> T?): ChannelWriterFactory<S> = object : ChannelWriterFactory<S> {

		override fun createChannelWriter(outCh: FileChannel, bufferSize: Int?): WriterBS<S> =
			this@ChannelWriterFactory.createChannelWriter(outCh, DEFAULT_BUFFER_SIZE).mapper(mapper)

		override fun createFileWriter(file: File, bufferSize: Int): WriterBS<S> =
			this@ChannelWriterFactory.createFileWriter(file, bufferSize).mapper(mapper)
	}


}


fun interface StreamWriterFactory<T>: FileWriterFactory<T> {


	fun createStreamWriter(outStr: OutputStream): WriterBS<T>


	override fun createFileWriter(file: File, bufferSize: Int): WriterBS<T> =
		createStreamWriter(file.toOutStream(bufferSize))


	override fun <S> mapper(mapper: (S?) -> T?): StreamWriterFactory<S> = object : StreamWriterFactory<S> {

		override fun createStreamWriter(outStr: OutputStream): WriterBS<S> =
			this@StreamWriterFactory.createStreamWriter(outStr).mapper(mapper)

		override fun createFileWriter(file: File, bufferSize: Int): WriterBS<S> =
			this@StreamWriterFactory.createFileWriter(file, bufferSize).mapper(mapper)
	}


}

