/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
package tachyon.runtime.type

import java.io.IOException

// implements Supplier, BooleanSupplier, DoubleSupplier, IntSupplier, LongSupplier
abstract class EnvUDF : UDFImpl {
    protected var variables: Variables? = null
    protected var applicationContext: ApplicationContext? = null

    constructor() : super() { // needed for externalize
    }

    internal constructor(properties: UDFProperties?) : super(properties) {
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc.undefinedScope().getCheckArguments()) {
            variables = ClosureScope(pc, pc.argumentsScope(), pc.localScope(), pc.variablesScope())
        } else {
            variables = pc.variablesScope()
            variables.setBind(true)
        }
        applicationContext = pc.getApplicationContext()
    }

    internal constructor(properties: UDFProperties?, variables: Variables?) : super(properties) {
        this.variables = variables
    }

    @Override
    override fun duplicate(c: Component?): UDF? {
        return _duplicate(c)
    }

    abstract fun _duplicate(c: Component?): UDF?

    @Override
    @Throws(PageException::class)
    override fun callWithNamedValues(pc: PageContext?, calledName: Collection.Key?, values: Struct?, doIncludePath: Boolean): Object? {
        val parent: Variables = pc.variablesScope()
        var orgAC: ApplicationContext? = null
        if ((pc as PageContextImpl?).isDummy()) {
            orgAC = pc.getApplicationContext()
            pc.setApplicationContext(applicationContext)
        }
        return try {
            if (parent !== variables) pc.setVariablesScope(variables)
            super.callWithNamedValues(pc, calledName, values, doIncludePath)
        } finally {
            if (parent !== variables) pc.setVariablesScope(parent)
            if (orgAC != null) pc.setApplicationContext(orgAC)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun callWithNamedValues(pc: PageContext?, values: Struct?, doIncludePath: Boolean): Object? {
        val parent: Variables = pc.variablesScope()
        var orgAC: ApplicationContext? = null
        if ((pc as PageContextImpl?).isDummy()) {
            orgAC = pc.getApplicationContext()
            pc.setApplicationContext(applicationContext)
        }
        return try {
            if (parent !== variables) pc.setVariablesScope(variables)
            super.callWithNamedValues(pc, values, doIncludePath)
        } finally {
            if (parent !== variables) pc.setVariablesScope(parent)
            if (orgAC != null) pc.setApplicationContext(orgAC)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun call(pc: PageContext?, calledName: Collection.Key?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        val parent: Variables = pc.variablesScope()
        var orgAC: ApplicationContext? = null
        if ((pc as PageContextImpl?).isDummy()) {
            orgAC = pc.getApplicationContext()
            pc.setApplicationContext(applicationContext)
        }
        return try {
            if (parent !== variables) pc.setVariablesScope(variables)
            super.call(pc, calledName, args, doIncludePath)
        } finally {
            if (parent !== variables) pc.setVariablesScope(parent)
            if (orgAC != null) pc.setApplicationContext(orgAC)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun call(pc: PageContext?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        val parent: Variables = pc.variablesScope()
        var orgAC: ApplicationContext? = null
        if ((pc as PageContextImpl?).isDummy()) {
            orgAC = pc.getApplicationContext()
            pc.setApplicationContext(applicationContext)
        }
        return try {
            if (parent !== variables) pc.setVariablesScope(variables)
            super.call(pc, args, doIncludePath)
        } finally {
            if (parent !== variables) pc.setVariablesScope(parent)
            if (orgAC != null) pc.setApplicationContext(orgAC)
        }
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(`in`: ObjectInput?) {
        variables = `in`.readObject() as Variables
        super.readExternal(`in`)
    }

    @Override
    @Throws(IOException::class)
    override fun writeExternal(out: ObjectOutput?) {
        out.writeObject(ClosureScope.prepare(variables))
        super.writeExternal(out)
    }

    @Override
    override fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return _toDumpData(pageContext, maxlevel, dp)
    }

    abstract fun _toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData?

    @Override
    @Throws(PageException::class)
    override fun getMetaData(pc: PageContext?): Struct? {
        return _getMetaData(pc)
    }

    @Throws(PageException::class)
    abstract fun _getMetaData(pc: PageContext?): Struct?

    companion object {
        private const val serialVersionUID = -7200106903813254844L // do not change
    }
}