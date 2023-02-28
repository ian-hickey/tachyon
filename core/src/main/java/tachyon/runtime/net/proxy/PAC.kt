package tachyon.runtime.net.proxy

import kotlin.Throws
import kotlin.jvm.Synchronized
import tachyon.commons.io.SystemUtil.Caller
import kotlin.jvm.Transient
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Volatile
import tachyon.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntrySet
import tachyon.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntryIterator
import tachyon.commons.collection.LongKeyList.Pair
import tachyon.commons.collection.AbstractCollection
import tachyon.runtime.type.Array
import java.sql.Array
import tachyon.commons.lang.Pair
import tachyon.runtime.exp.CatchBlockImpl.Pair
import tachyon.runtime.type.util.ListIteratorImpl
import tachyon.runtime.type.Lambda
import java.util.Random
import tachyon.runtime.config.Constants
import tachyon.runtime.engine.Request
import tachyon.runtime.engine.ExecutionLogSupport.Pair
import tachyon.runtime.functions.other.NullValue
import tachyon.runtime.functions.string.Val
import tachyon.runtime.reflection.Reflector.JavaAnnotation
import tachyon.transformer.cfml.evaluator.impl.Output
import tachyon.transformer.cfml.evaluator.impl.Property
import tachyon.transformer.bytecode.statement.Condition.Pair

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