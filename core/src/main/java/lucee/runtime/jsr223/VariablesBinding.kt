package lucee.runtime.jsr223

import java.util.Collection

class VariablesBinding : Bindings {
    private val `var`: VariablesImpl?
    fun getVaraibles(): Variables? {
        return `var`
    }

    @Override
    fun size(): Int {
        return `var`.size()
    }

    @Override
    fun isEmpty(): Boolean {
        return `var`.isEmpty()
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return `var`.containsValue(value)
    }

    @Override
    fun clear() {
        `var`.clear()
    }

    @Override
    fun keySet(): Set<String?>? {
        return `var`.keySet()
    }

    @Override
    fun values(): Collection<Object?>? {
        return `var`.values()
    }

    @Override
    fun entrySet(): Set<Map.Entry<String?, Object?>?>? {
        return `var`.entrySet()
    }

    @Override
    fun put(name: String?, value: Object?): Object? {
        return `var`.put(name, value)
    }

    @Override
    fun putAll(toMerge: Map<out String?, Object?>?) {
        `var`.putAll(toMerge)
    }

    @Override
    fun containsKey(key: Object?): Boolean {
        return `var`.containsKey(key)
    }

    @Override
    operator fun get(key: Object?): Object? {
        return `var`.get(key)
    }

    @Override
    fun remove(key: Object?): Object? {
        return `var`.remove(key)
    }

    init {
        `var` = VariablesImpl()
    }
}