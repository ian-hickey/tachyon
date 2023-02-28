package tachyon.commons.lang.compiler

import java.io.IOException

/**
 * Created by trung on 5/3/15. Edited by turpid-monkey on 9/25/15, completed support for multiple
 * compile units.
 */
class ExtendedStandardJavaFileManager(fileManager: JavaFileManager?, cl: DynamicClassLoader) : ForwardingJavaFileManager<JavaFileManager?>(fileManager) {
    private val compiledCode: List<CompiledCode> = ArrayList<CompiledCode>()
    private val cl: DynamicClassLoader
    @Override
    @Throws(IOException::class)
    fun list(location: Location?, packageName: String?, kinds: Set<Kind?>?, recurse: Boolean): Iterable<JavaFileObject> {
        // print.e("listt:" + location);
        // print.e(packageName);
        /*
		 * Iterable<JavaFileObject> it = super.list(location, packageName, kinds, recurse); for
		 * (JavaFileObject jfo: it) { print.e("- " + jfo); }
		 */
        return super.list(location, packageName, kinds, recurse)
    }

    @Override
    fun inferBinaryName(location: Location?, file: JavaFileObject?): String {
        // print.e("inferBinary:" + location);
        // print.e(file);
        return super.inferBinaryName(location, file)
    }

    @Override
    fun hasLocation(location: Location?): Boolean {
        // print.e("has" + location);
        return super.hasLocation(location)
    }

    @Override
    @Throws(IOException::class)
    fun getFileForInput(location: Location?, packageName: String?, relativeName: String?): FileObject {
        // print.e("forInput:" + location);
        // print.e(packageName);
        // print.e(relativeName);
        return super.getFileForInput(location, packageName, relativeName)
    }

    /*
	 * @Override public String inferModuleName(Location location) throws IOException { print.e("infer" +
	 * location); return super.inferModuleName(location); }
	 */
    /*
	 * @Override public Iterable<Set<Location>> listLocationsForModules(Location location) throws
	 * IOException { print.e("listLoc" + location); return super.listLocationsForModules(location); }
	 */
    /*
	 * public boolean contains(Location location, FileObject fo) throws IOException { print.e("contains"
	 * + location); return super.contains(location, fo); }
	 */
    @Override
    @Throws(IOException::class)
    fun getJavaFileForOutput(location: JavaFileManager.Location?, className: String, kind: JavaFileObject.Kind?, sibling: FileObject?): JavaFileObject {
        // print.e("getJavaFileForOutput:" + location);
        // print.e(className);
        return try {
            val innerClass = CompiledCode(className)
            compiledCode.add(innerClass)
            cl.addCode(innerClass)
            innerClass
        } catch (e: Exception) {
            throw RuntimeException("Error while creating in-memory output file for $className", e)
        }
    }

    @Override
    fun getClassLoader(location: JavaFileManager.Location?): ClassLoader {
        // print.e("getClassLoader" + location);
        return cl
    }

    /**
     * Creates a new instance of ForwardingJavaFileManager.
     *
     * @param fileManager delegate to this file manager
     * @param cl
     */
    init {
        this.cl = cl
    }
}