package lucee.runtime.config

import java.io.IOException

/**
 *
 * Die FunctionLibFactory ist der Produzent fuer eine oder mehrere FunctionLib, d.H. ueber statische
 * Methoden (get, getDir) koennen FunctionLibs geladen werden. Die FunctionLibFactory erbt sich vom
 * DefaultHandler.
 */
class XMLConfigReader(file: Resource?, trimBody: Boolean, readRule: ReadRule?, nameRule: NameRule?) : DefaultHandler(), LexicalHandler {
    private var xmlReader: XMLReader? = null
    private val root: Struct? = StructImpl()
    private var current: Struct?

    // private Struct parent;
    private val ancestor: Stack<Struct?>? = Stack()
    private val trimBody: Boolean
    private val readRule: ReadRule?
    private val nameRule: NameRule?
    @Throws(SAXException::class, IOException::class)
    private fun init(`is`: InputSource?) {
        xmlReader = XMLUtil.createXMLReader()
        xmlReader.setContentHandler(this)
        xmlReader.setErrorHandler(this)
        // xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", this);
        xmlReader.parse(`is`)
    }

    @Override
    fun startElement(uri: String?, name: String?, qName: String?, attrs: Attributes?) {
        var qName = qName
        qName = nameRule!!.translate(qName)
        val parent: Struct? = current
        ancestor.add(parent)
        current = StructImpl(Struct.TYPE_LINKED)

        // attrs
        val len: Int = attrs.getLength()
        for (i in 0 until len) {
            current.setEL(nameRule.translate(attrs.getQName(i)), attrs.getValue(i))
        }
        val existing: Object = parent.get(qName, null)
        if (!readRule!!.asArray(qName)) {
            if (existing is Array) {
                (existing as Array).appendEL(current)
            } else if (existing is Struct) {
                val arr: Array = ArrayImpl()
                arr.appendEL(existing)
                arr.appendEL(current)
                parent.setEL(qName, arr)
            } else {
                parent.setEL(qName, current)
            }
        } else {
            if (existing != null) {
                (existing as Array).appendEL(current)
            } else {
                val arr: Array = ArrayImpl()
                arr.appendEL(current)
                parent.setEL(qName, arr)
            }
        }
    }

    @Override
    fun endElement(uri: String?, name: String?, qName: String?) {
        var qName = qName
        qName = nameRule!!.translate(qName)
        if (trimBody) {
            val body = current.get("_body_", null) as String
            if (body != null) {
                if (StringUtil.isEmpty(body, true)) current.remove("_body_") else current.setEL("_body_", body.trim())
            }
        }
        current = ancestor.pop()
    }

    @Override
    fun characters(ch: CharArray?, start: Int, length: Int) {
        val body = current.get("_body_", null) as String
        if (body == null) current.put("_body_", String(ch, start, length)) else current.put("_body_", body + String(ch, start, length))
    }

    fun getData(): Struct? {
        return root
    }

    @Override
    @Throws(SAXException::class)
    fun comment(ch: CharArray?, start: Int, length: Int) {
        val comment = current.get("_comment_", null) as String
        if (comment == null) current.put("_comment_", String(ch, start, length)) else current.put("_comment_", comment + String(ch, start, length))
    }

    @Override
    @Throws(SAXException::class)
    fun endCDATA() {
    }

    @Override
    @Throws(SAXException::class)
    fun endDTD() {
    }

    @Override
    @Throws(SAXException::class)
    fun endEntity(arg0: String?) {
    }

    @Override
    @Throws(SAXException::class)
    fun startCDATA() {
    }

    @Override
    @Throws(SAXException::class)
    fun startDTD(arg0: String?, arg1: String?, arg2: String?) {
    }

    @Override
    @Throws(SAXException::class)
    fun startEntity(arg0: String?) {
    }

    class ReadRule {
        private val names: Set<String?>? = HashSet()
        fun asArray(name: String?): Boolean {
            return names!!.contains(name)
        }

        init {
            names.add("data-source")
            names.add("label")
            names.add("debugEntry")
        }
    }

    class NameRule {
        fun translate(name: String?): String? {
            var name = name
            var last = 0
            var index: Int
            while (name.indexOf('-', last).also { index = it } != -1) {
                if (index + 1 == name!!.length()) break
                name = StringBuilder(name.substring(0, index)).append(Character.toUpperCase(name.charAt(index + 1))).append(name.substring(index + 2)).toString()
                last = index + 1
            }
            return name
        }
    }

    companion object {
        @Throws(Exception::class)
        fun main(args: Array<String?>?) {
            var src: Resource = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Projects/Lucee/Lucee5/core/src/main/java/resource/config/web.xml")
            var trg: Resource = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Projects/Lucee/Lucee5/core/src/main/java/resource/config/web.json")
            ConfigWebFactory.translateConfigFile(null, src, trg, "", false)
            src = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Projects/Lucee/Lucee5/core/src/main/java/resource/config/server.xml")
            trg = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Projects/Lucee/Lucee5/core/src/main/java/resource/config/server.json")
            ConfigWebFactory.translateConfigFile(null, src, trg, "single", true)

            // src =
            // ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test/webapps/ROOT/WEB-INF/lucee/lucee-web.xml.cfm");
            // trg =
            // ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test/webapps/ROOT/WEB-INF/lucee/lucee-web.json");
            // ConfigWebFactory.translateConfigFile(null, src, trg);

            // src =
            // ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test/lucee-server/context/lucee-server.xml");
            // trg =
            // ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test/lucee-server/context/lucee-server.json");
            // ConfigWebFactory.translateConfigFile(null, src, trg);
            src = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test/webapps/ROOT/WEB-INF/lucee/.CFConfig.json")
            src.delete()
            src = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test/lucee-server/context/.CFConfig.json")
            src.delete()

            /*
		 * XMLConfigReader reader = new XMLConfigReader(res, true, new ReadRule(), new NameRule()); String
		 * str = ser(reader.getData().get("cfLuceeConfiguration")); IOUtil.write(trg, str, CharsetUtil.UTF8,
		 * false); print.e(str);
		 */

            // Object result = new JSONExpressionInterpreter().interpret(null, str);
            // print.e(result);
        }

        @Throws(PageException::class)
        private fun ser(`var`: Object?): String? {
            return try {
                val json = JSONConverter(true, Charset.forName("UTF-8"), JSONDateFormat.PATTERN_CF, true, true)

                // TODO get secure prefix from application.cfc
                json.serialize(null, `var`, SerializationSettings.SERIALIZE_AS_ROW)
            } catch (e: ConverterException) {
                throw Caster.toPageException(e)
            }
        }
    }

    init {
        current = root
        this.trimBody = trimBody
        this.readRule = readRule
        this.nameRule = nameRule
        var r: Reader? = null
        try {
            init(InputSource(IOUtil.getReader(file.getInputStream(), null as Charset?).also { r = it }))
        } finally {
            IOUtil.closeEL(r)
        }
    }
}