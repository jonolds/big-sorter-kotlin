package com.jonolds.bigsorter

import com.jonolds.bigsorter.Util.inChannel
import com.jonolds.bigsorter.Util.toInStream
import java.io.*
import java.nio.channels.Channels
import java.nio.channels.FileChannel

interface ReaderFactory<T> {

	fun createStreamReader(inStr: InputStream): ReaderBS<T>

	fun createChannelReader(inCh: FileChannel): ReaderBS<T>

	fun createFileReader(file: File, bufferSize: Int = 8192): ReaderBS<T>



	fun <S> mapper(mapper: (T) -> S): ReaderFactory<S> = object : ReaderFactory<S> {
		override fun createStreamReader(inStr: InputStream): ReaderBS<S> =
			this@ReaderFactory.createStreamReader(inStr).mapper(mapper)

		override fun createChannelReader(inCh: FileChannel): ReaderBS<S> =
			this@ReaderFactory.createChannelReader(inCh).mapper(mapper)

		override fun createFileReader(file: File, bufferSize: Int): ReaderBS<S> =
			this@ReaderFactory.createFileReader(file, bufferSize).mapper(mapper)
	}

}

fun interface ChannelReaderFactory<T>: ReaderFactory<T> {


	override fun createStreamReader(inStr: InputStream): ReaderBS<T> = TODO("Not yet implemented")

	override fun createChannelReader(inCh: FileChannel): ReaderBS<T>

	override fun createFileReader(file: File, bufferSize: Int): ReaderBS<T> = createChannelReader(file.inChannel())

}

fun interface StreamReaderFactory<T>: ReaderFactory<T> {


	override fun createStreamReader(inStr: InputStream): ReaderBS<T>

	override fun createChannelReader(inCh: FileChannel): ReaderBS<T> =
		createStreamReader(Channels.newInputStream(inCh))

	override fun createFileReader(file: File, bufferSize: Int): ReaderBS<T> =
		createStreamReader(file.toInStream(bufferSize))
}


