package lucee.runtime.script

import java.io.PrintStream

object Main {
    private const val USAGE = "Usage: script [-options]\n\n" + "Where options include:\n" + "-l  language\n" + "-e  code\n"
    @Throws(Exception::class)
    fun main(args: Array<String>) {
        var lang = "CFML"
        var code: String? = null
        var arg: String
        val pw: String? = null
        val key: String? = null
        var i = 0
        while (i < args.size) {
            arg = args[i]
            if ("-l".equals(arg)) {
                if (args.size > i + 1) lang = args[++i].trim()
            } else if ("-e".equals(arg)) if (args.size > i + 1) code = args[++i].trim()
            i++
        }
        val dialect: Int = CFMLEngine.DIALECT_CFML
        if (code == null) printUsage("-e is missing", System.err)
        val factory = LuceeScriptEngineFactory()
        System.out.println(factory.getScriptEngine().eval(code))
        val engine: ScriptEngine = ScriptEngineManager().getEngineByName(lang)
        if (engine == null) System.out.println("could not load an engine with the name:$lang") else System.out.println(engine.eval(code))
    }

    private fun printUsage(msg: String, ps: PrintStream) {
        ps.println()
        ps.println("Failed to execute!")
        ps.println("Reason: $msg")
        ps.println()
        ps.print(USAGE)
        ps.flush()
        System.exit(0)
    }
}