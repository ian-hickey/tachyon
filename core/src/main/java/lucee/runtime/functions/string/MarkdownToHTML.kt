package lucee.runtime.functions.string

import java.io.BufferedReader

class MarkdownToHTML : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size < 1 || args.size > 2) {
            throw FunctionException(pc, "MarkdownToHTML", 1, 2, args.size)
        }
        return call(pc, Caster.toString(args[0]))
    }

    companion object {
        private const val serialVersionUID = 3775127934350736736L
        @Throws(PageException::class)
        fun call(pc: PageContext?, markdown: String?): String? {
            return call(pc, markdown, false, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, markdown: String?, safeMode: Boolean): String? {
            return call(pc, markdown, safeMode, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, markdown: String?, safeMode: Boolean, encoding: String?): String? {
            if (markdown!!.length() < 2000 && !StringUtil.isEmpty(markdown, true)) {
                val res: Resource = ResourceUtil.toResourceExisting(pc, markdown.trim(), false, null)
                if (res != null) {
                    val cs: Charset
                    cs = if (StringUtil.isEmpty(encoding, true)) ThreadLocalPageContext.getConfig(pc).getWebCharset() else CharsetUtil.toCharset(encoding.trim())
                    var br: BufferedReader? = null
                    return try {
                        br = BufferedReader(InputStreamReader(res.getInputStream(), cs))
                        Processor.process(br, safeMode)
                    } catch (e: IOException) {
                        throw Caster.toPageException(e)
                    } finally {
                        IOUtil.closeEL(br)
                    }
                }
            }
            return Processor.process(markdown, safeMode)
        } /*
	 * public static void main(String[] args) { print.e(Processor.process("This is ***TXTMARK***",
	 * false)); }
	 */
    }
}