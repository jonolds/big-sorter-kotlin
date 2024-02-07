package com.jonolds.bigsorter

import java.io.*
import java.util.function.Function


abstract class DataSerializer<T> : SerializerBS<T> {

	@Throws(IOException::class)
	abstract fun read(dis: DataInputStream): T?

	@Throws(IOException::class)
	abstract fun write(dos: DataOutputStream, value: T?)

	override fun createReader(inStr: InputStream): ReaderBS<T> {
		return object : ReaderBS<T> {
			val dis: DataInputStream = DataInputStream(inStr)

			@Throws(IOException::class)
			override fun read(): T? {
				return try {
					this@DataSerializer.read(dis)
				} catch (e: EOFException) {
					null
				}
			}

			@Throws(IOException::class)
			override fun close() {
				dis.close()
			}
		}
	}
	
	

	override fun createWriter(out: OutputStream): WriterBS<T> {

		return object : WriterBS<T> {

			val dos: DataOutputStream = DataOutputStream(out)

			override fun <S> map(mapper: Function<in S?, out T?>): WriterBS<S> {
				val tWriter: WriterBS<T> = this

				return object : WriterBS<S> {
					@Throws(IOException::class)
					override fun close() {
						tWriter.close()
					}

					@Throws(IOException::class)
					override fun write(value: S?) {
						tWriter.write(mapper.apply(value))
					}

					@Throws(IOException::class)
					override fun flush() {
						tWriter.flush()
					}
				}
			}

			@Throws(IOException::class)
			override fun write(value: T?) {
				this@DataSerializer.write(dos, value)
			}

			@Throws(IOException::class)
			override fun close() {
				dos.close()
			}

			@Throws(IOException::class)
			override fun flush() {
				dos.flush()
			}
		}
	}
}
