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

import java.io.IOException

class UDFPropertiesImpl : UDFPropertiesBase {
    var functionName: String? = null
    var returnType = 0
    var strReturnType: String? = null
    var output = false
    var bufferOutput: Boolean? = null
    var hint: String? = null
    var displayName: String? = null
    var index = 0
    var arguments: Array<FunctionArgument?>?
    var meta: Struct? = null
    var description: String? = null
    var secureJson: Boolean? = null
    var verifyClient: Boolean? = null
    var strReturnFormat: String? = null
    var returnFormat = 0
    var argumentsSet: Set<Collection.Key?>? = null
    var access = 0
    var cachedWithin: Object? = null
    var localMode: Integer? = null
    var modifier = 0

    /**
     * NEVER USE THIS CONSTRUCTOR, this constructor is only for deserialize this object from stream
     */
    constructor() {}
    constructor(page: Page?, pageSource: PageSource?, startLine: Int, endLine: Int, arguments: Array<FunctionArgument?>?, index: Int, functionName: String?, strReturnType: String?,
                strReturnFormat: String?, output: Boolean, access: Int, bufferOutput: Boolean?, displayName: String?, description: String?, hint: String?, secureJson: Boolean?, verifyClient: Boolean?,
                cachedWithin: Object?, localMode: Integer?, modifier: Int, meta: StructImpl?) : this(page, pageSource, startLine, endLine, arguments, index, functionName, CFTypes.toShortStrict(strReturnType, CFTypes.TYPE_UNKNOW), strReturnType, strReturnFormat,
            output, access, bufferOutput, displayName, description, hint, secureJson, verifyClient, cachedWithin, localMode, modifier, meta) {
    }

    constructor(page: Page?, pageSource: PageSource?, startLine: Int, endLine: Int, arguments: Array<FunctionArgument?>?, index: Int, functionName: String?, returnType: Short,
                strReturnFormat: String?, output: Boolean, access: Int, bufferOutput: Boolean?, displayName: String?, description: String?, hint: String?, secureJson: Boolean?, verifyClient: Boolean?,
                cachedWithin: Object?, localMode: Integer?, modifier: Int, meta: StructImpl?) : this(page, pageSource, startLine, endLine, arguments, index, functionName, returnType, CFTypes.toString(returnType, "any"), strReturnFormat, output, access, bufferOutput,
            displayName, description, hint, secureJson, verifyClient, cachedWithin, localMode, modifier, meta) {
    }

    constructor(page: Page?, pageSource: PageSource?, startLine: Int, endLine: Int, arguments: Array<FunctionArgument?>?, index: Int, functionName: String?, returnType: Short,
                strReturnFormat: String?, output: Boolean, access: Int) : this(page, pageSource, startLine, endLine, arguments, index, functionName, returnType, CFTypes.toString(returnType, "any"), strReturnFormat, output, access, null, "", "",
            "", null, null, null, null, Component.MODIFIER_NONE, null) {
    }

    private constructor(page: Page?, pageSource: PageSource?, startLine: Int, endLine: Int, arguments: Array<FunctionArgument?>?, index: Int, functionName: String?, returnType: Short,
                        strReturnType: String?, strReturnFormat: String?, output: Boolean, access: Int, bufferOutput: Boolean?, displayName: String?, description: String?, hint: String?, secureJson: Boolean?,
                        verifyClient: Boolean?, cachedWithin: Object?, localMode: Integer?, modifier: Int, meta: StructImpl?) : super(page, pageSource, startLine, endLine) {
        // this happens when an active is based on older source code
        if (arguments!!.size > 0) {
            argumentsSet = HashSet<Collection.Key?>()
            for (i in arguments.indices) {
                argumentsSet.add(arguments[i].getName())
            }
        } else argumentsSet = null
        this.arguments = arguments
        this.description = description
        this.displayName = displayName
        this.functionName = functionName
        this.hint = hint
        this.index = index
        this.meta = meta
        this.output = output
        this.bufferOutput = bufferOutput
        this.strReturnType = strReturnType
        this.returnType = returnType.toInt()
        this.strReturnFormat = strReturnFormat
        returnFormat = UDFUtil.toReturnFormat(strReturnFormat, -1)
        this.secureJson = secureJson
        this.verifyClient = verifyClient
        this.access = access
        this.cachedWithin = if (cachedWithin is Long) TimeSpanImpl.fromMillis((cachedWithin as Long?).longValue()) else cachedWithin
        this.localMode = localMode
        this.modifier = modifier
    }

    @Override
    fun getAccess(): Int {
        return access
    }

    @Override
    fun getModifier(): Int {
        return modifier
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput?) {
        try {
            val sm: SerMapping = `in`.readObject() as SerMapping
            val mapping: Mapping? = if (sm == null) null else sm.toMapping()
            val relPath: String = ExternalizableUtil.readString(`in`)
            val relPathwV: String = ExternalizableUtil.readString(`in`)
            val pc: PageContextImpl = ThreadLocalPageContext.get() as PageContextImpl
            val cw: ConfigPro = ThreadLocalPageContext.getConfig(pc) as ConfigPro
            ps = toPageSource(pc, cw, mapping, relPath, relPathwV)
        } catch (e: Exception) {
            throw ExceptionUtil.toIOException(e)
        }
        startLine = `in`.readInt()
        endLine = `in`.readInt()
        arguments = `in`.readObject()
        access = `in`.readInt()
        index = `in`.readInt()
        returnFormat = `in`.readInt()
        returnType = `in`.readInt()
        description = ExternalizableUtil.readString(`in`)
        displayName = ExternalizableUtil.readString(`in`)
        functionName = ExternalizableUtil.readString(`in`)
        hint = ExternalizableUtil.readString(`in`)
        meta = `in`.readObject() as Struct
        output = `in`.readBoolean()
        bufferOutput = ExternalizableUtil.readBoolean(`in`)
        secureJson = ExternalizableUtil.readBoolean(`in`)
        strReturnFormat = ExternalizableUtil.readString(`in`)
        strReturnType = ExternalizableUtil.readString(`in`)
        verifyClient = ExternalizableUtil.readBoolean(`in`)
        cachedWithin = StringUtil.emptyAsNull(ExternalizableUtil.readString(`in`), true)
        val tmp: Int = `in`.readInt()
        localMode = if (tmp == -1) null else tmp
        if (arguments != null && arguments!!.size > 0) {
            argumentsSet = HashSet<Collection.Key?>()
            for (i in arguments.indices) {
                argumentsSet.add(arguments!![i].getName())
            }
        }
    }

    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput?) {
        val m: Mapping = getPageSource().getMapping()
        val c: Config = m.getConfig()
        var sm: SerMapping? = null
        if (c is ConfigWebPro) {
            val cwi: ConfigWebPro = c as ConfigWebPro
            if (m is MappingImpl && cwi.isApplicationMapping(m)) {
                sm = (m as MappingImpl).toSerMapping()
            }
        }
        out.writeObject(sm)
        out.writeObject(getPageSource().getRealpath())
        out.writeObject(getPageSource().getRealpathWithVirtual())
        out.writeInt(getStartLine())
        out.writeInt(getEndLine())
        out.writeObject(arguments)
        out.writeInt(access)
        out.writeInt(index)
        out.writeInt(returnFormat)
        out.writeInt(returnType)
        ExternalizableUtil.writeString(out, description)
        ExternalizableUtil.writeString(out, displayName)
        ExternalizableUtil.writeString(out, functionName)
        ExternalizableUtil.writeString(out, hint)
        out.writeObject(meta)
        out.writeBoolean(output)
        ExternalizableUtil.writeBoolean(out, bufferOutput)
        ExternalizableUtil.writeBoolean(out, secureJson)
        ExternalizableUtil.writeString(out, strReturnFormat)
        ExternalizableUtil.writeString(out, strReturnType)
        ExternalizableUtil.writeBoolean(out, verifyClient)
        ExternalizableUtil.writeString(out, Caster.toString(cachedWithin, null))
        out.writeInt(if (localMode == null) -1 else localMode.intValue())
    }

    @Override
    override fun getFunctionName(): String? {
        return functionName
    }

    @Override
    override fun getOutput(): Boolean {
        return output
    }

    @Override
    override fun getBufferOutput(): Boolean? {
        return bufferOutput
    }

    @Override
    override fun getReturnType(): Int {
        return returnType
    }

    @Override
    override fun getReturnTypeAsString(): String? {
        return strReturnType
    }

    @Override
    override fun getDescription(): String? {
        return description
    }

    @Override
    override fun getReturnFormat(): Int {
        return returnFormat
    }

    @Override
    override fun getReturnFormatAsString(): String? {
        return strReturnFormat
    }

    @Override
    override fun getIndex(): Int {
        return index
    }

    @Override
    override fun getCachedWithin(): Object? {
        return cachedWithin
    }

    @Override
    override fun getSecureJson(): Boolean? {
        return secureJson
    }

    @Override
    override fun getVerifyClient(): Boolean? {
        return verifyClient
    }

    @Override
    override fun getFunctionArguments(): Array<FunctionArgument?>? {
        return arguments
    }

    @Override
    override fun getDisplayName(): String? {
        return displayName
    }

    @Override
    override fun getHint(): String? {
        return hint
    }

    @Override
    override fun getMeta(): Struct? {
        return meta
    }

    @Override
    override fun getLocalMode(): Integer? {
        return localMode
    }

    @Override
    override fun getArgumentsSet(): Set<Key?>? {
        return argumentsSet
    }

    companion object {
        private const val serialVersionUID = 8679484452640746605L // do not change
        @Throws(PageException::class)
        fun toPageSource(pc: PageContextImpl?, config: ConfigPro?, mapping: Mapping?, relPath: String?, relPathwV: String?): PageSource? {
            if (mapping != null) return mapping.getPageSource(relPath)
            val ps: PageSource = PageSourceImpl.best(config.getPageSources(pc, null, relPathwV, false, true, true))
            if (ps != null) return ps
            throw ApplicationException("File [$relPath] not found")
        }
    }
}