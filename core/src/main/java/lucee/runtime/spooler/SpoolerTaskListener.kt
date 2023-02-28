package lucee.runtime.spooler

import java.io.Serializable

abstract class SpoolerTaskListener : Serializable {
    abstract fun listen(config: Config?, e: Exception?, before: Boolean)
}