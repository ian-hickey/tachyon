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
package lucee.runtime.script

import java.io.File

abstract class BaseScriptEngineFactory(tag: Boolean, dialect: Int) : ScriptEngineFactory {
    private var factory: ScriptEngineFactory? = null
    @Override
    fun getEngineName(): String {
        return factory.getEngineName()
    }

    @Override
    fun getEngineVersion(): String {
        return factory.getEngineVersion()
    }

    @Override
    fun getExtensions(): List<String> {
        return factory.getExtensions()
    }

    @Override
    fun getMimeTypes(): List<String> {
        return factory.getMimeTypes()
    }

    @Override
    fun getNames(): List<String> {
        return factory.getNames()
    }

    @Override
    fun getLanguageName(): String {
        return factory.getLanguageName()
    }

    @Override
    fun getLanguageVersion(): String {
        return factory.getLanguageVersion()
    }

    @Override
    fun getParameter(key: String?): Object {
        return factory.getParameter(key)
    }

    @Override
    fun getMethodCallSyntax(obj: String?, m: String?, vararg args: String?): String {
        return factory.getMethodCallSyntax(obj, m, args)
    }

    @Override
    fun getOutputStatement(toDisplay: String?): String {
        return factory.getOutputStatement(toDisplay)
    }

    @Override
    fun getProgram(vararg statements: String?): String {
        return factory.getProgram(statements)
    }

    @Override
    fun getScriptEngine(): ScriptEngine {
        return factory.getScriptEngine()
    }

    init {
        try {
            System.setProperty("lucee.cli.call", "true")

            // returns null when not used within Lucee
            var engine: CFMLEngine? = null
            try {
                engine = CFMLEngineFactory.getInstance()
            } catch (re: RuntimeException) {
            }

            // create Engine
            if (engine == null) {
                val servletName = ""
                val attributes: Map<String, Object> = HashMap<String, Object>()
                val initParams: Map<String, String> = HashMap<String, String>()

                // Allow override of context root
                var rootPath: String = System.getProperty("lucee.cli.contextRoot")
                if (Util.isEmpty(rootPath)) {
                    // working directory that the java command was called from
                    rootPath = "."
                }
                val root = File(rootPath)
                val servletContext = ServletContextImpl(root, attributes, initParams, 1, 0)
                val servletConfig = ServletConfigImpl(servletContext, servletName)
                engine = CFMLEngineFactory.getInstance(servletConfig)
                servletContext.setLogger(engine.getCFMLEngineFactory().getLogger())
            }
            factory = if (tag) CFMLEngineFactory.getInstance().getTagEngineFactory(dialect) else CFMLEngineFactory.getInstance().getScriptEngineFactory(dialect)
        } catch (se: ServletException) {
            se.printStackTrace()
            throw se
        } catch (re: RuntimeException) {
            re.printStackTrace()
            throw re
        }
    }
}