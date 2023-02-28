package tachyon.runtime.functions.system

import tachyon.commons.lang.StringUtil

class ExtensionExists : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toString(args[0]), Caster.toString(args[1])) else if (args.size == 1) call(pc, Caster.toString(args[0])) else throw FunctionException(pc, "ExtensionExists", 1, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 2627423175121799118L
        @Throws(PageException::class)
        fun call(pc: PageContext?, id: String?): Boolean {
            return call(pc, id, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, id: String?, version: String?): Boolean {
            if (find(id, version, (pc.getConfig() as ConfigWebPro).getServerRHExtensions())) return true
            return if (find(id, version, (pc.getConfig() as ConfigWebPro).getRHExtensions())) true else false
        }

        private fun find(id: String?, version: String?, extensions: Array<RHExtension?>?): Boolean {
            for (ext in extensions!!) {
                if (ext.getId().equalsIgnoreCase(id) || ext.getSymbolicName().equalsIgnoreCase(id)) {
                    if (StringUtil.isEmpty(version) || ext.getVersion().equalsIgnoreCase(version)) return true
                }
            }
            return false
        }
    }
}