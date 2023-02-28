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
/**
 * Implements the CFML Function isdate
 */
package lucee.runtime.functions.decision

import java.io.IOException

object IsInstanceOf : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?, obj: Object?, typeName: String?): Boolean {
        if (obj is Component) return (obj as Component?).instanceOf(typeName)
        if (obj is JavaObject) {
            return try {
                Reflector.isInstaneOf((pc as PageContextImpl?).getClassLoader(), (obj as JavaObject?).getClazz(), typeName)
            } catch (ioe: IOException) {
                throw Caster.toPageException(ioe)
            }
        }
        return if (obj is ObjectWrap) call(pc, (obj as ObjectWrap?).getEmbededObject(), typeName) else try {
            Reflector.isInstaneOf((pc as PageContextImpl?).getClassLoader(), obj.getClass(), typeName)
        } catch (ioe: IOException) {
            throw Caster.toPageException(ioe)
        }
    }
}