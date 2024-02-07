package com.jonolds.bigsorter

import java.io.*

@Suppress("UNCHECKED_CAST")
internal class JavaSerializer<T : Serializable?> : SerializerBS<T> {

	override fun createReader(inStr: InputStream): ReaderBS<T> {
		val ois = ObjectInputStream(inStr)

		return object : ReaderBS<T> {

			@Throws(IOException::class)
			override fun read(): T? {
				return try {
					ois.readObject() as T
				} catch (e: EOFException) {
					null
				} catch (e: ClassNotFoundException) {
					throw RuntimeException(e)
				}
			}

			@Throws(IOException::class)
			override fun close() {
				ois.close()
			}
		}
	}


	override fun createWriter(out: OutputStream): WriterBS<T> {
		val oos = ObjectOutputStream(out)

		return object : WriterBS<T> {
			@Throws(IOException::class)
			override fun write(value: T?) {
				oos.writeObject(value)
			}

			@Throws(IOException::class)
			override fun close() {
				oos.close()
			}

			@Throws(IOException::class)
			override fun flush() {
				oos.flush()
			}
		}
	}

	companion object {
		private val INSTANCE = JavaSerializer<Serializable>()


		@JvmStatic
        fun <T : Serializable?> instance(): JavaSerializer<T> {
			return INSTANCE as JavaSerializer<T>
		}
	}
}
