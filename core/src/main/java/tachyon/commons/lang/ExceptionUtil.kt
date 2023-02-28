/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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

object ExceptionUtil {
    fun toString(trace: Array<StackTraceElement?>): String {
        val sb = StringBuilder()
        // Print our stack trace
        for (ste in trace) sb.append("\tat ").append(ste).append('\n')
        return sb.toString()
    }

    fun getStacktrace(t: Throwable, addMessage: Boolean): String {
        return getStacktrace(t, addMessage, true)
    }

    fun getStacktrace(t: Throwable, addMessage: Boolean, onlyTachyonPart: Boolean): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        t.printStackTrace(pw)
        pw.close()
        var st: String = sw.toString()
        // shrink the stacktrace
        if (onlyTachyonPart && st.indexOf("Caused by:") === -1) {
            var index: Int = st.indexOf("tachyon.loader.servlet.CFMLServlet.service(")
            if (index == -1) index = st.indexOf("tachyon.runtime.jsr223.ScriptEngineImpl.eval(")
            if (index != -1) {
                index = st.indexOf(")", index + 1)
                if (index != -1) {
                    st = st.substring(0, index + 1).toString() + "\n..."
                }
            }
        }
        val msg: String = t.getMessage()
        if (addMessage && !StringUtil.isEmpty(msg) && !st.startsWith(msg.trim())) st = """
     $msg
     $st
     """.trimIndent()
        return st
    }

    fun getMessage(t: Throwable): String {
        var msg: String = t.getMessage()
        if (StringUtil.isEmpty(msg, true)) msg = t.getClass().getName()
        val sb = StringBuilder(msg)
        if (t is PageException) {
            val pe: PageException = t as PageException
            val detail: String = pe.getDetail()
            if (!StringUtil.isEmpty(detail, true)) {
                sb.append('\n')
                sb.append(detail)
            }
        }
        return sb.toString()
    }

    fun addHint(pe: PageExceptionImpl, hint: String?): PageException {
        pe.setAdditional(KeyConstants._Hint, hint)
        return pe
    }

    /**
     * creates a message for key not found with soundex check for similar key
     *
     * @param _keys
     * @param keyLabel
     * @return
     */
    fun similarKeyMessage(_keys: Array<Collection.Key>, keySearched: String, keyLabels: String, `in`: String, listAll: Boolean): String {
        var listAll = listAll
        val inThe = if (StringUtil.isEmpty(`in`, true)) "" else " in the $`in`"
        val empty = _keys.size == 0
        if (listAll && (_keys.size > 50 || empty)) {
            listAll = false
        }
        var list: String? = null
        if (listAll) {
            Arrays.sort(_keys)
            list = ListUtil.arrayToList(_keys, ", ")
        }
        val keySearchedSoundex: String = StringUtil.soundex(keySearched)
        for (i in _keys.indices) {
            val k: String = _keys[i].getString()
            if (StringUtil.soundex(k).equals(keySearchedSoundex)) {
                if (keySearched.equalsIgnoreCase(k)) continue  // must be a null value in a partial null-support environment
                var appendix = ""
                if (listAll) appendix = "Here is a complete list of all available $keyLabels [ $list ]." else if (empty) appendix = "The structure is empty"
                return appendix
            }
        }
        var appendix = ""
        if (listAll) appendix = "Only the following $keyLabels are available [ $list ]." else if (empty) appendix = "The structure is empty"
        return appendix
    }

    fun similarKeyMessage(_keys: Array<Collection.Key>, keySearched: String, keyLabel: String, keyLabels: String?, `in`: String, listAll: Boolean): String {
        var listAll = listAll
        val inThe = if (StringUtil.isEmpty(`in`, true)) "" else " in the $`in`"
        val empty = _keys.size == 0
        if (listAll && (_keys.size > 50 || empty)) {
            listAll = false
        }
        var list: String? = null
        if (listAll) {
            Arrays.sort(_keys)
            list = ListUtil.arrayToList(_keys, ", ")
        }
        val keySearchedSoundex: String = StringUtil.soundex(keySearched)
        for (i in _keys.indices) {
            val k: String = _keys[i].getString()
            if (StringUtil.soundex(k).equals(keySearchedSoundex)) {
                if (keySearched.equalsIgnoreCase(k)) continue  // must be a null value in a partial null-support environment
                return ("The " + keyLabel + " [" + keySearched + "] does not exist" + inThe + ", but there is a similar " + keyLabel + " with name [" + _keys[i].getString()
                        + "] available.")
            }
        }
        return "The $keyLabel [$keySearched] does not exist$inThe"
    }

    fun similarKeyMessage(coll: Collection?, keySearched: String?, keyLabel: String?, keyLabels: String?, `in`: String?, listAll: Boolean): String {
        return similarKeyMessage(CollectionUtil.keys(coll), keySearched, keyLabel, keyLabels, `in`, listAll)
    }

    fun similarKeyMessage(coll: Collection?, keySearched: String?, keyLabels: String?, `in`: String?, listAll: Boolean): String {
        return similarKeyMessage(CollectionUtil.keys(coll), keySearched, keyLabels, `in`, listAll)
    }

    fun toIOException(t: Throwable): IOException {
        rethrowIfNecessary(t)
        if (t is IOException) return t as IOException
        if (t is InvocationTargetException) return toIOException((t as InvocationTargetException).getCause())
        if (t is NativeException) return toIOException((t as NativeException).getCause())
        val ioe = IOException(t.getClass().getName().toString() + ":" + t.getMessage())
        ioe.setStackTrace(t.getStackTrace())
        return ioe
    }

    fun createSoundexDetail(name: String?, it: Iterator<String>, keyName: String): String {
        val sb = StringBuilder()
        var k: String
        val sname: String = StringUtil.soundex(name)
        while (it.hasNext()) {
            k = it.next()
            if (StringUtil.soundex(k).equals(sname)) return "did you mean [$k]"
            if (sb.length() !== 0) sb.append(',')
            sb.append(k)
        }
        return "available $keyName are [$sb]"
    }

    fun toRuntimeException(t: Throwable?): RuntimeException {
        rethrowIfNecessary(t)
        // TODO is there an improvement necessary?
        return RuntimeException(t)
    }

    private fun unwrap(t: Throwable?): Throwable? {
        if (t == null) return t
        if (t is NativeException) return unwrap((t as NativeException).getException())
        val cause: Throwable = t.getCause()
        return if (cause != null && cause !== t) unwrap(cause) else t
    }

    /**
     * A java.lang.ThreadDeath must never be caught, so any catch(Throwable t) must go through this
     * method in order to ensure that the throwable is not of type ThreadDeath
     *
     * @param t the thrown Throwable
     */
    fun rethrowIfNecessary(t: Throwable?) {
        if (unwrap(t) is ThreadDeath) throw t as ThreadDeath? // never catch a ThreadDeath
    }

    fun getThrowingPosition(pc: PageContext?, t: Throwable): TemplateLine? {
        val cause: Throwable = t.getCause()
        if (cause != null) getThrowingPosition(pc, cause)
        val traces: Array<StackTraceElement> = t.getStackTrace()
        var template: String
        for (trace in traces) {
            template = trace.getFileName()
            if (trace.getLineNumber() <= 0 || template == null || ResourceUtil.getExtension(template, "").equals("java")) continue
            return TemplateLine(abs(pc as PageContextImpl?, template), trace.getLineNumber())
        }
        return null
    }

    private fun abs(pc: PageContextImpl?, template: String?): String? {
        val config: ConfigWeb = pc.getConfig()
        var res: Resource? = config.getResource(template)
        if (res.exists()) return template
        val ps: PageSource? = if (pc == null) null else pc.getPageSource(template)
        res = if (ps == null) null else ps.getPhyscalFile()
        if (res == null || !res.exists()) {
            res = config.getResource(ps.getDisplayPath())
            if (res != null && res.exists()) return res.getAbsolutePath()
        } else return res.getAbsolutePath()
        return template
    }

    fun toThrowable(stackTrace: Array<StackTraceElement?>?): Throwable {
        val t = Throwable()
        t.setStackTrace(stackTrace)
        return t
    }
}