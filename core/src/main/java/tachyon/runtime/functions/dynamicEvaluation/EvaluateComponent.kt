/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package tachyon.runtime.functions.dynamicEvaluation

import java.util.Iterator

object EvaluateComponent {
    @Throws(PageException::class)
    fun call(pc: PageContext?, name: String?, md5: String?, sctThis: Struct?): Object? {
        return invoke(pc, name, md5, sctThis, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, name: String?, md5: String?, sctThis: Struct?, sctVariables: Struct?): Object? {
        return invoke(pc, name, md5, sctThis, sctVariables)
    }

    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, name: String?, md5: String?, sctThis: Struct?, sctVariables: Struct?): Component? {
        // Load comp
        var comp: Component? = null
        try {
            comp = pc.loadComponent(name)
            if (!ComponentUtil.md5(comp).equals(md5)) {
                LogUtil.log(pc, Log.LEVEL_INFO, EvaluateComponent::class.java.getName(), "component [" + name
                        + "] in this environment has not the same interface as the component to load, it is possible that one off the components has Functions added dynamically.")
            }
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        setInternalState(comp, sctThis, sctVariables)
        return comp
    }

    @Throws(PageException::class)
    fun setInternalState(comp: Component?, sctThis: Struct?, sctVariables: Struct?) {

        // this
        // delete this scope data members
        val cw: ComponentSpecificAccess = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, comp)
        val cwKeys: Array<Collection.Key?> = CollectionUtil.keys(cw)
        var member: Object
        for (i in cwKeys.indices) {
            member = cw.get(cwKeys[i])
            if (member is UDF) continue
            cw.removeEL(cwKeys[i])
        }

        // set this scope data members
        var it: Iterator<Entry<Key?, Object?>?> = sctThis.entryIterator()
        var e: Entry<Key?, Object?>?
        // keys = sctThis.keys();
        while (it.hasNext()) {
            e = it.next()
            comp.set(e.getKey(), e.getValue())
        }

        // Variables
        val scope: ComponentScope = comp.getComponentScope()

        // delete variables scope data members
        val sKeys: Array<Key?> = CollectionUtil.keys(scope)
        for (i in sKeys.indices) {
            if (KeyConstants._this.equals(sKeys[i])) continue
            if (scope.get(sKeys[i]) is UDF) continue
            scope.removeEL(sKeys[i])
        }

        // set variables scope data members
        it = sctVariables.entryIterator()
        // keys = sctVariables.keys();
        while (it.hasNext()) {
            e = it.next()
            scope.set(e.getKey(), e.getValue())
        }
    }
}