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
package tachyon.cli

import java.rmi.registry.Registry

class Closer(reg: Registry?, invoker: CLIInvokerImpl, name: String, idleTime: Long) : Thread() {
    private val name: String
    private val reg: Registry?
    private val idleTime: Long
    private val invoker: CLIInvokerImpl
    @Override
    fun run() {
        // idle
        do sleepEL(idleTime) while (invoker.lastAccess() + idleTime > System.currentTimeMillis())
        try {
            reg.unbind(name)
            UnicastRemoteObject.unexportObject(invoker, true)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun sleepEL(millis: Long) {
        try {
            sleep(millis)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    init {
        this.reg = reg
        this.name = name
        this.idleTime = idleTime
        this.invoker = invoker
    }
}