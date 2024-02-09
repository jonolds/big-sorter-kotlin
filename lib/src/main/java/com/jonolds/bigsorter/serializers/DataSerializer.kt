package com.jonolds.bigsorter.serializers

import com.jonolds.bigsorter.ReaderBS
import com.jonolds.bigsorter.WriterBS
import java.io.*
import java.util.function.Function


abstract class DataSerializer<T> : SerializerBS<T> {

	abstract fun read(dis: DataInputStream): T?


	abstract fun write(dos: DataOutputStream, value: T?)

	override fun createReader(inStr: InputStream): ReaderBS<T> = object : ReaderBS<T> {

		val dis: DataInputStream = DataInputStream(inStr)

		override fun read(): T? = try {
			this@DataSerializer.read(dis)
		} catch (e: EOFException) {
			null
		}

		override fun close() = dis.close()
	}
	
	

	override fun createWriter(out: OutputStream): WriterBS<T> = object : WriterBS<T> {

		val dos: DataOutputStream = DataOutputStream(out)

		override fun <S> map(mapper: Function<in S?, out T?>): WriterBS<S> {

			val tWriter: WriterBS<T> = this

			return object : WriterBS<S> {
				override fun close() = tWriter.close()

				override fun write(value: S?) = tWriter.write(mapper.apply(value))

				override fun flush() = tWriter.flush()
			}
		}

		override fun write(value: T?) = this@DataSerializer.write(dos, value)

		override fun close() = dos.close()

		override fun flush() = dos.flush()
	}
}
