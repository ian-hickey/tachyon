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
package tachyon.intergral.fusiondebug.server

import java.util.ArrayList

class FDStackFrameImpl(thread: FDThreadImpl?, pc: PageContextImpl?, ps: PageSource?, line: Int) : IFDStackFrame {
    private val pc: PageContextImpl? = null
    private val thread: FDThreadImpl? = null

    // private StackTraceElement trace;
    private val ps: PageSource? = null
    private val line = 0

    constructor(thread: FDThreadImpl?, pc: PageContextImpl?, trace: StackTraceElement?, ps: PageSource?) : this(thread, pc, ps, trace.getLineNumber()) {}

    @Override
    @Throws(FDLanguageException::class)
    fun evaluate(expression: String?): IFDVariable? {
        return try {
            FDVariable(this, expression, FDCaster.toFDValue(this, pc.evaluate(expression)))
        } catch (e: PageException) {
            throw FDLanguageException(e)
        }
    }

    @Override
    fun getExecutionUnitName(): String? {
        return ClassUtil.extractName(ps.getClassName())
    }

    @Override
    fun getExecutionUnitPackage(): String? {
        return ClassUtil.extractPackage(ps.getClassName())
    }

    @Override
    fun getLineNumber(): Int {
        return line
    }

    @Override
    fun getSourceFileName(): String? {
        return ps.getFileName()
    }

    @Override
    fun getSourceFilePath(): String? {
        val name = getSourceFileName()
        var path: String? = ps.getDisplayPath()
        if (StringUtil.endsWithIgnoreCase(path, name)) path = path.substring(0, path!!.length() - name!!.length())
        return path
    }

    @Override
    fun getThread(): IFDThread? {
        return thread
    }

    @Override
    fun getScopeNames(): List<String?>? {
        val implScopes: List<String?> = pc.undefinedScope().getScopeNames()
        for (i in SCOPES_AS_INT.indices) {
            if (!implScopes.contains(SCOPES_AS_STRING!![i]) && enabled(pc, SCOPES_AS_INT!![i])) implScopes.add(SCOPES_AS_STRING[i])
        }
        return implScopes
    }

    @Override
    fun getVariables(): List? {
        val it: Iterator = getScopeNames()!!.iterator()
        val list: List = ArrayList()
        while (it.hasNext()) {
            try {
                getVariables(this, pc, list, it.next() as String)
            } catch (e: FDLanguageException) {
                LogUtil.log("integral", e)
            }
        }
        return sort(list)
    }

    @Override
    @Throws(FDLanguageException::class)
    fun getVariables(strScope: String?): List? {
        return sort(getVariables(this, pc, ArrayList(), strScope))
    }

    @Override
    fun getFrameInformation(): String? {
        return ps.getRealpathWithVirtual()
    }

    @Override
    override fun toString(): String {
        return ("path:" + getSourceFilePath() + ";name:" + getSourceFileName() + ";unit-pack:" + getExecutionUnitPackage() + ";unit-name:" + getExecutionUnitName() + ";line:"
                + getLineNumber())
    }

    companion object {
        private val SCOPES_AS_INT: IntArray? = intArrayOf(Scope.SCOPE_VARIABLES, Scope.SCOPE_CGI, Scope.SCOPE_URL, Scope.SCOPE_FORM, Scope.SCOPE_COOKIE, Scope.SCOPE_CLIENT,
                Scope.SCOPE_APPLICATION, Scope.SCOPE_CALLER, Scope.SCOPE_CLUSTER, Scope.SCOPE_REQUEST, Scope.SCOPE_SERVER, Scope.SCOPE_SESSION)
        private val SCOPES_AS_STRING: Array<String?>? = arrayOf("variables", "cgi", "url", "form", "cookie", "client", "application", "caller", "cluster", "request", "server",
                "session")
        private val comparator: Comparator? = FDVariableComparator()
        fun testScopeNames(pc: PageContextImpl?): List<String?>? {
            return FDStackFrameImpl(null, pc, null, null).getScopeNames()
        }

        private fun enabled(pc: PageContextImpl?, scope: Int): Boolean {
            if (Scope.SCOPE_CLIENT === scope) {
                return pc.getApplicationContext().isSetClientManagement()
            }
            if (Scope.SCOPE_SESSION === scope) {
                return pc.getApplicationContext().isSetSessionManagement()
            }
            if (Scope.SCOPE_CALLER === scope) {
                return pc.undefinedScope().get(KeyConstants._caller, null) is Struct
            }
            return if (Scope.SCOPE_CLUSTER === scope) {
                try {
                    pc.clusterScope() !is ClusterNotSupported
                } catch (e: PageException) {
                    false
                }
            } else true
        }

        fun testVariables(pc: PageContextImpl?): List? {
            return FDStackFrameImpl(null, pc, null, null).getVariables()
        }

        @Throws(FDLanguageException::class)
        fun testVariables(pc: PageContextImpl?, strScope: String?): List? {
            return FDStackFrameImpl(null, pc, null, null).getVariables(strScope)
        }

        @Throws(FDLanguageException::class)
        private fun getVariables(frame: FDStackFrameImpl?, pc: PageContextImpl?, list: List?, strScope: String?): List? {
            val scope: Scope
            try {
                scope = pc.scope(strScope, null)
                if (scope != null) return copyValues(frame, list, scope)
                val value: Object = pc.undefinedScope().get(strScope, null)
                if (value != null) {
                    if (value is Struct) return copyValues(frame, ArrayList(), value as Struct)
                    throw FDLanguageException("[" + strScope + "] is not of type scope, type is [" + Caster.toTypeName(value) + "]")
                }
                throw FDLanguageException("[$strScope] does not exist in the current context")
            } catch (e: PageException) {
                throw FDLanguageException(e)
            }
        }

        /**
         * copy all data from given struct to given list and translate it to a FDValue
         *
         * @param to list to fill with values
         * @param from struct to read values from
         * @return the given list
         */
        private fun copyValues(frame: FDStackFrameImpl?, to: List?, from: Struct?): List? {
            val it: Iterator = from.entrySet().iterator()
            var entry: Entry
            while (it.hasNext()) {
                entry = it.next() as Entry
                to.add(FDVariable(frame, entry.getKey() as String, FDCaster.toFDValue(frame, entry.getValue())))
            }
            return to
        }

        private fun sort(list: List?): List? {
            Collections.sort(list, comparator)
            return list
        }
    }

    init {
        this.thread = thread
        this.pc = pc
        this.line = line
        this.ps = ps
    }
}

internal class FDVariableComparator : Comparator {
    @Override
    fun compare(o1: Object?, o2: Object?): Int {
        return (o1 as FDVariable?).getName().compareToIgnoreCase((o2 as FDVariable?).getName())
    }
}