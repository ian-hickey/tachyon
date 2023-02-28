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
package lucee.runtime.type.scope

import java.io.UnsupportedEncodingException

/**
 * Implements URL Scope
 */
class URLImpl
/**
 * Standart Constructor
 */
    : ScopeSupport("url", SCOPE_URL, Struct.TYPE_LINKED), URL, ScriptProtected {
    private var encoding: String? = null
    private var scriptProtected: Int = ScriptProtected.UNDEFINED
    private var raw: Array<URLItem?>? = empty
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
        if (isInitalized()) fillDecoded(raw, encoding, isScriptProtected(), ac.getSameFieldAsArray(SCOPE_URL))
    }

    @Override
    override fun initialize(pc: PageContext?) {
        if (encoding == null) encoding = pc.getWebCharset().name()
        if (scriptProtected == ScriptProtected.UNDEFINED) {
            scriptProtected = if (pc.getApplicationContext().getScriptProtect() and ApplicationContext.SCRIPT_PROTECT_URL > 0) ScriptProtected.YES else ScriptProtected.NO
        }
        try {
            super.initialize(pc)
            raw = setFromQueryString(ReqRspUtil.getQueryString(pc.getHttpServletRequest()))
            fillDecoded(raw, encoding, isScriptProtected(), pc.getApplicationContext().getSameFieldAsArray(SCOPE_URL))
            if (raw!!.size > 0 && pc.getConfig().isAllowURLRequestTimeout()) {
                val o: Object = get(REQUEST_TIMEOUT, null)
                if (o != null) {
                    val timeout: Long = Caster.toLongValue(o, -1)
                    if (timeout != -1L) pc.setRequestTimeout(timeout * 1000)
                }
                Caster.toDoubleValue(o, false, -1)
            }
        } catch (e: Exception) {
        }
    }

    @Override
    fun reinitialize(ac: ApplicationContext?) {
        if (isInitalized()) {
            if (scriptProtected == ScriptProtected.UNDEFINED) {
                scriptProtected = if (ac.getScriptProtect() and ApplicationContext.SCRIPT_PROTECT_URL > 0) ScriptProtected.YES else ScriptProtected.NO
            }
            fillDecodedEL(raw, encoding, isScriptProtected(), ac.getSameFieldAsArray(SCOPE_URL))
        }
    }

    @Override
    override fun release(pc: PageContext?) {
        encoding = null
        raw = empty
        scriptProtected = ScriptProtected.UNDEFINED
        super.release(pc)
    }

    @Override
    override fun setScriptProtecting(ac: ApplicationContext?, scriptProtected: Boolean) {
        val _scriptProtected: Int = if (scriptProtected) ScriptProtected.YES else ScriptProtected.NO
        // print.out(isInitalized()+"x"+(_scriptProtected+"!="+this.scriptProtected));
        if (isInitalized() && _scriptProtected != this.scriptProtected) {
            fillDecodedEL(raw, encoding, scriptProtected, ac.getSameFieldAsArray(SCOPE_URL))
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

    companion object {
        private val empty: Array<URLItem?>? = arrayOfNulls<URLItem?>(0)
        private val REQUEST_TIMEOUT: Collection.Key? = KeyImpl.getInstance("RequestTimeout")
    }
}