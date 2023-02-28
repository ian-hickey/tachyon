package lucee.runtime.listener

import lucee.commons.lang.StringUtil

class SessionCookieDataImpl(private val httpOnly: Boolean, private val secure: Boolean, timeout: TimeSpan?, domain: String?, disableUpdate: Boolean, samesite: Short, path: String?) : SessionCookieData {
    private val timeout: TimeSpan?
    private val domain: String?
    private val path: String?
    private val disableUpdate: Boolean
    private val samesite: Short

    @Override
    override fun isHttpOnly(): Boolean {
        return httpOnly
    }

    @Override
    override fun isSecure(): Boolean {
        return secure
    }

    @Override
    override fun getTimeout(): TimeSpan? {
        return timeout
    }

    @Override
    override fun getDomain(): String? {
        return domain
    }

    @Override
    override fun getPath(): String? {
        return path
    }

    @Override
    override fun isDisableUpdate(): Boolean {
        return disableUpdate
    }

    @Override
    override fun getSamesite(): Short {
        return samesite
    }

    companion object {
        val DEFAULT: SessionCookieData? = SessionCookieDataImpl(true, false, TimeSpanImpl.fromMillis(CookieImpl.NEVER * 1000L), null, false,
                CookieData.SAMESITE_EMPTY, "/")

        @Throws(ApplicationException::class)
        fun toSamesite(str: String?): Short {
            var str = str
            if (StringUtil.isEmpty(str, true)) return SAMESITE_EMPTY
            str = str.trim()
            if ("NONE".equalsIgnoreCase(str)) return SAMESITE_NONE
            if ("LAX".equalsIgnoreCase(str)) return SAMESITE_LAX
            if ("STRICT".equalsIgnoreCase(str)) return SAMESITE_STRICT
            throw ApplicationException("invalid value [$str] for samesite cookie, valid values are [none,lax,strict]")
        }

        fun toSamesite(str: String?, defaultValue: Short): Short {
            var str = str
            if (StringUtil.isEmpty(str, true)) return SAMESITE_EMPTY
            str = str.trim()
            if ("NONE".equalsIgnoreCase(str)) return SAMESITE_NONE
            if ("LAX".equalsIgnoreCase(str)) return SAMESITE_LAX
            return if ("STRICT".equalsIgnoreCase(str)) SAMESITE_STRICT else defaultValue
        }

        fun toSamesite(s: Short): String? {
            if (s == SAMESITE_STRICT) return "Strict"
            if (s == SAMESITE_LAX) return "Lax"
            return if (s == SAMESITE_NONE) "None" else ""
        }
    }

    init {
        this.timeout = timeout
        this.domain = if (StringUtil.isEmpty(domain, true)) null else domain.trim()
        this.path = if (StringUtil.isEmpty(path, true)) null else path.trim()
        this.disableUpdate = disableUpdate
        this.samesite = samesite
    }
}