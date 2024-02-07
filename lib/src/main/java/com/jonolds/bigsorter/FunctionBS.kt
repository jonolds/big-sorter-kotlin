package com.jonolds.bigsorter

fun interface FunctionBS<S, T> {
	@Throws(Exception::class)
	fun apply(s: S): T
}
