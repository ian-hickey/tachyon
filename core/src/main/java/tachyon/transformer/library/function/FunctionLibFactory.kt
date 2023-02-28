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
package tachyon.transformer.library.function

import java.io.IOException

/**
 *
 * Die FunctionLibFactory ist der Produzent fuer eine oder mehrere FunctionLib, d.H. ueber statische
 * Methoden (get, getDir) koennen FunctionLibs geladen werden. Die FunctionLibFactory erbt sich vom
 * DefaultHandler.
 */
class FunctionLibFactory : DefaultHandler {
    private var xmlReader: XMLReader? = null

    // private File file;
    private var insideFunction = false
    private var insideAttribute = false
    private var insideReturn = false
    private var insideBundle = false
    private var inside: String? = null
    private var content: StringBuilder? = StringBuilder()
    private val lib: FunctionLib?
    private var function: FunctionLibFunction? = null
    private var arg: FunctionLibFunctionArg? = null
    private var attributes: Map<String?, String?>? = null
    private val id: Identification?
    private val core: Boolean
    /**
     * Privater Konstruktor, der als Eingabe die FLD als InputStream erhaelt.
     *
     * @param saxParser String Klassenpfad zum Sax Parser.
     * @param is InputStream auf die TLD.
     * @throws FunctionLibException
     *
     * private FunctionLibFactory(String saxParser,InputSource is) throws
     * FunctionLibException { super(); init(saxParser,is); }
     */
    /**
     * Privater Konstruktor, der als Eingabe die FLD als File Objekt erhaelt.
     *
     * @param saxParser String Klassenpfad zum Sax Parser.
     * @param file File Objekt auf die TLD.
     * @throws FunctionLibException
     */
    private constructor(lib: FunctionLib?, file: Resource?, id: Identification?, core: Boolean) : super() {
        this.id = id
        this.lib = if (lib == null) FunctionLib() else lib
        this.core = core
        var r: Reader? = null
        try {
            init(InputSource(IOUtil.getReader(file.getInputStream(), null as Charset?).also { r = it }))
        } catch (e: IOException) {
            throw FunctionLibException("File not found: " + e.getMessage())
        } finally {
            try {
                IOUtil.close(r)
            } catch (e: IOException) {
                throw FunctionLibException("closing failed: " + e.getMessage())
            }
        }
    }

    /**
     * Privater Konstruktor nur mit Sax Parser Definition, liest Default FLD vom System ein.
     *
     * @param saxParser String Klassenpfad zum Sax Parser.
     * @throws FunctionLibException
     */
    private constructor(lib: FunctionLib?, systemFLD: String?, id: Identification?, core: Boolean) : super() {
        this.id = id
        this.lib = if (lib == null) FunctionLib() else lib
        this.core = core
        val `is` = InputSource(this.getClass().getResourceAsStream(systemFLD))
        init(`is`)
    }

    /**
     * Generelle Initialisierungsmetode der Konstruktoren.
     *
     * @param saxParser String Klassenpfad zum Sax Parser.
     * @param is InputStream auf die TLD.
     * @throws FunctionLibException
     */
    @Throws(FunctionLibException::class)
    private fun init(`is`: InputSource?) {
        try {
            xmlReader = XMLUtil.createXMLReader()
            xmlReader.setContentHandler(this)
            xmlReader.setErrorHandler(this)
            xmlReader.setEntityResolver(FunctionLibEntityResolver())
            xmlReader.parse(`is`)
        } catch (e: IOException) {
            throw FunctionLibException("IO Exception: " + e.getMessage())
        } catch (e: SAXException) {
            throw FunctionLibException("SaxException: " + e.getMessage())
        }
    }

    /**
     * Geerbte Methode von org.xml.sax.ContentHandler, wird bei durchparsen des XML, beim Auftreten
     * eines Start-Tag aufgerufen.
     *
     * @see org.xml.sax.ContentHandler.startElement
     */
    @Override
    fun startElement(uri: String?, name: String?, qName: String?, atts: Attributes?) {
        // Start Function
        inside = qName
        attributes = SaxUtil.toMap(atts)
        if (qName!!.equals("function")) startFunction() else if (qName.equals("argument")) startArg() else if (qName.equals("return")) startReturn() else if (qName.equals("bundle")) startBundle()
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
        content = StringBuilder()
        inside = ""
        if (qName!!.equals("function")) endFunction() else if (qName.equals("argument")) endArg() else if (qName.equals("return")) endReturn() else if (qName.equals("bundle")) endBundle()
    }

    /**
     * Wird jedesmal wenn das Tag function beginnt aufgerufen, um intern in einen anderen Zustand zu
     * gelangen.
     */
    private fun startFunction() {
        function = FunctionLibFunction(core)
        insideFunction = true
    }

    /**
     * Wird jedesmal wenn das Tag function endet aufgerufen, um intern in einen anderen Zustand zu
     * gelangen.
     */
    private fun endFunction() {
        lib!!.setFunction(function)
        insideFunction = false
    }

    /**
     * Wird jedesmal wenn das Tag argument beginnt aufgerufen, um intern in einen anderen Zustand zu
     * gelangen.
     */
    private fun startArg() {
        insideAttribute = true
        arg = FunctionLibFunctionArg()
    }

    /**
     * Wird jedesmal wenn das Tag argument endet aufgerufen, um intern in einen anderen Zustand zu
     * gelangen.
     */
    private fun endArg() {
        function!!.setArg(arg)
        insideAttribute = false
    }

    /**
     * Wird jedesmal wenn das Tag return beginnt aufgerufen, um intern in einen anderen Zustand zu
     * gelangen.
     */
    private fun startReturn() {
        insideReturn = true
    }

    /**
     * Wird jedesmal wenn das Tag return endet aufgerufen, um intern in einen anderen Zustand zu
     * gelangen.
     */
    private fun endReturn() {
        insideReturn = false
    }

    private fun startBundle() {
        insideBundle = true
    }

    private fun endBundle() {
        insideBundle = false
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
        if (insideFunction) {
            // Attributes Value
            if (insideAttribute) {
                if (inside!!.equals("type")) arg!!.setType(value) else if (inside!!.equals("name")) arg!!.setName(value) else if (inside!!.equals("default")) arg!!.setDefaultValue(value) else if (inside!!.equals("default-value")) arg!!.setDefaultValue(value) // deprecated
                else if (inside!!.equals("status")) arg!!.setStatus(TagLibFactory.toStatus(value)) else if (inside!!.equals("description")) arg!!.setDescription(value) else if (inside!!.equals("alias")) arg!!.setAlias(value) else if (inside!!.equals("introduced")) arg!!.setIntroduced(value) else if (inside!!.equals("required")) {
                    arg!!.setRequired(value)
                    if (arg!!.isRequired()) function!!.setArgMin(function!!.getArgMin() + 1)
                }
            } else if (insideReturn) {
                if (inside!!.equals("type")) function!!.setReturn(value)
            } else if (insideBundle) {
                if (inside!!.equals("class")) function!!.setFunctionClass(value, id, attributes)
                // if(inside.equals("name")) function.setBundleName(value);
                // if(inside.equals("version")) function.setBundleVersion(value);
            } else {
                if (inside!!.equals("name")) function!!.setName(value) else if (inside!!.equals("class")) function!!.setFunctionClass(value, id, attributes) else if (inside!!.equals("tte-class")) function!!.setTTEClass(value, id, attributes)
                if (inside!!.equals("keywords")) function!!.setKeywords(value) else if (inside!!.equals("introduced")) function!!.setIntroduced(value) else if (inside!!.equals("description")) function!!.setDescription(value) else if (inside!!.equals("member-name")) function!!.setMemberName(value) else if (inside!!.equals("member-position")) function!!.setMemberPosition(Caster.toIntValue(value, 1)) else if (inside!!.equals("member-chaining")) function!!.setMemberChaining(Caster.toBooleanValue(value, false)) else if (inside!!.equals("member-type")) function!!.setMemberType(value) else if (inside!!.equals("status")) function!!.setStatus(TagLibFactory.toStatus(value)) else if (inside!!.equals("argument-type")) function!!.setArgType(if (value.equalsIgnoreCase("dynamic")) FunctionLibFunction.ARG_DYNAMIC else FunctionLibFunction.ARG_FIX) else if (inside!!.equals("argument-min")) function!!.setArgMin(Integer.parseInt(value)) else if (inside!!.equals("argument-max")) function!!.setArgMax(Integer.parseInt(value))
            }
        } else {
            // function lib values
            if (inside!!.equals("flib-version")) lib!!.setVersion(value) else if (inside!!.equals("short-name")) lib!!.setShortName(value) else if (inside!!.equals("uri")) {
                try {
                    lib.setUri(value)
                } catch (e: URISyntaxException) {
                }
            } else if (inside!!.equals("display-name")) lib!!.setDisplayName(value) else if (inside!!.equals("description")) lib!!.setDescription(value)
        }
    }

    /**
     * Gibt die interne FunctionLib zurueck.
     *
     * @return Interne Repraesentation der zu erstellenden FunctionLib.
     */
    private fun getLib(): FunctionLib? {
        return lib
    }

    companion object {
        private val hashLib: Map<String?, FunctionLib?>? = HashMap<String?, FunctionLib?>()
        private val systemFLDs: Array<FunctionLib?>? = arrayOfNulls<FunctionLib?>(2)

        // private final static String FLD_1_0= "/resource/fld/web-cfmfunctionlibrary_1_0";
        private val FLD_BASE: String? = "/resource/fld/core-base.fld"
        private val FLD_CFML: String? = "/resource/fld/core-cfml.fld"
        private val FLD_LUCEE: String? = "/resource/fld/core-tachyon.fld"

        /**
         * Laedt mehrere FunctionLib's die innerhalb eines Verzeichnisses liegen.
         *
         * @param dir Verzeichnis im dem die FunctionLib's liegen.
         * @param saxParser Definition des Sax Parser mit dem die FunctionLib's eingelesen werden sollen.
         * @return FunctionLib's als Array
         * @throws FunctionLibException
         */
        @Throws(FunctionLibException::class)
        fun loadFromDirectory(dir: Resource?, id: Identification?): Array<FunctionLib?>? {
            if (!dir.isDirectory()) return arrayOfNulls<FunctionLib?>(0)
            val arr: ArrayList<FunctionLib?> = ArrayList<FunctionLib?>()
            val files: Array<Resource?> = dir.listResources(ExtensionResourceFilter(arrayOf<String?>("fld", "fldx")))
            for (i in files.indices) {
                if (files[i].isFile()) arr.add(loadFromFile(files[i], id))
            }
            return arr.toArray(arrayOfNulls<FunctionLib?>(arr.size()))
        }

        /**
         * Laedt eine einzelne FunctionLib.
         *
         * @param res FLD die geladen werden soll.
         * @param saxParser Definition des Sax Parser mit dem die FunctionLib eingelsesen werden soll.
         * @return FunctionLib
         * @throws FunctionLibException
         */
        @Throws(FunctionLibException::class)
        fun loadFromFile(res: Resource?, id: Identification?): FunctionLib? {
            // Read in XML
            var lib: FunctionLib? = hashLib!![id(res)] // getHashLib(file.getAbsolutePath());
            if (lib == null) {
                lib = FunctionLibFactory(null, res, id, false).getLib()
                hashLib.put(id(res), lib)
            }
            lib.setSource(res.toString())
            return lib
        }

        /**
         * does not involve the content to create an id, value returned is based on metadata of the file
         * (lastmodified,size)
         *
         * @param res
         * @return
         * @throws NoSuchAlgorithmException
         */
        fun id(res: Resource?): String? {
            val str: String = ResourceUtil.getCanonicalPathEL(res).toString() + "|" + res.length() + "|" + res.lastModified()
            return try {
                Hash.md5(str)
            } catch (e: NoSuchAlgorithmException) {
                Caster.toString(HashUtil.create64BitHash(str))
            }
        }

        /**
         * Laedt die Systeminterne FLD.
         *
         * @param saxParser Definition des Sax Parser mit dem die FunctionLib eingelsesen werden soll.
         * @return FunctionLib
         * @throws FunctionLibException
         */
        @Throws(FunctionLibException::class)
        fun loadFromSystem(id: Identification?): Array<FunctionLib?>? {
            if (systemFLDs!![CFMLEngine.DIALECT_CFML] == null) {
                val cfml: FunctionLib = FunctionLibFactory(null, FLD_BASE, id, true).getLib()
                val tachyon: FunctionLib = cfml!!.duplicate(false)
                tachyon.transformer.library.function.FunctionLibFactory.Companion.systemFLDs.get(CFMLEngine.DIALECT_CFML) = tachyon.transformer.library.function.FunctionLibFactory(cfml, tachyon.transformer.library.function.FunctionLibFactory.Companion.FLD_CFML, id, true).getLib()
                tachyon.transformer.library.function.FunctionLibFactory.Companion.systemFLDs.get(CFMLEngine.DIALECT_LUCEE) = tachyon.transformer.library.function.FunctionLibFactory(tachyon, tachyon.transformer.library.function.FunctionLibFactory.Companion.FLD_LUCEE, id, true).getLib()
            }
            return systemFLDs
        }

        @Throws(FunctionLibException::class)
        fun loadFromSystem(dialect: Int, id: Identification?): FunctionLib? {
            return loadFromSystem(id)!![dialect]
        }

        /**
         * return one FunctionLib contain content of all given Function Libs
         *
         * @param flds
         * @return combined function lib
         */
        fun combineFLDs(flds: Array<FunctionLib?>?): FunctionLib? {
            val fl = FunctionLib()
            if (ArrayUtil.isEmpty(flds)) return fl
            setAttributes(flds!![0], fl)

            // add functions
            for (i in flds.indices) {
                copyFunctions(flds[i], fl)
            }
            return fl
        }

        fun combineFLDs(flds: Set?): FunctionLib? {
            val newFL = FunctionLib()
            var tmp: FunctionLib
            if (flds.size() === 0) return newFL
            val it: Iterator = flds.iterator()
            var count = 0
            while (it.hasNext()) {
                tmp = it.next()
                if (count++ == 0) setAttributes(tmp, newFL)
                copyFunctions(tmp, newFL)
            }
            return newFL
        }

        /**
         * copy function from one FunctionLib to another
         *
         * @param extFL
         * @param newFL
         */
        private fun copyFunctions(extFL: FunctionLib?, newFL: FunctionLib?) {
            val it: Iterator<Entry<String?, FunctionLibFunction?>?> = extFL!!.getFunctions().entrySet().iterator()
            var flf: FunctionLibFunction
            while (it.hasNext()) {
                flf = it.next().getValue() // TODO function must be duplicated because it gets a new FunctionLib assigned
                newFL!!.setFunction(flf)
            }
        }

        /**
         * copy attributes from old fld to the new
         *
         * @param extFL
         * @param newFL
         */
        private fun setAttributes(extFL: FunctionLib?, newFL: FunctionLib?) {
            newFL!!.setDescription(extFL!!.getDescription())
            newFL!!.setDisplayName(extFL!!.getDisplayName())
            newFL!!.setShortName(extFL!!.getShortName())
            newFL.setUri(extFL!!.getUri())
            newFL!!.setVersion(extFL!!.getVersion())
        }
    }
}