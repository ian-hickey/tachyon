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
package lucee.runtime.net.proxy

import java.io.Serializable

class ProxyDataImpl : ProxyData, Serializable {
    companion object {
        val NO_PROXY: ProxyData? = ProxyDataImpl()
        val LOCALS: Set<String?>? = HashSet<String?>()
        fun isValid(pd: ProxyData?): Boolean {
            return if (pd == null || pd.equals(NO_PROXY) || StringUtil.isEmpty(pd.getServer(), true)) false else true
        }

        /**
         * check if the proxy is valid for the given host
         */
        fun isValid(pd: ProxyData?, host: String?): Boolean {
            return if (pd == null || pd.equals(NO_PROXY) || StringUtil.isEmpty(pd.getServer(), true)) false else isProxyEnableFor(pd, host)
        }

        /**
         * returns the given proxy in case it is valid for the given host, if not null is returned
         */
        fun validate(pd: ProxyData?, host: String?): ProxyData? {
            return if (isValid(pd, host)) pd else null
        }

        fun isProxyEnableFor(pd: ProxyData?, host: String?): Boolean {
            var host = host
            if (pd == null) return false
            val pdi = pd as ProxyDataImpl?
            if (StringUtil.isEmpty(host)) return true
            host = host.trim().toLowerCase()
            var doesInclude = false

            // if we have includes it needs to be part of it
            if (pdi!!.includes != null && !pdi.includes!!.isEmpty()) {
                if (!pdi.includes!!.contains(host)) return false
                doesInclude = true
            }
            if (!doesInclude && LOCALS!!.contains(host)) return false

            // if we have excludes it should NOT be part of it
            if (pdi.excludes != null && !pdi.excludes!!.isEmpty()) {
                if (pdi.excludes!!.contains(host)) return false
            }
            return true
        }

        fun hasCredentials(data: ProxyData?): Boolean {
            return StringUtil.isEmpty(data.getUsername(), true)
        }

        fun getInstance(proxyserver: String?, proxyport: Int, proxyuser: String?, proxypassword: String?): ProxyData? {
            return if (StringUtil.isEmpty(proxyserver, true)) null else ProxyDataImpl(proxyserver, proxyport, proxyuser, proxypassword)
        }

        fun toProxyData(sct: Struct?): ProxyData? {
            var pd: ProxyDataImpl? = null
            if (sct != null) {
                val srv: String = Caster.toString(sct.get(KeyConstants._server, null), null)
                val port: Integer = Caster.toInteger(sct.get(KeyConstants._port, null), null)
                val usr: String = Caster.toString(sct.get(KeyConstants._username, null), null)
                val pwd: String = Caster.toString(sct.get(KeyConstants._password, null), null)
                if (!StringUtil.isEmpty(srv, true)) {
                    pd = ProxyDataImpl()
                    pd.server = srv.trim()
                    if (port != null) pd.port = port
                    if (!StringUtil.isEmpty(usr, true)) {
                        pd.username = usr
                        pd.password = pwd ?: ""
                    }

                    // includes/excludes
                    pd.excludes = toStringSet(sct.get("excludes", null))
                    pd.includes = toStringSet(sct.get("includes", null))
                }
            }
            return pd
        }

        /*
	 * public static String[] toStringArray(Object obj) { String[] rtn = null; if
	 * (Decision.isArray(obj)) { Array arr = Caster.toArray(obj, null); if (arr != null) { rtn =
	 * ListUtil.trim(ListUtil.trimItems(ListUtil.toStringArray(arr, null))); }
	 * 
	 * } else { String list = Caster.toString(obj, null); if (!StringUtil.isEmpty(list, true)) { rtn =
	 * ListUtil.trim(ListUtil.trimItems(ListUtil.listToStringArray(list, ","))); } } return rtn; }
	 */
        fun toStringSet(obj: Object?): Set<String?>? {
            var rtn: Set<String?>? = null
            var arr: Array? = null
            if (Decision.isArray(obj)) {
                arr = Caster.toArray(obj, null)
            } else {
                val list: String = Caster.toString(obj, null)
                if (!StringUtil.isEmpty(list, true)) {
                    arr = ListUtil.listToArray(list, ',')
                }
            }
            if (arr != null) {
                rtn = HashSet<String?>()
                val it: Iterator<*> = arr.getIterator()
                var str: String
                while (it.hasNext()) {
                    str = Caster.toString(it.next(), null)
                    if (!StringUtil.isEmpty(str, true)) rtn.add(str.trim().toLowerCase())
                }
                if (rtn!!.isEmpty()) rtn = null
            }
            return rtn
        } /*
	 * public static void main(String[] args) {
	 * 
	 * // ProxyDataImpl pd = new ProxyDataImpl(); pd.setServer("213.109.7.135"); pd.setPort(59918); //
	 * // pd.setIncludes(new String[] { "localhostx" }); pd.setExcludes(new String[] { "localhost" });
	 * // print.e(isValid(pd, "localhost"));
	 * 
	 * Struct sct = new StructImpl(); sct.setEL("server", "213.109.7.135"); sct.setEL("port", "59918");
	 * // sct.setEL("excludes", "localhostd"); sct.setEL("includes",
	 * "localhost,snapshot.lucee.org,lucee.org");
	 * 
	 * ProxyDataImpl pd = (ProxyDataImpl) ProxyDataImpl.toProxyData(sct);
	 * 
	 * print.e(pd.getExcludes()); print.e(pd.getIncludes()); print.e(isValid(pd, "localhost")); }
	 */

        init {
            LOCALS.add("localhost")
            LOCALS.add("127.0.0.1")
            LOCALS.add("0:0:0:0:0:0:0:1")
        }
    }
    /**
     * @return the server
     */
    /**
     * @param server the server to set
     */
    @get:Override
    @set:Override
    var server: String? = null
    /**
     * @return the port
     */
    /**
     * @param port the port to set
     */
    @get:Override
    @set:Override
    var port = -1
    /**
     * @return the username
     */
    /**
     * @param username the username to set
     */
    @get:Override
    @set:Override
    var username: String? = null
    /**
     * @return the password
     */
    /**
     * @param password the password to set
     */
    @get:Override
    @set:Override
    var password: String? = null
    var excludes: Set<String?>? = null
    var includes: Set<String?>? = null
    private val includeLocals = false

    constructor(server: String?, port: Int, username: String?, password: String?) {
        if (!StringUtil.isEmpty(server, true)) this.server = server
        if (port > 0) this.port = port
        if (!StringUtil.isEmpty(username, true)) this.username = username
        if (!StringUtil.isEmpty(password, true)) this.password = password
    }

    constructor() {}

    @Override
    fun release() {
        server = null
        port = -1
        username = null
        password = null
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        if (obj === this) return true
        if (obj !is ProxyData) return false
        val other: ProxyData? = obj as ProxyData?
        return _eq(other.getServer(), server) && _eq(other.getUsername(), username) && _eq(other.getPassword(), password) && other.getPort() === port
    }

    private fun _eq(left: String?, right: String?): Boolean {
        return left?.equals(right) ?: (right == null)
    }

    @Override
    override fun toString(): String {
        return "server:$server;port:$port;user:$username;pass:$password"
    }
}