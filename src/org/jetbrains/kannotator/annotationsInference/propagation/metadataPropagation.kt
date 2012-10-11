package org.jetbrains.kannotator.annotationsInference.propagation

import java.util.HashSet
import kotlinlib.*
import org.jetbrains.kannotator.classHierarchy.HierarchyGraph
import org.jetbrains.kannotator.classHierarchy.HierarchyNode
import org.jetbrains.kannotator.declarations.Annotations
import org.jetbrains.kannotator.declarations.AnnotationsImpl
import org.jetbrains.kannotator.declarations.Method
import org.jetbrains.kannotator.declarations.MutableAnnotations
import org.jetbrains.kannotator.declarations.PositionWithinDeclaration
import org.jetbrains.kannotator.declarations.PositionsForMethod
import org.jetbrains.kannotator.declarations.Variance.*
import org.jetbrains.kannotator.declarations.getValidPositions
import java.util.LinkedHashSet
import org.jetbrains.kannotator.classHierarchy.parentNodes

fun propagateMetadata<A>(
        graph: HierarchyGraph<Method>,
        lattice: AnnotationLattice<A>,
        annotations: Annotations<A>
): Annotations<A> {
    val result = AnnotationsImpl(annotations)

    val leafMethods = graph.nodes.filter { it.children.isEmpty() }

    val visited = HashSet<Method>()
    for (leafMethod in leafMethods) {
        visited.addAll(resolveAnnotationConflicts(leafMethod, lattice, result))
    }

    val allMethods = graph.nodes.map{ n -> n.method }.toSet()
    val unvisited = allMethods - visited
    assert (unvisited.isEmpty()) { "Methods not visited: $unvisited" }

    return result
}

private fun resolveAnnotationConflicts<A>(
        leafMethod: HierarchyNode<Method>,
        lattice: AnnotationLattice<A>,
        annotationsToFix: MutableAnnotations<A>
): Set<Method> {
    val visited = HashSet<Method>()
    val propagatedAnnotations = AnnotationsImpl(annotationsToFix)

    val queue = LinkedHashSet<HierarchyNode<Method>>()
    queue.add(leafMethod)
    while (!queue.isEmpty()) {
        val node = queue.removeFirst()
        val method = node.method
        visited.add(method)

        val typePositions = PositionsForMethod(method).getValidPositions()

        val parents = node.parentNodes()
        for (parent in parents) {
            val positionsWithinParent = PositionsForMethod(parent.method)

            for (positionInChild in typePositions) {
                val relativePosition = positionInChild.relativePosition
                val positionInParent = positionsWithinParent[relativePosition].position

                val fromChild = propagatedAnnotations[positionInChild]
                val inParent = annotationsToFix[positionInParent]

                if (fromChild != null) {
                    if (inParent != null) {
                        annotationsToFix[positionInParent] = lattice.resolveConflictInParent<A>(
                                relativePosition, inParent, fromChild)
                    }
                    else {
                        propagatedAnnotations[positionInParent] = fromChild
                    }
                }
            }
        }
        queue.addAll(parents)
    }

    return visited
}

fun <A> AnnotationLattice<A>.resolveConflictInParent(position: PositionWithinDeclaration, parent: A, child: A): A {
    return when (position.variance) {
        COVARIANT -> leastCommonUpperBound(parent, child)
        CONTRAVARIANT -> greatestCommonLowerBound(parent, child)
        INVARIANT -> throw UnsupportedOperationException("Invariant position is not supported: $position")
    }
}

private val HierarchyNode<Method>.method: Method
    get() = data