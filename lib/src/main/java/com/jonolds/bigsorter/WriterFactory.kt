package com.jonolds.bigsorter

import com.jonolds.bigsorter.Util.outChannel
import com.jonolds.bigsorter.Util.toOutStream
import java.io.File
import java.io.OutputStream
import java.nio.channels.FileChannel

interface WriterFactory<T> {


	fun createStreamWriter(outStr: OutputStream): WriterBS<T>

	fun createChannelWriter(outCh: FileChannel): WriterBS<T>

	fun createFileWriter(file: File, bufferSize: Int): WriterBS<T>


	fun <S> mapper(mapper: (S?) -> T?): WriterFactory<S> = object : WriterFactory<S> {

		override fun createStreamWriter(outStr: OutputStream): WriterBS<S> =
			this@WriterFactory.createStreamWriter(outStr).mapper(mapper)

		override fun createChannelWriter(outCh: FileChannel): WriterBS<S> =
			this@WriterFactory.createChannelWriter(outCh).mapper(mapper)

		override fun createFileWriter(file: File, bufferSize: Int): WriterBS<S> =
			this@WriterFactory.createFileWriter(file, bufferSize).mapper(mapper)
	}

}



fun interface ChannelWriterFactory<T>: WriterFactory<T> {


	override fun createStreamWriter(outStr: OutputStream): WriterBS<T>  {
		TODO("Not yet implemented")
	}

	override fun createChannelWriter(outCh: FileChannel): WriterBS<T>

	override fun createFileWriter(file: File, bufferSize: Int): WriterBS<T> =
		createChannelWriter(file.outChannel())

}


fun interface StreamWriterFactory<T>: WriterFactory<T> {


	override fun createStreamWriter(outStr: OutputStream): WriterBS<T>

	override fun createChannelWriter(outCh: FileChannel): WriterBS<T>   {
		TODO("Not yet implemented")
	}

	override fun createFileWriter(file: File, bufferSize: Int): WriterBS<T> =
		createStreamWriter(file.toOutStream(bufferSize))


}

