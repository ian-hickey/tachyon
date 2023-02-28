package lucee.runtime.listener

import lucee.runtime.type.dt.TimeSpan

interface CookieData {
    fun getTimeout(): TimeSpan?
    fun isDisableUpdate(): Boolean

    companion object {
        const val SAMESITE_EMPTY: Short = 0
        const val SAMESITE_NONE: Short = 1
        const val SAMESITE_STRICT: Short = 2
        const val SAMESITE_LAX: Short = 3
    }
}