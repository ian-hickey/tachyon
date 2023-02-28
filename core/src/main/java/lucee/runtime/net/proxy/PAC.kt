package lucee.runtime.net.proxy

import kotlin.Throws
import kotlin.jvm.Synchronized
import lucee.commons.io.SystemUtil.Caller
import kotlin.jvm.Transient
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Volatile
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntrySet
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntryIterator
import lucee.commons.collection.LongKeyList.Pair
import lucee.commons.collection.AbstractCollection
import lucee.runtime.type.Array
import java.sql.Array
import lucee.commons.lang.Pair
import lucee.runtime.exp.CatchBlockImpl.Pair
import lucee.runtime.type.util.ListIteratorImpl
import lucee.runtime.type.Lambda
import java.util.Random
import lucee.runtime.config.Constants
import lucee.runtime.engine.Request
import lucee.runtime.engine.ExecutionLogSupport.Pair
import lucee.runtime.functions.other.NullValue
import lucee.runtime.functions.string.Val
import lucee.runtime.reflection.Reflector.JavaAnnotation
import lucee.transformer.cfml.evaluator.impl.Output
import lucee.transformer.cfml.evaluator.impl.Property
import lucee.transformer.bytecode.statement.Condition.Pair

// Proxy Auto Config
object PAC {
    private val str: String? = """function FindProxyForURL(url, host)

    {

        if (shExpMatch(host, "192.168.*") ||

            shExpMatch(host, "127.*")     ||

            shExpMatch(host, "172.16.*")  ||

            shExpMatch(host, "172.17.*")  ||

            shExpMatch(host, "172.18.*")  ||

            shExpMatch(host, "172.19.*")  ||

            shExpMatch(host, "172.20.*")  ||

            shExpMatch(host, "172.21.*")  ||

            shExpMatch(host, "172.22.*")  ||

            shExpMatch(host, "172.23.*")  ||

            shExpMatch(host, "172.24.*")  ||

            shExpMatch(host, "172.25.*")  ||

            shExpMatch(host, "172.26.*")  ||

            shExpMatch(host, "172.27.*")  ||

            shExpMatch(host, "172.28.*")  ||

            shExpMatch(host, "172.29.*")  ||

            shExpMatch(host, "172.30.*")  ||

            shExpMatch(host, "172.31.*")  ||

            shExpMatch(host, "10.*")      ||

            shExpMatch(host, "*.ads.hel.kko.ch")         ||

            shExpMatch(host, "*.hel.kko.ch")             ||

            shExpMatch(host, "*.ovan.ch")                ||

            shExpMatch(host, "*.ncag.helsana.ch")        ||

            shExpMatch(host, "helsana.ncag.ch")          ||

            shExpMatch(host, "helsanapod.ncag.ch")       ||

            shExpMatch(host, "printform.ncag.ch")        ||

            shExpMatch(host, "k4webportal.ncag.ch")              ||

            shExpMatch(host, "k4webportal.mycontent.ch")         ||

            shExpMatch(host, "printformtest.ncag.ch")            ||

            shExpMatch(host, "helsanapod-demo.ncag.ch")          ||

            shExpMatch(host, "*.seczone.centrisag.ch")           ||

            dnsDomainIs(host, ".kko.ch")                         ||

            isPlainHostName(host) )                      {

            return "DIRECT";  }

        else if (shExpMatch(host, "*helsana-test.ch")            ||

                 shExpMatch(host, "*helsana-entwicklung.ch")     ||

                 shExpMatch(host, "*helsana-integration.ch")     ||

                 shExpMatch(host, "*helsana-preprod02.ch")       ||

                 shExpMatch(host, "*helsana-preprod.ch") )   {

                 return "PROXY Client-Proxy-PreProd.hel.kko.ch:8080"; }

        else { return "PROXY Client-Proxy.hel.kko.ch:8080"; }

    }"""
}