package lucee.runtime.config

import lucee.runtime.db.ClassDefinition

object ConfigBase : ConfigPro {
    var onlyFirstMatch = false

    class Startup(cd: ClassDefinition<*>?, instance: Object?) {
        val cd: ClassDefinition<*>?
        val instance: Object?

        init {
            this.cd = cd
            this.instance = instance
        }
    }
}