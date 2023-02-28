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
package tachyon.runtime.spooler.mail

import java.io.UnsupportedEncodingException

class MailSpoolerTask private constructor(plans: Array<ExecutionPlan?>?, client: SMTPClient?, servers: Array<Server?>?, sendTime: Long) : SpoolerTaskSupport(plans, sendTime) {
    private val client: SMTPClient?
    private val servers: Array<Server?>?
    private var listener: SpoolerTaskListener? = null

    constructor(client: SMTPClient?, servers: Array<Server?>?, sendTime: Long) : this(EXECUTION_PLANS, client, servers, sendTime) {}

    @Override
    fun getType(): String? {
        return "mail"
    }

    @Override
    fun subject(): String? {
        return client.getSubject()
    }

    @Override
    fun detail(): Struct? {
        val sct = StructImpl()
        sct.setEL("subject", client.getSubject())
        if (client.hasHTMLText()) sct.setEL("body", StringUtil.max(client.getHTMLTextAsString(), 1024, "...")) else if (client.hasPlainText()) sct.setEL("body", StringUtil.max(client.getPlainTextAsString(), 1024, "..."))
        sct.setEL("from", toString(client.getFrom()))
        var adresses: Array<InternetAddress?> = client.getTos()
        sct.setEL("to", toString(adresses))
        adresses = client.getCcs()
        if (!ArrayUtil.isEmpty(adresses)) sct.setEL("cc", toString(adresses))
        adresses = client.getBccs()
        if (!ArrayUtil.isEmpty(adresses)) sct.setEL("bcc", toString(adresses))
        return sct
    }

    fun getCharset(): String? {
        return client.getCharset()
    }

    fun getReplyTos(): String? {
        return toString(client.getReplyTos())
    }

    fun getFailTos(): String? {
        return toString(client.getFailTos())
    }

    @Override
    @Throws(PageException::class)
    fun execute(config: Config?): Object? {
        try {
            client._send(config as ConfigWeb?, servers)
        } catch (e: MailException) {
            throw Caster.toPageException(e)
        }
        return null
    }

    @Override
    fun getListener(): SpoolerTaskListener? {
        return listener
    }

    fun setListener(listener: SpoolerTaskListener?) {
        this.listener = listener
    }

    @Throws(UnsupportedEncodingException::class, PageException::class, MailException::class)
    operator fun mod(sct: Struct?) {

        // charset
        val str: String = Caster.toString(sct.get(KeyConstants._charset, null), null)
        if (str != null) {
            val cs: CharSet = CharsetUtil.toCharSet(str, null)
            if (cs != null) client.setCharSet(cs)
        }

        // FROM
        var o: Object = sct.get(KeyConstants._from, null)
        if (o != null) client.setFrom(MailUtil.toInternetAddress(o))

        // TO
        o = sct.get(KeyConstants._to, null)
        if (o != null) client.setTos(MailUtil.toInternetAddresses(o))

        // CC
        o = sct.get(CC, null)
        if (o != null) client.setCCs(MailUtil.toInternetAddresses(o))

        // BCC
        o = sct.get(BCC, null)
        if (o != null) client.setBCCs(MailUtil.toInternetAddresses(o))

        // failto
        o = sct.get(FAILTO, null)
        if (o != null) client.setFailTos(MailUtil.toInternetAddresses(o))

        // replyto
        o = sct.get(REPLYTO, null)
        if (o != null) client.setReplyTos(MailUtil.toInternetAddresses(o))

        // subject
        o = sct.get(KeyConstants._subject, null)
        if (o != null) client.setSubject(StringUtil.collapseWhitespace(Caster.toString(o)))
    }

    companion object {
        private val EXECUTION_PLANS: Array<ExecutionPlan?>? = arrayOf<ExecutionPlan?>(ExecutionPlanImpl(1, 60), ExecutionPlanImpl(1, 5 * 60), ExecutionPlanImpl(1, 3600),
                ExecutionPlanImpl(2, 24 * 3600))
        private val CC: Key? = KeyImpl.init("cc")
        private val BCC: Key? = KeyImpl.init("bcc")
        private val FAILTO: Key? = KeyImpl.init("failto")
        private val REPLYTO: Key? = KeyImpl.init("replyto")
        private fun toString(adresses: Array<InternetAddress?>?): String? {
            if (adresses == null) return ""
            val sb = StringBuffer()
            for (i in adresses.indices) {
                if (i > 0) sb.append(", ")
                sb.append(toString(adresses[i]))
            }
            return sb.toString()
        }

        private fun toString(address: InternetAddress?): String? {
            if (address == null) return ""
            val addr: String = address.getAddress()
            val per: String = address.getPersonal()
            if (StringUtil.isEmpty(per)) return addr
            return if (StringUtil.isEmpty(addr)) per else "$per ($addr)"
        }
    }

    init {
        this.client = client
        this.servers = servers
    }
}