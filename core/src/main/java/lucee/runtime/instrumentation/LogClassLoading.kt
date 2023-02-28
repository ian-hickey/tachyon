package lucee.runtime.instrumentation

import java.lang.instrument.ClassFileTransformer

object LogClassLoading {
    private var log: Log? = null
    private val logName: String? = null
    fun enable(config: Config?) {
        enable(config, null)
    }

    fun enable(config: Config?, logName: String?) {
        var logName = logName
        if (StringUtil.isEmpty(logName)) logName = "application"
        if (LogClassLoading.logName == null) {
            InstrumentationFactory.getInstrumentation(config).addTransformer(LogClassFileTransformer())
            log = ThreadLocalPageContext.getLog(config, logName)
        } else if (!LogClassLoading.logName.equalsIgnoreCase(logName)) {
            log = ThreadLocalPageContext.getLog(config, logName)
        }
    }

    private class LogClassFileTransformer : ClassFileTransformer {
        @Override
        @Throws(IllegalClassFormatException::class)
        fun transform(loader: ClassLoader?, className: String?, classBeingRedefined: Class<*>?, protectionDomain: ProtectionDomain?, classfileBuffer: ByteArray?): ByteArray? {
            val line = ("{'loader':'" + loader + "','class':'" + className + "','classBeingRedefined':'" + (if (classBeingRedefined == null) "" else classBeingRedefined.getName())
                    + "'}")
            log.info("class-loading", line)
            return null
        } /*
		 * public byte[] transform(Module module, ClassLoader loader, String className, Class<?>
		 * classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws
		 * IllegalClassFormatException { print.e(loader + ":" + className); return null; }
		 */
    }
}