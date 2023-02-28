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
package tachyon.runtime.dump

import java.io.File

object DumpUtil {
    val MAX_LEVEL_REACHED: DumpData? = null

    // FUTURE add to interface
    fun toDumpData(o: Object?, pageContext: PageContext?, maxlevel: Int, props: DumpProperties?): DumpData? {
        var maxlevel = maxlevel
        if (maxlevel < 0) return MAX_LEVEL_REACHED

        // null
        if (o == null) {
            val table = DumpTable("null", "#ff6600", "#ffcc99", "#000000")
            table.appendRow(DumpRow(0, SimpleDumpData("Empty:null")))
            return table
        }
        if (o is DumpData) {
            return o as DumpData?
        }
        // Date
        if (o is Date) {
            return DateTimeImpl(o as Date?).toDumpData(pageContext, maxlevel, props)
        }
        // Calendar
        if (o is Calendar) {
            val c: Calendar? = o as Calendar?
            val df = SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zz", Locale.ENGLISH)
            df.setTimeZone(c.getTimeZone())
            val table = DumpTable("date", "#ff9900", "#ffcc00", "#000000")
            table.setTitle("java.util.Calendar")
            table.appendRow(1, SimpleDumpData("Timezone"), SimpleDumpData(TimeZoneUtil.toString(c.getTimeZone())))
            table.appendRow(1, SimpleDumpData("Time"), SimpleDumpData(df.format(c.getTime())))
            return table
        }
        // StringBuffer
        if (o is StringBuffer) {
            val dt: DumpTable? = toDumpData(o.toString(), pageContext, maxlevel, props) as DumpTable?
            if (StringUtil.isEmpty(dt.getTitle())) dt.setTitle(Caster.toClassName(o))
            return dt
        }
        // StringBuilder
        if (o is StringBuilder) {
            val dt: DumpTable? = toDumpData(o.toString(), pageContext, maxlevel, props) as DumpTable?
            if (StringUtil.isEmpty(dt.getTitle())) dt.setTitle(Caster.toClassName(o))
            return dt
        }
        // String
        if (o is String) {
            val str = o as String?
            if (str.trim().startsWith("<wddxPacket ")) {
                try {
                    val converter = WDDXConverter(pageContext.getTimeZone(), false, true)
                    converter.setTimeZone(pageContext.getTimeZone())
                    val rst: Object = converter.deserialize(str, false)
                    val data: DumpData? = toDumpData(rst, pageContext, maxlevel, props)
                    val table = DumpTable("string", "#cc9999", "#ffffff", "#000000")
                    table.setTitle("WDDX")
                    table.appendRow(1, SimpleDumpData("encoded"), data)
                    table.appendRow(1, SimpleDumpData("raw"), SimpleDumpData(str))
                    return table
                } catch (e: Exception) {
                    // this dump entry is optional, so if it is not possible to create the decoded wddx entry, we simply
                    // don't do it
                }
            }
            val table = DumpTable("string", "#ff6600", "#ffcc99", "#000000")
            table.appendRow(1, SimpleDumpData("string"), SimpleDumpData(str))
            return table
        }
        // Character
        if (o is Character) {
            val table = DumpTable("character", "#ff6600", "#ffcc99", "#000000")
            table.appendRow(1, SimpleDumpData("character"), SimpleDumpData(o.toString()))
            return table
        }
        // Number
        if (o is Number) {
            val table = DumpTable("numeric", "#ff6600", "#ffcc99", "#000000")
            table.appendRow(1, SimpleDumpData("number"), SimpleDumpData(Caster.toString(o as Number?)))
            return table
        }
        // Charset
        if (o is Charset) {
            val table = DumpTable("charset", "#ff6600", "#ffcc99", "#000000")
            table.appendRow(1, SimpleDumpData("charset"), SimpleDumpData((o as Charset?).name()))
            return table
        }
        // CharSet
        if (o is CharSet) {
            val table = DumpTable("charset", "#ff6600", "#ffcc99", "#000000")
            table.appendRow(1, SimpleDumpData("charset"), SimpleDumpData((o as CharSet?).name()))
            return table
        }
        // Locale
        if (o is Locale) {
            val l: Locale? = o as Locale?
            val env: Locale = ThreadLocalPageContext.getLocale()
            val table = DumpTable("locale", "#ff6600", "#ffcc99", "#000000")
            table.setTitle("Locale " + LocaleFactory.getDisplayName(l))
            table.appendRow(1, SimpleDumpData("Code (ISO-3166)"), SimpleDumpData(l.toString()))
            table.appendRow(1, SimpleDumpData("Country"), SimpleDumpData(l.getDisplayCountry(env)))
            table.appendRow(1, SimpleDumpData("Language"), SimpleDumpData(l.getDisplayLanguage(env)))
            return table
        }
        // TimeZone
        if (o is TimeZone) {
            val table = DumpTable("numeric", "#ff6600", "#ffcc99", "#000000")
            table.appendRow(1, SimpleDumpData("TimeZone"), SimpleDumpData(TimeZoneUtil.toString(o as TimeZone?)))
            return table
        }
        // Boolean
        if (o is Boolean) {
            val table = DumpTable("boolean", "#ff6600", "#ffcc99", "#000000")
            table.appendRow(1, SimpleDumpData("boolean"), SimpleDumpData((o as Boolean?).booleanValue()))
            return table
        }
        // File
        if (o is File) {
            val table = DumpTable("file", "#ffcc00", "#ffff66", "#000000")
            table.appendRow(1, SimpleDumpData("File"), SimpleDumpData(o.toString()))
            return table
        }
        // Cookie
        if (o is Cookie) {
            val c: Cookie? = o as Cookie?
            val table = DumpTable("Cookie", "#979EAA", "#DEE9FB", "#000000")
            table.setTitle("Cookie (" + c.getClass().getName().toString() + ")")
            table.appendRow(1, SimpleDumpData("name"), SimpleDumpData(c.getName()))
            table.appendRow(1, SimpleDumpData("value"), SimpleDumpData(c.getValue()))
            table.appendRow(1, SimpleDumpData("path"), SimpleDumpData(c.getPath()))
            table.appendRow(1, SimpleDumpData("secure"), SimpleDumpData(c.getSecure()))
            table.appendRow(1, SimpleDumpData("maxAge"), SimpleDumpData(c.getMaxAge()))
            table.appendRow(1, SimpleDumpData("version"), SimpleDumpData(c.getVersion()))
            table.appendRow(1, SimpleDumpData("domain"), SimpleDumpData(c.getDomain()))
            table.appendRow(1, SimpleDumpData("httpOnly"), SimpleDumpData(CookieImpl.isHTTPOnly(c)))
            table.appendRow(1, SimpleDumpData("comment"), SimpleDumpData(c.getComment()))
            return table
        }
        // Resource
        if (o is Resource) {
            val table = DumpTable("resource", "#ffcc00", "#ffff66", "#000000")
            table.appendRow(1, SimpleDumpData("Resource"), SimpleDumpData(o.toString()))
            return table
        }
        // byte[]
        if (o is ByteArray) {
            val bytes = o as ByteArray?
            val max = 5000
            val table = DumpTable("array", "#ff9900", "#ffcc00", "#000000")
            table.setTitle("Native Array  (" + Caster.toClassName(o).toString() + ")")
            val sb = StringBuilder("[")
            for (i in bytes.indices) {
                if (i != 0) sb.append(",")
                sb.append(bytes!![i])
                if (i == max) {
                    sb.append(", ...truncated")
                    break
                }
            }
            sb.append("]")
            table.appendRow(1, SimpleDumpData("Raw" + if (bytes!!.size < max) "" else " (truncated)"), SimpleDumpData(sb.toString()))
            if (bytes!!.size < max) {
                // base64
                table.appendRow(1, SimpleDumpData("Base64 Encoded"), SimpleDumpData(Base64Coder.encode(bytes)))
                /*
				 * try { table.appendRow(1,new SimpleDumpData("CFML expression"),new
				 * SimpleDumpData("evaluateJava('"+JavaConverter.serialize(bytes)+"')"));
				 * 
				 * } catch (IOException e) {}
				 */
            }
            return table
        }
        // Collection.Key
        if (o is Collection.Key) {
            val key: Collection.Key? = o as Collection.Key?
            val table = DumpTable("string", "#ff6600", "#ffcc99", "#000000")
            table.appendRow(1, SimpleDumpData("Collection.Key"), SimpleDumpData(key.getString()))
            return table
        }
        val id = "" + IDGenerator.intId()
        val refid: String = ThreadLocalDump.get(o)
        if (refid != null) {
            val table = DumpTable("ref", "#ffffff", "#cccccc", "#000000")
            table.appendRow(1, SimpleDumpData("Reference"), SimpleDumpData(refid))
            table.setRef(refid)
            return setId(id, table)
        }
        ThreadLocalDump.set(o, id)
        return try {
            val top: Int = props.getMaxlevel()

            // Dumpable
            if (o is Dumpable) {
                val dd: DumpData = (o as Dumpable?).toDumpData(pageContext, maxlevel, props)
                if (dd != null) return setId(id, dd)
            }
            if (o is UDF) {
                return UDFUtil.toDumpData(pageContext, maxlevel, props, o as UDF?, UDFUtil.TYPE_UDF)
            }
            // Map
            if (o is Map) {
                val map: Map? = o
                val it: Iterator = map.keySet().iterator()
                val table = DumpTable("struct", "#ff9900", "#ffcc00", "#000000")
                table.setTitle("Map (" + Caster.toClassName(o).toString() + ")")
                while (it.hasNext()) {
                    val next: Object = it.next()
                    table.appendRow(1, toDumpData(next, pageContext, maxlevel, props), toDumpData(map.get(next), pageContext, maxlevel, props))
                }
                return setId(id, table)
            }

            // List
            if (o is List) {
                val list: List? = o
                val it: ListIterator = list.listIterator()
                val table = DumpTable("array", "#ff9900", "#ffcc00", "#000000")
                table.setTitle("Array (List)")
                if (list.size() > top) table.setComment("Rows: " + list.size().toString() + " (showing top " + top.toString() + ")")
                var i = 0
                while (it.hasNext() && i++ < top) {
                    table.appendRow(1, SimpleDumpData(it.nextIndex() + 1), toDumpData(it.next(), pageContext, maxlevel, props))
                }
                return setId(id, table)
            }

            // Set
            if (o is Set) {
                val set: Set? = o
                val it: Iterator = set.iterator()
                val table = DumpTable("array", "#ff9900", "#ffcc00", "#000000")
                table.setTitle("Set (" + set.getClass().getName().toString() + ")")
                var i = 0
                while (it.hasNext() && i++ < top) {
                    table.appendRow(1, toDumpData(it.next(), pageContext, maxlevel, props))
                }
                return setId(id, table)
            }

            // Resultset
            if (o is ResultSet) {
                try {
                    val dd: DumpData = QueryImpl(o as ResultSet?, "query", pageContext.getTimeZone()).toDumpData(pageContext, maxlevel, props)
                    if (dd is DumpTable) (dd as DumpTable).setTitle(Caster.toClassName(o))
                    return setId(id, dd)
                } catch (e: PageException) {
                }
            }
            // Enumeration
            if (o is Enumeration) {
                val e: Enumeration? = o as Enumeration?
                val table = DumpTable("enumeration", "#ff9900", "#ffcc00", "#000000")
                table.setTitle("Enumeration")
                var i = 0
                while (e.hasMoreElements() && i++ < top) {
                    table.appendRow(0, toDumpData(e.nextElement(), pageContext, maxlevel, props))
                }
                return setId(id, table)
            }
            // Object[]
            if (Decision.isNativeArray(o)) {
                val arr: Array
                return try {
                    arr = Caster.toArray(o)
                    val htmlBox = DumpTable("array", "#ff9900", "#ffcc00", "#000000")
                    htmlBox.setTitle("Native Array (" + Caster.toClassName(o).toString() + ")")
                    val length: Int = arr.size()
                    for (i in 1..length) {
                        var ox: Object? = null
                        try {
                            ox = arr.getE(i)
                        } catch (e: Exception) {
                        }
                        htmlBox.appendRow(1, SimpleDumpData(i), toDumpData(ox, pageContext, maxlevel, props))
                    }
                    setId(id, htmlBox)
                } catch (e: PageException) {
                    setId(id, SimpleDumpData(""))
                }
            }
            // Node
            if (o is Node) {
                return setId(id, XMLCaster.toDumpData(o as Node?, pageContext, maxlevel, props))
            }
            // ObjectWrap
            if (o is ObjectWrap) {
                maxlevel++
                return setId(id, toDumpData((o as ObjectWrap?).getEmbededObject(null), pageContext, maxlevel, props))
            }
            // NodeList
            if (o is NodeList) {
                val list: NodeList? = o as NodeList?
                val len: Int = list.getLength()
                val table = DumpTable("xml", "#cc9999", "#ffffff", "#000000")
                for (i in 0 until len) {
                    table.appendRow(1, SimpleDumpData(i), toDumpData(list.item(i), pageContext, maxlevel, props))
                }
                return setId(id, table)
            }
            // AttributeMap
            if (o is NamedNodeMap) {
                val attr: NamedNodeMap? = o as NamedNodeMap?
                val len: Int = attr.getLength()
                val dt = DumpTable("array", "#ff9900", "#ffcc00", "#000000")
                dt.setTitle("NamedNodeMap (" + Caster.toClassName(o).toString() + ")")
                for (i in 0 until len) {
                    dt.appendRow(1, SimpleDumpData(i), toDumpData(attr.item(i), pageContext, maxlevel, props))
                }
                return setId(id, dt)
            }
            // HttpSession
            if (o is HttpSession) {
                val hs: HttpSession? = o as HttpSession?
                val e: Enumeration = hs.getAttributeNames()
                val htmlBox = DumpTable("httpsession", "#9999ff", "#ccccff", "#000000")
                htmlBox.setTitle("HttpSession")
                while (e.hasMoreElements()) {
                    val key: String = e.nextElement().toString()
                    htmlBox.appendRow(1, SimpleDumpData(key), toDumpData(hs.getAttribute(key), pageContext, maxlevel, props))
                }
                return setId(id, htmlBox)
            }
            if (o is Pojo) {
                val table = DumpTable(o.getClass().getName(), "#ff99cc", "#ffccff", "#000000")
                var clazz: Class? = o.getClass()
                if (o is Class) clazz = o as Class?
                val fullClassName: String = clazz.getName()
                val pos: Int = fullClassName.lastIndexOf('.')
                val className = if (pos == -1) fullClassName else fullClassName.substring(pos + 1)
                table.setTitle("Java Bean - $className ($fullClassName)")
                table.appendRow(3, SimpleDumpData("Property Name"), SimpleDumpData("Value"))

                // collect the properties
                val methods: Array<Method?> = clazz.getMethods()
                var propName: String
                var value: Object?
                var exName: String? = null
                var exValue: String? = null
                for (i in methods.indices) {
                    val method: Method? = methods[i]
                    if (Object::class.java === method.getDeclaringClass()) continue
                    propName = method.getName()
                    if (propName.startsWith("get") && method.getParameterTypes().length === 0) {
                        propName = StringUtil.lcFirst(propName.substring(3))
                        value = null
                        try {
                            value = method.invoke(o, arrayOfNulls<Object?>(0))
                            if (exName == null && value is String && (value as String?)!!.length() < 20) {
                                exName = propName
                                exValue = value.toString()
                            }
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            value = "not able to retrieve the data:" + t.getMessage()
                        }
                        table.appendRow(0, SimpleDumpData(propName), toDumpData(value, pageContext, maxlevel, props))
                    }
                }
                if (exName == null) {
                    exName = "LastName"
                    exValue = "Sorglos"
                }
                table.setComment("""
    JavaBeans are reusable software components for Java.
    They are classes that encapsulate many objects into a single object (the bean).
    They allow access to properties using getter and setter methods or directly.
    """.trimIndent())

                /*
				 * "\n\nExample:\n" + "   x=myBean.get"+exName+"(); // read a property with a getter method\n" +
				 * "   x=myBean."+exName+"; // read a property directly\n" +
				 * "   myBean.set"+exName+"(\""+exValue+"\"); // write a property with a setter method\n" +
				 * "   myBean."+exName+"=\""+exValue+"\"; // write a property directly");
				 */return setId(id, table)
            }

            // reflect
            // else {
            val table = DumpTable(o.getClass().getName(), "#6289a3", "#dee3e9", "#000000")
            var clazz: Class? = o.getClass()
            if (o is Class) clazz = o as Class?
            val fullClassName: String = clazz.getName()
            val pos: Int = fullClassName.lastIndexOf('.')
            val className = if (pos == -1) fullClassName else fullClassName.substring(pos + 1)
            table.setTitle(className)
            table.appendRow(1, SimpleDumpData("class"), SimpleDumpData(fullClassName))

            // Fields
            val fields: Array<Field?> = clazz.getFields()
            val fieldDump = DumpTable("#6289a3", "#dee3e9", "#000000")
            fieldDump.appendRow(-1, SimpleDumpData("name"), SimpleDumpData("pattern"), SimpleDumpData("value"))
            for (i in fields.indices) {
                val field: Field? = fields[i]
                var value: DumpData?
                try { // print.out(o+":"+maxlevel);
                    value = SimpleDumpData(Caster.toString(field.get(o), ""))
                } catch (e: Exception) {
                    value = SimpleDumpData("")
                }
                fieldDump.appendRow(0, SimpleDumpData(field.getName()), SimpleDumpData(field.toString()), value)
            }
            if (fields.size > 0) table.appendRow(1, SimpleDumpData("fields"), fieldDump)

            // Constructors
            val constructors: Array<Constructor?> = clazz.getConstructors()
            val constrDump = DumpTable("#6289a3", "#dee3e9", "#000000")
            constrDump.appendRow(-1, SimpleDumpData("interface"), SimpleDumpData("exceptions"))
            for (i in constructors.indices) {
                val constr: Constructor? = constructors[i]

                // exceptions
                val sbExp = StringBuilder()
                val exceptions: Array<Class?> = constr.getExceptionTypes()
                for (p in exceptions.indices) {
                    if (p > 0) sbExp.append("\n")
                    sbExp.append(Caster.toClassName(exceptions[p]))
                }

                // parameters
                val sbParams = StringBuilder("<init>")
                sbParams.append('(')
                val parameters: Array<Class?> = constr.getParameterTypes()
                for (p in parameters.indices) {
                    if (p > 0) sbParams.append(", ")
                    sbParams.append(Caster.toClassName(parameters[p]))
                }
                sbParams.append(')')
                constrDump.appendRow(0, SimpleDumpData(sbParams.toString()), SimpleDumpData(sbExp.toString()))
            }
            if (constructors.size > 0) table.appendRow(1, SimpleDumpData("constructors"), constrDump)

            // Methods
            val objMethods = StringBuilder()
            val methods: Array<Method?> = clazz.getMethods()
            val methDump = DumpTable("#6289a3", "#dee3e9", "#000000")
            methDump.appendRow(-1, SimpleDumpData("return"), SimpleDumpData("interface"), SimpleDumpData("exceptions"))
            for (i in methods.indices) {
                val method: Method? = methods[i]
                if (Object::class.java === method.getDeclaringClass()) {
                    if (objMethods.length() > 0) objMethods.append(", ")
                    objMethods.append(method.getName())
                    continue
                }

                // exceptions
                val sbExp = StringBuilder()
                val exceptions: Array<Class?> = method.getExceptionTypes()
                for (p in exceptions.indices) {
                    if (p > 0) sbExp.append("\n")
                    sbExp.append(Caster.toClassName(exceptions[p]))
                }

                // parameters
                val sbParams = StringBuilder(method.getName())
                sbParams.append('(')
                val parameters: Array<Class?> = method.getParameterTypes()
                for (p in parameters.indices) {
                    if (p > 0) sbParams.append(", ")
                    sbParams.append(Caster.toClassName(parameters[p]))
                }
                sbParams.append(')')
                methDump.appendRow(0, SimpleDumpData(Caster.toClassName(method.getReturnType())),
                        SimpleDumpData(sbParams.toString()), SimpleDumpData(sbExp.toString()))
            }
            if (methods.size > 0) table.appendRow(1, SimpleDumpData("methods"), methDump)
            val inherited = DumpTable("#6289a3", "#dee3e9", "#000000")
            inherited.appendRow(7, SimpleDumpData("Methods inherited from java.lang.Object"))
            inherited.appendRow(0, SimpleDumpData(objMethods.toString()))
            table.appendRow(1, SimpleDumpData(""), inherited)

            // Bundle Info
            val cl: ClassLoader = clazz.getClassLoader()
            if (cl is BundleClassLoader) {
                try {
                    val bcl: BundleClassLoader = cl as BundleClassLoader
                    val b: Bundle = bcl.getBundle()
                    if (b != null) {
                        val sct: Struct = StructImpl()
                        sct.setEL(KeyConstants._id, b.getBundleId())
                        sct.setEL(KeyConstants._name, b.getSymbolicName())
                        sct.setEL(KeyConstants._location, b.getLocation())
                        sct.setEL(KeyConstants._version, b.getVersion().toString())
                        val bd = DumpTable("#6289a3", "#dee3e9", "#000000")
                        bd.appendRow(1, SimpleDumpData("id"), SimpleDumpData(b.getBundleId()))
                        bd.appendRow(1, SimpleDumpData("symbolic-name"), SimpleDumpData(b.getSymbolicName()))
                        bd.appendRow(1, SimpleDumpData("version"), SimpleDumpData(b.getVersion().toString()))
                        bd.appendRow(1, SimpleDumpData("location"), SimpleDumpData(b.getLocation()))
                        requiredBundles(bd, b)
                        table.appendRow(1, SimpleDumpData("bundle-info"), bd)
                    }
                } catch (e: NoSuchMethodError) {
                }
            }
            setId(id, table)
            // }
        } finally {
            ThreadLocalDump.remove(o)
        }
    }

    private fun getBundle(cl: ClassLoader?): Bundle? {
        return try {
            val m: Method = cl.getClass().getMethod("getBundle", arrayOfNulls<Class?>(0))
            m.invoke(cl, arrayOfNulls<Object?>(0)) as Bundle
        } catch (e: Exception) {
            null
        }
    }

    private fun requiredBundles(parent: DumpTable?, b: Bundle?) {
        try {
            val list: List<BundleDefinition?> = OSGiUtil.getRequiredBundles(b)
            if (list.isEmpty()) return
            val dt = DumpTable("#6289a3", "#dee3e9", "#000000")
            dt.appendRow(-1, SimpleDumpData("name"), SimpleDumpData("version"), SimpleDumpData("operator"))
            val it: Iterator<BundleDefinition?> = list.iterator()
            var bd: BundleDefinition?
            var vd: VersionDefinition
            var v: String
            var op: String
            while (it.hasNext()) {
                bd = it.next()
                vd = bd.getVersionDefiniton()
                if (vd != null) {
                    v = vd.getVersionAsString()
                    op = vd.getOpAsString()
                } else {
                    v = ""
                    op = ""
                }
                dt.appendRow(0, SimpleDumpData(bd.getName()), SimpleDumpData(v), SimpleDumpData(op))
            }
            parent.appendRow(1, SimpleDumpData("required-bundles"), dt)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    private fun toArray2(list: List<PackageQuery?>?): Array? {
        var sct: Struct?
        var _sct: Struct?
        val arr: Array = ArrayImpl()
        var _arr: Array?
        val it: Iterator<PackageQuery?> = list!!.iterator()
        var pd: PackageQuery?
        var _it: Iterator<VersionDefinition?>
        var vd: VersionDefinition?
        while (it.hasNext()) {
            pd = it.next()
            sct = StructImpl()
            sct.setEL(KeyConstants._package, pd.getName())
            sct.setEL("versions", ArrayImpl().also { _arr = it })
            _it = pd.getVersionDefinitons().iterator()
            while (_it.hasNext()) {
                vd = _it.next()
                _sct = StructImpl()
                _sct.setEL(KeyConstants._bundleVersion, vd.getVersion().toString())
                _sct.setEL("operator", vd.getOpAsString())
                _arr.appendEL(_sct)
            }
            arr.appendEL(sct)
        }
        return arr
    }

    private fun setId(id: String?, data: DumpData?): DumpData? {
        if (data is DumpTable) {
            (data as DumpTable?).setId(id)
        }
        // TODO Auto-generated method stub
        return data
    }

    fun keyValid(props: DumpProperties?, level: Int, key: String?): Boolean {
        if (props.getMaxlevel() - level > 1) return true

        // show
        var set: Set = props.getShow()
        if (set != null && !set.contains(StringUtil.toLowerCase(key))) return false

        // hide
        set = props.getHide()
        return if (set != null && set.contains(StringUtil.toLowerCase(key))) false else true
    }

    fun keyValid(props: DumpProperties?, level: Int, key: Collection.Key?): Boolean {
        if (props.getMaxlevel() - level > 1) return true

        // show
        var set: Set = props.getShow()
        if (set != null && !set.contains(key.getLowerString())) return false

        // hide
        set = props.getHide()
        return if (set != null && set.contains(key.getLowerString())) false else true
    }

    fun toDumpProperties(): DumpProperties? {
        return DumpProperties.DEFAULT
    }

    init {
        MAX_LEVEL_REACHED = DumpTable("Max Level Reached", "#e0e0e0", "#ffcc99", "#888888")
        (MAX_LEVEL_REACHED as DumpTable?).appendRow(DumpRow(1, SimpleDumpData("[Max Dump Level Reached]")))
    }
}