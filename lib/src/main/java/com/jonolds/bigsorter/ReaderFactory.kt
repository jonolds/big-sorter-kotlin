package com.jonolds.bigsorter

import com.jonolds.bigsorter.Util.inChannel
import com.jonolds.bigsorter.Util.toInStream
import java.io.*
import java.nio.channels.Channels
import java.nio.channels.FileChannel



interface FileReaderFactory<T> {

	fun createFileReader(file: File, bufferSize: Int = 8192): ReaderBS<T>

	fun <S> mapper(mapper: (T) -> S): FileReaderFactory<S>

}


fun interface ChannelReaderFactory<T>: FileReaderFactory<T> {

	fun createChannelReader(inCh: FileChannel, bufferSize: Int?): ReaderBS<T>

	override fun createFileReader(file: File, bufferSize: Int): ReaderBS<T> = createChannelReader(file.inChannel(), bufferSize)


	override fun <S> mapper(mapper: (T) -> S): ChannelReaderFactory<S> = object : ChannelReaderFactory<S> {


		override fun createChannelReader(inCh: FileChannel, bufferSize: Int?): ReaderBS<S> =
			this@ChannelReaderFactory.createChannelReader(inCh, bufferSize).mapper(mapper)

		override fun createFileReader(file: File, bufferSize: Int): ReaderBS<S> =
			this@ChannelReaderFactory.createFileReader(file, bufferSize).mapper(mapper)

	}

}

fun interface StreamReaderFactory<T>: FileReaderFactory<T> {


	fun createStreamReader(inStr: InputStream): ReaderBS<T>

	override fun createFileReader(file: File, bufferSize: Int): ReaderBS<T> =
		createStreamReader(file.toInStream(bufferSize))

	override fun <S> mapper(mapper: (T) -> S): StreamReaderFactory<S> = object : StreamReaderFactory<S> {


		override fun createStreamReader(inStr: InputStream): ReaderBS<S> =
			this@StreamReaderFactory.createStreamReader(inStr).mapper(mapper)

		override fun createFileReader(file: File, bufferSize: Int): ReaderBS<S> =
			this@StreamReaderFactory.createFileReader(file, bufferSize).mapper(mapper)
	}


}


