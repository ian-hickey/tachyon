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
package tachyon.commons.lang

import java.io.IOException

/**
 * Directory ClassLoader
 */
class PCLCollection(mapping: MappingImpl, directory: Resource, resourceCL: ClassLoader, maxBlockSize: Int) {
    private val directory: Resource
    private val resourceCL: ClassLoader
    private val maxBlockSize: Int
    private val mapping: MappingImpl
    private val cfcs: LinkedList<PCLBlock> = LinkedList<PCLBlock>()
    private val cfms: LinkedList<PCLBlock> = LinkedList<PCLBlock>()
    private var cfc: PCLBlock
    private var cfm: PCLBlock
    private val index: Map<String, PCLBlock> = HashMap<String, PCLBlock>()
    private fun current(isCFC: Boolean): PCLBlock {
        if ((if (isCFC) cfc.count() else cfm.count()) >= maxBlockSize) {
            synchronized(if (isCFC) cfcs else cfms) {
                if (isCFC) {
                    cfc = PCLBlock(directory, resourceCL)
                    cfcs.add(cfc)
                } else {
                    cfm = PCLBlock(directory, resourceCL)
                    cfms.add(cfm)
                }
            }
        }
        return if (isCFC) cfc else cfm
    }

    @Synchronized
    fun loadClass(name: String, barr: ByteArray, isCFC: Boolean): Class<*> {
        // if class is already loaded flush the classloader and do new classloader
        val block: PCLBlock? = index[name]
        if (block != null) {

            // flush classloader when update is not possible
            mapping.clearPages(block)
            StructUtil.removeValue(index, block)
            if (isCFC) {
                cfcs.remove(block)
                if (block === cfc) cfc = PCLBlock(directory, resourceCL)
            } else {
                cfms.remove(block)
                if (block === cfm) cfm = PCLBlock(directory, resourceCL)
            }
        }

        // load class from byte array
        val c: PCLBlock = current(isCFC)
        index.put(name, c)
        return c.loadClass(name, barr)
    }

    /**
     * load existing class
     *
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    @Synchronized
    @Throws(ClassNotFoundException::class)
    fun loadClass(className: String): Class<*> {
        // if class is already loaded flush the classloader and do new classloader
        val cl: PCLBlock? = index[className]
        if (cl != null) {
            return cl.loadClass(className)
        }
        throw ClassNotFoundException("class $className not found")
    }

    @Synchronized
    fun getResourceAsStream(name: String?): InputStream? {
        return current(false).getResourceAsStream(name)
    }

    fun count(): Long {
        return index.size()
    }

    /**
     * shrink the classloader elements
     *
     * @return how many page have removed from classloaders
     */
    @Synchronized
    fun shrink(force: Boolean): Int {
        val before: Int = index.size()

        // CFM
        var flushCFM = 0
        while (cfms.size() > 1) {
            flush(cfms.poll())
            flushCFM++
        }

        // CFC
        if (force && flushCFM < 2 && cfcs.size() > 1) {
            flush(oldest(cfcs))
            if (cfcs.size() > 1) flush(cfcs.poll())
        }
        // print.o("shrink("+mapping.getVirtual()+"):"+(before-index.size())+">"+force+";"+(flushCFM));
        return before - index.size()
    }

    private fun flush(cl: PCLBlock) {
        mapping.clearPages(cl)
        StructUtil.removeValue(index, cl)
        // System.gc(); gc is in Controller call, to make sure gc is only called once
    }

    companion object {
        private fun oldest(queue: LinkedList<PCLBlock>): PCLBlock {
            val index: Int = NumberUtil.randomRange(0, queue.size() - 2)
            return queue.remove(index)
            // return queue.poll();
        }
    }

    /**
     * Constructor of the class
     *
     * @param directory
     * @param parent
     * @throws IOException
     */
    init {
        // check directory
        if (!directory.exists()) directory.mkdirs()
        if (!directory.isDirectory()) throw IOException("resource $directory is not a directory")
        if (!directory.canRead()) throw IOException("no access to $directory directory")
        this.directory = directory
        this.mapping = mapping
        // this.pcl=systemCL;
        this.resourceCL = resourceCL
        cfc = PCLBlock(directory, resourceCL)
        cfcs.add(cfc)
        cfm = PCLBlock(directory, resourceCL)
        cfms.add(cfm)
        this.maxBlockSize = maxBlockSize
    }
}