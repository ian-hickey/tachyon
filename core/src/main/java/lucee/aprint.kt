/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee

import java.io.IOException

/**
 *
 */
object aprint {
    fun date(value: String?) {
        val millis: Long = System.currentTimeMillis()
        o(Date(millis).toString() + "-" + (millis - millis / 1000 * 1000) + " " + value)
    }

    @JvmOverloads
    fun ds(useOutStream: Boolean = false) {
        Exception("Stack trace").printStackTrace(if (useOutStream) CFMLEngineImpl.CONSOLE_OUT else CFMLEngineImpl.CONSOLE_ERR)
    }

    @JvmOverloads
    fun ds(max: Int, useOutStream: Boolean = false) {
        printStackTrace(if (useOutStream) CFMLEngineImpl.CONSOLE_OUT else CFMLEngineImpl.CONSOLE_ERR, max)
    }

    private fun printStackTrace(ps: PrintStream?, max: Int) {

        // Guard against malicious overrides of Throwable.equals by
        // using a Set with identity equality semantics.

        // Print our stack trace
        val traces: Array<StackTraceElement?> = Exception("Stack trace").getStackTrace()
        var i = 0
        while (i < traces.size && i < max) {
            val trace: StackTraceElement? = traces[i]
            ps.println("\tat $trace")
            i++
        }
    }

    fun ds(label: Object?, useOutStream: Boolean) {
        _eo(if (useOutStream) CFMLEngineImpl.CONSOLE_OUT else CFMLEngineImpl.CONSOLE_ERR, label)
        ds(useOutStream)
    }

    fun ds(label: Object?) {
        ds(label, false)
    }

    fun dumpStack() {
        ds(false)
    }

    fun dumpStack(useOutStream: Boolean) {
        ds(useOutStream)
    }

    fun dumpStack(label: String?) {
        ds(label, false)
    }

    fun dumpStack(label: String?, useOutStream: Boolean) {
        ds(label, useOutStream)
    }

    fun err(o: Boolean) {
        CFMLEngineImpl.CONSOLE_ERR.println(o)
    }

    fun err(d: Double) {
        CFMLEngineImpl.CONSOLE_ERR.println(d)
    }

    fun err(d: Long) {
        CFMLEngineImpl.CONSOLE_ERR.println(d)
    }

    fun err(d: Float) {
        CFMLEngineImpl.CONSOLE_ERR.println(d)
    }

    fun err(d: Int) {
        CFMLEngineImpl.CONSOLE_ERR.println(d)
    }

    fun err(d: Short) {
        CFMLEngineImpl.CONSOLE_ERR.println(d)
    }

    fun out(o1: Object?, o2: Object?, o3: Object?) {
        CFMLEngineImpl.CONSOLE_OUT.print(o1)
        CFMLEngineImpl.CONSOLE_OUT.print(o2)
        CFMLEngineImpl.CONSOLE_OUT.println(o3)
    }

    fun out(o1: Object?, o2: Object?) {
        CFMLEngineImpl.CONSOLE_OUT.print(o1)
        CFMLEngineImpl.CONSOLE_OUT.println(o2)
    }

    fun out(o: Object?, l: Long) {
        CFMLEngineImpl.CONSOLE_OUT.print(o)
        CFMLEngineImpl.CONSOLE_OUT.println(l)
    }

    fun out(o: Object?, d: Double) {
        CFMLEngineImpl.CONSOLE_OUT.print(o)
        CFMLEngineImpl.CONSOLE_OUT.println(d)
    }

    fun out(arr: ByteArray?, offset: Int, len: Int) {
        CFMLEngineImpl.CONSOLE_OUT.print("byte[]{")
        for (i in offset until len + offset) {
            if (i > 0) CFMLEngineImpl.CONSOLE_OUT.print(',')
            CFMLEngineImpl.CONSOLE_OUT.print(arr!![i])
        }
        CFMLEngineImpl.CONSOLE_OUT.println("}")
    }

    fun out(o: Double) {
        CFMLEngineImpl.CONSOLE_OUT.println(o)
    }

    fun out(o: Float) {
        CFMLEngineImpl.CONSOLE_OUT.println(o)
    }

    fun out(o: Long) {
        CFMLEngineImpl.CONSOLE_OUT.println(o)
    }

    fun out(o: Int) {
        CFMLEngineImpl.CONSOLE_OUT.println(o)
    }

    fun out(o: Char) {
        CFMLEngineImpl.CONSOLE_OUT.println(o)
    }

    fun out(o: Boolean) {
        CFMLEngineImpl.CONSOLE_OUT.println(o)
    }

    fun out() {
        CFMLEngineImpl.CONSOLE_OUT.println()
    }

    fun printST(t: Throwable?) {
        var t = t
        if (t is InvocationTargetException) {
            t = (t as InvocationTargetException?).getTargetException()
        }
        err(t.getClass().getName())
        t.printStackTrace()
    }

    fun printST(t: Throwable?, ps: PrintStream?) {
        var t = t
        if (t is InvocationTargetException) {
            t = (t as InvocationTargetException?).getTargetException()
        }
        err(t.getClass().getName())
        t.printStackTrace(ps)
    }

    fun out(o: Object?) {
        _eo(CFMLEngineImpl.CONSOLE_OUT, o)
    }

    fun err(o: Object?) {
        _eo(CFMLEngineImpl.CONSOLE_ERR, o)
    }

    fun writeTemp(name: String?, o: Object?, addStackTrace: Boolean) {
        // write(SystemUtil.getTempDirectory().getRealResource(name+".log"), o);
        write(SystemUtil.getHomeDirectory().getRealResource(name.toString() + ".log"), o, addStackTrace)
    }

    fun writeHome(name: String?, o: Object?, addStackTrace: Boolean) {
        write(SystemUtil.getHomeDirectory().getRealResource(name.toString() + ".log"), o, addStackTrace)
    }

    fun writeCustom(path: String?, o: Object?, addStackTrace: Boolean) {
        write(ResourcesImpl.getFileResourceProvider().getResource(path), o, addStackTrace)
    }

    fun write(res: Resource?, o: Object?, addStackTrace: Boolean) {
        var os: OutputStream? = null
        var ps: PrintStream? = null
        try {
            ResourceUtil.touch(res)
            os = res.getOutputStream(true)
            ps = PrintStream(os)
            _eo(ps, o)
            if (addStackTrace) Exception("Stack trace").printStackTrace(ps)
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        } finally {
            IOUtil.closeEL(ps)
            IOUtil.closeEL(os)
        }
    }

    fun _eo(o: Object?, d: Boolean) {
        _eo(CFMLEngineImpl.CONSOLE_OUT, o)
    }

    fun o(o: Object?) {
        _eo(CFMLEngineImpl.CONSOLE_OUT, o)
    }

    fun e(o: Object?) {
        _eo(CFMLEngineImpl.CONSOLE_ERR, o)
    }

    fun oe(o: Object?, valid: Boolean) {
        _eo(if (valid) CFMLEngineImpl.CONSOLE_OUT else CFMLEngineImpl.CONSOLE_ERR, o)
    }

    fun dateO(value: String?) {
        _date(CFMLEngineImpl.CONSOLE_OUT, value)
    }

    fun dateE(value: String?) {
        _date(CFMLEngineImpl.CONSOLE_ERR, value)
    }

    private fun _date(ps: PrintStream?, value: String?) {
        val millis: Long = System.currentTimeMillis()
        _eo(ps, Date(millis).toString() + "-" + (millis - millis / 1000 * 1000) + " " + value)
    }

    private fun _eo(ps: PrintStream?, o: Object?) {
        if (o is Enumeration) _eo(ps, o as Enumeration?) else if (o is Array<Object>) _eo(ps, o as Array<Object?>?) else if (o is BooleanArray) _eo(ps, o as BooleanArray?) else if (o is ByteArray) _eo(ps, o as ByteArray?) else if (o is IntArray) _eo(ps, o as IntArray?) else if (o is FloatArray) _eo(ps, o as FloatArray?) else if (o is LongArray) _eo(ps, o as LongArray?) else if (o is DoubleArray) _eo(ps, o as DoubleArray?) else if (o is CharArray) _eo(ps, o as CharArray?) else if (o is ShortArray) _eo(ps, o as ShortArray?) else if (o is Set) _eo(ps, o as Set?) else if (o is List) _eo(ps, o as List?) else if (o is Map) _eo(ps, o as Map?) else if (o is Collection) _eo(ps, o as Collection?) else if (o is Iterator) _eo(ps, o as Iterator?) else if (o is NamedNodeMap) _eo(ps, o as NamedNodeMap?) else if (o is ResultSet) _eo(ps, o as ResultSet?) else if (o is Node) _eo(ps, o as Node?) else if (o is Throwable) _eo(ps, o as Throwable?) else if (o is Attributes) _eo(ps, o as Attributes?) else if (o is Cookie) {
            val c: Cookie? = o as Cookie?
            ps.println("Cookie(name:" + c.getName().toString() + ";domain:" + c.getDomain().toString() + ";maxage:" + c.getMaxAge().toString() + ";path:" + c.getPath().toString() + ";value:" + c.getValue().toString() + ";version:"
                    + c.getVersion().toString() + ";secure:" + c.getSecure().toString() + ")")
        } else if (o is InputSource) {
            val `is`: InputSource? = o as InputSource?
            val r: Reader = `is`.getCharacterStream()
            try {
                ps.println(IOUtil.toString(`is`.getCharacterStream()))
            } catch (e: IOException) {
            } finally {
                IOUtil.closeEL(r)
            }
        } else ps.println(o)
    }

    private fun _eo(ps: PrintStream?, arr: Array<Object?>?) {
        if (arr == null) {
            ps.println("null")
            return
        }
        ps.print(arr.getClass().getComponentType().getName().toString() + "[]{")
        for (i in arr.indices) {
            if (i > 0) {
                ps.print("\t,")
            }
            _eo(ps, arr[i])
        }
        ps.println("}")
    }

    private fun _eo(ps: PrintStream?, arr: IntArray?) {
        ps.print("int[]{")
        for (i in arr.indices) {
            if (i > 0) ps.print(',')
            ps.print(arr!![i])
        }
        ps.println("}")
    }

    private fun _eo(ps: PrintStream?, arr: ByteArray?) {
        ps.print("byte[]{")
        for (i in arr.indices) {
            if (i > 0) ps.print(',')
            ps.print(arr!![i])
        }
        ps.println("}")
    }

    private fun _eo(ps: PrintStream?, arr: BooleanArray?) {
        ps.print("boolean[]{")
        for (i in arr.indices) {
            if (i > 0) ps.print(',')
            ps.print(arr!![i])
        }
        ps.println("}")
    }

    private fun _eo(ps: PrintStream?, arr: CharArray?) {
        ps.print("char[]{")
        for (i in arr.indices) {
            if (i > 0) ps.print(',')
            ps.print(arr!![i])
        }
        ps.println("}")
    }

    private fun _eo(ps: PrintStream?, arr: ShortArray?) {
        ps.print("short[]{")
        for (i in arr.indices) {
            if (i > 0) ps.print(',')
            ps.print(arr!![i])
        }
        ps.println("}")
    }

    private fun _eo(ps: PrintStream?, arr: FloatArray?) {
        ps.print("float[]{")
        for (i in arr.indices) {
            if (i > 0) ps.print(',')
            ps.print(arr!![i])
        }
        ps.println("}")
    }

    private fun _eo(ps: PrintStream?, arr: LongArray?) {
        ps.print("long[]{")
        for (i in arr.indices) {
            if (i > 0) ps.print(',')
            ps.print(arr!![i])
        }
        ps.println("}")
    }

    private fun _eo(ps: PrintStream?, arr: DoubleArray?) {
        ps.print("double[]{")
        for (i in arr.indices) {
            if (i > 0) ps.print(',')
            ps.print(arr!![i])
        }
        ps.println("}")
    }

    private fun _eo(ps: PrintStream?, n: Node?) {
        ps.print(Caster.toString(n, null))
    }

    private fun _eo(ps: PrintStream?, t: Throwable?) {
        t.printStackTrace(ps)
    }

    private fun _eo(ps: PrintStream?, en: Enumeration?) {
        _eo(ps, en.getClass().getName().toString() + " [")
        while (en.hasMoreElements()) {
            ps.print(en.nextElement())
            ps.println(",")
        }
        _eo(ps, "]")
    }

    private fun _eo(ps: PrintStream?, list: List?) {
        val it: ListIterator = list.listIterator()
        _eo(ps, list.getClass().getName().toString() + " {")
        while (it.hasNext()) {
            val index: Int = it.nextIndex()
            it.next()
            ps.print(index)
            ps.print(":")
            ps.print(list.get(index))
            ps.println(";")
        }
        _eo(ps, "}")
    }

    private fun _eo(ps: PrintStream?, coll: Collection?) {
        val it: Iterator = coll.iterator()
        _eo(ps, coll.getClass().getName().toString() + " {")
        while (it.hasNext()) {
            _eo(ps, it.next())
        }
        _eo(ps, "}")
    }

    private fun _eo(ps: PrintStream?, it: Iterator?) {
        _eo(ps, it.getClass().getName().toString() + " {")
        while (it.hasNext()) {
            ps.print(it.next())
            ps.println(";")
        }
        _eo(ps, "}")
    }

    private fun _eo(ps: PrintStream?, set: Set?) {
        val it: Iterator = set.iterator()
        ps.println(set.getClass().getName().toString() + " {")
        var first = true
        while (it.hasNext()) {
            if (!first) ps.print(",")
            first = false
            _eo(ps, it.next())
        }
        _eo(ps, "}")
    }

    private fun _eo(ps: PrintStream?, res: ResultSet?) {
        try {
            _eo(ps, QueryImpl(res, "query", null).toString())
        } catch (e: PageException) {
            _eo(ps, res.toString())
        }
    }

    private fun _eo(ps: PrintStream?, map: Map?) {
        if (map == null) {
            ps.println("null")
            return
        }
        val it: Iterator = map.keySet().iterator()
        if (map.size() < 2) {
            ps.print(map.getClass().getName().toString() + " {")
            while (it.hasNext()) {
                val key: Object = it.next()
                _eo(ps, key)
                ps.print(":")
                _eo(ps, map.get(key))
            }
            ps.println("}")
        } else {
            ps.println(map.getClass().getName().toString() + " {")
            while (it.hasNext()) {
                val key: Object = it.next()
                ps.print("	")
                _eo(ps, key)
                ps.print(":")
                _eo(ps, map.get(key))
                ps.println(";")
            }
            ps.println("}")
        }
    }

    private fun _eo(ps: PrintStream?, map: NamedNodeMap?) {
        if (map == null) {
            ps.println("null")
            return
        }
        val len: Int = map.getLength()
        ps.print(map.getClass().getName().toString() + " {")
        var attr: Attr
        for (i in 0 until len) {
            attr = map.item(i) as Attr
            ps.print(attr.getName())
            ps.print(":")
            ps.print(attr.getValue())
            ps.println(";")
        }
        ps.println("}")
    }

    private fun _eo(ps: PrintStream?, attrs: Attributes?) {
        if (attrs == null) {
            ps.println("null")
            return
        }
        val len: Int = attrs.getLength()
        ps.print(attrs.getClass().getName().toString() + " {")
        for (i in 0 until len) {
            ps.print(attrs.getLocalName(i))
            ps.print(":")
            ps.print(attrs.getValue(i))
            ps.println()
        }
        ps.println("}")
    }
}