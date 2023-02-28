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
package tachyon.runtime.net.proxy

import java.util.Properties

object Proxy {
    // private static Map map=new HashTable();
    private val kl: KeyLock? = KeyLock()
    fun start(proxyData: ProxyData?) {
        start(proxyData.getServer(), proxyData.getPort(), proxyData.getUsername(), proxyData.getPassword())
    }

    fun start(server: String?, port: Int, user: String?, password: String?) {
        val key: String = StringUtil.toString(server, "").toString() + ":" + StringUtil.toString(port.toString() + "", "") + ":" + StringUtil.toString(user, "") + ":" + StringUtil.toString(password, "")
        kl.setListener(ProxyListener(server, port, user, password))
        kl.start(key)
    }

    fun end() {
        kl.end()
    }
}

internal class ProxyListener(private val server: String?, private val port: Int, private val user: String?, private var password: String?) : KeyLockListener {
    @Override
    fun onStart(key: String?, isFirst: Boolean) {
        // print.ln(" start:"+key+" _ "+isFirst);
        if (!isFirst) return
        val props: Properties = System.getProperties()
        if (!StringUtil.isEmpty(server)) {
            // Server
            props.setProperty("socksProxyHost", server)
            props.setProperty("http.proxyHost", server)
            props.setProperty("https.proxyHost", server)
            props.setProperty("ftp.proxyHost", server)
            props.setProperty("smtp.proxyHost", server)

            // Port
            if (port > 0) {
                val strPort: String = String.valueOf(port)
                props.setProperty("socksProxyPort", strPort)
                props.setProperty("http.proxyPort", strPort)
                props.setProperty("https.proxyPort", strPort)
                props.setProperty("ftp.proxyPort", strPort)
                props.setProperty("smtp.proxyPort", strPort)
            } else removePort(props)
            if (!StringUtil.isEmpty(user)) {
                props.setProperty("socksProxyUser", user)
                props.setProperty("java.net.socks.username", user)
                props.setProperty("http.proxyUser", user)
                props.setProperty("https.proxyUser", user)
                props.setProperty("ftp.proxyUser", user)
                props.setProperty("smtp.proxyUser", user)
                if (password == null) password = ""
                props.setProperty("socksProxyPassword", user)
                props.setProperty("java.net.socks.password", user)
                props.setProperty("http.proxyPassword", user)
                props.setProperty("https.proxyPassword", user)
                props.setProperty("ftp.proxyPassword", user)
                props.setProperty("smtp.proxyPassword", user)
            } else removeUserPass(props)
        } else {
            removeAll(props)
        }
    }

    @Override
    fun onEnd(key: String?, isLast: Boolean) {
        // print.ln(" end:"+key+key+" _ "+isLast);
        if (!isLast) return
        removeAll(System.getProperties())
    }

    private fun removeAll(props: Properties?) {
        removeHost(props)
        removePort(props)
        removeUserPass(props)
    }

    private fun removeHost(props: Properties?) {
        remove(props, "socksProxyHost")
        remove(props, "http.proxyHost")
        remove(props, "https.proxyHost")
        remove(props, "ftp.proxyHost")
        remove(props, "smtp.proxyHost")
    }

    private fun removePort(props: Properties?) {
        remove(props, "socksProxyPort")
        remove(props, "http.proxyPort")
        remove(props, "https.proxyPort")
        remove(props, "ftp.proxyPort")
        remove(props, "smtp.proxyPort")
    }

    private fun removeUserPass(props: Properties?) {
        remove(props, "socksProxyUser")
        remove(props, "socksProxyPassword")
        remove(props, "java.net.socks.username")
        remove(props, "java.net.socks.password")
        remove(props, "http.proxyUser")
        remove(props, "http.proxyPassword")
        remove(props, "https.proxyUser")
        remove(props, "https.proxyPassword")
        remove(props, "ftp.proxyUser")
        remove(props, "ftp.proxyPassword")
        remove(props, "smtp.proxyUser")
        remove(props, "smtp.proxyPassword")
    }

    companion object {
        private fun remove(props: Properties?, key: String?) {
            if (props.containsKey(key)) props.remove(key)
        }
    }
}