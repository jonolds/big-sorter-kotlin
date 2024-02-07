package com.jonolds.bigsorter

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.*

internal class JsonArraySerializer private constructor() : SerializerBS<ObjectNode?> {


	private val mapper = ObjectMapper()


	override fun createReader(inStr: InputStream): ReaderBS<ObjectNode?> {
		try {
			val parser = mapper.factory.createParser(inStr)
			check(parser.nextToken() == JsonToken.START_ARRAY) { "Expected an array" }
			return object : ReaderBS<ObjectNode?> {


				@Throws(IOException::class)
				override fun read(): ObjectNode? {
					if (parser.nextToken() == JsonToken.START_OBJECT) {
						// read everything from this START_OBJECT to the matching END_OBJECT
						// and return it as a tree model ObjectNode
						return mapper.readTree(parser)
					} else {
						// at end
						parser.close()
						return null
					}
				}

				@Throws(IOException::class)
				override fun close() {
					parser.close()
				}
			}
		} catch (e: IOException) {
			throw UncheckedIOException(e)
		}
	}

	override fun createWriter(out: OutputStream): WriterBS<ObjectNode?> {
		val w: java.io.Writer = OutputStreamWriter(out)

		return object : WriterBS<ObjectNode?> {
			var first: Boolean = true
			var closed: Boolean = false

			@Throws(IOException::class)
			override fun write(value: ObjectNode?) {
				if (first) {
					w.write("[\n")
					first = false
				} else {
					w.write(",\n")
				}
				w.write(mapper.writeValueAsString(value))
			}

			@Throws(IOException::class)
			override fun flush() {
				w.flush()
			}

			@Throws(IOException::class)
			override fun close() {
				if (!closed) {
					w.write("\n]")
					w.close()
					closed = true
				}
			}
		}
	}

	companion object {
		@JvmField
        val INSTANCE: JsonArraySerializer = JsonArraySerializer()
	}
}
