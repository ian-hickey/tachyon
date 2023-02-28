package lucee.commons.io.log.log4j2.appender

import java.io.Writer

/**
 * Appends log events to a [Writer].
 */
@Plugin(name = "Writer", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
class ConsoleAppender private constructor(name: String, layout: StringLayout?, filter: Filter, manager: WriterManager, ignoreExceptions: Boolean,
                                          properties: Array<Property>?) : AbstractWriterAppender<WriterManager?>(name, layout, filter, ignoreExceptions, true, properties, manager) {
    /**
     * Builds WriterAppender instances.
     */
    class Builder<B : Builder<B>?> : AbstractAppender.Builder<B>(), org.apache.logging.log4j.core.util.Builder<ConsoleAppender?> {
        private var follow = false
        private var target: Writer? = null
        @Override
        fun build(): ConsoleAppender {
            val layout: StringLayout? = getLayout() as StringLayout?
            val actualLayout: StringLayout = if (layout != null) layout else PatternLayout.createDefaultLayout()
            return ConsoleAppender(getName(), actualLayout, getFilter(), getManager(target, follow, actualLayout), isIgnoreExceptions(), getPropertyArray())
        }

        fun setFollow(shouldFollow: Boolean): B {
            follow = shouldFollow
            return asBuilder()
        }

        fun setTarget(aTarget: Writer?): B {
            target = aTarget
            return asBuilder()
        }
    }

    /**
     * Holds data to pass to factory method.
     */
    private class FactoryData(writer: Writer, type: String, layout: StringLayout?) {
        val layout: StringLayout?
        val name: String
        val writer: Writer

        /**
         * Builds instances.
         *
         * @param writer The OutputStream.
         * @param type The name of the target.
         * @param layout A String layout
         */
        init {
            this.writer = writer
            name = type
            this.layout = layout
        }
    }

    private class WriterManagerFactory : ManagerFactory<WriterManager?, FactoryData?> {
        /**
         * Creates a WriterManager.
         *
         * @param name The name of the entity to manage.
         * @param data The data required to create the entity.
         * @return The WriterManager
         */
        @Override
        fun createManager(name: String?, data: FactoryData): WriterManager {
            return WriterManager(data.writer, data.name, data.layout, true)
        }
    }

    companion object {
        private val factory = WriterManagerFactory()

        /**
         * Creates a WriterAppender.
         *
         * @param layout The layout to use or null to get the default layout.
         * @param filter The Filter or null.
         * @param target The target Writer
         * @param follow If true will follow changes to the underlying output stream. Use false as the
         * default.
         * @param name The name of the Appender (required).
         * @param ignore If `"true"` (default) exceptions encountered when appending events are
         * logged; otherwise they are propagated to the caller. Use true as the default.
         * @return The ConsoleAppender.
         */
        @PluginFactory
        fun createAppender(layout: StringLayout?, filter: Filter, target: Writer?, name: String?, follow: Boolean, ignore: Boolean): ConsoleAppender? {
            var layout: StringLayout? = layout
            if (name == null) {
                LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_ERROR, "log-loading", "No name provided for WriterAppender")
                return null
            }
            if (layout == null) {
                layout = PatternLayout.createDefaultLayout()
            }
            return ConsoleAppender(name, layout, filter, getManager(target, follow, layout), ignore, null)
        }

        private fun getManager(target: Writer?, follow: Boolean, layout: StringLayout?): WriterManager {
            val writer: Writer = CloseShieldWriter(target)
            val managerName: String = target.getClass().getName().toString() + "@" + Integer.toHexString(target.hashCode()) + '.' + follow
            return WriterManager.getManager(managerName, FactoryData(writer, managerName, layout), factory)
        }

        @PluginBuilderFactory
        fun <B : Builder<B>?> newBuilder(): B {
            return Builder<B>().asBuilder()
        }
    }
}