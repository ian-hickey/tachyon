package tachyon.runtime.util

import java.util.HashSet

// LDEV-3333 / LDEV-3731
// this is just a partial implementation of HashSet<Object>, using System.identityHashCode
// instead of the default virtually dispatched <object-impl>.hashCode; this avoids the problem
// of "hashing arrays which contain themselves causing a stackoverflow" 
class ObjectIdentityHashSet {
    private val elements: HashSet<Integer?>? = HashSet<Integer?>()
    operator fun contains(`object`: Object?): Boolean {
        return elements.contains(System.identityHashCode(`object`))
    }

    fun add(`object`: Object?): Boolean {
        return elements.add(System.identityHashCode(`object`))
    }

    fun remove(`object`: Object?): Boolean {
        return elements.remove(System.identityHashCode(`object`))
    }
}