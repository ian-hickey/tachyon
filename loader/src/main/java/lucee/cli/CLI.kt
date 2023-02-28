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

object CLI {
    /*
	 * Config
	 * 
	 * webroot - webroot directory servlet-name - name of the servlet (default:CFMLServlet) server-name
	 * - server name (default:localhost) uri - host/scriptname/query cookie - cookies (same pattern as
	 * query string) form - form (same pattern as query string)
	 */
    @Throws(ServletException::class, IOException::class, JspException::class)
    fun main(args: Array<String>?) {
        val config = toMap(args)
        System.setProperty("lucee.cli.call", "true")
        val useRMI: Boolean = "true".equalsIgnoreCase(config["rmi"])
        val root: File
        val param = config["webroot"]
        if (Util.isEmpty(param, true)) {
            root = File(".") // working directory that the java command was called from
            config.put("webroot", root.getAbsolutePath())
        } else {
            root = File(param)
            root.mkdirs()
        }
        var servletName = config["servlet-name"]
        if (Util.isEmpty(servletName, true)) servletName = "CFMLServlet"
        if (useRMI) {
            val factory = CLIFactory(root, servletName, config)
            factory.setDaemon(false)
            factory.start()
        } else {
            val invoker = CLIInvokerImpl(root, servletName)
            invoker.invoke(config)
        }
    }

    private fun toMap(args: Array<String>?): Map<String, String> {
        var index: Int
        var raw: String
        var key: String
        var value: String
        val config: Map<String, String> = HashMap<String, String>()
        if (args != null && args.size > 0) for (arg in args) {
            raw = arg.trim()
            if (raw.startsWith("-")) raw = raw.substring(1)
            if (!raw.isEmpty()) {
                index = raw.indexOf('=')
                if (index == -1) {
                    key = raw
                    value = ""
                } else {
                    key = raw.substring(0, index).trim()
                    value = raw.substring(index + 1).trim()
                }
                config.put(key.toLowerCase(), value)
            }
        }
        return config
    }
}