package lucee.commons.lang.compiler

import java.io.FileNotFoundException

class CustomJavaFileFolder(context: BundleContext, packageName: String) {
    private val elements: List<JavaFileObject> = ArrayList<JavaFileObject>()
    private val context: BundleContext
    val packageName: String
    val entries: List<Any>
        get() = Collections.unmodifiableList(elements)

    @Throws(IOException::class, URISyntaxException::class)
    private fun findAll(packageName: String): List<JavaFileObject> {
        var packageName = packageName
        val result: List<JavaFileObject>
        packageName = packageName.replaceAll("\\.", "/")
        result = ArrayList<JavaFileObject>()
        val b: Array<Bundle> = context.getBundles()
        for (bundle in b) {
            enumerateWiring(packageName, result, bundle)
        }
        return result
    }

    @Throws(URISyntaxException::class, IOException::class)
    private fun enumerateWiring(packageName: String, result: List<JavaFileObject>, b: Bundle) {
        val bw: BundleWiring = b.adapt(BundleWiring::class.java)
        val cc: Collection<String> = bw.listResources(packageName, null, BundleWiring.LISTRESOURCES_RECURSE)
        for (resource in cc) {
            val u: URL = b.getResource(resource)
            if (u != null) {
                var openStream: InputStream? = null
                try {
                    openStream = u.openStream()
                    val customJavaFileObject = CustomJavaFileObject(resource, u.toURI(), openStream, Kind.CLASS)
                    result.add(customJavaFileObject)
                } catch (e: FileNotFoundException) {
                    val customJavaFileObject = CustomJavaFileObject(resource, u.toURI(), null, Kind.CLASS)
                    result.add(customJavaFileObject)
                }
            }
        }
    }

    init {
        this.context = context
        this.packageName = packageName
        elements.addAll(findAll(packageName))
    }
}