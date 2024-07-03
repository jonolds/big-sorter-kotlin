package com.jonolds.bigsorter.v2.fastlist

internal class RandomAccessSubList2<E>(
    list: AbstractFastList2<E>, fromIndex: Int, toIndex: Int
): SubList2<E>(list, fromIndex, toIndex), RandomAccess {

	override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> =
		RandomAccessSubList2(this, fromIndex, toIndex)

}
