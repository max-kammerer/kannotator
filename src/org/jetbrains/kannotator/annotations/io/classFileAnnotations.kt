package org.jetbrains.kannotator.annotations.io

import com.gs.collections.impl.multimap.set.UnifiedSetMultimap
import org.jetbrains.kannotator.declarations.AnnotatedType
import org.jetbrains.kannotator.declarations.AnnotationPosition
import org.jetbrains.kannotator.declarations.Annotations
import org.jetbrains.kannotator.declarations.AnnotationsImpl
import org.jetbrains.kannotator.declarations.ClassName
import org.jetbrains.kannotator.declarations.Method
import org.jetbrains.kannotator.declarations.PositionsWithinMember
import org.jetbrains.kannotator.declarations.canonicalName
import org.jetbrains.kannotator.declarations.forEachValidPosition
import org.jetbrains.kannotator.declarations.isStatic
import org.jetbrains.kannotator.declarations.setIfNotNull
import org.jetbrains.kannotator.index.ClassSource
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

public fun <A> getAnnotationsFromClassFiles(
        classSource: ClassSource,
        // We use canonical names to avoid making users have annotations.jar on the class path
        annotationClassesToAnnotation: (canonicalAnnotationClassNames: Set<String>) -> A?
): Annotations<A> {
    val annotations = AnnotationsImpl<A>()

    classSource forEach {
        reader ->

        reader.accept(object : ClassVisitor(Opcodes.ASM4) {
                override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
                    val method = Method(ClassName.fromInternalName(reader.getClassName()), access, name, desc, signature)
                    return object : MethodVisitor(Opcodes.ASM4) {
                        private val positions = PositionsWithinMember(method)
                        private val canonicalAnnotationClassNames = UnifiedSetMultimap<AnnotationPosition, String>()

                        private fun setAnnotation(annotatedType: AnnotatedType, desc: String) {
                            val annotationClass = ClassName.fromType(Type.getType(desc)).canonicalName
                            canonicalAnnotationClassNames.put(annotatedType.position, annotationClass)
                        }

                        override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
                            setAnnotation(positions.forReturnType(), desc)
                            return null
                        }

                        override fun visitParameterAnnotation(parameter: Int, desc: String, visible: Boolean): AnnotationVisitor? {
                            val thisOffset = if (method.isStatic()) 0 else 1
                            setAnnotation(positions.forParameter(parameter + thisOffset), desc)
                            return null
                        }

                        override fun visitEnd() {
                            positions.forEachValidPosition {
                                pos ->
                                val classNames = canonicalAnnotationClassNames[pos]!!
                                val annotation = annotationClassesToAnnotation(classNames)
                                annotations.setIfNotNull(pos, annotation)
                            }
                        }
                    }
                }
            }, ClassReader.SKIP_CODE)
    }

    return annotations
}
