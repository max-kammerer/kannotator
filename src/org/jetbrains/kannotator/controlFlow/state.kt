package org.jetbrains.kannotator.controlFlow

import kotlinlib.IndexedElement
import kotlinlib.indexedIterator

public trait State {
    val stack: Stack
    val localVariables: LocalVariableTable
}

public trait Stack {
    val size: Int
    fun get(indexFromTop: Int): Set<Value>
}

public trait LocalVariableTable {
    val size: Int
    fun get(variableIndex: Int): Set<Value>
}

public trait Value {
    val parameterIndex: Int?

    final val interesting : Boolean
        get() = parameterIndex != null
}

public val LocalVariableTable.indexed: Iterator<IndexedElement<Set<Value>>>
        get() = indexedIterator(this, size) { c, i -> c.get(i) }

public val Stack.indexed: Iterator<IndexedElement<Set<Value>>>
        get() = indexedIterator(this, size) { c, i -> c.get(i) }

fun State.containsValue(v: Value): Boolean {
    return stack.indexed.any {element -> element.value.contains(v)} || localVariables.indexed.any {element -> element.value.contains(v)}
}