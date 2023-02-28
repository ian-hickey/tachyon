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
package lucee.runtime.text.feed

import java.io.IOException

class FeedHandler : DefaultHandler {
    private var xmlReader: XMLReader? = null

    // private StringBuffer content=new StringBuffer();
    private var deep = 0
    private var data: FeedStruct? = null
    private var path: String? = ""
    private var inside: Collection.Key? = null
    private val parents: Stack<FeedStruct?>? = Stack<FeedStruct?>()
    private var decl: FeedDeclaration? = null
    private val root: Map<String?, String?>? = HashMap<String?, String?>()
    private var hasDC = false
    private var isAtom = false
    private var inAuthor = false
    private var inEntry = false

    /**
     * Constructor of the class
     *
     * @param res
     * @throws IOException
     * @throws SAXException
     */
    constructor(res: Resource?) {
        var `is`: InputStream? = null
        try {
            val source = InputSource(res.getInputStream().also { `is` = it })
            source.setSystemId(res.getPath())
            init(source)
        } finally {
            IOUtil.close(`is`)
        }
    }

    constructor(`is`: InputSource?) {
        init(`is`)
    }

    /**
     * Constructor of the class
     *
     * @param stream
     * @throws IOException
     * @throws SAXException
     */
    constructor(stream: InputStream?) {
        val `is` = InputSource(IOUtil.getReader(stream, SystemUtil.getCharset()))
        init(`is`)
    }

    @Throws(SAXException::class, IOException::class)
    private fun init(`is`: InputSource?) {
        // print.out("is:"+is);
        hasDC = false
        data = FeedStruct()
        xmlReader = XMLUtil.createXMLReader()
        xmlReader.setContentHandler(this)
        xmlReader.setErrorHandler(this)
        xmlReader.setDTDHandler(DummyDTDHandler())
        xmlReader.parse(`is`)
    }

    /**
     * @return the hasDC
     */
    fun hasDC(): Boolean {
        return hasDC
    }

    @Override
    fun setDocumentLocator(locator: Locator?) {
        if (locator is Locator2) {
            val locator2: Locator2? = locator as Locator2?
            root.put("encoding", locator2.getEncoding())
        }
    }

    @Override
    fun startElement(uri: String?, name: String?, qName: String?, atts: Attributes?) {
        var name = name
        deep++
        name = name(name, qName)
        if ("entry".equals(name)) inEntry = true else if ("author".equals(name)) inAuthor = true
        if (qName.startsWith("dc:")) {
            name = "dc_$name"
            hasDC = true
        }
        inside = KeyImpl.getInstance(name)
        if (StringUtil.isEmpty(path)) path = name else {
            path += ".$name"
        }
        if (decl == null) {
            var decName = name
            val version: String = atts.getValue("version")
            if ("feed".equals(decName)) {
                decName = if (!StringUtil.isEmpty(version)) "atom_$version" else "atom_1.0"
            } else {
                if (!StringUtil.isEmpty(version)) decName += "_$version"
            }
            decl = FeedDeclaration.getInstance(decName)
            root.put("version", decName)
            isAtom = decl.getType().equals("atom")
        }
        val sct = FeedStruct(path, inside, uri)

        // attributes
        val attrs = getAttributes(atts, path)
        if (attrs != null) {
            var entry: Entry<String?, String?>
            val it: Iterator<Entry<String?, String?>?> = attrs.entrySet().iterator()
            sct!!.setHasAttribute(true)
            while (it.hasNext()) {
                entry = it.next()
                sct.setEL(entry.getKey(), entry.getValue())
            }
        }

        // assign
        if (!isAtom || deep < 4) {
            val obj: Object = data.get(inside, null)
            if (obj is Array) {
                (obj as Array).appendEL(sct)
            } else if (obj is FeedStruct) {
                val arr: Array = ArrayImpl()
                arr.appendEL(obj)
                arr.appendEL(sct)
                data.setEL(inside, arr)
            } else if (obj is String) {
                // wenn wert schon existiert wird castableArray in setContent erstellt
            } else {
                val el: El = decl!!.getDeclaration()!!.get(path)
                if (el != null && (el.getQuantity() === El.QUANTITY_0_N || el.getQuantity() === El.QUANTITY_1_N)) {
                    val arr: Array = ArrayImpl()
                    arr.appendEL(sct)
                    data.setEL(inside, arr)
                } else data.setEL(inside, sct)
            }
        }
        parents.add(data)
        data = sct
    }

    private fun name(name: String?, qName: String?): String? {
        return if (!StringUtil.isEmpty(name, true)) name else ListUtil.last(qName, ':')
    }

    @Override
    fun endElement(uri: String?, name: String?, qName: String?) {
        var name = name
        name = name(name, qName)
        if ("entry".equals(name)) inEntry = false else if ("author".equals(name)) inAuthor = false
        deep--
        if (isAtom && deep >= (if (inEntry && inAuthor) 4 else 3)) {
            val content: String = data.getString()
            val keys: Array<Key?> = data.keys()
            val sb = StringBuilder()
            sb.append("<")
            sb.append(qName)

            // xmlns
            if (!parents.peek().getUri().equals(uri)) {
                sb.append(" xmlns=\"")
                sb.append(uri)
                sb.append("\"")
            }
            for (i in keys.indices) {
                sb.append(" ")
                sb.append(keys[i].getString())
                sb.append("=\"")
                sb.append(Caster.toString(data.get(keys[i], ""), ""))
                sb.append("\"")
            }
            if (!StringUtil.isEmpty(content)) {
                sb.append(">")
                sb.append(content)
                sb.append("</$qName>")
            } else sb.append("/>")
            data = parents.pop()
            data.append(sb.toString().trim())
            // setContent(sb.toString().trim());
            path = data.getPath()
            inside = data.getInside()
            return
        }

        // setContent(content.toString().trim());
        setContent(data.getString().trim())
        data = parents.pop()
        path = data.getPath()
        inside = data.getInside()
    }

    @Override
    fun characters(ch: CharArray?, start: Int, length: Int) {
        data!!.append(String(ch, start, length))
        // content.append(new String(ch,start,length));
    }

    private fun setContent(value: String?) {
        // print.out(path+":"+inside);
        if (StringUtil.isEmpty(inside)) return
        if (data!!.hasAttribute()) {
            if (!StringUtil.isEmpty(value)) setEl(data, KeyConstants._value, value)
        } else {
            val parent: FeedStruct = parents.peek()
            setEl(parent, inside, value)
        }
    }

    private fun setEl(sct: Struct?, key: Collection.Key?, value: String?) {
        val existing: Object = sct.get(key, null)
        if (existing is CastableArray) {
            (existing as CastableArray).appendEL(value)
        } else if (existing is String) {
            val ca = CastableArray(existing)
            ca.appendEL(existing)
            ca.appendEL(value)
            sct.setEL(key, ca)
        } else sct.setEL(key, Caster.toString(value))

        /*
		 * if(existing instanceof Struct)sct.setEL(key,value); else if(existing instanceof
		 * Array)((Array)existing).appendEL(value); else if(existing!=null){ CastableArray ca=new
		 * CastableArray(existing); ca.appendEL(existing); ca.appendEL(value); sct.setEL(key,ca); } else
		 */
    }

    private fun getAttributes(attrs: Attributes?, path: String?): Map<String?, String?>? {
        val el: El = decl!!.getDeclaration()!!.get(path)
        val len: Int = attrs.getLength()
        if ((el == null || el.getAttrs() == null) && len == 0) return null
        val map: Map<String?, String?> = HashMap<String?, String?>()
        if (el != null) {
            val defaults: Array<Attr?> = el.getAttrs()
            if (defaults != null) {
                for (i in defaults.indices) {
                    if (defaults[i]!!.hasDefaultValue()) map.put(defaults[i].getName(), defaults[i].getDefaultValue())
                }
            }
        }
        for (i in 0 until len) {
            map.put(attrs.getQName(i), attrs.getValue(i))
        }
        return map
    }

    /**
     * @return the properties
     */
    fun getData(): Struct? {
        return data
    }

    @Override
    @Throws(SAXException::class)
    fun endDocument() {
        super.endDocument()
        val def: Struct = StructImpl()
        val entryLevel: Array<Key?> = decl!!.getEntryLevel()
        for (i in entryLevel.indices) {
            data = data.get(entryLevel[i], def) as FeedStruct
        }
        data.putAll(root)
    }
}