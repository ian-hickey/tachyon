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
package tachyon.runtime.jsr223

import java.util.List

class ScriptEngineFactoryImpl(engine: CFMLEngine?, tag: Boolean, dialect: Int) : ScriptEngineFactory {
    val engine: CFMLEngine?
    val tag: Boolean
    val dialect: Int
    val isCFML: Boolean
    @Override
    fun getExtensions(): List<String?>? {
        return ListUtil.arrayToList(if (isCFML) Constants.getCFMLExtensions() else Constants.getTachyonExtensions())
    }

    @Override
    fun getMimeTypes(): List<String?>? {
        return ListUtil.arrayToList(if (isCFML) Constants.CFML_MIMETYPES else Constants.LUCEE_MIMETYPES)
    }

    @Override
    fun getNames(): List<String?>? {
        return ListUtil.arrayToList(if (dialect == CFMLEngine.DIALECT_CFML) Constants.CFML_ALIAS_NAMES else Constants.LUCEE_ALIAS_NAMES)
    }

    @Override
    fun getParameter(key: String?): Object? {
        if (key.equalsIgnoreCase(ScriptEngine.NAME)) return ConfigWebUtil.toDialect(dialect, "")
        if (key.equalsIgnoreCase(ScriptEngine.ENGINE)) return Constants.NAME.toString() + " (dialect:" + ConfigWebUtil.toDialect(dialect, "") + ")"
        if (key.equalsIgnoreCase(ScriptEngine.ENGINE_VERSION) || key.equalsIgnoreCase(ScriptEngine.LANGUAGE_VERSION)) return engine.getInfo().getVersion().toString()
        if (key.equalsIgnoreCase(ScriptEngine.LANGUAGE)) return (if (isCFML) Constants.CFML_NAME else Constants.LUCEE_NAME).toLowerCase().toString() + if (tag) "-tag" else ""
        if (key.equalsIgnoreCase("THREADING")) return "THREAD-ISOLATED"
        throw IllegalArgumentException("Invalid key")
    }

    @Override
    fun getMethodCallSyntax(obj: String?, m: String?, vararg args: String?): String? {
        val sb = StringBuilder()
        if (tag) sb.append("<").append(getSetTagName()).append(" ")
        sb.append(obj).append('.').append(m).append('(')
        if (args != null) for (i in 0 until args.size) {
            sb.append("'")
            sb.append(escape(args[i]))
            sb.append("'")
            if (i == args.size - 1) sb.append(')') else sb.append(',')
        }
        if (tag) sb.append(">") else sb.append(";")
        return sb.toString()
    }

    @Override
    fun getOutputStatement(toDisplay: String?): String? {
        val sb = StringBuilder()
        if (tag) sb.append("<").append(getSetTagName()).append(" ")
        sb.append("echo(").append("'").append(escape(toDisplay)).append("'").append(")")
        if (tag) sb.append(">") else sb.append(";")
        return sb.toString()
    }

    @Override
    fun getProgram(vararg statements: String?): String? {
        // String name=getScriptTagName();
        val sb = StringBuilder() /*
		 * .append("<") .append(name) .append(">\n")
		 */
        val len = statements.size
        for (i in 0 until len) {
            sb.append(statements[i]).append(";\n")
        }
        // sb.append("</").append(name).append(">");
        return sb.toString()
    }

    private fun getScriptTagName(): String? {
        val prefix: String = (ThreadLocalPageContext.getConfig() as ConfigPro).getCoreTagLib(dialect).getNameSpaceAndSeparator()
        return prefix + if (dialect == CFMLEngine.DIALECT_CFML) Constants.CFML_SCRIPT_TAG_NAME else Constants.LUCEE_SCRIPT_TAG_NAME
    }

    private fun getSetTagName(): String? {
        val prefix: String = (ThreadLocalPageContext.getConfig() as ConfigPro).getCoreTagLib(dialect).getNameSpaceAndSeparator()
        return prefix + if (dialect == CFMLEngine.DIALECT_CFML) Constants.CFML_SET_TAG_NAME else Constants.LUCEE_SET_TAG_NAME
    }

    @Override
    fun getScriptEngine(): ScriptEngine? {
        return ScriptEngineImpl(this)
    }

    private fun escape(str: String?): Object? {
        return StringUtil.replace(str, "'", "''", false)
    }

    fun getName(): String? {
        return getParameter(ScriptEngine.NAME)
    }

    @Override
    fun getEngineName(): String? {
        return getParameter(ScriptEngine.ENGINE)
    }

    @Override
    fun getEngineVersion(): String? {
        return getParameter(ScriptEngine.ENGINE_VERSION)
    }

    @Override
    fun getLanguageName(): String? {
        return getParameter(ScriptEngine.LANGUAGE)
    }

    @Override
    fun getLanguageVersion(): String? {
        return getParameter(ScriptEngine.LANGUAGE_VERSION)
    }

    init {
        this.engine = engine
        this.tag = tag
        this.dialect = dialect
        isCFML = dialect == CFMLEngine.DIALECT_CFML
    }
}