/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2016, Lucee Assosication Switzerland
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
package lucee.runtime.type.scope

import java.io.BufferedReader

/**
 * Form Scope
 */
class FormImpl
/**
 * standart class Constructor
 */
    : ScopeSupport("form", SCOPE_FORM, Struct.TYPE_LINKED), Form, ScriptProtected {
    private val EQL: Byte = 61
    private val NL: Byte = 10
    private val AMP: Byte = 38
    private val _fileItems: Map<String?, Item?>? = MapFactory.< String, Item>getConcurrentMap<String?, lucee.runtime.type.scope.FormImpl.Item?>()
    private var initException: Exception? = null
    private var encoding: String? = null
    private var scriptProtected: Int = ScriptProtected.UNDEFINED

    // private static final ResourceFilter FILTER = new ExtensionResourceFilter(".upload",false);
    private var raw: Array<URLItem?>? = empty
    private var headerType = HEADER_TYPE_UNKNOWN
    @Override
    fun getEncoding(): String? {
        return encoding
    }

    @Override
    @Throws(UnsupportedEncodingException::class)
    fun setEncoding(ac: ApplicationContext?, encoding: String?) {
        var encoding = encoding
        encoding = encoding.trim().toUpperCase()
        if (encoding.equals(this.encoding)) return
        this.encoding = encoding
        if (!isInitalized()) return
        fillDecoded(raw, encoding, isScriptProtected(), ac.getSameFieldAsArray(Scope.SCOPE_FORM))
        setFieldNames()
    }

    @Override
    override fun initialize(pc: PageContext?) {
        if (encoding == null) encoding = pc.getWebCharset().name()
        if (scriptProtected == ScriptProtected.UNDEFINED) {
            scriptProtected = if (pc.getApplicationContext().getScriptProtect() and ApplicationContext.SCRIPT_PROTECT_FORM > 0) ScriptProtected.YES else ScriptProtected.NO
        }
        super.initialize(pc)
        var contentType: String = pc.getHttpServletRequest().getContentType() ?: return
        contentType = StringUtil.toLowerCase(contentType)
        if (contentType.startsWith("multipart/form-data")) {
            headerType = HEADER_MULTIPART_FORM_DATA
            initializeMultiPart(pc, isScriptProtected())
        } else if (contentType.startsWith("text/plain")) {
            headerType = HEADER_TEXT_PLAIN
            initializeUrlEncodedOrTextPlain(pc, '\n', isScriptProtected())
        } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
            headerType = HEADER_APP_URL_ENC
            initializeUrlEncodedOrTextPlain(pc, '&', isScriptProtected())
        }
        setFieldNames()
    }

    @Override
    fun reinitialize(ac: ApplicationContext?) {
        if (isInitalized()) {
            if (scriptProtected == ScriptProtected.UNDEFINED) {
                scriptProtected = if (ac.getScriptProtect() and ApplicationContext.SCRIPT_PROTECT_FORM > 0) ScriptProtected.YES else ScriptProtected.NO
            }
            fillDecodedEL(raw, encoding, isScriptProtected(), ac.getSameFieldAsArray(SCOPE_FORM))
            setFieldNames()
        }
    }

    fun setFieldNames() {
        if (size() > 0) {
            setEL(KeyConstants._fieldnames, ListUtil.arrayToList(keys(), ","))
        }
    }

    private fun initializeMultiPart(pc: PageContext?, scriptProteced: Boolean) {
        // get temp directory
        val tempDir: Resource = pc.getConfig().getTempDirectory()
        var tempFile: Resource

        // Create a new file upload handler
        val encoding = getEncoding()
        val factory: FileItemFactory = if (tempDir is File) DiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, tempDir as File) else DiskFileItemFactory()
        val upload = ServletFileUpload(factory)
        upload.setHeaderEncoding(encoding)
        // ServletRequestContext c = new ServletRequestContext(pc.getHttpServletRequest());
        val req: HttpServletRequest = pc.getHttpServletRequest()
        val context: ServletRequestContext = object : ServletRequestContext(req) {
            @Override
            fun getCharacterEncoding(): String? {
                return encoding
            }
        }

        // Parse the request
        try {
            val iter: FileItemIterator = upload.getItemIterator(context)
            // byte[] value;
            var `is`: InputStream
            val list: ArrayList<URLItem?> = ArrayList<URLItem?>()
            var fileName: String?
            while (iter.hasNext()) {
                val item: FileItemStream = iter.next()
                `is` = IOUtil.toBufferedInputStream(item.openStream())
                if (item.isFormField() || StringUtil.isEmpty(item.getName())) {
                    list.add(URLItem(item.getFieldName(), String(IOUtil.toBytes(`is`), encoding), false))
                } else {
                    fileName = getFileName()
                    tempFile = tempDir.getRealResource(fileName)
                    IOUtil.copy(`is`, tempFile, true)
                    var ct: String = item.getContentType()
                    if (StringUtil.isEmpty(ct) && tempFile.length() > 0) {
                        ct = IOUtil.getMimeType(tempFile, null)
                    } else if ("application/octet-stream".equalsIgnoreCase(ct)) {
                        ct = IOUtil.getMimeType(tempFile, ct)
                    }
                    if (StringUtil.isEmpty(ct)) {
                        `is` = tempFile.getInputStream()
                        try {
                            list.add(URLItem(item.getFieldName(), String(IOUtil.toBytes(`is`), encoding), false))
                        } finally {
                            IOUtil.close(`is`)
                            tempFile.delete()
                        }
                    } else {
                        val value: String = tempFile.toString()
                        _fileItems.put(fileName, Item(tempFile, ct, item.getName(), item.getFieldName()))
                        list.add(URLItem(item.getFieldName(), value, false))
                    }
                }
            }
            raw = list.toArray(arrayOfNulls<URLItem?>(list.size()))
            fillDecoded(raw, encoding, scriptProteced, pc.getApplicationContext().getSameFieldAsArray(SCOPE_FORM))
        } catch (e: Exception) {
            val log: Log = ThreadLocalPageContext.getLog(pc, "application")
            if (log != null) log.error("form.scope", e)
            fillDecodedEL(arrayOfNulls<URLItem?>(0), encoding, scriptProteced, pc.getApplicationContext().getSameFieldAsArray(SCOPE_FORM))
            initException = e
        }
    }

    /*
	 * private void initializeMultiPart(PageContext pc, boolean scriptProteced) {
	 * 
	 * File tempDir=FileWrapper.toFile(pc.getConfig().getTempDirectory());
	 * 
	 * // Create a factory for disk-based file items DiskFileItemFactory factory = new
	 * DiskFileItemFactory(-1,tempDir);
	 * 
	 * // Create a new file upload handler ServletFileUpload upload = new ServletFileUpload(factory);
	 * 
	 * upload.setHeaderEncoding(getEncoding());
	 * 
	 * //FileUpload fileUpload=new FileUpload(new DiskFileItemFactory(0,tempDir)); java.util.List list;
	 * try { list = upload.parseRequest(pc.getHttpServletRequest()); raw=new
	 * ByteNameValuePair[list.size()];
	 * 
	 * for(int i=0;i<raw.length;i++) { DiskFileItem val=(DiskFileItem) list.get(i);
	 * if(val.isFormField()) { raw[i]=new
	 * ByteNameValuePair(getBytes(val.getFieldName()),val.get(),false); } else {
	 * print.out("-------------------------------"); print.out("fieldname:"+val.getFieldName());
	 * print.out("name:"+val.getName()); print.out("formfield:"+val.isFormField());
	 * print.out("memory:"+val.isInMemory());
	 * print.out("exist:"+val.getStoreLocation().getCanonicalFile().exists());
	 * 
	 * fileItems.put(val.getFieldName().toLowerCase(),val);
	 * 
	 * raw[i]=new
	 * ByteNameValuePair(getBytes(val.getFieldName()),val.getStoreLocation().getCanonicalFile().toString
	 * ().getBytes(),false);
	 * //raw.put(val.getFieldName(),val.getStoreLocation().getCanonicalFile().toString()); } }
	 * fillDecoded(raw,encoding,scriptProteced); } catch (Exception e) {
	 * 
	 * //throw new PageRuntimeException(Caster.toPageException(e)); fillDecodedEL(new
	 * ByteNameValuePair[0],encoding,scriptProteced); initException=e; } }
	 */
    private fun initializeUrlEncodedOrTextPlain(pc: PageContext?, delimiter: Char, scriptProteced: Boolean) {
        var reader: BufferedReader? = null
        try {
            reader = pc.getHttpServletRequest().getReader()
            raw = setFrom___(IOUtil.toString(reader, false), delimiter)
            fillDecoded(raw, encoding, scriptProteced, pc.getApplicationContext().getSameFieldAsArray(SCOPE_FORM))
        } catch (e: Exception) {
            val log: Log = ThreadLocalPageContext.getLog(pc, "application")
            if (log != null) log.error("form.scope", e)
            fillDecodedEL(arrayOfNulls<URLItem?>(0), encoding, scriptProteced, pc.getApplicationContext().getSameFieldAsArray(SCOPE_FORM))
            initException = e
        } finally {
            try {
                IOUtil.close(reader)
            } catch (e: IOException) {
                val log: Log = ThreadLocalPageContext.getLog(pc, "application")
                if (log != null) log.error("form.scope", e)
            }
        }
    }

    @Override
    override fun release(pc: PageContext?) {
        super.release(pc)
        encoding = null
        scriptProtected = ScriptProtected.UNDEFINED
        raw = empty
        if (!_fileItems!!.isEmpty()) {
            val it: Iterator<Item?> = _fileItems.values().iterator()
            var item: Item?
            while (it.hasNext()) {
                item = it.next()
                item!!.getResource().delete()
            }
            _fileItems.clear()
        }
        initException = null
    }

    @Override
    fun getFileItems(): Array<FormItem?>? {
        if (_fileItems == null || _fileItems.isEmpty()) return arrayOfNulls<Item?>(0)
        val it: Iterator<Item?> = _fileItems.values().iterator()
        val rtn = arrayOfNulls<Item?>(_fileItems.size())
        var index = 0
        while (it.hasNext()) {
            rtn[index++] = it.next()
        }
        return rtn
    }

    fun getFileUpload(key: String?): DiskFileItem? {
        return null
    }

    @Override
    fun getUploadResource(key: String?): FormItem? {
        val keyC = makeComparable(key)
        if (_fileItems == null || _fileItems.isEmpty()) return null
        val it: Iterator<Entry<String?, Item?>?> = _fileItems.entrySet().iterator()
        var entry: Entry<String?, Item?>?
        var item: Item
        while (it.hasNext()) {
            entry = it.next()
            item = entry.getValue()
            if (item.getFieldName().equalsIgnoreCase(keyC)) return item

            // /file.tmp
            if (item.getResource().getAbsolutePath().equalsIgnoreCase(key)) return item
            if (entry.getKey().equalsIgnoreCase(key)) return item
        }
        return null
    }

    private fun makeComparable(key: String?): String? {
        var key = key
        key = StringUtil.trim(key, "")

        // form.x
        if (StringUtil.startsWithIgnoreCase(key, "form.")) key = key.substring(5).trim()

        // form . x
        try {
            val array: Array = ListUtil.listToArray(key, '.')
            if (array.size() > 1 && array.getE(1).toString().trim().equalsIgnoreCase("form")) {
                array.removeE(1)
                key = ListUtil.arrayToList(array, ".").trim()
            }
        } catch (e: PageException) {
        }
        return key
    }

    @Override
    fun getInitException(): PageException? {
        return if (initException != null) Caster.toPageException(initException) else null
    }

    @Override
    override fun setScriptProtecting(ac: ApplicationContext?, scriptProtected: Boolean) {
        val _scriptProtected: Int = if (scriptProtected) ScriptProtected.YES else ScriptProtected.NO
        if (isInitalized() && _scriptProtected != this.scriptProtected) {
            fillDecodedEL(raw, encoding, scriptProtected, ac.getSameFieldAsArray(SCOPE_FORM))
            setFieldNames()
        }
        this.scriptProtected = _scriptProtected
    }

    @Override
    override fun isScriptProtected(): Boolean {
        return scriptProtected == ScriptProtected.YES
    }

    /**
     * @return the raw
     */
    fun getRaw(): Array<URLItem?>? {
        return raw
    }

    fun addRaw(ac: ApplicationContext?, raw: Array<URLItem?>?) {
        val nr: Array<URLItem?> = arrayOfNulls<URLItem?>(this.raw!!.size + raw!!.size)
        for (i in this.raw.indices) {
            nr[i] = this.raw!![i]
        }
        for (i in raw.indices) {
            nr[this.raw!!.size + i] = raw!![i]
        }
        this.raw = nr
        if (!isInitalized()) return
        fillDecodedEL(this.raw, encoding, isScriptProtected(), if (ac != null) ac.getSameFieldAsArray(SCOPE_FORM) else false)
        setFieldNames()
    }

    private inner class Item(resource: Resource?, contentType: String?, var name: String?, private val fieldName: String?) : FormItem {
        var resource: Resource?
        var contentType: String?

        /**
         * @return the resource
         */
        @Override
        fun getResource(): Resource? {
            return resource
        }

        /**
         * @return the contentType
         */
        @Override
        fun getContentType(): String? {
            return contentType
        }

        /**
         * @return the name
         */
        @Override
        fun getName(): String? {
            return name
        }

        /**
         * @return the fieldName
         */
        @Override
        fun getFieldName(): String? {
            return fieldName
        }

        init {
            this.resource = resource
            this.contentType = contentType
        }
    }

    /**
     * @return return content as a http header input stream
     */
    @Override
    fun getInputStream(): ServletInputStream? {
        if (headerType == HEADER_APP_URL_ENC) {
            return ServletInputStreamDummy(toBarr(raw, AMP))
        } else if (headerType == HEADER_TEXT_PLAIN) {
            return ServletInputStreamDummy(toBarr(raw, NL))
        }
        /*
		 * else if(headerType==HEADER_MULTIPART_FORM_DATA) { return new FormImplInputStream(this); // TODO }
		 */return ServletInputStreamDummy(byteArrayOf())
    }

    private fun toBarr(items: Array<URLItem?>?, del: Byte): ByteArray? {
        val raw: Array<ByteNameValuePair?> = arrayOfNulls<ByteNameValuePair?>(items!!.size)
        for (i in raw.indices) {
            try {
                raw[i] = ByteNameValuePair(items!![i].getName().getBytes("iso-8859-1"), items[i].getValue().getBytes("iso-8859-1"), items[i].isUrlEncoded())
            } catch (e: UnsupportedEncodingException) {
            }
        }
        var size = 0
        if (!ArrayUtil.isEmpty(raw)) {
            for (i in raw.indices) {
                size += raw[i].getName().length
                size += raw[i].getValue().length
                size += 2
            }
            size--
        }
        val barr = ByteArray(size)
        var bname: ByteArray
        var bvalue: ByteArray
        var count = 0
        for (i in raw.indices) {
            bname = raw[i].getName()
            bvalue = raw[i].getValue()
            // name
            for (y in bname.indices) {
                barr[count++] = bname[y]
            }
            barr[count++] = EQL
            // value
            for (y in bvalue.indices) {
                barr[count++] = bvalue[y]
            }
            if (i + 1 < raw.size) barr[count++] = del
        }
        return barr
    }

    companion object {
        private const val serialVersionUID = -2618472604584253354L
        private val empty: Array<URLItem?>? = arrayOfNulls<URLItem?>(0)
        private const val count: Long = 1
        private const val HEADER_TYPE_UNKNOWN = -1
        private const val HEADER_TEXT_PLAIN = 0
        private const val HEADER_MULTIPART_FORM_DATA = 1
        private const val HEADER_APP_URL_ENC = 2
        private fun getFileName(): String? {
            val uuid: UUID = UUID.randomUUID()
            val setUUID: String = uuid.toString()
            return "tmp-$setUUID.upload"
        }
    }
}