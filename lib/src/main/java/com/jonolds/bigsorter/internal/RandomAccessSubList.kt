package com.jonolds.bigsorter.internal

import java.util.RandomAccess


internal class RandomAccessSubList<E>(
	list: AbstractFastList<E>,
	fromIndex: Int,
	toIndex: Int
) : SubList<E>(list, fromIndex, toIndex), RandomAccess {

	override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = RandomAccessSubList(this, fromIndex, toIndex)

}




