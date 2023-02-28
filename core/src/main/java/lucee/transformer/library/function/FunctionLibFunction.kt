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
package lucee.transformer.library.function

import java.io.IOException

/**
 * Eine FunctionLibFunction repraesentiert eine einzelne Funktion innerhalb einer FLD.
 */
class FunctionLibFunction {
    private var functionLib: FunctionLib? = null
    private var name: String? = null
    private val argument: ArrayList<FunctionLibFunctionArg?>? = ArrayList<FunctionLibFunctionArg?>()
    private var argMin = 0
    private var argMax = -1
    private var argType = ARG_FIX
    private var strReturnType: String? = null
    private val clazz: ClassDefinition? = null
    private var description: String? = null
    private var hasDefaultValues = false
    private var eval: FunctionEvaluator? = null
    private var tteCD: ClassDefinition? = null
    private var status: Short = TagLib.STATUS_IMPLEMENTED
    private var memberNames: Array<String?>?
    private var memberPosition = 1
    private var memberType: Short = CFTypes.TYPE_UNKNOW
    private var memberChaining = false
    private var bif: BIF? = null
    private var keywords: Array<String?>?
    private var functionCD: ClassDefinition? = null
    private var introduced: Version? = null
    private val core: Boolean

    /**
     * Geschuetzer Konstruktor ohne Argumente.
     */
    /*
	 * public FunctionLibFunction() { this.core=false; }
	 */
    constructor(core: Boolean) {
        this.core = core
    }

    constructor(functionLib: FunctionLib?, core: Boolean) {
        this.functionLib = functionLib
        this.core = core
    }

    /**
     * Gibt den Namen der Funktion zurueck.
     *
     * @return name Name der Funktion.
     */
    fun getName(): String? {
        return name
    }

    /**
     * Gibt alle Argumente einer Funktion als ArrayList zurueck.
     *
     * @return Argumente der Funktion.
     */
    fun getArg(): ArrayList<FunctionLibFunctionArg?>? {
        return argument
    }

    /**
     * Gibt zurueck wieviele Argumente eine Funktion minimal haben muss.
     *
     * @return Minimale Anzahl Argumente der Funktion.
     */
    fun getArgMin(): Int {
        return argMin
    }

    /**
     * Gibt zurueck wieviele Argumente eine Funktion minimal haben muss.
     *
     * @return Maximale Anzahl Argumente der Funktion.
     */
    fun getArgMax(): Int {
        return argMax
    }

    /**
     * @return the status
     * (TagLib.,TagLib.STATUS_IMPLEMENTED,TagLib.STATUS_DEPRECATED,TagLib.STATUS_UNIMPLEMENTED)
     */
    fun getStatus(): Short {
        return status
    }

    /**
     * @param status the status to set
     * (TagLib.,TagLib.STATUS_IMPLEMENTED,TagLib.STATUS_DEPRECATED,TagLib.STATUS_UNIMPLEMENTED)
     */
    fun setStatus(status: Short) {
        this.status = status
    }

    /**
     * Gibt die argument art zurueck.
     *
     * @return argument art
     */
    fun getArgType(): Int {
        return argType
    }

    /**
     * Gibt die argument art als String zurueck.
     *
     * @return argument art
     */
    fun getArgTypeAsString(): String? {
        return if (argType == ARG_DYNAMIC) "dynamic" else "fixed"
    }

    /**
     * Gibt zurueck von welchem Typ der Rueckgabewert dieser Funktion sein muss (query, string, struct,
     * number usw.).
     *
     * @return Typ des Rueckgabewert.
     */
    fun getReturnTypeAsString(): String? {
        return strReturnType
    }

    /**
     * Gibt die Klasse zurueck, welche diese Funktion implementiert.
     *
     * @return Klasse der Function.
     * @throws ClassException
     */
    fun getFunctionClassDefinition(): ClassDefinition? {
        return functionCD
    }

    /**
     * Gibt die Beschreibung der Funktion zurueck.
     *
     * @return String
     */
    fun getDescription(): String? {
        return description
    }

    /**
     * Gibt die FunctionLib zurueck, zu der die Funktion gehoert.
     *
     * @return Zugehoerige FunctionLib.
     */
    fun getFunctionLib(): FunctionLib? {
        return functionLib
    }

    /**
     * Setzt den Namen der Funktion.
     *
     * @param name Name der Funktion.
     */
    fun setName(name: String?) {
        this.name = name.toLowerCase()
    }

    /**
     * Fuegt der Funktion ein Argument hinzu.
     *
     * @param arg Argument zur Funktion.
     */
    fun addArg(arg: FunctionLibFunctionArg?) {
        arg!!.setFunction(this)
        argument.add(arg)
        if (arg!!.getDefaultValue() != null) hasDefaultValues = true
    }

    /**
     * Fuegt der Funktion ein Argument hinzu, alias fuer addArg.
     *
     * @param arg Argument zur Funktion.
     */
    fun setArg(arg: FunctionLibFunctionArg?) {
        addArg(arg)
    }

    /**
     * Setzt wieviele Argumente eine Funktion minimal haben muss.
     *
     * @param argMin Minimale Anzahl Argumente der Funktion.
     */
    fun setArgMin(argMin: Int) {
        this.argMin = argMin
    }

    /**
     * Setzt wieviele Argumente eine Funktion minimal haben muss.
     *
     * @param argMax Maximale Anzahl Argumente der Funktion.
     */
    fun setArgMax(argMax: Int) {
        this.argMax = argMax
    }

    /**
     * Setzt den Rueckgabewert der Funktion (query,array,string usw.)
     *
     * @param value
     */
    fun setReturn(value: String?) {
        strReturnType = value
    }

    /**
     * Setzt die Klassendefinition als Zeichenkette, welche diese Funktion implementiert.
     *
     * @param value Klassendefinition als Zeichenkette.
     */
    fun setFunctionClass(value: String?, id: Identification?, attrs: Map<String?, String?>?) {
        functionCD = ClassDefinitionImpl.toClassDefinition(value, id, attrs)
    }

    fun setFunctionClass(cd: ClassDefinition?) {
        functionCD = cd
    }

    /**
     * Setzt die Beschreibung der Funktion.
     *
     * @param description Beschreibung der Funktion.
     */
    fun setDescription(description: String?) {
        this.description = description
    }

    /**
     * Setzt die zugehoerige FunctionLib.
     *
     * @param functionLib Zugehoerige FunctionLib.
     */
    fun setFunctionLib(functionLib: FunctionLib?) {
        this.functionLib = functionLib
    }

    /**
     * sets the argument type of the function
     *
     * @param argType
     */
    fun setArgType(argType: Int) {
        this.argType = argType
    }

    fun getHash(): String? {
        val sb = StringBuilder()
        sb.append(getArgMax())
        sb.append(getArgMin())
        sb.append(getArgType())
        sb.append(getArgTypeAsString())
        sb.append(getFunctionClassDefinition().toString())
        sb.append(tteCD)
        sb.append(getName())
        sb.append(getReturnTypeAsString())
        val it: Iterator<FunctionLibFunctionArg?> = getArg().iterator()
        var arg: FunctionLibFunctionArg?
        while (it.hasNext()) {
            arg = it.next()
            sb.append(arg!!.getHash())
        }
        return try {
            Md5.getDigestAsString(sb.toString())
        } catch (e: IOException) {
            ""
        }
    }

    fun hasDefaultValues(): Boolean {
        return hasDefaultValues
    }

    fun hasTteClass(): Boolean {
        return tteCD != null
    }

    @Throws(TemplateException::class)
    fun getEvaluator(): FunctionEvaluator? {
        if (!hasTteClass()) return null
        if (eval != null) return eval
        eval = try {
            tteCD.getClazz().newInstance() as FunctionEvaluator
        } catch (e: Exception) {
            throw TemplateException(e.getMessage())
        }
        return eval
    }

    fun setTTEClass(tteClass: String?, id: Identification?, attrs: Map<String?, String?>?) {
        tteCD = ClassDefinitionImpl.toClassDefinition(tteClass, id, attrs)
    }

    fun setMemberName(memberNames: String?) {
        if (StringUtil.isEmpty(memberNames, true)) return
        this.memberNames = ListUtil.trimItems(ListUtil.listToStringArray(memberNames, ','))
    }

    fun getMemberNames(): Array<String?>? {
        return memberNames
    }

    fun setKeywords(keywords: String?) {
        this.keywords = ListUtil.trimItems(ListUtil.listToStringArray(keywords, ','))
    }

    fun getKeywords(): Array<String?>? {
        return keywords
    }

    fun isCore(): Boolean {
        return core
    }

    fun setMemberPosition(pos: Int) {
        memberPosition = pos
    }

    fun getMemberPosition(): Int {
        return memberPosition
    }

    fun setMemberChaining(memberChaining: Boolean) {
        this.memberChaining = memberChaining
    }

    fun getMemberChaining(): Boolean {
        return memberChaining
    }

    fun setMemberType(memberType: String?) {
        this.memberType = CFTypes.toShortStrict(memberType, CFTypes.TYPE_UNKNOW)
    }

    fun getMemberType(): Short {
        if (memberNames != null && memberType == CFTypes.TYPE_UNKNOW) {
            val args: ArrayList<FunctionLibFunctionArg?>? = getArg()
            if (args.size() >= 1) {
                memberType = CFTypes.toShortStrict(args.get(getMemberPosition() - 1).getTypeAsString(), CFTypes.TYPE_UNKNOW)
            }
        }
        return memberType
    }

    fun getMemberTypeAsString(): String? {
        return CFTypes.toString(getMemberType(), "any")
    }

    fun getBIF(): BIF? {
        if (bif != null) return bif
        var clazz: Class? = null
        clazz = try {
            getFunctionClassDefinition().getClazz()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw PageRuntimeException(Caster.toPageException(t))
        }
        bif = if (Reflector.isInstaneOf(clazz, BIF::class.java, false)) {
            try {
                ClassUtil.newInstance(clazz) as BIF
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                throw RuntimeException(t)
            }
        } else {
            return BIFProxy(clazz)
        }
        return bif
    }

    fun setIntroduced(introduced: String?) {
        this.introduced = OSGiUtil.toVersion(introduced, null)
    }

    fun getIntroduced(): Version? {
        return introduced
    }

    companion object {
        /**
         * Dynamischer Argument Typ
         */
        const val ARG_DYNAMIC = 0

        /**
         * statischer Argument Typ
         */
        const val ARG_FIX = 1
    }
}