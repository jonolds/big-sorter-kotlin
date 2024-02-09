package com.jonolds.bigsorter

fun interface FunctionBS<S, T> {
	fun apply(s: S): T
}
