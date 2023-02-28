package lucee.runtime.functions.system

import java.util.Collection

class InspectTemplates : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 0) call(pc) else throw FunctionException(pc, "InspectTemplates", 0, 0, args.size)
    }

    companion object {
        private const val serialVersionUID = -2777306151061026079L
        fun call(pc: PageContext?): Boolean {
            reset(pc, null)
            return true
        }

        fun reset(pc: PageContext?, c: Config?) {
            var pc: PageContext? = pc
            val config: ConfigWebPro
            pc = ThreadLocalPageContext.get(pc)
            config = if (c == null) ThreadLocalPageContext.getConfig(pc) as ConfigWebPro else c as ConfigWebPro?

            // application context
            if (pc != null) {
                val ac: ApplicationContext = pc.getApplicationContext()
                if (ac != null) {
                    reset(config, ac.getMappings())
                    reset(config, ac.getComponentMappings())
                    reset(config, ac.getCustomTagMappings())
                }
            }

            // config
            reset(config, config.getMappings())
            reset(config, config.getCustomTagMappings())
            reset(config, config.getComponentMappings())
            reset(config, config.getFunctionMappings())
            reset(config, config.getServerFunctionMappings())
            reset(config, config.getTagMappings())
            reset(config, config.getServerTagMappings())
        }

        fun reset(config: Config?, mappings: Collection<Mapping?>?) {
            if (mappings == null) return
            val it: Iterator<Mapping?> = mappings.iterator()
            while (it.hasNext()) {
                reset(config, it.next())
            }
        }

        fun reset(config: Config?, mappings: Array<Mapping?>?) {
            if (mappings == null) return
            for (i in mappings.indices) {
                reset(config, mappings[i])
            }
        }

        fun reset(config: Config?, mapping: Mapping?) {
            if (mapping == null) return
            (mapping as MappingImpl?).resetPages(null)
        }
    }
}