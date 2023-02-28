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
package lucee.cli

import java.io.File

object MainEntryPoint {
    @Throws(Throwable::class)
    fun main(args: Array<String?>) {
        var libDir: File = File("./").getCanonicalFile()
        System.out.println(libDir)

        // Fix for tomcat
        if (libDir.getName().equals(".") || libDir.getName().equals("..")) libDir = libDir.getParentFile()
        var children: Array<File> = libDir.listFiles(ExtFilter())
        if (children.size < 2) {
            libDir = File(libDir, "lib")
            children = libDir.listFiles(ExtFilter())
        }
        val urls: Array<URL?> = arrayOfNulls<URL>(children.size)
        System.out.println("Loading Jars")
        for (i in children.indices) {
            urls[i] = URL("jar:file://" + children[i] + "!/")
            System.out.println("- " + urls[i])
        }
        System.out.println()
        var cl: URLClassLoader? = null
        try {
            cl = URLClassLoader(urls, ClassLoader.getSystemClassLoader())
            val cli: Class<*> = cl.loadClass("lucee.cli.CLI")
            val main: Method = cli.getMethod("main", arrayOf<Class>(Array<String>::class.java))
            main.invoke(null, arrayOf(args))
        } finally {
            if (cl != null) try {
                cl.close()
            } catch (ioe: IOException) {
            }
        }
    }

    class ExtFilter : FilenameFilter {
        private val ext = ".jar"
        @Override
        fun accept(dir: File?, name: String): Boolean {
            return name.toLowerCase().endsWith(ext)
        }
    }
}