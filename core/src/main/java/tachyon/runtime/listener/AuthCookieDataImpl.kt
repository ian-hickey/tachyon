package tachyon.runtime.listener

import tachyon.runtime.type.dt.TimeSpan

class AuthCookieDataImpl(timeout: TimeSpan?, disableUpdate: Boolean) : AuthCookieData {
    private val timeout: TimeSpan?
    private val disableUpdate: Boolean

    @Override
    override fun getTimeout(): TimeSpan? {
        return timeout
    }

    @Override
    override fun isDisableUpdate(): Boolean {
        return disableUpdate
    }

    companion object {
        val DEFAULT: AuthCookieData? = AuthCookieDataImpl(TimeSpanImpl.fromMillis(CookieImpl.NEVER * 1000), false)
    }

    init {
        this.timeout = timeout
        this.disableUpdate = disableUpdate
    }
}