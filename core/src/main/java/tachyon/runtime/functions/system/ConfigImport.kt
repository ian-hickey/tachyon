package tachyon.runtime.functions.system

import java.nio.charset.Charset

class ConfigImport : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]), null, null, null, null)
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), null, null, null)
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), null, null)
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toStruct(args[3]), null)
        return if (args.size == 4) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toStruct(args[3]), Caster.toString(args[4])) else throw FunctionException(pc, "ConfigFileImport", 1, 5, args.size)
    }

    companion object {
        private const val serialVersionUID = 2877661269574331695L
        @Throws(PageException::class)
        fun call(pc: PageContext?, pathOrData: Object?, type: String?, password: String?, placeHolderData: Struct?, charset: String?): Struct? {

            // path
            var type = type
            var password = password
            var res: Resource? = null
            var data: Struct? = null
            if (pathOrData is CharSequence) res = ResourceUtil.toResourceExisting(pc, pathOrData.toString()) else if (pathOrData is Map) data = Caster.toStruct(pathOrData) else throw FunctionException(pc, "ConfigFileImport", "first", "pathOrData",
                    "Invalid value for argument pathOrData, the argument must contain a string that points to a .CFConfig.json file or a struct containing the data itself.", null)

            // type
            if (StringUtil.isEmpty(type)) type = "server" else if (!"server".equalsIgnoreCase(type) && !"web".equalsIgnoreCase(type)) throw FunctionException(pc, "ConfigFileImport", "second", "type", "Invalid value for argument type ($type), valid values are [server,web]", null)

            // password
            if (StringUtil.isEmpty(password)) {
                password = SystemUtil.getSystemPropOrEnvVar("tachyon." + type.toLowerCase().toString() + ".admin.password", null)
                if (StringUtil.isEmpty(password)) throw FunctionException(pc, "ConfigFileImport", "third", "password", "There is no password defined as an argument for the function",
                        "You can define a password to access the " + type.toLowerCase().toString() + " config in 3 ways. As an argument with this function, as enviroment variable [LUCEE_"
                                + type.toUpperCase().toString() + "_ADMIN_PASSWORD] or as system property [tachyon." + type.toLowerCase().toString() + ".admin.password]")
            }

            // charset
            val cs: Charset = if (StringUtil.isEmpty(charset, true)) pc.getResourceCharset() else CharsetUtil.toCharset(charset)
            return (if (res != null) CFConfigImport(pc.getConfig(), res, cs, password, type, placeHolderData) else CFConfigImport(pc.getConfig(), data, cs, password, type, placeHolderData)).execute()
        }
    }
}