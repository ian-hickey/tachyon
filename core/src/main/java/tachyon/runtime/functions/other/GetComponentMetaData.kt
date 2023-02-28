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
 * Implements the CFML Function getmetadata
 */
package tachyon.runtime.functions.other

import tachyon.runtime.Component

object GetComponentMetaData : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?, obj: Object?): Struct? {
        return if (obj is Component) {
            (obj as Component?).getMetaData(pc)
        } else try {
            // Component cfc = CreateObject.doComponent(pc, Caster.toString(obj));
            val cfc: Component = ComponentLoader.searchComponent(pc, null, Caster.toString(obj), null, null, false, true /* MUST false does not produce properties */, false)
            cfc.getMetaData(pc)
        } // TODO better solution
        catch (ae: ApplicationException) {
            try {
                val inter: InterfaceImpl = ComponentLoader.searchInterface(pc, (pc as PageContextImpl?).getCurrentPageSource(null), Caster.toString(obj), true, false)
                inter.getMetaData(pc)
            } catch (pe: PageException) {
                throw ae
            }
        }
        // load existing meta without loading the cfc
        /*
		 * try{ Page page = ComponentLoader.loadPage(pc,((PageContextImpl)pc).getCurrentPageSource(null),
		 * Caster.toString(obj), null,null); if(page.metaData!=null && page.metaData.get()!=null) return
		 * page.metaData.get(); }catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
		 */

        // load the cfc when metadata was not defined before
    }
}