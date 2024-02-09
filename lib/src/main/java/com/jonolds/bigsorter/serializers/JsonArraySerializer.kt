package com.jonolds.bigsorter.serializers

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.jonolds.bigsorter.ReaderBS
import com.jonolds.bigsorter.WriterBS
import java.io.*

internal class JsonArraySerializer private constructor() : SerializerBS<ObjectNode?> {


	private val mapper = ObjectMapper()


	override fun createReader(inStr: InputStream): ReaderBS<ObjectNode?> = try {

		val parser = mapper.factory.createParser(inStr)
		check(parser.nextToken() == JsonToken.START_ARRAY) { "Expected an array" }

		object : ReaderBS<ObjectNode?> {

			override fun read(): ObjectNode? = if (parser.nextToken() == JsonToken.START_OBJECT) {
				// read everything from this START_OBJECT to the matching END_OBJECT
				// and return it as a tree model ObjectNode
				mapper.readTree(parser)
			} else {
				// at end
				parser.close()
				null
			}


			override fun close() = parser.close()

		}

	} catch (e: IOException) {
		throw UncheckedIOException(e)
	}

	override fun createWriter(out: OutputStream): WriterBS<ObjectNode?> {
		val w: Writer = OutputStreamWriter(out)

		return object : WriterBS<ObjectNode?> {
			var first: Boolean = true
			var closed: Boolean = false

			override fun write(value: ObjectNode?) {
				if (first) {
					w.write("[\n")
					first = false
				} else
					w.write(",\n")
				w.write(mapper.writeValueAsString(value))
			}

			override fun flush() = w.flush()

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
        val INSTANCE: JsonArraySerializer = JsonArraySerializer()
	}
}
