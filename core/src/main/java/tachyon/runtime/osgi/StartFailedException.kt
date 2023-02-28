package tachyon.runtime.osgi

import org.osgi.framework.Bundle

class StartFailedException(bundleException: BundleException?, bundle: Bundle?) : Exception() {
    val bundleException: BundleException?
    val bundle: Bundle?
    private var bd: BundleDefinition? = null
    var bundleDefinition: BundleDefinition?
        get() = bd
        set(bd) {
            this.bd = bd
        }

    companion object {
        private const val serialVersionUID = -6268178595687225586L
    }

    init {
        this.bundleException = bundleException
        this.bundle = bundle
    }
}