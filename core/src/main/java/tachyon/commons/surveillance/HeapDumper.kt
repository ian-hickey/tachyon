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
package tachyon.commons.surveillance

import java.io.IOException

object HeapDumper {
    /**
     * Dumps the heap to the outputFile file in the same format as the hprof heap dump. If this method
     * is called remotely from another process, the heap dump output is written to a file named
     * outputFile on the machine where the target VM is running. If outputFile is a relative path, it is
     * relative to the working directory where the target VM was started.
     *
     * @param res Resource to write the .hprof file.
     * @param live if true dump only live objects i.e. objects that are reachable from others
     * @throws IOException
     */
    @Throws(IOException::class)
    fun dumpTo(res: Resource, live: Boolean) {
        val mbserver: MBeanServer = ManagementFactory.getPlatformMBeanServer()
        val mxbean: HotSpotDiagnosticMXBean = ManagementFactory.newPlatformMXBeanProxy(mbserver, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean::class.java)
        val path: String
        var tmp: Resource? = null
        if (res is FileResource) path = res.getAbsolutePath() else {
            tmp = SystemUtil.getTempFile("hprof", false)
            path = tmp.getAbsolutePath()
        }
        try {
            // it only
            mxbean.dumpHeap(path, live)
        } finally {
            if (tmp != null && tmp.exists()) {
                tmp.moveTo(res)
            }
        }
    }
}