package lucee.commons.lang.compiler

import java.util.ArrayList

/**
 * Compile Java sources in-memory
 */
object JavaCCompiler {
    @Throws(JavaCompilerException::class, ApplicationException::class)
    fun compile(parent: PageSource?, sc: SourceCode): JavaFunction {
        val cl: ClassLoader = CFMLEngineFactory.getInstance().getCFMLEngineFactory().getClass().getClassLoader()
        val compilationUnits: Collection<SourceCode> = ArrayList()
        compilationUnits.add(sc)
        val dcl = DynamicClassLoader(cl)
        val javac: javax.tools.JavaCompiler = ToolProvider.getSystemJavaCompiler()
                ?: throw ApplicationException("Java compiling is not suppprted with your current JVM Enviroment (" + System.getProperty("java.vendor").toString() + " "
                        + System.getProperty("java.version")
                        .toString() + "). Update to a newer version or add a tools.jar to the enviroment. Read more here: https://stackoverflow.com/questions/15513330/toolprovider-getsystemjavacompiler-returns-null-usable-with-only-jre-install")
        val options: List<String> = ArrayList<String>()

        // TODO MUST better way to do this!!!
        options.add("-classpath")
        options.add(OSGiUtil.getClassPath())
        val collector: DiagnosticCollector<JavaFileObject> = DiagnosticCollector()
        val fileManager = ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), dcl)
        val task: javax.tools.JavaCompiler.CompilationTask = javac.getTask(null, fileManager, collector, options, null, compilationUnits)
        val result: Boolean = task.call()
        if (!result || collector.getDiagnostics().size() > 0) {
            val exceptionMsg = StringBuilder()
            exceptionMsg.append("Unable to compile the source")
            var hasWarnings = false
            var hasErrors = false
            for (d in collector.getDiagnostics()) {
                when (d.getKind()) {
                    NOTE, MANDATORY_WARNING, WARNING -> hasWarnings = true
                    OTHER, ERROR -> hasErrors = true
                    else -> hasErrors = true
                }
                if (hasErrors) throw JavaCompilerException(d.getMessage(Locale.US), d.getLineNumber(), d.getColumnNumber(), d.getKind())
            }
        }
        return JavaFunction(parent, sc, dcl.getCompiledCode(sc.getClassName()).getByteCode())
    }
}