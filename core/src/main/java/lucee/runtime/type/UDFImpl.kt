/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package lucee.runtime.type

import java.io.Externalizable

/**
 * defines an abstract class for a User defined Functions
 */
class UDFImpl : MemberSupport, UDFPlus, Externalizable, Cloneable {
    protected var ownerComponent: Component? = null
    var properties: UDFPropertiesBase? = null

    /**
     * DO NOT USE THIS CONSTRUCTOR! this constructor is only for deserialize process
     */
    constructor() : super(0) {}
    constructor(properties: UDFProperties?) : super(properties.getAccess(), properties.getModifier()) {
        this.properties = properties
    }

    constructor(properties: UDFProperties?, owner: Component?) : super(properties.getAccess(), properties.getModifier()) {
        this.properties = properties
        setOwnerComponent(owner)
    }

    fun duplicate(cfc: Component?): UDF? {
        val udf = UDFImpl(properties)
        udf.ownerComponent = cfc
        udf.setAccess(getAccess())
        return udf
    }

    @Override
    fun duplicate(deepCopy: Boolean): UDF? {
        return duplicate(ownerComponent)
    }

    @Override
    fun duplicate(): UDF? {
        return duplicate(ownerComponent)
    }

    @Override
    @Throws(Throwable::class)
    fun implementation(pageContext: PageContext?): Object? {
        return properties!!.getPage(pageContext).udfCall(pageContext, this, properties!!.getIndex())
    }

    @Throws(PageException::class)
    private fun castToAndClone(pc: PageContext?, arg: FunctionArgument?, value: Object?, index: Int): Object? {
        if (value == null && (pc as PageContextImpl?).getFullNullSupport()) return value
        if (!(pc as PageContextImpl?).getTypeChecking() || Decision.isCastableTo(pc, arg.getType(), arg.getTypeAsString(), value)) return if (arg.isPassByReference()) value else Duplicator.duplicate(value, true)
        throw UDFCasterException(this, arg, value, index)
    }

    @Throws(PageException::class)
    private fun castTo(pc: PageContext?, arg: FunctionArgument?, value: Object?, index: Int): Object? {
        if (Decision.isCastableTo(pc, arg.getType(), arg.getTypeAsString(), value)) return value
        throw UDFCasterException(this, arg, value, index)
    }

    @Throws(PageException::class)
    private fun defineArguments(pc: PageContext?, funcArgs: Array<FunctionArgument?>?, args: Array<Object?>?, newArgs: Argument?) {
        // define argument scope
        val fns: Boolean = NullSupportHelper.full(pc)
        val _null: Object = NullSupportHelper.NULL(fns)
        for (i in funcArgs.indices) {
            // argument defined
            if (args!!.size > i && (args[i] != null || fns)) {
                newArgs.setEL(funcArgs!![i].getName(), castToAndClone(pc, funcArgs[i], args[i], i + 1))
            } else {
                val d: Object? = getDefaultValue(pc, i, _null)
                if (d === _null) {
                    if (funcArgs!![i].isRequired()) {
                        throw ExpressionException("The parameter [" + funcArgs[i].getName().toString() + "] to function [" + getFunctionName().toString() + "] is required but was not passed in.")
                    }
                    if (!fns) newArgs.setEL(funcArgs[i].getName(), Argument.NULL)
                } else {
                    newArgs.setEL(funcArgs!![i].getName(), castTo(pc, funcArgs[i], d, i + 1))
                }
            }
        }
        for (i in funcArgs!!.size until args!!.size) {
            newArgs.setEL(ArgumentIntKey.init(i + 1), args!![i])
        }
    }

    @Throws(PageException::class)
    private fun defineArguments(pageContext: PageContext?, funcArgs: Array<FunctionArgument?>?, values: Struct?, newArgs: Argument?) {
        // argumentCollection
        UDFUtil.argumentCollection(values, funcArgs)
        // print.out(values.size());
        var value: Object
        var name: Collection.Key
        val _null: Object = NullSupportHelper.NULL(pageContext)
        for (i in funcArgs.indices) {
            // argument defined
            name = funcArgs!![i].getName()
            value = values.remove(name, _null)
            if (value !== _null) {
                newArgs.set(name, castToAndClone(pageContext, funcArgs[i], value, i + 1))
                continue
            }
            value = values.remove(ArgumentIntKey.init(i + 1), _null)
            if (value !== _null) {
                newArgs.set(name, castToAndClone(pageContext, funcArgs[i], value, i + 1))
                continue
            }

            // default argument or exception
            val defaultValue: Object? = getDefaultValue(pageContext, i, _null) // funcArgs[i].getDefaultValue();
            if (defaultValue === _null) {
                if (funcArgs[i].isRequired()) {
                    throw ExpressionException("The parameter [" + funcArgs[i].getName().toString() + "] to function [" + getFunctionName().toString() + "] is required but was not passed in.")
                }
                if (pageContext.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML && !pageContext.getConfig().getFullNullSupport()) newArgs.set(name, Argument.NULL)
            } else newArgs.set(name, castTo(pageContext, funcArgs[i], defaultValue, i + 1))
        }
        val it: Iterator<Entry<Key?, Object?>?> = values.entryIterator()
        var e: Entry<Key?, Object?>?
        while (it.hasNext()) {
            e = it.next()
            newArgs.set(e.getKey(), e.getValue())
        }
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, values: Struct?, doIncludePath: Boolean): Object? {
        return if (hasCachedWithin(pc)) _callCachedWithin(pc, null, null, values, doIncludePath) else _call(pc, null, null, values, doIncludePath)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, calledName: Collection.Key?, values: Struct?, doIncludePath: Boolean): Object? {
        return if (hasCachedWithin(pc)) _callCachedWithin(pc, calledName, null, values, doIncludePath) else _call(pc, calledName, null, values, doIncludePath)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        return if (hasCachedWithin(pc)) _callCachedWithin(pc, null, args, null, doIncludePath) else _call(pc, null, args, null, doIncludePath)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, calledName: Collection.Key?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        return if (hasCachedWithin(pc)) _callCachedWithin(pc, calledName, args, null, doIncludePath) else _call(pc, calledName, args, null, doIncludePath)
    }

    private fun hasCachedWithin(pc: PageContext?): Boolean {
        return properties!!.getCachedWithin() != null || pc.getCachedWithin(Config.CACHEDWITHIN_FUNCTION) != null
        // Maybe better return !StringUtil.isEmpty(this.properties.cachedWithin) ||
        // !StringUtil.isEmpty(pc.getCachedWithin(Config.CACHEDWITHIN_FUNCTION));
    }

    private fun getCachedWithin(pc: PageContext?): Object? {
        return if (properties!!.getCachedWithin() != null) properties!!.getCachedWithin() else pc.getCachedWithin(Config.CACHEDWITHIN_FUNCTION)
    }

    @Throws(PageException::class)
    private fun _callCachedWithin(pc: PageContext?, calledName: Collection.Key?, args: Array<Object?>?, values: Struct?, doIncludePath: Boolean): Object? {
        val pci: PageContextImpl? = pc as PageContextImpl?
        val cachedWithin: Object? = getCachedWithin(pc)
        val cacheId: String = CacheHandlerCollectionImpl.createId(this, args, values)
        val cacheHandler: CacheHandler = pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_FUNCTION, null).getInstanceMatchingObject(getCachedWithin(pc), null)
        if (cacheHandler is CacheHandlerPro) {
            val cacheItem: CacheItem = (cacheHandler as CacheHandlerPro).get(pc, cacheId, cachedWithin)
            if (cacheItem is UDFCacheItem) {
                val entry: UDFCacheItem = cacheItem as UDFCacheItem
                try {
                    pc.write(entry.output)
                } catch (e: IOException) {
                    throw Caster.toPageException(e)
                }
                return entry.returnValue
            }
        } else if (cacheHandler != null) { // TODO this else block can be removed when all cache handlers implement CacheHandlerPro
            val cacheItem: CacheItem = cacheHandler.get(pc, cacheId)
            if (cacheItem is UDFCacheItem) {
                val entry: UDFCacheItem = cacheItem as UDFCacheItem
                // if(entry.creationdate+properties.cachedWithin>=System.currentTimeMillis()) {
                try {
                    pc.write(entry.output)
                } catch (e: IOException) {
                    throw Caster.toPageException(e)
                }
                return entry.returnValue
                // }
                // cache.remove(id);
            }
        }

        // cached item not found, process and cache result if needed
        val start: Long = System.nanoTime()

        // execute the function
        val bc: BodyContent = pci.pushBody()
        return try {
            val rtn: Object? = _call(pci, calledName, args, values, doIncludePath)
            if (cacheHandler != null) {
                val out: String = bc.getString()
                cacheHandler.set(pc, cacheId, cachedWithin, UDFCacheItem(out, rtn, getFunctionName(), getSource(), System.nanoTime() - start))
            }
            rtn
        } finally {
            BodyContentUtil.flushAndPop(pc, bc)
        }
    }

    @Throws(PageException::class)
    private fun _call(pc: PageContext?, calledName: Collection.Key?, args: Array<Object?>?, values: Struct?, doIncludePath: Boolean): Object? {
        val pci: PageContextImpl? = pc as PageContextImpl?
        val newArgs: Argument = pci.getScopeFactory().getArgumentInstance()
        newArgs.setFunctionArgumentNames(properties!!.getArgumentsSet())
        val newLocal: LocalImpl = pci.getScopeFactory().getLocalInstance()
        val undefined: Undefined = pc.undefinedScope()
        val oldArgs: Argument = pc.argumentsScope()
        val oldLocal: Local = pc.localScope()
        val oldCalledName: Collection.Key = pci.getActiveUDFCalledName()
        pc.setFunctionScopes(newLocal, newArgs)
        pci.setActiveUDFCalledName(calledName)
        val oldCheckArgs: Int = undefined.setMode(if (pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML) if (properties!!.getLocalMode() == null) pc.getApplicationContext().getLocalMode() else properties!!.getLocalMode().intValue() else Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS)
        var ps: PageSource? = null
        var psInc: PageSource? = null
        try {
            ps = properties!!.getPageSource()
            if (doIncludePath) psInc = ps
            if (doIncludePath && getOwnerComponent() != null) {
                psInc = ComponentUtil.getPageSource(getOwnerComponent())
                if (psInc === pci.getCurrentTemplatePageSource()) {
                    psInc = null
                }
            }
            if (ps != null) pci.addPageSource(ps, psInc)
            pci.addUDF(this)

            //////////////////////////////////////////
            var bc: BodyContent? = null
            var wasSilent: Boolean? = null
            val bufferOutput = getBufferOutput(pci)
            if (!getOutput()) {
                if (bufferOutput) bc = pci.pushBody() else wasSilent = if (pc.setSilent()) Boolean.TRUE else Boolean.FALSE
            }
            var parent: UDF? = null
            if (ownerComponent != null) {
                parent = pci.getActiveUDF()
                pci.setActiveUDF(this)
            }
            var returnValue: Object? = null
            try {
                if (args != null) defineArguments(pc, getFunctionArguments(), args, newArgs) else defineArguments(pc, getFunctionArguments(), values, newArgs)
                returnValue = implementation(pci)
                if (ownerComponent != null) pci.setActiveUDF(parent)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                if (ownerComponent != null) pci.setActiveUDF(parent)
                if (!getOutput()) {
                    if (bufferOutput) BodyContentUtil.flushAndPop(pc, bc) else if (!wasSilent!!) pc.unsetSilent()
                }
                throw Caster.toPageException(t)
            }
            if (!getOutput()) {
                if (bufferOutput) BodyContentUtil.clearAndPop(pc, bc) else if (!wasSilent!!) pc.unsetSilent()
            }
            // BodyContentUtil.clearAndPop(pc,bc);
            if (returnValue == null && (pc as PageContextImpl?).getFullNullSupport()) return returnValue
            if (properties!!.getReturnType() === CFTypes.TYPE_ANY || !(pc as PageContextImpl?).getTypeChecking()) return returnValue
            if (Decision.isCastableTo(properties!!.getReturnTypeAsString(), returnValue, false, false, -1)) return returnValue
            throw UDFCasterException(this, properties!!.getReturnTypeAsString(), returnValue)

            // REALCAST return Caster.castTo(pageContext,returnType,returnValue,false);
            //////////////////////////////////////////
        } finally {
            if (ps != null) pc.removeLastPageSource(psInc != null)
            pci.removeUDF()
            pci.setFunctionScopes(oldLocal, oldArgs)
            pci.setActiveUDFCalledName(oldCalledName)
            undefined.setMode(oldCheckArgs)
            pci.getScopeFactory().recycle(pci, newArgs)
            pci.getScopeFactory().recycle(pci, newLocal)
        }
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return UDFUtil.toDumpData(pageContext, maxlevel, dp, this, UDFUtil.TYPE_UDF)
    }

    @Override
    fun getDisplayName(): String? {
        return properties!!.getDisplayName()
    }

    @Override
    fun getHint(): String? {
        return properties!!.getHint()
    }

    /*
	 * @Override public PageSource getPageSource() { return properties.pageSource; }
	 */
    @Override
    fun getSource(): String? {
        return if (properties!!.getPageSource() != null) properties!!.getPageSource().getDisplayPath() else ""
    }

    fun getMeta(): Struct? {
        return properties!!.getMeta()
    }

    @Override
    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?): Struct? {
        return ComponentUtil.getMetaData(pc, this, properties, null)
        // return getMetaData(pc, this);
    }

    @Override
    fun getValue(): Object? {
        return this
    }

    /**
     * @param component the componentImpl to set
     */
    @Override
    override fun setOwnerComponent(component: Component?) {
        ownerComponent = component
    }

    @Override
    fun getOwnerComponent(): Component? {
        return ownerComponent // +++
    }

    @Override
    override fun toString(): String {
        val sb = StringBuffer(properties!!.getFunctionName())
        sb.append("(")
        var optCount = 0
        val args: Array<FunctionArgument?> = properties!!.getFunctionArguments()
        for (i in args.indices) {
            if (i > 0) sb.append(", ")
            if (!args[i].isRequired()) {
                sb.append("[")
                optCount++
            }
            sb.append(args[i].getTypeAsString())
            sb.append(" ")
            sb.append(args[i].getName())
        }
        for (i in 0 until optCount) {
            sb.append("]")
        }
        sb.append(")")
        return sb.toString()
    }

    @Override
    fun getSecureJson(): Boolean? {
        return properties!!.getSecureJson()
    }

    @Override
    fun getVerifyClient(): Boolean? {
        return properties!!.getVerifyClient()
    }

    @Override
    fun clone(): Object {
        return duplicate()
    }

    @Override
    fun getFunctionArguments(): Array<FunctionArgument?>? {
        return properties!!.getFunctionArguments()
    }

    @Override
    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int): Object? {
        return getDefaultValue(pc, index, null)
    }

    @Override
    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int, defaultValue: Object?): Object? {
        return properties!!.getPage(pc).udfDefaultValue(pc, properties!!.getIndex(), index, defaultValue)
    }

    // public abstract Object getDefaultValue(PageContext pc,int index) throws PageException;
    @Override
    fun getFunctionName(): String? {
        return properties!!.getFunctionName()
    }

    @Override
    fun getOutput(): Boolean {
        return properties!!.getOutput()
    }

    fun getBufferOutput(): Boolean? {
        return properties!!.getBufferOutput()
    }

    @Override
    fun getBufferOutput(pc: PageContext?): Boolean {
        return if (properties!!.getBufferOutput() != null) properties!!.getBufferOutput().booleanValue() else (pc.getApplicationContext() as ApplicationContextSupport).getBufferOutput()
    }

    @Override
    fun getReturnType(): Int {
        return properties!!.getReturnType()
    }

    @Override
    fun getReturnTypeAsString(): String? {
        return properties!!.getReturnTypeAsString()
    }

    @Override
    fun getDescription(): String? {
        return properties!!.getDescription()
    }

    @Override
    fun getReturnFormat(): Int {
        return if (properties!!.getReturnFormat() < 0) UDF.RETURN_FORMAT_WDDX else properties!!.getReturnFormat()
    }

    @Override
    fun getReturnFormat(defaultValue: Int): Int {
        return if (properties!!.getReturnFormat() < 0) defaultValue else properties!!.getReturnFormat()
    }

    fun getReturnFormatAsString(): String? {
        return properties!!.getReturnFormatAsString()
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput?) {
        // access
        setAccess(`in`.readInt())

        // properties
        properties = `in`.readObject()
    }

    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput?) {
        // access
        out.writeInt(getAccess())

        // properties
        out.writeObject(properties)
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        return if (obj !is UDF) false else equals(this, obj as UDF?)
    }

    @Override
    fun getIndex(): Int {
        return properties!!.getIndex()
    }

    @Override
    fun id(): String? {
        return properties!!.id()
    }

    @Override
    fun getPageSource(): PageSource? {
        return properties!!.getPageSource()
    }

    companion object {
        private const val serialVersionUID = -7288148349256615519L // do not change
        fun toKey(obj: Object?): Collection.Key? {
            if (obj == null) return null
            if (obj is Collection.Key) return obj as Collection.Key?
            val str: String = Caster.toString(obj, null) ?: return KeyImpl.init(obj.toString())
            return KeyImpl.init(str)
        }

        fun equals(left: UDF?, right: UDF?): Boolean {
            // print.e(left.getFunctionName()+":"+right.getFunctionName());
            if (!left.id().equals(right.id()) || !_eq(left.getFunctionName(), right.getFunctionName()) || left.getAccess() !== right.getAccess() || !_eq(left.getFunctionName(), right.getFunctionName()) || left.getOutput() !== right.getOutput() || left.getReturnFormat() !== right.getReturnFormat() || left.getReturnType() !== right.getReturnType() || !_eq(left.getReturnTypeAsString(), right.getReturnTypeAsString())
                    || !_eq(left.getSecureJson(), right.getSecureJson()) || !_eq(left.getVerifyClient(), right.getVerifyClient())) return false

            // Arguments
            val largs: Array<FunctionArgument?> = left.getFunctionArguments()
            val rargs: Array<FunctionArgument?> = right.getFunctionArguments()
            if (largs.size != rargs.size) return false
            for (i in largs.indices) {
                if (!largs[i].equals(rargs[i])) return false
            }
            return true
        }

        private fun _eq(left: Object?, right: Object?): Boolean {
            return if (left == null) right == null else left.equals(right)
        }
    }
}