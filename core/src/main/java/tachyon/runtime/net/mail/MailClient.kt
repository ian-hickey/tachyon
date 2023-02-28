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
package tachyon.runtime.net.mail

import java.io.IOException

abstract class MailClient(server: String?, port: Int, username: String?, password: String?, secure: Boolean) : PoolItem {
    // TODO Auto-generated catch block
    // goal is to be valid if requested so we try to be
    @get:Override
    val isValid: Boolean
        get() {
            if (_store != null && !_store.isConnected()) {
                // goal is to be valid if requested so we try to be
                try {
                    start()
                } catch (e: MessagingException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }
            return _store != null && _store.isConnected()
        }

    /**
     * Simple authenicator implmentation
     */
    private inner class _Authenticator(s: String?, s1: String?) : Authenticator() {
        private var _fldif: String? = null
        private var a: String? = null

        @get:Override
        protected val passwordAuthentication: PasswordAuthentication?
            protected get() = PasswordAuthentication(_fldif, a)

        init {
            _fldif = s
            a = s1
        }
    }

    private val _flddo: Array<String?>? = arrayOf("date", "from", "messagenumber", "messageid", "replyto", "subject", "cc", "to", "size", "header", "uid")
    private val _fldnew: Array<String?>? = arrayOf("date", "from", "messagenumber", "messageid", "replyto", "subject", "cc", "to", "size", "header", "uid", "body", "textBody", "HTMLBody",
            "attachments", "attachmentfiles", "cids")
    private val server: String? = null
    private val username: String? = null
    private val password: String? = null
    private var _session: Session? = null
    private var _store: Store? = null
    private val port = 0
    private var timeout = 0
    private var startrow = 0
    private var maxrows = 0
    private var uniqueFilenames = false
    private var attachmentDirectory: Resource? = null
    private val secure: Boolean
    private var delimiter: String? = ","

    /**
     * @param maxrows The maxrows to set.
     */
    fun setMaxrows(maxrows: Int) {
        this.maxrows = maxrows
    }

    /**
     * @param startrow The startrow to set.
     */
    fun setStartrow(startrow: Int) {
        this.startrow = startrow
    }

    /**
     * @param timeout The timeout to set.
     */
    fun setTimeout(timeout: Int) {
        this.timeout = timeout
    }

    /**
     * @param uniqueFilenames The uniqueFilenames to set.
     */
    fun setUniqueFilenames(uniqueFilenames: Boolean) {
        this.uniqueFilenames = uniqueFilenames
    }

    /**
     * @param attachmentDirectory The attachmentDirectory to set.
     */
    fun setAttachmentDirectory(attachmentDirectory: Resource?) {
        this.attachmentDirectory = attachmentDirectory
    }

    /**
     * @param delimiter The delimiter to set.
     */
    fun setDelimiter(delimiter: String?) {
        this.delimiter = delimiter
    }

    /**
     * connects to pop server
     *
     * @throws MessagingException
     */
    @Override
    @Throws(MessagingException::class)
    fun start() {
        val properties = Properties()
        val type = typeAsString
        properties.setProperty("mail.$type.host", server)
        properties.setProperty("mail.$type.port", String.valueOf(port))
        properties.setProperty("mail.$type.connectiontimeout", String.valueOf(timeout))
        properties.setProperty("mail.$type.timeout", String.valueOf(timeout))
        // properties.setProperty("mail.mime.charset", "UTF-8");
        if (secure) {
            properties.setProperty("mail.$type.ssl.enable", "true")
            // properties.setProperty("mail."+type+".starttls.enable", "true" );
            // allow using untrusted certs, good for CI
            if (!Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("tachyon.ssl.checkserveridentity", null), true)) {
                properties.setProperty("mail.$type.ssl.trust", "*")
                properties.setProperty("mail.$type.ssl.checkserveridentity", "false")
            }
        }
        if (TYPE_IMAP == type) {
            if (secure) {
                properties.put("mail.store.protocol", "imaps")
                properties.put("mail.imaps.partialfetch", "false")
                properties.put("mail.imaps.fetchsize", "1048576")
            } else {
                properties.put("mail.store.protocol", "imap")
                properties.put("mail.imap.partialfetch", "false")
                properties.put("mail.imap.fetchsize", "1048576")
            }
        }
        // if(TYPE_POP3==getType()){}
        _session = if (username != null) Session.getInstance(properties, _Authenticator(username, password)) else Session.getInstance(properties)
        val t: Thread = Thread.currentThread()
        val ccl: ClassLoader = t.getContextClassLoader()
        t.setContextClassLoader(_session.getClass().getClassLoader())
        try {
            _store = _session.getStore(type)
            if (!StringUtil.isEmpty(username)) _store.connect(server, port, username, password) else _store.connect()
        } finally {
            t.setContextClassLoader(ccl)
        }
    }

    protected abstract val typeAsString: String?
    protected abstract val type: Int

    /**
     * delete all message in ibox that match given criteria
     *
     * @param messageNumbers
     * @param uIds
     * @throws MessagingException
     * @throws IOException
     * @throws PageException
     */
    @Throws(MessagingException::class, IOException::class, PageException::class)
    fun deleteMails(messageNumber: String?, uid: String?) {
        val folder: Folder
        val amessage: Array<Message?>?
        folder = _store.getFolder("INBOX")
        folder.open(2)
        val map: Map<String?, Message?>? = getMessages(null, folder, uid, messageNumber, startrow, maxrows, false)
        val iterator: Iterator<String?> = map.keySet().iterator()
        amessage = arrayOfNulls<Message?>(map!!.size())
        var i = 0
        while (iterator.hasNext()) {
            amessage!![i++] = map!![iterator.next()]
        }
        try {
            folder.setFlags(amessage, Flags(javax.mail.Flags.Flag.DELETED), true)
        } finally {
            folder.close(true)
        }
    }

    /**
     * return all messages from inbox
     *
     * @param messageNumbers all messages with this ids
     * @param uIds all messages with this uids
     * @param withBody also return body
     * @return all messages from inbox
     * @throws MessagingException
     * @throws IOException
     * @throws PageException
     */
    @Throws(MessagingException::class, IOException::class, PageException::class)
    fun getMails(messageNumbers: String?, uids: String?, all: Boolean, folderName: String?): Query? {
        var folderName = folderName
        val qry: Query = QueryImpl(if (all) _fldnew else _flddo, 0, "query")
        if (StringUtil.isEmpty(folderName, true)) folderName = "INBOX" else folderName = folderName.trim()
        val folder: Folder = _store.getFolder(folderName)
        folder.open(Folder.READ_ONLY)
        try {
            getMessages(qry, folder, uids, messageNumbers, startrow, maxrows, all)
        } finally {
            folder.close(false)
        }
        return qry
    }

    private fun toQuery(qry: Query?, message: Message?, uid: Object?, all: Boolean) {
        val row: Int = qry.addRow()
        // date
        try {
            qry.setAtEL(DATE, row, Caster.toDate(message.getSentDate(), true, null, null))
        } catch (e: MessagingException) {
        }

        // subject
        try {
            qry.setAtEL(SUBJECT, row, message.getSubject())
        } catch (e: MessagingException) {
            qry.setAtEL(SUBJECT, row, "MessagingException:" + e.getMessage())
        }

        // size
        try {
            qry.setAtEL(SIZE, row, Double.valueOf(message.getSize()))
        } catch (e: MessagingException) {
        }
        qry.setAtEL(FROM, row, toList(getHeaderEL(message, "from")))
        qry.setAtEL(MESSAGE_NUMBER, row, Double.valueOf(message.getMessageNumber()))
        qry.setAtEL(MESSAGE_ID, row, toList(getHeaderEL(message, "Message-ID")))
        var s = toList(getHeaderEL(message, "reply-to"))
        if (s!!.length() === 0) {
            s = Caster.toString(qry.getAt(FROM, row, null), "")
        }
        qry.setAtEL(REPLYTO, row, s)
        qry.setAtEL(CC, row, toList(getHeaderEL(message, "cc")))
        qry.setAtEL(BCC, row, toList(getHeaderEL(message, "bcc")))
        qry.setAtEL(TO, row, toList(getHeaderEL(message, "to")))
        qry.setAtEL(UID, row, uid)
        val content = StringBuffer()
        try {
            val enumeration: Enumeration = message.getAllHeaders()
            while (enumeration.hasMoreElements()) {
                val header: Header = enumeration.nextElement() as Header
                content.append(header.getName())
                content.append(": ")
                content.append(header.getValue())
                content.append('\n')
            }
        } catch (e: MessagingException) {
        }
        qry.setAtEL(HEADER, row, content.toString())
        if (all) {
            getContentEL(qry, message, row)
        }
    }

    private fun getHeaderEL(message: Message?, key: String?): Array<String?>? {
        return try {
            message.getHeader(key)
        } catch (e: MessagingException) {
            null
        }
    }

    /**
     * gets all messages from given Folder that match given criteria
     *
     * @param qry
     * @param folder
     * @param uIds
     * @param messageNumbers
     * @param all
     * @param startrow
     * @param maxrows
     * @return
     * @return matching Messages
     * @throws MessagingException
     * @throws PageException
     */
    @Throws(MessagingException::class, PageException::class)
    private fun getMessages(qry: Query?, folder: Folder?, uids: String?, messageNumbers: String?, startRow: Int, maxRow: Int, all: Boolean): Map<String?, Message?>? {
        var startRow = startRow
        var maxRow = maxRow
        var messages: Array<Message?>? = null
        var uidsStringArray: Array<String?>? = null
        val map: Map<String?, Message?>? = if (qry == null) HashMap<String?, Message?>() else null
        var k = 0
        if (uids != null || messageNumbers != null) {
            startRow = 0
            maxRow = -1
        }
        if (uids != null) {
            if (type == TYPE_IMAP) {
                messages = (folder as UIDFolder?).getMessagesByUID(ListUtil.listToLongArray(uids, delimiter))
            } else { // POP3 folder doesn't supports the getMessagesByUID method from UIDFolder
                uidsStringArray = ArrayUtil.trimItems(ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(uids, delimiter)))
            }
        } else if (messageNumbers != null) {
            messages = folder.getMessages(ListUtil.listToIntArrayWithMaxRange(messageNumbers, ',', folder.getMessageCount()))
        }
        if (messages == null) messages = folder.getMessages()
        var message: Message?
        for (l in startRow until messages.size) {
            if (maxRow != -1 && k == maxRow) {
                break
            }
            message = messages[l]
            if (message == null) continue  // because the message can be a null for non existing messageNumbers
            val id = getId(folder, message)
            if (uidsStringArray == null || uidsStringArray != null && contains(uidsStringArray, id)) {
                k++
                if (qry != null) {
                    toQuery(qry, message, id, all)
                } else map.put(id, message)
            }
        }
        return map
    }

    @Throws(MessagingException::class)
    protected fun getId(folder: Folder?, message: Message?): String? {
        return _getId(folder, message)
    }

    @Throws(MessagingException::class)
    protected abstract fun _getId(folder: Folder?, message: Message?): String?
    private fun getContentEL(query: Query?, message: Message?, row: Int) {
        try {
            getContent(query, message, row)
        } catch (e: Exception) {
            val st: String = ExceptionUtil.getStacktrace(e, true)
            query.setAtEL(BODY, row, st)
        }
    }

    /**
     * write content data to query
     *
     * @param qry
     * @param content
     * @param row
     * @throws MessagingException
     * @throws IOException
     */
    @Throws(MessagingException::class, IOException::class)
    private fun getContent(query: Query?, message: Message?, row: Int) {
        val body = StringBuffer()
        val cids: Struct = StructImpl()
        query.setAtEL(CIDS, row, cids)
        if (message.isMimeType("text/plain")) {
            val content = getConent(message)
            query.setAtEL(TEXT_BODY, row, content)
            body.append(content)
        } else if (message.isMimeType("text/html")) {
            val content = getConent(message)
            query.setAtEL(HTML_BODY, row, content)
            body.append(content)
        } else {
            val content: Object = message.getContent()
            if (content is MimeMultipart) {
                val attachments: Array = ArrayImpl()
                val attachmentFiles: Array = ArrayImpl()
                getMultiPart(query, row, attachments, attachmentFiles, cids, content as MimeMultipart, body)
                if (attachments.size() > 0) {
                    try {
                        query.setAtEL(ATTACHMENTS, row, ListUtil.arrayToList(attachments, "\t"))
                    } catch (pageexception: PageException) {
                    }
                }
                if (attachmentFiles.size() > 0) {
                    try {
                        query.setAtEL(ATTACHMENT_FILES, row, ListUtil.arrayToList(attachmentFiles, "\t"))
                    } catch (pageexception1: PageException) {
                    }
                }
            }
        }
        query.setAtEL(BODY, row, body.toString())
    }

    @Throws(MessagingException::class, IOException::class)
    private fun getMultiPart(query: Query?, row: Int, attachments: Array?, attachmentFiles: Array?, cids: Struct?, multiPart: Multipart?, body: StringBuffer?) {
        val j: Int = multiPart.getCount()
        for (k in 0 until j) {
            val bodypart: BodyPart = multiPart.getBodyPart(k)
            var content: Object?
            if (bodypart.getFileName() != null) {
                var filename: String = bodypart.getFileName()
                try {
                    filename = Normalizer.normalize(MimeUtility.decodeText(filename), Normalizer.Form.NFC)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
                if (bodypart.getHeader("Content-ID") != null) {
                    val ids: Array<String?> = bodypart.getHeader("Content-ID")
                    val cid: String = ids[0].substring(1, ids[0]!!.length() - 1)
                    cids.setEL(KeyImpl.init(filename), cid)
                }
                if (filename != null && ArrayUtil.find(attachments, filename) >= 0) {
                    attachments.appendEL(filename)
                    if (attachmentDirectory != null) {
                        var file: Resource = attachmentDirectory.getRealResource(filename)
                        var l = 1
                        var s2: String
                        while (uniqueFilenames && file.exists()) {
                            val `as`: Array<String?> = ResourceUtil.splitFileName(filename)
                            s2 = if (`as`.size != 1) `as`[0] + l++ + '.'.toInt() + `as`[1] else `as`[0] + l++
                            file = attachmentDirectory.getRealResource(s2)
                        }
                        IOUtil.copy(bodypart.getInputStream(), file, true)
                        attachmentFiles.appendEL(file.getAbsolutePath())
                    }
                }
            } else if (bodypart.isMimeType("text/plain")) {
                content = getConent(bodypart)
                query.setAtEL(TEXT_BODY, row, content)
                if (body.length() === 0) body.append(content)
            } else if (bodypart.isMimeType("text/html")) {
                content = getConent(bodypart)
                query.setAtEL(HTML_BODY, row, content)
                if (body.length() === 0) body.append(content)
            } else if (bodypart.getContent().also { content = it } is Multipart) {
                getMultiPart(query, row, attachments, attachmentFiles, cids, content as Multipart?, body)
            } else if (bodypart.getHeader("Content-ID") != null) {
                val ids: Array<String?> = bodypart.getHeader("Content-ID")
                val cid: String = ids[0].substring(1, ids[0]!!.length() - 1)
                var filename = "cid:$cid"
                attachments.appendEL(filename)
                if (attachmentDirectory != null) {
                    filename = "_" + Md5.getDigestAsString(filename)
                    var file: Resource = attachmentDirectory.getRealResource(filename)
                    var l = 1
                    var s2: String
                    while (uniqueFilenames && file.exists()) {
                        val `as`: Array<String?> = ResourceUtil.splitFileName(filename)
                        s2 = if (`as`.size != 1) `as`[0] + l++ + '.'.toInt() + `as`[1] else `as`[0] + l++
                        file = attachmentDirectory.getRealResource(s2)
                    }
                    IOUtil.copy(bodypart.getInputStream(), file, true)
                    attachmentFiles.appendEL(file.getAbsolutePath())
                }
                cids.setEL(KeyImpl.init(filename), cid)
            } else if (bodypart.getContent().also { content = it } is MimeMessage) {
                content = getConent(bodypart)
                if (body.length() === 0) body.append(content)
            }
        }
    }

    /*
	 * * writes BodyTag data to query, if there is a problem with encoding, encoding will removed a do
	 * it again
	 * 
	 * @param qry
	 * 
	 * @param columnName
	 * 
	 * @param row
	 * 
	 * @param bp
	 * 
	 * @param body
	 * 
	 * @throws IOException
	 * 
	 * @throws MessagingException / private void setBody(Query qry, String columnName, int row, BodyPart
	 * bp, StringBuffer body) throws IOException, MessagingException { String content = getConent(bp);
	 * 
	 * qry.setAtEL(columnName,row,content); if(body.length()==0)body.append(content);
	 * 
	 * }
	 */
    @Throws(MessagingException::class)
    private fun getConent(bp: Part?): String? {
        var `is`: InputStream? = null
        return try {
            `is` = if (bp.getContent() is MimeMessage) {
                val mimeContent: MimeMessage = bp.getContent() as MimeMessage
                mimeContent.getInputStream()
            } else {
                bp.getInputStream()
            }
            getContent(`is`, CharsetUtil.toCharset(getCharsetFromContentType(bp.getContentType())))
        } catch (mie: IOException) {
            IOUtil.closeEL(`is`)
            try {
                getContent(`is`, SystemUtil.getCharset())
            } catch (e: IOException) {
                "Cannot read body of this message: " + e.getMessage()
            }
        } finally {
            IOUtil.closeEL(`is`)
        }
    }

    @Throws(IOException::class)
    private fun getContent(`is`: InputStream?, charset: Charset?): String? {
        return MailUtil.decode(IOUtil.toString(`is`, charset))
    }

    /**
     * checks if a String Array (ids) has one element that is equal to id
     *
     * @param ids
     * @param id
     * @return has element found or not
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun contains(ids: Array<String?>?, id: String?): Boolean {
        for (i in ids.indices) {
            if (OpUtil.compare(ThreadLocalPageContext.get(), ids!![i], id) === 0) return true
        }
        return false
    }

    /**
     * checks if a String Array (ids) has one element that is equal to id
     *
     * @param ids
     * @param id
     * @return has element found or not
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun contains(ids: Array<String?>?, id: Int): Boolean {
        for (i in ids.indices) {
            if (OpUtil.compare(ThreadLocalPageContext.get(), ids!![i], id) === 0) return true
        }
        return false
    }

    /**
     * translate a String Array to String List
     *
     * @param arr Array to translate
     * @return List from Array
     */
    private fun toList(ids: Array<String?>?): String? {
        return if (ids == null) "" else ListUtil.arrayToList(ids, ",")
    }

    /**
     * disconnect without an exception
     */
    @Override
    fun end() {
        try {
            if (_store != null) _store.close()
        } catch (exception: Exception) {
        }
    }

    // IMAP only
    @Throws(MessagingException::class, ApplicationException::class)
    fun createFolder(folderName: String?) {
        if (folderExists(folderName)) throw ApplicationException("Cannot create imap folder [$folderName], the folder already exists.")
        val folder: Folder? = getFolder(folderName, null, false, true)
        if (!folder.exists()) folder.create(Folder.HOLDS_MESSAGES)
    }

    @Throws(MessagingException::class)
    private fun folderExists(folderName: String?): Boolean {
        val folderNames = toFolderNames(folderName)
        var folder: Folder? = null
        for (i in folderNames.indices) {
            folder = if (folder == null) _store.getFolder(folderNames!![i]) else folder.getFolder(folderNames!![i])
            if (!folder.exists()) return false
        }
        return true
    }

    private fun toFolderNames(folderName: String?): Array<String?>? {
        return if (StringUtil.isEmpty(folderName)) arrayOfNulls<String?>(0) else ListUtil.trimItems(ListUtil.trim(ListUtil.listToStringArray(folderName, '/')))
    }

    @Throws(MessagingException::class, ApplicationException::class)
    fun deleteFolder(folderName: String?) {
        if (folderName.equalsIgnoreCase("INBOX") || folderName.equalsIgnoreCase("OUTBOX")) throw ApplicationException("Cannot delete folder [$folderName], this folder is protected.")
        val folderNames = toFolderNames(folderName)
        val folder: Folder = _store.getFolder(folderNames!![0])
        if (!folder.exists()) {
            throw ApplicationException("There is no folder with name [$folderName].")
        }
        folder.delete(true)
    }

    @Throws(MessagingException::class, ApplicationException::class)
    fun renameFolder(srcFolderName: String?, trgFolderName: String?) {
        if (srcFolderName.equalsIgnoreCase("INBOX") || srcFolderName.equalsIgnoreCase("OUTBOX")) throw ApplicationException("Cannot rename folder [$srcFolderName], this folder is protected.")
        if (trgFolderName.equalsIgnoreCase("INBOX") || trgFolderName.equalsIgnoreCase("OUTBOX")) throw ApplicationException("Cannot rename folder to [$trgFolderName], this folder name is protected.")
        val src: Folder? = getFolder(srcFolderName, true, true, false)
        val trg: Folder? = getFolder(trgFolderName, null, false, true)
        if (!src.renameTo(trg)) throw ApplicationException("Cannot rename folder [$srcFolderName] to [$trgFolderName].")
    }

    @Throws(MessagingException::class, PageException::class)
    fun listAllFolder(folderName: String?, recurse: Boolean, startrow: Int, maxrows: Int): Query? {
        val qry: Query = QueryImpl(arrayOf<Collection.Key?>(FULLNAME, KeyConstants._NAME, TOTALMESSAGES, UNREAD, PARENT, NEW), 0, "folders")
        // if(StringUtil.isEmpty(folderName)) folderName="INBOX";
        val folder: Folder = if (StringUtil.isEmpty(folderName)) _store.getDefaultFolder() else _store.getFolder(folderName)
        // Folder folder=_store.getFolder(folderName);
        if (!folder.exists()) throw ApplicationException("There is no folder with name [$folderName].")
        list(folder, qry, recurse, startrow, maxrows, 0)
        return qry
    }

    @Throws(MessagingException::class, PageException::class)
    fun moveMail(srcFolderName: String?, trgFolderName: String?, messageNumber: String?, uid: String?) {
        var srcFolderName = srcFolderName
        if (StringUtil.isEmpty(srcFolderName, true)) srcFolderName = "INBOX"
        val srcFolder: Folder? = getFolder(srcFolderName, true, true, false)
        val trgFolder: Folder? = getFolder(trgFolderName, true, true, false)
        try {
            srcFolder.open(2)
            trgFolder.open(2)
            val amessage: Array<Message?>?
            val map: Map<String?, Message?>? = getMessages(null, srcFolder, uid, messageNumber, startrow, maxrows, false)
            val iterator: Iterator<String?> = map.keySet().iterator()
            amessage = arrayOfNulls<Message?>(map!!.size())
            var i = 0
            while (iterator.hasNext()) {
                amessage!![i++] = map!![iterator.next()]
            }
            srcFolder.copyMessages(amessage, trgFolder)
            srcFolder.setFlags(amessage, Flags(javax.mail.Flags.Flag.DELETED), true)
        } finally {
            srcFolder.close(true)
            trgFolder.close(true)
        }
    }

    @Throws(MessagingException::class, ApplicationException::class)
    fun markRead(folderName: String?) {
        var folderName = folderName
        if (StringUtil.isEmpty(folderName)) folderName = "INBOX"
        var folder: Folder? = null
        try {
            folder = getFolder(folderName, true, true, false)
            folder.open(2)
            val msgs: Array<Message?> = folder.getMessages()
            folder.setFlags(msgs, Flags(Flags.Flag.SEEN), true)
        } finally {
            if (folder != null) folder.close(false)
        }
    }

    @Throws(MessagingException::class, ApplicationException::class)
    private fun getFolder(folderName: String?, existingParent: Boolean?, existing: Boolean?, createParentIfNotExists: Boolean): Folder? {
        val folderNames = toFolderNames(folderName)
        var folder: Folder? = null
        var fn: String?
        for (i in folderNames.indices) {
            fn = folderNames!![i]
            folder = if (folder == null) _store.getFolder(fn) else folder.getFolder(fn)

            // top
            if (i + 1 == folderNames.size) {
                if (existing != null) {
                    if (existing.booleanValue() && !folder.exists()) throw ApplicationException("There is no folder with name [$folderName].")
                    if (!existing.booleanValue() && folder.exists()) throw ApplicationException("There is already a folder with name [$folderName].")
                }
            } else {
                if (existingParent != null) {
                    if (existingParent.booleanValue() && !folder.exists()) throw ApplicationException("There is no parent folder for folder with name [$folderName].")
                    if (!existingParent.booleanValue() && folder.exists()) throw ApplicationException("There is already a parent folder for folder with name [$folderName].")
                }
                if (createParentIfNotExists && !folder.exists()) {
                    folder.create(Folder.HOLDS_MESSAGES)
                }
            }
        }
        return folder
    }

    @Throws(MessagingException::class, PageException::class)
    private fun list(folder: Folder?, qry: Query?, recurse: Boolean, startrow: Int, maxrows: Int, rowsMissed: Int) {
        var rowsMissed = rowsMissed
        val folders: Array<Folder?> = folder.list()
        if (ArrayUtil.isEmpty(folders)) return
        for (f in folders) {
            // start row
            if (startrow - 1 > rowsMissed) {
                rowsMissed++
                continue
            }
            // max rows
            if (maxrows > 0 && qry.getRecordcount() >= maxrows) break
            if (f.getType() and Folder.HOLDS_MESSAGES === 0) continue
            val row: Int = qry.addRow()
            var p: Folder? = null
            try {
                p = f.getParent()
            } catch (me: MessagingException) {
            }
            qry.setAt(KeyConstants._NAME, row, f.getName())
            qry.setAt(FULLNAME, row, f.getFullName())
            qry.setAt(UNREAD, row, Caster.toDouble(f.getUnreadMessageCount()))
            qry.setAt(TOTALMESSAGES, row, Caster.toDouble(f.getMessageCount()))
            qry.setAt(NEW, row, Caster.toDouble(f.getNewMessageCount()))
            qry.setAt(PARENT, row, if (p != null) p.getName() else null)
            if (recurse) list(f, qry, recurse, startrow, maxrows, rowsMissed)
        }
    }

    /**
     * Open: Initiates an open session or connection with the IMAP server.
     *
     * Close: Terminates the open session or connection with the IMAP server.
     *
     */
    companion object {
        private val FULLNAME: Collection.Key? = KeyImpl.getInstance("FULLNAME")
        private val UNREAD: Collection.Key? = KeyImpl.getInstance("UNREAD")
        private val PARENT: Collection.Key? = KeyImpl.getInstance("PARENT")
        private val TOTALMESSAGES: Collection.Key? = KeyImpl.getInstance("TOTALMESSAGES")
        private val NEW: Collection.Key? = KeyImpl.getInstance("NEW")
        private val DATE: Collection.Key? = KeyImpl.getInstance("date")
        private val SUBJECT: Collection.Key? = KeyImpl.getInstance("subject")
        private val SIZE: Collection.Key? = KeyImpl.getInstance("size")
        private val FROM: Collection.Key? = KeyImpl.getInstance("from")
        private val MESSAGE_NUMBER: Collection.Key? = KeyImpl.getInstance("messagenumber")
        private val MESSAGE_ID: Collection.Key? = KeyImpl.getInstance("messageid")
        private val REPLYTO: Collection.Key? = KeyImpl.getInstance("replyto")
        private val CC: Collection.Key? = KeyImpl.getInstance("cc")
        private val BCC: Collection.Key? = KeyImpl.getInstance("bcc")
        private val TO: Collection.Key? = KeyImpl.getInstance("to")
        private val UID: Collection.Key? = KeyImpl.getInstance("uid")
        private val HEADER: Collection.Key? = KeyImpl.getInstance("header")
        private val BODY: Collection.Key? = KeyImpl.getInstance("body")
        private val CIDS: Collection.Key? = KeyImpl.getInstance("cids")
        private val TEXT_BODY: Collection.Key? = KeyImpl.getInstance("textBody")
        private val HTML_BODY: Collection.Key? = KeyImpl.getInstance("HTMLBody")
        private val ATTACHMENTS: Collection.Key? = KeyImpl.getInstance("attachments")
        private val ATTACHMENT_FILES: Collection.Key? = KeyImpl.getInstance("attachmentfiles")
        const val TYPE_POP3 = 0
        const val TYPE_IMAP = 1
        private val pool: Pool? = Pool(60000, 100, 5000)
        @Throws(Exception::class)
        fun getInstance(type: Int, server: String?, port: Int, username: String?, password: String?, secure: Boolean, name: String?, id: String?): MailClient? {
            var uid: String?
            uid = if (StringUtil.isEmpty(name)) createName(type, server, port, username, password, secure) else name
            uid = "$type;$uid;$id"
            var item: PoolItem? = pool.get(uid)
            if (item == null) {
                if (StringUtil.isEmpty(server)) {
                    if (StringUtil.isEmpty(name)) throw ApplicationException("missing mail server information") else throw ApplicationException("There is no connection available with name [$name]")
                }
                if (TYPE_POP3 == type) pool.put(uid, PopClient(server, port, username, password, secure).also { item = it })
                if (TYPE_IMAP == type) pool.put(uid, ImapClient(server, port, username, password, secure).also { item = it })
            }
            return item
        }

        @Throws(Exception::class)
        fun removeInstance(client: MailClient?) {
            pool.remove(client) // this will also call the stop method of the
        }

        private fun createName(type: Int, server: String?, port: Int, username: String?, password: String?, secure: Boolean): String? {
            return HashUtil.create64BitHashAsString(
                    StringBuilder().append(server).append(';').append(port).append(';').append(username).append(';').append(password).append(';').append(secure).append(';'), 16)
        }

        private fun getCharsetFromContentType(contentType: String?): String? {
            val arr: Array = ListUtil.listToArrayRemoveEmpty(contentType, "; ")
            for (i in 1..arr.size()) {
                val inner: Array = ListUtil.listToArray(arr.get(i, null) as String, "= ")
                if (inner.size() === 2 && (inner.get(1, "") as String).trim().equalsIgnoreCase("charset")) {
                    var charset: String? = inner.get(2, "")
                    charset = charset.trim()
                    if (!StringUtil.isEmpty(charset)) {
                        if (StringUtil.startsWith(charset, '"') && StringUtil.endsWith(charset, '"')) {
                            charset = charset.substring(1, charset!!.length() - 1)
                        }
                        if (StringUtil.startsWith(charset, '\'') && StringUtil.endsWith(charset, '\'')) {
                            charset = charset.substring(1, charset!!.length() - 1)
                        }
                    }
                    return charset
                }
            }
            return "us-ascii"
        }
    }

    /**
     * constructor of the class
     *
     * @param server
     * @param port
     * @param username
     * @param password
     * @param secure
     */
    init {
        timeout = 60000
        startrow = 0
        maxrows = -1
        delimiter = ","
        uniqueFilenames = false
        this.server = server
        this.port = port
        this.username = username
        this.password = password
        this.secure = secure
    }
}