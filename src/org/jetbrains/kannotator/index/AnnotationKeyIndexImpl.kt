package org.jetbrains.kannotator.index

import org.jetbrains.kannotator.declarations.TypePosition
import java.util.regex.Pattern
import kotlinlib.*
import org.jetbrains.kannotator.annotations.io.parseAnnotationKey

class AnnotationKeyIndexImpl(private val declarationIndex: DeclarationIndex): AnnotationKeyIndex {
    override fun findPositionByAnnotationKeyString(annotationKey: String): TypePosition? {
        val (canonicalClassName, _, methodName) = parseAnnotationKey(annotationKey)

        return null
    }

}