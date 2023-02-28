/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.loader.osgi

import java.io.File

object BundleUtil {
    /*
	 * public static Bundle addBundlex(BundleContext context,File bundle, boolean start) throws
	 * IOException, BundleException { return addBundle(context,bundle.getAbsolutePath(),bundle,start); }
	 */
    @Throws(IOException::class, BundleException::class)
    fun addBundle(factory: CFMLEngineFactory?, context: BundleContext, bundle: File, log: Log?): Bundle {
        return addBundle(factory, context, bundle.getAbsolutePath(), FileInputStream(bundle), true, log)
    }

    @Throws(IOException::class, BundleException::class)
    fun addBundle(factory: CFMLEngineFactory?, context: BundleContext, bundle: Resource, log: Log?): Bundle {
        return addBundle(factory, context, bundle.getAbsolutePath(), bundle.getInputStream(), true, log)
    }

    @Throws(IOException::class, BundleException::class)
    fun addBundle(factory: CFMLEngineFactory?, context: BundleContext, path: String, `is`: InputStream?, closeIS: Boolean, log: Log?): Bundle {

        // if possible use that feature from core, it is smarter (can also load relations)
        /*
		 * we no longer use this code, because it cause problem when the core is restarted ClassUtil cu =
		 * null; try { cu = CFMLEngineFactory.getInstance().getClassUtil(); } catch (final Throwable t) {}
		 * if (cu != null) return cu.addBundle(context, is, closeIS, true);
		 */
        if (log != null) log.debug("OSGI", "add bundle:$path") else {
            // factory.log(Log.LEVEL_INFO, "add_bundle:" + bundle);
        }
        return try {
            installBundle(context, path, `is`)
        } finally {
            if (closeIS) CFMLEngineFactorySupport.closeEL(`is`)
        }
    }

    @Throws(BundleException::class)
    fun installBundle(context: BundleContext, path: String?, `is`: InputStream?): Bundle {
        return context.installBundle(path, `is`)
    }

    @Throws(BundleException::class)
    fun start(factory: CFMLEngineFactory, bundles: List<Bundle>?) {
        if (bundles == null || bundles.isEmpty()) return
        val it: Iterator<Bundle> = bundles.iterator()
        while (it.hasNext()) start(factory, it.next())
    }

    @Throws(BundleException::class)
    fun start(factory: CFMLEngineFactory, bundle: Bundle) {

        /*
		 * we no longer use this code, because it cause problem when the core is restarted ClassUtil cu =
		 * null; try { cu = CFMLEngineFactory.getInstance().getClassUtil(); } catch (final Throwable t) { }
		 * if (cu != null) { cu.start(bundle); return; }
		 */
        val fh: String = bundle.getHeaders().get("Fragment-Host")
        if (!Util.isEmpty(fh)) {
            factory.log(Logger.LOG_INFO, "do not start [" + bundle.getSymbolicName().toString() + "], because this is a fragment bundle for [" + fh + "]")
            return
        }
        factory.log(Logger.LOG_INFO, "start bundle:" + bundle.getSymbolicName().toString() + ":" + bundle.getVersion().toString())
        start(bundle, false)
    }

    @Deprecated
    @Throws(BundleException::class)
    fun start(bundle: Bundle?) {
        start(bundle, false)
    }

    @Throws(BundleException::class)
    fun start(bundle: Bundle, async: Boolean) {
        bundle.start()
        if (!async) waitFor(bundle, Bundle.STARTING, Bundle.RESOLVED, Bundle.INSTALLED, 60000L)
    }

    @Throws(BundleException::class)
    fun stop(bundle: Bundle, async: Boolean) {
        bundle.stop()
        if (!async) waitFor(bundle, Bundle.STOPPING, Bundle.ACTIVE, Bundle.ACTIVE, 60000L)
    }

    @Throws(BundleException::class)
    private fun waitFor(bundle: Bundle, action1: Int, action2: Int, action3: Int, timeout: Long) {
        // we poll because opening a new thread is an overhead
        val start: Long = System.currentTimeMillis()
        while (bundle.getState() === action1 || bundle.getState() === action2 || bundle.getState() === action3) {
            if (start + timeout < System.currentTimeMillis()) throw BundleException("timeout [" + timeout + "] reached for action ["
                    + (if (action1 == Bundle.STARTING) "starting" else "stopping") + "], bundle is still in [" + bundle.getState() + "]")
            try {
                Thread.sleep(1)
            } catch (e: InterruptedException) {
            } // take a nap, before trying again
        }
    }

    @Throws(BundleException::class)
    fun startIfNecessary(factory: CFMLEngineFactory, bundle: Bundle) {
        if (bundle.getState() === Bundle.ACTIVE) return
        start(factory, bundle)
    }

    fun bundleState(state: Int, defaultValue: String): String {
        when (state) {
            Bundle.UNINSTALLED -> return "UNINSTALLED"
            Bundle.INSTALLED -> return "INSTALLED"
            Bundle.RESOLVED -> return "RESOLVED"
            Bundle.STARTING -> return "STARTING"
            Bundle.STOPPING -> return "STOPPING"
            Bundle.ACTIVE -> return "ACTIVE"
        }
        return defaultValue
    }

    @Throws(BundleException::class)
    fun toFrameworkBundleParent(str: String?): String {
        var str = str
        if (str != null) {
            str = str.trim()
            if (Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK.equalsIgnoreCase(str)) return Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK
            if (Constants.FRAMEWORK_BUNDLE_PARENT_APP.equalsIgnoreCase(str)) return Constants.FRAMEWORK_BUNDLE_PARENT_APP
            if (Constants.FRAMEWORK_BUNDLE_PARENT_BOOT.equalsIgnoreCase(str)) return Constants.FRAMEWORK_BUNDLE_PARENT_BOOT
            if (Constants.FRAMEWORK_BUNDLE_PARENT_EXT.equalsIgnoreCase(str)) return Constants.FRAMEWORK_BUNDLE_PARENT_EXT
        }
        throw BundleException(
                "value [" + str + "] for [" + Constants.FRAMEWORK_BUNDLE_PARENT + "] definition is invalid, " + "valid values are [" + Constants.FRAMEWORK_BUNDLE_PARENT_APP + ", "
                        + Constants.FRAMEWORK_BUNDLE_PARENT_BOOT + ", " + Constants.FRAMEWORK_BUNDLE_PARENT_EXT + ", " + Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK + "]")
    }

    fun isSystemBundle(bundle: Bundle?): Boolean {
        // TODO make a better implementation for this, independent of felix
        return bundle.getSymbolicName().equals("org.apache.felix.framework")
    }
}