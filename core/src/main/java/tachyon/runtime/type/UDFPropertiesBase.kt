package tachyon.runtime.type

import java.util.Set

abstract class UDFPropertiesBase : UDFProperties {
    private var page: Page? = null
    private var id: String? = null
    protected var ps: PageSource? = null
    protected var psOrg: PageSource? = null
    protected var startLine = 0
    protected var endLine = 0

    constructor() {}
    constructor(page: Page?, ps: PageSource?, startLine: Int, endLine: Int) {
        var ps: PageSource? = ps
        psOrg = ps
        this.page = page
        if (ps == null) {
            ps = ThreadLocalPageSource.get()
            if (ps == null && page != null) {
                ps = page.getPageSource()
            }
        }
        this.ps = ps
        this.startLine = startLine
        this.endLine = endLine
    }

    @Throws(PageException::class)
    fun getPage(pc: PageContext?): Page? {
        val p: Page? = getPage()
        if (p != null) return p

        // MUST no page source
        var pe: PageException? = null
        if (getPageSource() != null) {
            pe = try {
                return ComponentUtil.getPage(pc, getPageSource())
            } catch (e: PageException) {
                e
            }
            val log: Log = pc.getConfig().getLog("application")
            if (log != null) log.error("compiler", "UDFPropertiesBase does not have a page defintion for " + getPageSource().getDisplayPath())
        }
        if (pe != null) throw pe
        throw ApplicationException("missing Page Source")
    }

    fun id(): String? {
        if (id == null) {
            // MUST no page source
            if (getPageSource() != null) {
                id = getPageSource().getDisplayPath().toString() + ":" + getIndex()
            } else if (getPage() != null) {
                // MUST id for Page
                id = getPage().hashCode().toString() + ":" + getIndex()
            }
        }
        return id
    }

    protected fun getPage(): Page? {
        return page
    }

    fun getPageSource(): PageSource? {
        return ps
    }

    fun getStartLine(): Int {
        return startLine
    }

    fun getEndLine(): Int {
        return endLine
    }

    abstract fun getFunctionName(): String?
    abstract fun getOutput(): Boolean
    abstract fun getBufferOutput(): Boolean?
    abstract fun getReturnType(): Int
    abstract fun getReturnTypeAsString(): String?
    abstract fun getDescription(): String?
    abstract fun getReturnFormat(): Int
    abstract fun getReturnFormatAsString(): String?
    abstract fun getIndex(): Int
    abstract fun getCachedWithin(): Object?
    abstract fun getSecureJson(): Boolean?
    abstract fun getVerifyClient(): Boolean?
    abstract fun getFunctionArguments(): Array<FunctionArgument?>?
    abstract fun getDisplayName(): String?
    abstract fun getHint(): String?
    abstract fun getMeta(): Struct?
    abstract fun getLocalMode(): Integer?
    abstract fun getArgumentsSet(): Set<Key?>?
}