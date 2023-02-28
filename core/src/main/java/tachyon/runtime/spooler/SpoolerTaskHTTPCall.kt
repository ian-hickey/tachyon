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
package tachyon.runtime.spooler

import java.io.IOException

abstract class SpoolerTaskHTTPCall(plans: Array<ExecutionPlan?>?, client: RemoteClient?) : SpoolerTaskSupport(plans) {
    private val client: RemoteClient?

    /**
     * @return
     * @see tachyon.runtime.spooler.SpoolerTask.execute
     */
    @Override
    @Throws(PageException::class)
    fun execute(config: Config?): Object? {
        return execute(client, config, getMethodName(), getArguments())
    }

    /**
     * @see tachyon.runtime.spooler.SpoolerTask.subject
     */
    @Override
    fun subject(): String? {
        return client.getLabel()
    }

    /**
     * @see tachyon.runtime.spooler.SpoolerTask.detail
     */
    @Override
    fun detail(): Struct? {
        val sct: Struct = StructImpl()
        sct.setEL(KeyConstants._label, client.getLabel())
        sct.setEL(KeyConstants._url, client.getUrl())
        return sct
    }

    protected abstract fun getMethodName(): String?
    protected abstract fun getArguments(): Struct?

    companion object {
        private const val serialVersionUID = -1994776413696459993L
        @Throws(PageException::class)
        fun execute(client: RemoteClient?, config: Config?, methodName: String?, args: Struct?): Object? {
            // return rpc.callWithNamedValues(config, getMethodName(), getArguments());
            val pc: PageContext = ThreadLocalPageContext.get()

            // remove wsdl if necessary
            var url: String? = client.getUrl()
            if (StringUtil.endsWithIgnoreCase(url, "?wsdl")) url = url.substring(0, url!!.length() - 5)

            // Params
            val params: Map<String?, String?> = HashMap<String?, String?>()
            params.put("method", methodName)
            params.put("returnFormat", "json")
            return try {
                val cs: Charset = pc.getWebCharset()
                params.put("argumentCollection", JSONConverter(true, cs).serialize(pc, args, SerializationSettings.SERIALIZE_AS_ROW))
                val res: HTTPResponse = HTTPEngine4Impl.post(HTTPUtil.toURL(url, HTTPUtil.ENCODED_AUTO), client.getServerUsername(), client.getServerPassword(), -1L, true,
                        pc.getWebCharset().name(), Constants.NAME.toString() + " Remote Invocation", client.getProxyData(), null, params)
                JSONExpressionInterpreter().interpret(pc, res.getContentAsString())
            } catch (ioe: IOException) {
                throw Caster.toPageException(ioe)
            } catch (ce: ConverterException) {
                throw Caster.toPageException(ce)
            }
        }
    }

    init {
        this.client = client
    }
}