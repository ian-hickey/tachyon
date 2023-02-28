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
package lucee.transformer.library.tag

import java.io.IOException

/**
 * Die Klasse TagLibFactory liest die XML Repraesentation einer TLD ein und laedt diese in eine
 * Objektstruktur. Sie tut dieses mithilfe eines Sax Parser. Die Klasse kann sowohl einzelne Files
 * oder gar ganze Verzeichnisse von TLD laden.
 */
class TagLibFactory : DefaultHandler {
    // private short type=TYPE_CFML;
    private var xmlReader: XMLReader? = null
    private val lib: TagLib?
    private var tag: TagLibTag? = null
    private var insideTag = false
    private var insideScript = false

    // private boolean insideBundle=false;
    private var att: TagLibTagAttr? = null
    private var insideAtt = false
    private var inside: String? = null
    private var content: StringBuffer? = StringBuffer()
    private var script: TagLibTagScript? = null
    private var attributes: Map<String?, String?>? = null
    private val id: Identification?

    /**
     * Privater Konstruktor, der als Eingabe die TLD als File Objekt erhaelt.
     *
     * @param saxParser String Klassenpfad zum Sax Parser.
     * @param file File Objekt auf die TLD.
     * @throws TagLibException
     * @throws IOException
     */
    private constructor(lib: TagLib?, res: Resource?, id: Identification?) {
        this.id = id
        this.lib = if (lib == null) TagLib() else lib
        var r: Reader? = null
        try {
            val `is` = InputSource(IOUtil.getReader(res.getInputStream(), null as Charset?).also { r = it })
            `is`.setSystemId(res.getPath())
            init(`is`)
        } catch (e: IOException) {
            throw TagLibException(e)
        } finally {
            try {
                IOUtil.close(r)
            } catch (e: IOException) {
                throw TagLibException(e)
            }
        }
    }

    /**
     * Privater Konstruktor, der als Eingabe die TLD als File Objekt erhaelt.
     *
     * @param saxParser String Klassenpfad zum Sax Parser.
     * @param file File Objekt auf die TLD.
     * @throws TagLibException
     */
    private constructor(lib: TagLib?, stream: InputStream?, id: Identification?) {
        this.id = id
        this.lib = if (lib == null) TagLib() else lib
        try {
            val `is` = InputSource(IOUtil.getReader(stream, SystemUtil.getCharset()))
            // is.setSystemId(file.toString());
            init(`is`)
        } catch (e: IOException) {
            throw TagLibException(e)
        }
    }

    /**
     * Privater Konstruktor nur mit Sax Parser Definition, liest Default TLD vom System ein.
     *
     * @param saxParser String Klassenpfad zum Sax Parser.
     * @throws TagLibException
     */
    private constructor(lib: TagLib?, systemTLD: String?, id: Identification?) {
        this.id = id
        this.lib = if (lib == null) TagLib() else lib
        val `is` = InputSource(this.getClass().getResourceAsStream(systemTLD))
        init(`is`)
        this.lib.setIsCore(true)
    }

    /**
     * Generelle Initialisierungsmetode der Konstruktoren.
     *
     * @param saxParser String Klassenpfad zum Sax Parser.
     * @param is InputStream auf die TLD.
     * @throws TagLibException
     */
    @Throws(TagLibException::class)
    private fun init(`is`: InputSource?) {
        // print.dumpStack();
        try {
            xmlReader = XMLUtil.createXMLReader()
            xmlReader.setContentHandler(this)
            xmlReader.setErrorHandler(this)
            xmlReader.setEntityResolver(TagLibEntityResolver())
            xmlReader.parse(`is`)
        } catch (e: IOException) {

            // String fileName=is.getSystemId();
            // String message="IOException: ";
            // if(fileName!=null) message+="In File ["+fileName+"], ";
            throw TagLibException(e)
        } catch (e: SAXException) {
            // String fileName=is.getSystemId();
            // String message="SAXException: ";
            // if(fileName!=null) message+="In File ["+fileName+"], ";
            throw TagLibException(e)
        }
    }

    /**
     * Geerbte Methode von org.xml.sax.ContentHandler, wird bei durchparsen des XML, beim Auftreten
     * eines Start-Tag aufgerufen.
     *
     * @see org.xml.sax.ContentHandler.startElement
     */
    @Override
    fun startElement(uri: String?, name: String?, qName: String?, attributes: Attributes?) {
        inside = qName
        this.attributes = SaxUtil.toMap(attributes)
        if (qName!!.equals("tag")) startTag() else if (qName.equals("attribute")) startAtt() else if (qName.equals("script")) startScript()
    }

    /**
     * Geerbte Methode von org.xml.sax.ContentHandler, wird bei durchparsen des XML, beim auftreten
     * eines End-Tag aufgerufen.
     *
     * @see org.xml.sax.ContentHandler.endElement
     */
    @Override
    fun endElement(uri: String?, name: String?, qName: String?) {
        setContent(content.toString().trim())
        content = StringBuffer()
        inside = ""
        /*
		 * if(tag!=null && tag.getName().equalsIgnoreCase("input")) {
		 * print.ln(tag.getName()+"-"+att.getName()+":"+inside+"-"+insideTag+"-"+insideAtt);
		 * 
		 * }
		 */if (qName!!.equals("tag")) endTag() else if (qName.equals("attribute")) endAtt() else if (qName.equals("script")) endScript()
    }

    /**
     * Geerbte Methode von org.xml.sax.ContentHandler, wird bei durchparsen des XML, zum einlesen des
     * Content eines Body Element aufgerufen.
     *
     * @see org.xml.sax.ContentHandler.characters
     */
    @Override
    fun characters(ch: CharArray?, start: Int, length: Int) {
        content.append(String(ch, start, length))
    }

    private fun setContent(value: String?) {
        if (insideTag) {
            // Att Args
            if (insideAtt) {
                // description?
                // Name
                if (inside!!.equals("name")) att!!.setName(value) else if (inside!!.equals("alias")) att!!.setAlias(value) else if (inside!!.equals("values")) att!!.setValues(value) else if (inside!!.equals("value-delimiter")) att!!.setValueDelimiter(value) else if (inside!!.equals("introduced")) att!!.setIntroduced(value) else if (inside!!.equals("required")) att!!.setRequired(Caster.toBooleanValue(value, false)) else if (inside!!.equals("rtexprvalue")) att!!.setRtexpr(Caster.toBooleanValue(value, false)) else if (inside!!.equals("type")) att!!.setType(value) else if (inside!!.equals("default-value")) att!!.setDefaultValue(value) else if (inside!!.equals("undefined-value")) att!!.setUndefinedValue(value) else if (inside!!.equals("status")) att!!.setStatus(toStatus(value)) else if (inside!!.equals("description")) att!!.setDescription(value) else if (inside!!.equals("noname")) att!!.setNoname(Caster.toBooleanValue(value, false)) else if (inside!!.equals("default")) att!!.isDefault(Caster.toBooleanValue(value, false)) else if (inside!!.equals("script-support")) att!!.setScriptSupport(value)
            } else if (insideScript) {
                // type
                if (inside!!.equals("type")) script!!.setType(TagLibTagScript.toType(value, TagLibTagScript.TYPE_NONE))
                if (inside!!.equals("rtexprvalue")) script!!.setRtexpr(Caster.toBooleanValue(value, false))
                if (inside!!.equals("context")) script!!.setContext(value)
            } else {
                // TODO TEI-class
                // Name
                if (inside!!.equals("name")) {
                    tag!!.setName(value)
                } else if (inside!!.equals("tag-class")) tag!!.setTagClassDefinition(value, id, attributes) else if (inside!!.equals("tagclass")) tag!!.setTagClassDefinition(value, id, attributes) else if (inside!!.equals("status")) tag!!.setStatus(toStatus(value)) else if (inside!!.equals("description")) tag!!.setDescription(value) else if (inside!!.equals("introduced")) tag!!.setIntroduced(value) else if (inside!!.equals("tte")) tag!!.setTagEval(toTagEvaluator(value)) else if (inside!!.equals("tte-class")) tag!!.setTTEClassDefinition(value, id, attributes) else if (inside!!.equals("ttt-class")) tag!!.setTTTClassDefinition(value, id, attributes) else if (inside!!.equals("tdbt-class")) tag!!.setTDBTClassDefinition(value, id, attributes) else if (inside!!.equals("att-class")) tag!!.setAttributeEvaluatorClassDefinition(value, id, attributes) else if (inside!!.equals("body-content") || inside!!.equals("bodycontent")) {
                    tag!!.setBodyContent(value)
                } else if (inside!!.equals("allow-removing-literal")) {
                    tag!!.setAllowRemovingLiteral(Caster.toBooleanValue(value, false))
                } else if (inside!!.equals("att-default-value")) tag!!.setAttributeUndefinedValue(value) else if (inside!!.equals("att-undefined-value")) tag!!.setAttributeUndefinedValue(value) else if (inside!!.equals("handle-exception")) {
                    tag!!.setHandleExceptions(Caster.toBooleanValue(value, false))
                } else if (inside!!.equals("appendix")) {
                    tag!!.setAppendix(Caster.toBooleanValue(value, false))
                } else if (inside!!.equals("body-rtexprvalue")) {
                    tag!!.setParseBody(Caster.toBooleanValue(value, false))
                } else if (inside!!.equals("attribute-min")) tag!!.setMin(Integer.parseInt(value)) else if (inside!!.equals("attribute-max")) tag!!.setMax(Integer.parseInt(value)) else if (inside!!.equals("attribute-type")) {
                    var type: Int = TagLibTag.ATTRIBUTE_TYPE_FIXED
                    if (value.toLowerCase().equals("fix")) type = TagLibTag.ATTRIBUTE_TYPE_FIXED else if (value.toLowerCase().equals("fixed")) type = TagLibTag.ATTRIBUTE_TYPE_FIXED else if (value.toLowerCase().equals("dynamic")) type = TagLibTag.ATTRIBUTE_TYPE_DYNAMIC else if (value.toLowerCase().equals("noname")) type = TagLibTag.ATTRIBUTE_TYPE_NONAME else if (value.toLowerCase().equals("mixed")) type = TagLibTag.ATTRIBUTE_TYPE_MIXED else if (value.toLowerCase().equals("fulldynamic")) type = TagLibTag.ATTRIBUTE_TYPE_DYNAMIC // deprecated
                    tag!!.setAttributeType(type)
                }
            }
        } else {
            // TagLib Typ
            if (inside!!.equals("jspversion")) {
                // type=TYPE_JSP;
                lib!!.setType("jsp")
            } else if (inside!!.equals("cfml-version")) {
                // type=TYPE_CFML;
                lib!!.setType("cfml")
            } else if (inside!!.equals("el-class")) lib!!.setELClass(value, id, attributes) else if (inside!!.equals("name-space")) lib!!.setNameSpace(value) else if (inside!!.equals("name-space-separator")) lib!!.setNameSpaceSeperator(value) else if (inside!!.equals("short-name")) lib!!.setShortName(value) else if (inside!!.equals("shortname")) lib!!.setShortName(value) else if (inside!!.equals("display-name")) lib!!.setDisplayName(value) else if (inside!!.equals("displayname")) lib!!.setDisplayName(value) else if (inside!!.equals("ignore-unknow-tags")) lib!!.setIgnoreUnknowTags(Caster.toBooleanValue(value, false)) else if (inside!!.equals("uri")) {
                try {
                    lib.setUri(value)
                } catch (e: URISyntaxException) {
                }
            } else if (inside!!.equals("description")) lib!!.setDescription(value)
        }
    }

    /**
     * Wird jedesmal wenn das Tag tag beginnt aufgerufen, um intern in einen anderen Zustand zu
     * gelangen.
     */
    private fun startTag() {
        tag = TagLibTag(lib)
        insideTag = true
    }

    /**
     * Wird jedesmal wenn das Tag tag endet aufgerufen, um intern in einen anderen Zustand zu gelangen.
     */
    private fun endTag() {
        lib!!.setTag(tag)
        insideTag = false
    }

    private fun startScript() {
        script = TagLibTagScript(tag)
        insideScript = true
    }

    /**
     * Wird jedesmal wenn das Tag tag endet aufgerufen, um intern in einen anderen Zustand zu gelangen.
     */
    private fun endScript() {
        tag!!.setScript(script)
        insideScript = false
    }

    /**
     * Wird jedesmal wenn das Tag attribute beginnt aufgerufen, um intern in einen anderen Zustand zu
     * gelangen.
     */
    private fun startAtt() {
        att = TagLibTagAttr(tag)
        insideAtt = true
    }

    /**
     * Wird jedesmal wenn das Tag tag endet aufgerufen, um intern in einen anderen Zustand zu gelangen.
     */
    private fun endAtt() {
        tag!!.setAttribute(att)
        insideAtt = false
    }

    /**
     * Gibt die interne TagLib zurueck.
     *
     * @return Interne Repraesentation der zu erstellenden TagLib.
     */
    private fun getLib(): TagLib? {
        return lib
    }

    companion object {
        /**
         * Field `TYPE_CFML`
         */
        const val TYPE_CFML: Short = 0

        /**
         * Field `TYPE_JSP`
         */
        const val TYPE_JSP: Short = 1
        private val hashLib: Map<String?, TagLib?>? = MapFactory.< String, TagLib>getConcurrentMap<String?, TagLib?>()
        private val systemTLDs: Array<TagLib?>? = arrayOfNulls<TagLib?>(2)

        // System default tld
        // private final static String TLD_1_0= "/resource/tld/web-cfmtaglibrary_1_0";
        private val TLD_BASE: String? = "/resource/tld/core-base.tld"
        private val TLD_CFML: String? = "/resource/tld/core-cfml.tld"
        private val TLD_LUCEE: String? = "/resource/tld/core-lucee.tld"
        private fun toTagEvaluator(value: String?): TagEvaluator? {
            val arr: Array<String?> = ListUtil.listToStringArray(value, ':')
            if (arr.size == 2 && arr[0].trim().equalsIgnoreCase("parent")) {
                val parent: String = arr[1].trim()
                return ChildEvaluator(parent)
            }
            throw RuntimeException(value.toString() + " is not supported as the definition, you can do for example [parent:<parent-name>]!")
        }

        /**
         * TagLib werden innerhalb der Factory in einer HashMap gecacht, so das diese einmalig von der
         * Factory geladen werden. Diese Methode gibt eine gecachte TagLib anhand dessen key zurueck, falls
         * diese noch nicht im Cache existiert, gibt die Methode null zurueck.
         *
         * @param key Absoluter Filepfad zur TLD.
         * @return TagLib
         */
        private fun getHashLib(key: String?): TagLib? {
            return hashLib!![key]
        }

        /**
         * Laedt mehrere TagLib's die innerhalb eines Verzeichnisses liegen.
         *
         * @param dir Verzeichnis im dem die TagLib's liegen.
         * @param saxParser Definition des Sax Parser mit dem die TagLib's eingelesen werden sollen.
         * @return TagLib's als Array
         * @throws TagLibException
         */
        @Throws(TagLibException::class)
        fun loadFromDirectory(dir: Resource?, id: Identification?): Array<TagLib?>? {
            if (!dir.isDirectory()) return arrayOfNulls<TagLib?>(0)
            val arr: ArrayList<TagLib?> = ArrayList<TagLib?>()
            val files: Array<Resource?> = dir.listResources(ExtensionResourceFilter(arrayOf<String?>("tld", "tldx")))
            for (i in files.indices) {
                if (files[i].isFile()) arr.add(loadFromFile(files[i], id))
            }
            return arr.toArray(arrayOfNulls<TagLib?>(arr.size()))
        }

        /**
         * Laedt eine einzelne TagLib.
         *
         * @param file TLD die geladen werden soll.
         * @param saxParser Definition des Sax Parser mit dem die TagLib eingelsesen werden soll.
         * @return TagLib
         * @throws TagLibException
         */
        @Throws(TagLibException::class)
        fun loadFromFile(res: Resource?, id: Identification?): TagLib? {

            // Read in XML
            var lib: TagLib? = getHashLib(FunctionLibFactory.id(res))
            if (lib == null) {
                lib = TagLibFactory(null, res, id).getLib()
                hashLib.put(FunctionLibFactory.id(res), lib)
            }
            lib.setSource(res.toString())
            return lib
        }

        /**
         * Laedt eine einzelne TagLib.
         *
         * @param file TLD die geladen werden soll.
         * @param saxParser Definition des Sax Parser mit dem die TagLib eingelsesen werden soll.
         * @return TagLib
         * @throws TagLibException
         */
        @Throws(TagLibException::class)
        fun loadFromStream(`is`: InputStream?, id: Identification?): TagLib? {
            return TagLibFactory(null, `is`, id).getLib()
        }

        /**
         * Laedt die Systeminterne TLD.
         *
         * @param saxParser Definition des Sax Parser mit dem die FunctionLib eingelsesen werden soll.
         * @return FunctionLib
         * @throws TagLibException
         */
        @Throws(TagLibException::class)
        private fun loadFromSystem(id: Identification?): Array<TagLib?>? {
            if (systemTLDs!![CFMLEngine.DIALECT_CFML] == null) {
                val cfml: TagLib = TagLibFactory(null, TLD_BASE, id).getLib()
                val lucee: TagLib = cfml!!.duplicate(false)
                lucee.transformer.library.tag.TagLibFactory.Companion.systemTLDs.get(CFMLEngine.DIALECT_CFML) = lucee.transformer.library.tag.TagLibFactory(cfml, lucee.transformer.library.tag.TagLibFactory.Companion.TLD_CFML, id).getLib()
                lucee.transformer.library.tag.TagLibFactory.Companion.systemTLDs.get(CFMLEngine.DIALECT_LUCEE) = lucee.transformer.library.tag.TagLibFactory(lucee, lucee.transformer.library.tag.TagLibFactory.Companion.TLD_LUCEE, id).getLib()
            }
            return systemTLDs
        }

        @Throws(TagLibException::class)
        fun loadFromSystem(dialect: Int, id: Identification?): TagLib? {
            return loadFromSystem(id)!![dialect]
        }

        @Throws(TagLibException::class)
        fun loadFrom(res: Resource?, id: Identification?): Array<TagLib?>? {
            if (res.isDirectory()) return loadFromDirectory(res, id)
            if (res.isFile()) return arrayOf<TagLib?>(loadFromFile(res, id))
            throw TagLibException("can not load tag library descriptor from [$res]")
        }

        /**
         * return one FunctionLib contain content of all given Function Libs
         *
         * @param tlds
         * @return combined function lib
         */
        fun combineTLDs(tlds: Array<TagLib?>?): TagLib? {
            val tl = TagLib()
            if (ArrayUtil.isEmpty(tlds)) return tl
            setAttributes(tlds!![0], tl)

            // add functions
            for (i in tlds.indices) {
                copyTags(tlds[i], tl)
            }
            return tl
        }

        fun combineTLDs(tlds: Set?): TagLib? {
            val newTL = TagLib()
            var tmp: TagLib
            if (tlds.size() === 0) return newTL
            val it: Iterator = tlds.iterator()
            var count = 0
            while (it.hasNext()) {
                tmp = it.next()
                if (count++ == 0) setAttributes(tmp, newTL)
                copyTags(tmp, newTL)
            }
            return newTL
        }

        private fun setAttributes(extTL: TagLib?, newTL: TagLib?) {
            newTL!!.setDescription(extTL!!.getDescription())
            newTL!!.setDisplayName(extTL!!.getDisplayName())
            newTL!!.setELClassDefinition(extTL!!.getELClassDefinition())
            newTL!!.setIsCore(extTL!!.isCore())
            newTL!!.setNameSpace(extTL!!.getNameSpace())
            newTL!!.setNameSpaceSeperator(extTL!!.getNameSpaceSeparator())
            newTL!!.setShortName(extTL!!.getShortName())
            newTL!!.setSource(extTL!!.getSource())
            newTL!!.setType(extTL!!.getType())
            newTL.setUri(extTL!!.getUri())
        }

        private fun copyTags(extTL: TagLib?, newTL: TagLib?) {
            val it: Iterator = extTL!!.getTags().entrySet().iterator()
            var tlt: TagLibTag?
            while (it.hasNext()) {
                tlt = (it.next() as Map.Entry).getValue() as TagLibTag? // TODO function must be duplicated because it gets a new FunctionLib assigned
                newTL!!.setTag(tlt)
            }
        }

        fun toStatus(value: String?): Short {
            var value = value
            value = value.trim().toLowerCase()
            if ("deprecated".equals(value)) return TagLib.STATUS_DEPRECATED
            if ("dep".equals(value)) return TagLib.STATUS_DEPRECATED
            if ("unimplemented".equals(value)) return TagLib.STATUS_UNIMPLEMENTED
            if ("unimplemeted".equals(value)) return TagLib.STATUS_UNIMPLEMENTED
            if ("notimplemented".equals(value)) return TagLib.STATUS_UNIMPLEMENTED
            if ("not-implemented".equals(value)) return TagLib.STATUS_UNIMPLEMENTED
            return if ("hidden".equals(value)) TagLib.STATUS_HIDDEN else TagLib.STATUS_IMPLEMENTED
        }

        fun toStatus(value: Short): String? {
            when (value) {
                TagLib.STATUS_DEPRECATED -> return "deprecated"
                TagLib.STATUS_UNIMPLEMENTED -> return "unimplemented"
                TagLib.STATUS_HIDDEN -> return "hidden"
            }
            return "implemented"
        }
    }
}