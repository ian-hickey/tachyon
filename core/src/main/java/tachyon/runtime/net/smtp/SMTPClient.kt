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
package tachyon.runtime.net.smtp

import java.io.FileNotFoundException

class SMTPClient : Serializable {
    // private static final int PORT = 25;
    private var spool = SPOOL_UNDEFINED
    private var timeout = -1
    var plainTextAsString: String? = null
        private set
    private var plainTextCharset: CharSet? = null
    var hTMLTextAsString: String? = null
        private set
    private var htmlTextCharset: CharSet? = null
    private var attachmentz: Array<Attachment?>?
    private var host: Array<String?>?
    private var charset: CharSet? = CharSet.UTF8
    private var from: InternetAddress? = null
    private var tos: Array<InternetAddress?>?
    private var bccs: Array<InternetAddress?>?
    private var ccs: Array<InternetAddress?>?
    private var rts: Array<InternetAddress?>?
    private var fts: Array<InternetAddress?>?

    /**
     * @return the subject
     */
    var subject: String? = ""
    private var xmailer: String? = Constants.NAME.toString() + " Mail"
    private val headers: Map<String?, String?>? = HashMap<String?, String?>()
    private var port = -1
    private var username: String? = null
    private var password: String? = ""
    private var ssl = SSL_NONE
    private var tls = TLS_NONE
    var proxyData: ProxyData? = ProxyDataImpl()
    private var parts: ArrayList<MailPart?>? = null
    private var timeZone: TimeZone? = null
    private var lifeTimespan = (100 * 60 * 5).toLong()
    private var idleTimespan = (100 * 60 * 1).toLong()
    private var listener: Object? = null
    private var debug = false
    fun setSpoolenable(spoolenable: Boolean) {
        spool = if (spoolenable) SPOOL_YES else SPOOL_NO
    }

    /**
     * set port of the mailserver
     *
     * @param port
     */
    fun setPort(port: Int) {
        this.port = port
    }

    /**
     * enable console logging of the mail session to console
     *
     * @param debug
     */
    fun setDebug(debug: Boolean) {
        this.debug = debug
    }

    /**
     * @param charset the charset to set
     */
    fun setCharset(charset: Charset?) {
        this.charset = CharsetUtil.toCharSet(charset)
    }

    fun setCharSet(charset: CharSet?) {
        this.charset = charset
    }

    @Throws(PageException::class)
    fun setHost(host: String?) {
        if (!StringUtil.isEmpty(host, true)) this.host = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(host, ','))
    }

    fun setLifeTimespan(life: Long) {
        lifeTimespan = life
    }

    fun setIdleTimespan(idle: Long) {
        idleTimespan = idle
    }

    /**
     * @param password the password to set
     */
    fun setPassword(password: String?) {
        this.password = password
    }

    /**
     * @param username the username to set
     */
    fun setUsername(username: String?) {
        this.username = username
    }

    fun addHeader(name: String?, value: String?) {
        headers.put(name, value)
    }

    fun addTo(to: InternetAddress?) {
        tos = add(tos, to)
    }

    fun setTos(tos: Array<InternetAddress?>?) {
        this.tos = tos
    }

    @Throws(UnsupportedEncodingException::class, PageException::class, MailException::class)
    fun addTo(to: Object?) {
        val tmp: Array<InternetAddress?> = MailUtil.toInternetAddresses(to)
        for (i in tmp.indices) {
            addTo(tmp[i])
        }
    }

    fun setFrom(from: InternetAddress?) {
        this.from = from
    }

    @Throws(UnsupportedEncodingException::class, MailException::class, PageException::class)
    fun setFrom(from: Object?): Boolean {
        val addrs: Array<InternetAddress?> = MailUtil.toInternetAddresses(from)
        if (addrs.size == 0) return false
        setFrom(addrs[0])
        return true
    }

    fun addBCC(bcc: InternetAddress?) {
        bccs = add(bccs, bcc)
    }

    fun setBCCs(bccs: Array<InternetAddress?>?) {
        this.bccs = bccs
    }

    @Throws(UnsupportedEncodingException::class, MailException::class, PageException::class)
    fun addBCC(bcc: Object?) {
        val tmp: Array<InternetAddress?> = MailUtil.toInternetAddresses(bcc)
        for (i in tmp.indices) {
            addBCC(tmp[i])
        }
    }

    fun addCC(cc: InternetAddress?) {
        ccs = add(ccs, cc)
    }

    fun setCCs(ccs: Array<InternetAddress?>?) {
        this.ccs = ccs
    }

    @Throws(UnsupportedEncodingException::class, MailException::class, PageException::class)
    fun addCC(cc: Object?) {
        val tmp: Array<InternetAddress?> = MailUtil.toInternetAddresses(cc)
        for (i in tmp.indices) {
            addCC(tmp[i])
        }
    }

    fun addReplyTo(rt: InternetAddress?) {
        rts = add(rts, rt)
    }

    @Throws(UnsupportedEncodingException::class, MailException::class, PageException::class)
    fun addReplyTo(rt: Object?) {
        val tmp: Array<InternetAddress?> = MailUtil.toInternetAddresses(rt)
        for (i in tmp.indices) {
            addReplyTo(tmp[i])
        }
    }

    fun addFailTo(ft: InternetAddress?) {
        fts = add(fts, ft)
    }

    @Throws(UnsupportedEncodingException::class, MailException::class, PageException::class)
    fun addFailTo(ft: Object?) {
        val tmp: Array<InternetAddress?> = MailUtil.toInternetAddresses(ft)
        for (i in tmp.indices) {
            addFailTo(tmp[i])
        }
    }

    /**
     * @param timeout the timeout to set
     */
    fun setTimeout(timeout: Int) {
        this.timeout = timeout
    }

    fun setXMailer(xmailer: String?) {
        this.xmailer = xmailer
    }

    @Throws(ApplicationException::class)
    fun setListener(listener: Object?) {
        if (listener !is UDF && listener !is Component && !dblUDF(listener)) throw ApplicationException("Listener must be a Function or a Component.")
        this.listener = listener
    }

    private fun dblUDF(o: Object?): Boolean {
        if (o !is Struct) return false
        val sct: Struct? = o as Struct?
        return sct.get("before", null) is UDF || sct.get("after", null) is UDF // we need "before" OR "after"!
    }

    class MimeMessageAndSession(message: MimeMessage?, session: SessionAndTransport?, messageId: String?) {
        val message: MimeMessage?
        val session: SessionAndTransport?
        val messageId: String?

        init {
            this.message = message
            this.session = session
            this.messageId = messageId
        }
    }

    @Throws(MessagingException::class)
    private fun createMimeMessage(config: tachyon.runtime.config.Config?, hostName: String?, port: Int, username: String?, password: String?, lifeTimesan: Long,
                                  idleTimespan: Long, tls: Boolean, ssl: Boolean, sendPartial: Boolean, newConnection: Boolean, userset: Boolean): MimeMessageAndSession? {
        val props: Properties = System.getProperties().clone() as Properties
        val strTimeout: String = Caster.toString(getTimeout(config))
        props.put("mail.smtp.host", hostName)
        props.put("mail.smtp.timeout", strTimeout)
        props.put("mail.smtp.connectiontimeout", strTimeout)
        props.put("mail.smtp.sendpartial", Caster.toString(sendPartial))
        props.put("mail.smtp.userset", userset)
        if (port > 0) {
            props.put("mail.smtp.port", Caster.toString(port))
        }
        if (ssl) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            props.put("mail.smtp.socketFactory.port", Caster.toString(port))
            props.put("mail.smtp.socketFactory.fallback", "false")
        } else {
            props.put("mail.smtp.socketFactory.class", "javax.net.SocketFactory")
            props.remove("mail.smtp.socketFactory.port")
            props.remove("mail.smtp.socketFactory.fallback")
        }
        var auth: Authenticator? = null
        if (!StringUtil.isEmpty(username)) {
            props.put("mail.smtp.auth", "true")
            props.put("mail.smtp.starttls.enable", if (tls) "true" else "false")
            props.put("mail.smtp.user", username)
            props.put("mail.smtp.password", password)
            props.put("password", password)
            auth = SMTPAuthenticator(username, password)
        } else {
            props.put("mail.smtp.auth", "false")
            props.remove("mail.smtp.starttls.enable")
            props.remove("mail.smtp.user")
            props.remove("mail.smtp.password")
            props.remove("password")
        }
        val sat: SessionAndTransport = if (newConnection) SessionAndTransport(hash(props), props, auth, lifeTimesan, idleTimespan) else SMTPConnectionPool.getSessionAndTransport(props, hash(props), auth, lifeTimesan, idleTimespan)
        if (debug) sat.session.setDebug(true) // enable logging mail debug output to console

        // Contacts
        val msg = SMTPMessage(sat.session)
        if (from == null) throw MessagingException("A [from] email address is required to send an email")
        // if(tos==null)throw new MessagingException("A [to] email address is required to send an email");
        checkAddress(from, charset)
        // checkAddress(tos,charset);
        msg.setFrom(from)
        // msg.setRecipients(Message.RecipientType.TO, tos);
        if (tos != null) {
            checkAddress(tos, charset)
            msg.setRecipients(Message.RecipientType.TO, tos)
        }
        if (ccs != null) {
            checkAddress(ccs, charset)
            msg.setRecipients(Message.RecipientType.CC, ccs)
        }
        if (bccs != null) {
            checkAddress(bccs, charset)
            msg.setRecipients(Message.RecipientType.BCC, bccs)
        }
        if (rts != null) {
            checkAddress(rts, charset)
            msg.setReplyTo(rts)
        }
        if (fts != null) {
            checkAddress(fts, charset)
            msg.setEnvelopeFrom(fts!![0].toString())
        }

        // Subject and headers
        try {
            msg.setSubject(MailUtil.encode(subject, charset.name()))
        } catch (e: UnsupportedEncodingException) {
            throw MessagingException("the encoding $charset is not supported")
        }
        msg.setHeader("X-Mailer", xmailer)
        msg.setHeader("Date", getNow(timeZone))
        val messageId = getMessageId(headers) // Message-Id needs to be set after calling message.saveChanges();
        var mp: Multipart? = null

        // only Plain
        if (StringUtil.isEmpty(hTMLTextAsString)) {
            if (ArrayUtil.isEmpty(attachmentz) && ArrayUtil.isEmpty(parts)) {
                fillPlainText(config, msg)
                setHeaders(msg, headers)
                return MimeMessageAndSession(msg, sat, messageId)
            }
            mp = MimeMultipart("mixed")
            mp.addBodyPart(getPlainText(config))
        } else if (StringUtil.isEmpty(plainTextAsString)) {
            if (ArrayUtil.isEmpty(attachmentz) && ArrayUtil.isEmpty(parts)) {
                fillHTMLText(config, msg)
                setHeaders(msg, headers)
                return MimeMessageAndSession(msg, sat, messageId)
            }
            mp = MimeMultipart("mixed")
            mp.addBodyPart(getHTMLText(config))
        } else {
            mp = MimeMultipart("alternative")
            mp.addBodyPart(getPlainText(config))
            mp.addBodyPart(getHTMLText(config)) // this need to be last
            if (!ArrayUtil.isEmpty(attachmentz) || !ArrayUtil.isEmpty(parts)) {
                val content = MimeBodyPart()
                content.setContent(mp)
                mp = MimeMultipart("mixed")
                mp.addBodyPart(content)
            }
        }
        /*
		 * - mixed -- alternative --- text --- related ---- html ---- inline image ---- inline image --
		 * attachment -- attachment
		 * 
		 */

        // parts
        if (!ArrayUtil.isEmpty(parts)) {
            val it: Iterator<MailPart?> = parts.iterator()
            if (mp is MimeMultipart) (mp as MimeMultipart?).setSubType("alternative")
            while (it.hasNext()) {
                mp.addBodyPart(toMimeBodyPart(config, it.next()))
            }
        }

        // Attachments
        if (!ArrayUtil.isEmpty(attachmentz)) {
            for (i in attachmentz.indices) {
                mp.addBodyPart(toMimeBodyPart(mp, config, attachmentz!![i]))
            }
        }
        msg.setContent(mp)
        setHeaders(msg, headers)
        return MimeMessageAndSession(msg, sat, messageId)
    }

    private fun checkAddress(ias: Array<InternetAddress?>?, charset: CharSet?) { // DIFF 23
        for (i in ias.indices) {
            checkAddress(ias!![i], charset)
        }
    }

    private fun checkAddress(ia: InternetAddress?, charset: CharSet?) { // DIFF 23
        try {
            if (!StringUtil.isEmpty(ia.getPersonal())) {
                val personal: String = MailUtil.encode(ia.getPersonal(), charset.name())
                if (!personal.equals(ia.getPersonal())) ia.setPersonal(personal)
            }
        } catch (e: UnsupportedEncodingException) {
        }
    }

    /**
     * @param plainText
     */
    fun setPlainText(plainText: String?) {
        plainTextAsString = plainText
        plainTextCharset = charset
    }

    /**
     * @param plainText
     * @param plainTextCharset
     */
    fun setPlainText(plainText: String?, plainTextCharset: Charset?) {
        plainTextAsString = plainText
        this.plainTextCharset = CharsetUtil.toCharSet(plainTextCharset)
    }

    /**
     * @param htmlText
     */
    fun setHTMLText(htmlText: String?) {
        hTMLTextAsString = htmlText
        htmlTextCharset = charset
    }

    fun hasHTMLText(): Boolean {
        return hTMLTextAsString != null
    }

    fun hasPlainText(): Boolean {
        return plainTextAsString != null
    }

    /**
     * @param htmlText
     * @param htmlTextCharset
     */
    fun setHTMLText(htmlText: String?, htmlTextCharset: Charset?) {
        hTMLTextAsString = htmlText
        this.htmlTextCharset = CharsetUtil.toCharSet(htmlTextCharset)
    }

    fun addAttachment(url: URL?) {
        val mbp = Attachment(url)
        attachmentz = add(attachmentz, mbp)
    }

    fun addAttachment(resource: Resource?, fileName: String?, type: String?, disposition: String?, contentID: String?, removeAfterSend: Boolean) {
        val att = Attachment(resource, fileName, type, disposition, contentID, removeAfterSend)
        attachmentz = add(attachmentz, att)
    }

    @Throws(MessagingException::class)
    fun toMimeBodyPart(mp: Multipart?, config: tachyon.runtime.config.Config?, att: Attachment?): MimeBodyPart? {
        val mbp = MimeBodyPart()

        // set Data Source
        val strRes: String = att.getAbsolutePath()
        if (!StringUtil.isEmpty(strRes)) {
            mbp.setDataHandler(DataHandler(ResourceDataSource(config.getResource(strRes))))
        } else mbp.setDataHandler(DataHandler(URLDataSource2(att.getURL())))
        //
        var fileName: String = att.getFileName()
        if (!StringUtil.isAscii(fileName)) {
            try {
                fileName = MimeUtility.encodeText(fileName, "UTF-8", null)
            } catch (e: UnsupportedEncodingException) {
            } // that should never happen!
        }
        mbp.setFileName(fileName)
        if (!StringUtil.isEmpty(att.getType())) mbp.setHeader("Content-Type", att.getType())
        val disposition: String = att.getDisposition()
        if (!StringUtil.isEmpty(disposition)) {
            mbp.setDisposition(disposition)
            if (mp is MimeMultipart && MimePart.INLINE.equalsIgnoreCase(disposition)) {
                (mp as MimeMultipart?).setSubType("related")
            }
        }
        if (!StringUtil.isEmpty(att.getContentID())) mbp.setContentID("<" + att.getContentID().toString() + ">")
        return mbp
    }

    /**
     * @param file
     * @throws MessagingException
     * @throws FileNotFoundException
     */
    @Throws(MessagingException::class)
    fun addAttachment(file: Resource?) {
        addAttachment(file, null, null, null, null, false)
    }

    @Throws(MailException::class, ApplicationException::class)
    fun send(pc: PageContext?, sendTime: Long) {
        if (plainTextAsString == null && hTMLTextAsString == null) throw MailException("you must define plaintext or htmltext")
        val servers: Array<Server?> = (pc as PageContextImpl?).getMailServers()
        val config: ConfigWeb = pc.getConfig()
        if (ArrayUtil.isEmpty(servers) && ArrayUtil.isEmpty(host)) throw MailException("no SMTP Server defined")
        if (spool == SPOOL_YES || spool == SPOOL_UNDEFINED && config.isMailSpoolEnable()) {
            val mst = MailSpoolerTask(this, servers, sendTime)
            if (listener != null) mst.setListener(toListener(mst, listener))
            (config.getSpoolerEngine() as SpoolerEngineImpl).add(config, mst)
        } else _send(config, servers)
    }

    @Throws(MailException::class)
    fun _send(config: tachyon.runtime.config.ConfigWeb?, servers: Array<Server?>?) {
        var servers: Array<Server?>? = servers
        val start: Long = System.nanoTime()
        val _timeout = getTimeout(config)
        try {
            Proxy.start(proxyData)
            val log: Log = ThreadLocalPageContext.getLog(config, "mail")
            // Server
            // Server[] servers = config.getMailServers();
            if (host != null) {
                var prt: Int
                var usr: String?
                var pwd: String?
                val nServers: Array<ServerImpl?> = arrayOfNulls<ServerImpl?>(host!!.size)
                for (i in host.indices) {
                    usr = null
                    pwd = null
                    prt = Server.DEFAULT_PORT
                    if (port > 0) prt = port
                    if (!StringUtil.isEmpty(username)) {
                        usr = username
                        pwd = password
                    }
                    nServers[i] = toServerImpl(host!![i], prt, usr, pwd, lifeTimespan, idleTimespan)
                    if (ssl == SSL_YES) nServers[i].setSSL(true)
                    if (tls == TLS_YES) nServers[i].setTLS(true)
                }
                servers = nServers
            }
            if (servers!!.size == 0) {
                // return;
                throw MailException("no SMTP Server defined")
            }
            var _ssl: Boolean
            var _tls: Boolean
            for (i in servers.indices) {
                val server: Server? = servers[i]
                var _username: String? = null
                var _password = ""
                // int _port;

                // username/password
                if (server.hasAuthentication()) {
                    _username = server.getUsername()
                    _password = server.getPassword()
                }

                // tls
                _tls = if (tls != TLS_NONE) tls == TLS_YES else (server as ServerImpl?).isTLS()
                if (_tls) {
                    MailUtil.setSystemPropMailSslProtocols()
                }

                // ssl
                _ssl = if (ssl != SSL_NONE) ssl == SSL_YES else (server as ServerImpl?).isSSL()
                var msgSess: MimeMessageAndSession?
                val recyleConnection: Boolean = (server as ServerImpl?).reuseConnections()
                run {
                    // synchronized(LOCK) {
                    msgSess = try {
                        createMimeMessage(config, server.getHostName(), server.getPort(), _username, _password, (server as ServerImpl?).getLifeTimeSpan(),
                                (server as ServerImpl?).getIdleTimeSpan(), _tls, _ssl, (config as ConfigPro?).isMailSendPartial(), !recyleConnection, (config as ConfigPro?).isUserset())
                    } catch (e: MessagingException) {
                        // listener
                        listener(config, server, log, e, System.nanoTime() - start)
                        val me = MailException(e.getMessage())
                        me.setStackTrace(e.getStackTrace())
                        throw me
                    }
                    try {
                        val lock = SerializableObject()
                        val sender = SMTPSender(lock, msgSess, server.getHostName(), server.getPort(), _username, _password, recyleConnection)
                        sender.start()
                        SystemUtil.wait(lock, _timeout)
                        if (!sender!!.isSent()) {
                            val t: Throwable = sender!!.getThrowable()
                            if (t != null) throw Caster.toPageException(Exception(t))

                            // stop when still running
                            try {
                                if (sender.isAlive()) sender.stop()
                            } catch (t2: Throwable) {
                                ExceptionUtil.rethrowIfNecessary(t2)
                            }

                            // after thread is stopped check sent flag again
                            if (!sender!!.isSent()) {
                                throw MessagingException("timeout occurred after " + _timeout / 1000 + " seconds while sending mail message")
                            }
                        }
                        // could have an exception but was send anyway
                        if (sender!!.getThrowable() != null) {
                            val t: Throwable = Exception(sender!!.getThrowable())
                            if (log != null) log.log(Log.LEVEL_ERROR, "send mail", t)
                        }
                        clean(config, attachmentz)
                        listener(config, server, log, null, System.nanoTime() - start)
                        break
                    } catch (e: Exception) {
                        LogUtil.log(ThreadLocalPageContext.getConfig(config), SMTPClient::class.java.getName(), e)
                        if (i + 1 == servers.size) {
                            listener(config, server, log, e, System.nanoTime() - start)
                            val me = MailException(server.getHostName().toString() + " " + e.getMessage() + ":" + i)
                            me.initCause(e.getCause())
                            throw me
                        }
                    }
                }
            }
        } finally {
            Proxy.end()
        }
    }

    private fun listener(config: ConfigWeb?, server: Server?, log: Log?, e: Exception?, exe: Long) {
        if (e == null) log.info("mail", "mail sent (subject:" + subject + "; server:" + server.getHostName() + "; port:" + server.getPort() + "; from:" + toString(from) + "; to:"
                + toString(*tos!!) + "; cc:" + toString(*ccs!!) + "; bcc:" + toString(*bccs!!) + "; ft:" + toString(*fts!!) + "; rt:" + toString(*rts!!) + ")") else log.log(Log.LEVEL_ERROR, "mail", e)

        // listener
        val props: Map<String?, Object?> = HashMap<String?, Object?>()
        props.put("attachments", attachmentz)
        props.put("bccs", bccs)
        props.put("ccs", ccs)
        props.put("charset", charset)
        props.put("from", from)
        props.put("fts", fts)
        props.put("headers", headers)
        props.put("host", server.getHostName())
        props.put("htmlText", hTMLTextAsString)
        props.put("htmlTextCharset", htmlTextCharset)
        props.put("parts", parts)
        props.put("password", password)
        props.put("plainText", plainTextAsString)
        props.put("plainTextCharset", plainTextCharset)
        props.put("port", server.getPort())
        props.put("proxyData", proxyData)
        props.put("rts", rts)
        props.put("subject", subject)
        props.put("timeout", getTimeout(config))
        props.put("timezone", timeZone)
        props.put("tos", tos)
        props.put("username", username)
        props.put("xmailer", xmailer)
        (config as ConfigWebPro?).getActionMonitorCollector().log(config, "mail", "Mail", exe, props)
    }

    private fun getTimeout(config: Config?): Long {
        return if (timeout > 0) timeout.toLong() else config.getMailTimeout() * 1000L
    }

    @Throws(MessagingException::class)
    private fun getHTMLText(config: Config?): MimeBodyPart? {
        val html = MimeBodyPart()
        fillHTMLText(config, html)
        return html
    }

    /*
	 * Users can opt-in to the old Tachyon behavior of allowing HTML emails to be sent using 7bit
	 * encoding. When 7bit transfer encoding is used, content must be wrapped to less than 1,000
	 * characters per line.
	 * 
	 * The new default behavior for sending HTML emails is to use "quoted-printable" encoding, encodings
	 * non-ASCII characters and automatically wraps lines to 76 characters wide, but encodes word
	 * breaks. This allows for strings longer than 1000 characters to be included in the output and
	 * still have the output conform to the SMTP RFCs.
	 * 
	 * https://stackoverflow.com/questions/25710599/content-transfer-encoding-7bit-or-8-bit/28531705#
	 * 28531705
	 */
    private val isUse7bitHtmlEncoding: Boolean
        private get() = try {
            Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("tachyon.mail.use.7bit.transfer.encoding.for.html.parts", "false"))
        } catch (t: Throwable) {
            false
        }

    @Throws(MessagingException::class)
    private fun fillHTMLText(config: Config?, mp: MimePart?) {
        if (htmlTextCharset == null) htmlTextCharset = getMailDefaultCharset(config)
        val transferEncoding: String

        /*
		 * Set the "tachyon.mail.use.7bit.transfer.encoding.for.html.parts" system property to "false" to
		 * force the previous behavior of using 7bit transfer encoding.
		 */if (isUse7bitHtmlEncoding) {
            transferEncoding = "7bit"
            // when using 7bit, we must always wrap lines
            mp.setDataHandler(DataHandler(StringDataSource(hTMLTextAsString, TEXT_HTML, htmlTextCharset, 998)))
            /*
			 * The default behavior is to using "quoted-printable" for HTML emails. This will force wrapping of
			 * lines to 76 characters and encoded any non-ASCII characters.
			 * 
			 * ACF uses this encoded for all HTML parts.
			 */
        } else {
            transferEncoding = "quoted-printable"
            mp.setDataHandler(DataHandler(StringDataSource(hTMLTextAsString, TEXT_HTML, htmlTextCharset)))
        }

        // headers must always be set after data handler is set or the headers will be replaced
        mp.setHeader("Content-Transfer-Encoding", transferEncoding)
        mp.setHeader("Content-Type", TEXT_HTML.toString() + "; charset=" + htmlTextCharset)
    }

    @Throws(MessagingException::class)
    private fun getPlainText(config: Config?): MimeBodyPart? {
        val plain = MimeBodyPart()
        fillPlainText(config, plain)
        return plain
    }

    @Throws(MessagingException::class)
    private fun fillPlainText(config: Config?, mp: MimePart?) {
        if (plainTextCharset == null) plainTextCharset = getMailDefaultCharset(config)
        mp.setDataHandler(DataHandler(StringDataSource(if (plainTextAsString != null) plainTextAsString else "", TEXT_PLAIN, plainTextCharset, 998)))
        // headers must always be set after data handler is set or the headers will be replaced
        mp.setHeader("Content-Transfer-Encoding", "7bit")
        mp.setHeader("Content-Type", TEXT_PLAIN.toString() + "; charset=" + plainTextCharset)
    }

    @Throws(MessagingException::class)
    private fun toMimeBodyPart(config: Config?, part: MailPart?): BodyPart? {
        var cs: CharSet? = CharsetUtil.toCharSet(part.getCharset())
        if (cs == null) cs = getMailDefaultCharset(config)
        val mbp = MimeBodyPart()
        var partSource: StringDataSource? = null
        /*
		 * HTML parts are encoded as "quoted-printable", which is automatically wrapped to 76 characters per
		 * line, so we do not need to wrap these lines.
		 */if (part.getType() === "text/html" && !isUse7bitHtmlEncoding) {
            partSource = StringDataSource(part.getBody(), part.getType(), cs)
        } else {
            partSource = StringDataSource(part.getBody(), part.getType(), cs, 998)
        }
        mbp.setDataHandler(DataHandler(partSource))
        return mbp
    }

    private fun getMailDefaultCharset(config: Config?): CharSet? {
        var cs: Charset = ThreadLocalPageContext.getConfig(config).getMailDefaultCharset()
        if (cs == null) cs = CharsetUtil.UTF8
        return CharsetUtil.toCharSet(cs)
    }

    /**
     * @return the proxyData
     */
    fun getProxyData(): ProxyData? {
        return proxyData
    }

    /**
     * @param proxyData the proxyData to set
     */
    fun setProxyData(proxyData: ProxyData?) {
        this.proxyData = proxyData
    }

    /**
     * @param ssl the ssl to set
     */
    fun setSSL(ssl: Boolean) {
        this.ssl = if (ssl) SSL_YES else SSL_NO
    }

    /**
     * @param tls the tls to set
     */
    fun setTLS(tls: Boolean) {
        this.tls = if (tls) TLS_YES else TLS_NO
    }

    /**
     * @return the from
     */
    fun getFrom(): InternetAddress? {
        return from
    }

    /**
     * @return the tos
     */
    fun getTos(): Array<InternetAddress?>? {
        return tos
    }

    /**
     * @return the bccs
     */
    fun getBccs(): Array<InternetAddress?>? {
        return bccs
    }

    /**
     * @return the ccs
     */
    fun getCcs(): Array<InternetAddress?>? {
        return ccs
    }

    /**
     * @return the charset
     */
    fun getCharset(): String? {
        return charset.toString()
    }

    /**
     * @return the replyTo
     */
    var replyTos: Array<Any?>?
        get() = rts
        set(rts) {
            this.rts = rts
        }

    /**
     * @return the failTo
     */
    var failTos: Array<Any?>?
        get() = fts
        set(fts) {
            this.fts = fts
        }

    fun setPart(part: MailPart?) {
        if (parts == null) parts = ArrayList<MailPart?>()
        parts.add(part)
    }

    fun setTimeZone(timeZone: TimeZone?) {
        this.timeZone = timeZone
    }

    companion object {
        private const val serialVersionUID = 5227282806519740328L
        private const val SPOOL_UNDEFINED = 0
        private const val SPOOL_YES = 1
        private const val SPOOL_NO = 2
        private const val SSL_NONE = 0
        private const val SSL_YES = 1
        private const val SSL_NO = 2
        private const val TLS_NONE = 0
        private const val TLS_YES = 1
        private const val TLS_NO = 2
        private val TEXT_HTML: String? = "text/html"
        private val TEXT_PLAIN: String? = "text/plain"
        private val MESSAGE_ID: String? = "Message-ID"

        // private static final SerializableObject LOCK = new SerializableObject();
        private val formatters: Map<TimeZone?, SoftReference<SimpleDateFormat?>?>? = ConcurrentHashMap<TimeZone?, SoftReference<SimpleDateFormat?>?>()
        fun getNow(tz: TimeZone?): String? {
            var tz: TimeZone? = tz
            tz = ThreadLocalPageContext.getTimeZone(tz)
            val tmp: SoftReference<SimpleDateFormat?>? = formatters!![tz]
            var df: SimpleDateFormat? = if (tmp == null) null else tmp.get()
            if (df == null) {
                df = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", Locale.US)
                df.setTimeZone(tz)
                formatters.put(tz, SoftReference<SimpleDateFormat?>(df))
            }
            return df.format(Date())
        }

        @Throws(MailException::class)
        fun toServerImpl(server: String?, port: Int, usr: String?, pwd: String?, lifeTimespan: Long, idleTimespan: Long): ServerImpl? {
            var server = server
            var port = port
            var usr = usr
            var pwd = pwd
            var index: Int

            // username/password
            index = server.indexOf('@')
            if (index != -1) {
                usr = server.substring(0, index)
                server = server.substring(index + 1)
                index = usr.indexOf(':')
                if (index != -1) {
                    pwd = usr.substring(index + 1)
                    usr = usr.substring(0, index)
                }
            }

            // port
            index = server.indexOf(':')
            if (index != -1) {
                port = try {
                    Caster.toIntValue(server.substring(index + 1))
                } catch (e: ExpressionException) {
                    throw MailException(e.getMessage())
                }
                server = server.substring(0, index)
            }
            return ServerImpl.getInstance(server, port, usr, pwd, lifeTimespan, idleTimespan, false, false)
        }

        /**
         * creates a new expanded array and return it;
         *
         * @param oldArr
         * @param newValue
         * @return new expanded array
         */
        protected fun add(oldArr: Array<InternetAddress?>?, newValue: InternetAddress?): Array<InternetAddress?>? {
            if (oldArr == null) return arrayOf<InternetAddress?>(newValue)
            // else {
            val tmp: Array<InternetAddress?> = arrayOfNulls<InternetAddress?>(oldArr.size + 1)
            for (i in oldArr.indices) {
                tmp[i] = oldArr[i]
            }
            tmp[oldArr.size] = newValue
            return tmp
            // }
        }

        protected fun add(oldArr: Array<Attachment?>?, newValue: Attachment?): Array<Attachment?>? {
            if (oldArr == null) return arrayOf<Attachment?>(newValue)
            // else {
            val tmp: Array<Attachment?> = arrayOfNulls<Attachment?>(oldArr.size + 1)
            for (i in oldArr.indices) {
                tmp[i] = oldArr[i]
            }
            tmp[oldArr.size] = newValue
            return tmp
            // }
        }

        /*
	 * private static void addMailcaps(ClassLoader cl) { try { Class<?> cCM =
	 * cl.loadClass("javax.activation.CommandMap"); Method getDefaultCommandMap =
	 * cCM.getMethod("getDefaultCommandMap", CLASS_EMPTY); Object oMCM =
	 * getDefaultCommandMap.invoke(null, OBJECT_EMPTY);
	 * 
	 * Method getMimeTypes = oMCM.getClass().getMethod("getMimeTypes", CLASS_EMPTY);
	 * 
	 * Method addMailcap = oMCM.getClass().getMethod("addMailcap", CLASS_STRING); addMailcap(oMCM,
	 * addMailcap,"text/plain;;		x-java-content-handler=com.sun.mail.handlers.text_plain");
	 * addMailcap(oMCM,
	 * addMailcap,"text/html;;		x-java-content-handler=com.sun.mail.handlers.text_html");
	 * addMailcap(oMCM,
	 * addMailcap,"text/xml;;		x-java-content-handler=com.sun.mail.handlers.text_xml");
	 * addMailcap(oMCM,
	 * addMailcap,"multipart/ *;;		x-java-content-handler=com.sun.mail.handlers.multipart_mixed; x-java-fallback-entry=true"
	 * ); addMailcap(oMCM,
	 * addMailcap,"message/rfc822;;	x-java-content-handler=com.sun.mail.handlers.message_rfc822"); }
	 * catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);} } private static void addMailcap(Object
	 * oMCM, Method addMailcap, String value) throws IllegalAccessException, IllegalArgumentException,
	 * InvocationTargetException { addMailcap.invoke(oMCM, new Object[]{value}); }
	 */
        private fun hash(props: Properties?): String? {
            val e: Enumeration<*> = props.propertyNames()
            val names: MutableList<String?> = ArrayList<String?>()
            var str: String
            while (e.hasMoreElements()) {
                str = Caster.toString(e.nextElement(), null)
                if (!StringUtil.isEmpty(str) && str.startsWith("mail.smtp.")) names.add(str)
            }
            Collections.sort(names)
            val sb = StringBuilder()
            val it: Iterator<String?> = names.iterator()
            while (it.hasNext()) {
                str = it.next()
                sb.append(str).append(':').append(props.getProperty(str)).append(';')
            }
            str = sb.toString()
            return MD5.getDigestAsString(str, str)
        }

        @Throws(MessagingException::class)
        private fun setHeaders(msg: SMTPMessage?, headers: Map<String?, String?>?) {
            val it: Iterator<Entry<String?, String?>?> = headers.entrySet().iterator()
            var e: Entry<String?, String?>?
            while (it.hasNext()) {
                e = it.next()
                msg.setHeader(e.getKey(), e.getValue())
            }
        }

        private fun getMessageId(headers: Map<String?, String?>?): String? {
            val it: Iterator<Entry<String?, String?>?> = headers.entrySet().iterator()
            var e: Entry<String?, String?>?
            while (it.hasNext()) {
                e = it.next()
                if (e.getKey().equals(MESSAGE_ID)) return e.getValue()
            }
            return null
        }

        @Throws(ApplicationException::class)
        fun toListener(st: SpoolerTask?, listener: Object?): SpoolerTaskListener? {
            if (listener is Component) return ComponentSpoolerTaskListener(SystemUtil.getCurrentContext(null), st, listener as Component?)
            if (listener is UDF) return UDFSpoolerTaskListener(SystemUtil.getCurrentContext(null), st, null, listener as UDF?)
            if (listener is Struct) {
                val before: UDF = Caster.toFunction((listener as Struct?).get("before", null), null)
                val after: UDF = Caster.toFunction((listener as Struct?).get("after", null), null)
                return UDFSpoolerTaskListener(SystemUtil.getCurrentContext(null), st, before, after)
            }
            throw ApplicationException("cannot convert [" + Caster.toTypeName(listener).toString() + "] to a listener")
        }

        private fun toString(vararg ias: InternetAddress?): String? {
            if (ArrayUtil.isEmpty(ias)) return ""
            val sb = StringBuilder()
            for (i in 0 until ias.size) {
                if (sb.length() > 0) sb.append(", ")
                sb.append(ias[i].toString())
            }
            return sb.toString()
        }

        // remove any attachments that are marked to remove after sending
        private fun clean(config: Config?, attachmentz: Array<Attachment?>?) {
            if (attachmentz != null) for (i in attachmentz.indices) {
                if (attachmentz[i]!!.isRemoveAfterSend()) {
                    val res: Resource = config.getResource(attachmentz[i].getAbsolutePath())
                    ResourceUtil.removeEL(res, true)
                }
            }
        }
    }
}