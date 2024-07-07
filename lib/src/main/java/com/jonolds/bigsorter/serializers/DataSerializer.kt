package com.jonolds.bigsorter.serializers

import com.jonolds.bigsorter.ReaderBS
import com.jonolds.bigsorter.WriterBS
import java.io.*


abstract class DataSerializer<T>(
	override val clazz: Class<T>
) : StreamSerializer<T> {

	abstract fun read(dis: DataInputStream): T?


	abstract fun write(dos: DataOutputStream, value: T?)

	override fun createStreamReader(inStr: InputStream): ReaderBS<T> = object : ReaderBS<T> {

		val dis: DataInputStream = DataInputStream(inStr)

		override fun read(): T? = try {
			this@DataSerializer.read(dis)
		} catch (e: EOFException) {
			null
		}

		override fun close() = dis.close()
	}
	
	

	override fun createStreamWriter(outStr: OutputStream): WriterBS<T> = object : WriterBS<T> {

		val dos: DataOutputStream = DataOutputStream(outStr)

		override fun <S> mapper(mapper: (S?) -> T?): WriterBS<S> {

			val tWriter: WriterBS<T> = this

			return object : WriterBS<S> {

				override fun write(value: S?) = tWriter.write(mapper(value))

				override fun flush() = tWriter.flush()

				override fun close() = tWriter.close()

			}
		}

		override fun write(value: T?) = this@DataSerializer.write(dos, value)

		override fun flush() = dos.flush()

		override fun close() = dos.close()
	}
}
