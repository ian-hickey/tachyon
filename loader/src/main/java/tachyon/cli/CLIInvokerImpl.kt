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

import java.io.File

class CLIInvokerImpl(root: File?, servletName: String?) : CLIInvoker {
    private val servletConfig: ServletConfigImpl
    private val engine: CFMLEngine
    private var lastAccess: Long = 0

    @Override
    @Throws(RemoteException::class)
    override operator fun invoke(config: Map<String, String?>?) {
        lastAccess = try {
            engine.cli(config, servletConfig)
            System.currentTimeMillis()
        } catch (t: Throwable) {
            throw RemoteException("failed to call CFML Engine", t)
        }
    }

    fun lastAccess(): Long {
        return lastAccess
    }

    init {
        val attributes: Map<String, Object> = HashMap<String, Object>()
        val initParams: Map<String, String> = HashMap<String, String>()
        val param: String = Util._getSystemPropOrEnvVar("tachyon.cli.config", null)
        if (param != null && !param.isEmpty()) {
            initParams.put("tachyon-web-directory", File(param, "tachyon-web").getAbsolutePath())
            initParams.put("tachyon-server-directory", File(param).getAbsolutePath()) // will create a subfolder named tachyon-server
        } else initParams.put("tachyon-server-directory", File(root, "WEB-INF").getAbsolutePath())
        val servletContext = ServletContextImpl(root, attributes, initParams, 1, 0)
        servletConfig = ServletConfigImpl(servletContext, servletName)
        engine = CFMLEngineFactory.getInstance(servletConfig)
        servletContext.setLogger(engine.getCFMLEngineFactory().getLogger())
    }
}