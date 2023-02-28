/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.loader.osgi

import java.util.ArrayList

class BundleCollection(felix: Felix, master: Bundle, slaves: List<Bundle>?) {
    val core: Bundle
    private val slaves: List<Bundle>
    val felix: Felix
    fun getSlaves(): Iterator<Bundle> {
        return slaves.iterator()
    }

    val slaveCount: Int
        get() = slaves.size()
    val bundleContext: BundleContext
        get() = felix.getBundleContext()

    init {
        this.felix = felix
        core = master
        this.slaves = ArrayList<Bundle>()
        if (slaves != null) for (slave in slaves) if (!slave.equals(master)) this.slaves.add(slave)
    }
}