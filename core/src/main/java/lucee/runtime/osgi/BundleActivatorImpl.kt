package lucee.runtime.osgi

import org.osgi.framework.BundleActivator

/**
 * This class implements a simple bundle that utilizes the OSGi framework's event mechanism to
 * listen for service events. Upon receiving a service event, it prints out the event's details.
 */
class BundleActivatorImpl : BundleActivator {
    /**
     * Implements BundleActivator.start(). Prints a message and adds itself to the bundle context as a
     * service listener.
     *
     * @param context the framework context for the bundle.
     */
    @Override
    fun start(context: BundleContext?) {
        System.out.println("BundleActivatorImpl:Starting to listen for service events.")
        // context.addServiceListener(this);
    }

    /**
     * Implements BundleActivator.stop(). Prints a message and removes itself from the bundle context as
     * a service listener.
     *
     * @param context the framework context for the bundle.
     */
    @Override
    fun stop(context: BundleContext?) {
        // context.removeServiceListener(this);
        System.out.println("BundleActivatorImpl:Stopped listening for service events.")
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()
        System.out.println("engine:" + (engine != null))
        // if (engine != null) engine.reset();
    }
}