/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime

import lucee.commons.lang.ExceptionUtil

/**
 * A Page that can produce Components
 */
abstract class InterfacePageImpl : InterfacePage(), PagePro {
    override fun getHash(): Int {
        return 0
    }

    override fun getSourceLength(): Long {
        return 0
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?): Object? {
        try {
            pc.setSilent()
            var interf: InterfaceImpl? = null
            interf = try {
                newInstance(pc, getPageSource().getComponentName(), false) // TODO was only getComponentName before, is that change ok?
            } finally {
                pc.unsetSilent()
            }
            val qs: String = ReqRspUtil.getQueryString(pc.getHttpServletRequest())
            if (pc.getBasePageSource() === this.getPageSource() && pc.getConfig().debug()) pc.getDebugger().setOutput(false)
            val isPost: Boolean = pc.getHttpServletRequest().getMethod().equalsIgnoreCase("POST")

            // POST
            if (isPost) {
                // Soap
                if (ComponentPageImpl.isSoap(pc)) throw ApplicationException("can not instantiate interface [" + getPageSource().getComponentName().toString() + "] as a component")
            } else if (qs != null && qs.trim().equalsIgnoreCase("wsdl")) throw ApplicationException("can not instantiate interface [" + getPageSource().getComponentName().toString() + "] as a component")

            // WDDX
            if (pc.urlFormScope().containsKey(KeyConstants._method)) throw ApplicationException("can not instantiate interface [" + getPageSource().getComponentName().toString() + "] as a component")

            // invoking via include
            if (pc.getTemplatePath().size() > 1) {
                throw ApplicationException("can not invoke interface [" + getPageSource().getComponentName().toString() + "] as a page")
            }

            // DUMP
            // TODO component.setAccess(pc,Component.ACCESS_PUBLIC);
            val cdf: String = pc.getConfig().getComponentDumpTemplate()
            if (cdf != null && cdf.trim().length() > 0) {
                pc.variablesScope().set(KeyConstants._component, interf)
                pc.doInclude(cdf, false)
            } else pc.write(pc.getConfig().getDefaultDumpWriter(DumpWriter.DEFAULT_RICH).toString(pc, interf!!.toDumpData(pc, 9999, DumpUtil.toDumpProperties()), true))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t) // Exception Handler.castAnd Stack(t, this, pc);
        }
        return null
    }

    /**
     * default implementation of the static constructor, that does nothing
     */
    fun staticConstructor(pagecontext: PageContext?, cfc: ComponentImpl?) {
        // do nothing
    }

    @Throws(PageException::class)
    abstract fun initInterface(i: InterfaceImpl?)
    @Throws(lucee.runtime.exp.PageException::class)
    abstract fun newInstance(pc: PageContext?, callPath: String?, isRealPath: Boolean): InterfaceImpl?
}