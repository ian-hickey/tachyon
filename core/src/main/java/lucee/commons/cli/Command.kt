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
package lucee.commons.cli

import java.io.IOException

object Command {
    @Throws(IOException::class)
    fun createProcess(cmdline: String, translate: Boolean): Process {
        return if (!translate) Runtime.getRuntime().exec(cmdline) else Runtime.getRuntime().exec(toArray(cmdline))
    }

    @Throws(IOException::class, ExpressionException::class)
    fun createProcess(pc: PageContext?, commands: Array<String?>?, workingDir: String?): Process {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        var dir: FileResource? = null
        if (!StringUtil.isEmpty(workingDir, true)) {
            val res: Resource = ResourceUtil.toResourceExisting(pc, workingDir)
            if (!res.isDirectory()) throw IOException("CFEXECUTE Directory [$workingDir] is not a existing directory")
            dir = if (res is FileResource) res as FileResource else throw IOException(
                    "CFEXECUTE directory [" + workingDir + "] must be a local directory, scheme [" + res.getResourceProvider().getScheme() + "] is not supported in this context.")
        }
        return Runtime.getRuntime().exec(commands, null, dir)
    }

    @Throws(IOException::class, ExpressionException::class)
    fun createProcess(pc: PageContext?, commands: Array<String?>?): Process {
        return createProcess(pc, commands, null)
    }

    /**
     * @param cmdline command line
     * @param translate translate the command line or not
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Throws(IOException::class, InterruptedException::class)
    fun execute(cmdline: String, translate: Boolean): CommandResult {
        return if (!translate) execute(Runtime.getRuntime().exec(cmdline)) else execute(Runtime.getRuntime().exec(toArray(cmdline)))
    }

    @Throws(IOException::class, InterruptedException::class)
    fun execute(cmdline: Array<String?>?): CommandResult {
        return execute(Runtime.getRuntime().exec(cmdline))
    }

    @Throws(IOException::class, InterruptedException::class)
    fun execute(cmdline: List<String?>): CommandResult {
        return execute(Runtime.getRuntime().exec(cmdline.toArray(arrayOfNulls<String>(cmdline.size()))))
    }

    @Throws(IOException::class, InterruptedException::class)
    fun execute(cmd: String?, args: Array<String?>?): CommandResult {
        return execute(StringUtil.merge(cmd, args))
    }

    @Throws(IOException::class, InterruptedException::class)
    fun execute(p: Process): CommandResult {
        var `is`: InputStream? = null
        var es: InputStream? = null
        var ioe: IOException?
        return try {
            val `in` = StreamGobbler(p.getInputStream().also { `is` = it })
            val err = StreamGobbler(p.getErrorStream().also { es = it })
            `in`.start()
            err.start()
            if (p.waitFor() !== 0) {
                err.join()
                if (err.exception.also { ioe = it } != null) throw IOException(ioe)
                val str = err.string
                if (!StringUtil.isEmpty(str)) throw CommandException(str)
            }
            `in`.join()
            if (`in`.exception.also { ioe = it } != null) throw IOException(ioe)
            CommandResult(`in`.string, err.string)
        } finally {
            IOUtil.close(`is`, es)
        }
    }

    fun toList(str: String): List<String> {
        var str = str
        if (StringUtil.isEmpty(str)) return ArrayList<String>()
        str = str.trim()
        val sb = StringBuilder()
        val list: ArrayList<String> = ArrayList<String>()
        val carr: CharArray = str.toCharArray()
        var c: Char // ,last=0;
        var inside = 0.toChar()
        for (i in carr.indices) {
            c = carr[i]
            when (c) {
                '\'', '"' -> if (inside.toInt() == 0) {
                    if (str.lastIndexOf(c) > i) inside = c else sb.append(c)
                } else if (inside == c) {
                    inside = 0.toChar()
                } else sb.append(c)
                ' ', '\b', '\t', '\n', '\r', '\f' ->                // if(last=='\\')sb.setCharAt(sb.length()-1,c);
                    if (inside.toInt() == 0) {
                        populateList(sb, list)
                    } else sb.append(c)
                else -> sb.append(c)
            }
        }
        populateList(sb, list)
        return list
    }

    fun toArray(str: String): Array<String> {
        val list = toList(str)
        return list.toArray(arrayOfNulls<String>(list.size()))
    }

    private fun populateList(sb: StringBuilder, list: ArrayList<String>) {
        var tmp: String = sb.toString()
        tmp = tmp.trim()
        if (tmp.length() > 0) list.add(tmp)
        sb.delete(0, sb.length())
    }
}

internal class StreamGobbler(`is`: InputStream) : Thread() {
    var `is`: InputStream

    /**
     * @return the str
     */
    var string: String? = null
        private set
    private var ioe: IOException? = null
    @Override
    fun run() {
        try {
            string = IOUtil.toString(`is`, SystemUtil.getCharset())
        } catch (ioe: IOException) {
            this.ioe = ioe
        }
    }

    /**
     * @return the ioe
     */
    val exception: IOException?
        get() = ioe

    init {
        this.`is` = `is`
    }
}