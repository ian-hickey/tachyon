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
package lucee.runtime.instrumentation

import lucee.commons.lang.StringUtil

/**
 * Utilities for dealing with different Java vendors.
 */
enum class JavaVendor(vmClass: String?) {
    IBM("com.ibm.tools.attach.VirtualMachine"), SUN("com.sun.tools.attach.VirtualMachine"),  // When in doubt, try the Sun implementation.
    OTHER("com.sun.tools.attach.VirtualMachine");

    companion object {
        private val _vendor: JavaVendor? = null

        /**
         * This static worker method returns the current Vendor.
         */
        fun getCurrentVendor(): JavaVendor? {
            return _vendor
        }

        init {
            val vendor: String = System.getProperty("java.vendor")
            if (StringUtil.containsIgnoreCase(lucee.runtime.instrumentation.vendor, "SUN MICROSYSTEMS")) {
                _vendor = SUN
            } else if (StringUtil.containsIgnoreCase(lucee.runtime.instrumentation.vendor, "IBM")) {
                _vendor = IBM
            } else {
                _vendor = OTHER
            }
        }
    }

    private var _virtualMachineClass: String? = null

    /**
     * This static worker method returns **true** if the current implementation is IBM.
     */
    fun isIBM(): Boolean {
        return _vendor == IBM
    }

    /**
     * This static worker method returns **true** if the current implementation is Sun.
     */
    fun isSun(): Boolean {
        return _vendor == SUN
    }

    fun getVirtualMachineClassName(): String? {
        return _virtualMachineClass
    }

    init {
        _virtualMachineClass = vmClass
    }
}