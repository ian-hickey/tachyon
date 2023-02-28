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
package tachyon.runtime.tag

import javax.servlet.jsp.tagext.Tag

class ProcParam : TagSupport() {
    private var param: ProcParamBean? = ProcParamBean()
    @Override
    fun release() {
        param = ProcParamBean()
        super.release()
    }

    /**
     * @param cfsqltype The cfsqltype to set.
     * @throws DatabaseException
     */
    @Throws(DatabaseException::class)
    fun setCfsqltype(cfsqltype: String?) {
        param.setType(SQLCaster.toSQLType(cfsqltype))
    }

    @Throws(DatabaseException::class)
    fun setSqltype(type: String?) {
        param.setType(SQLCaster.toSQLType(type))
    }

    /**
     * @param ignoreNull The ignoreNull to set.
     */
    fun setNull(_null: Boolean) {
        param.setNull(_null)
    }

    /**
     * @param maxLength The maxLength to set.
     */
    fun setMaxlength(maxLength: Double) {
        param!!.setMaxLength(maxLength.toInt())
    }

    /**
     * @param scale The scale to set.
     */
    fun setScale(scale: Double) {
        param!!.setScale(scale.toInt())
    }

    /**
     * @param type The type to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setType(type: String?) {
        var type = type
        type = type.trim().toLowerCase()
        if ("in".equals(type)) param!!.setDirection(ProcParamBean.DIRECTION_IN) else if ("inout".equals(type)) param!!.setDirection(ProcParamBean.DIRECTION_INOUT) else if ("in_out".equals(type)) param!!.setDirection(ProcParamBean.DIRECTION_INOUT) else if ("outin".equals(type)) param!!.setDirection(ProcParamBean.DIRECTION_INOUT) else if ("out_in".equals(type)) param!!.setDirection(ProcParamBean.DIRECTION_INOUT) else if ("out".equals(type)) param!!.setDirection(ProcParamBean.DIRECTION_OUT) else throw ApplicationException("attribute type of tag procparam has an invalid value [$type], valid values are [in, out, inout]")
    }

    /**
     * @param value The value to set.
     */
    fun setValue(value: Object?) {
        param!!.setValue(value)
    }

    /**
     * @param variable The variable to set.
     */
    fun setVariable(variable: String?) {
        param!!.setVariable(variable)
    }

    fun setDbvarname(dbvarname: String?) {
        // DeprecatedUtil.tagAttribute(pageContext,"procparam","dbvarname");
    }

    @Override
    @Throws(ApplicationException::class)
    fun doStartTag(): Int {
        // check
        if (param!!.getDirection() !== ProcParamBean.DIRECTION_IN && StringUtil.isEmpty(param!!.getVariable())) throw ApplicationException("attribute variable of tag ProcParam is required, when attribute type has value \"out\" or \"inout\"")
        if (param!!.getDirection() === ProcParamBean.DIRECTION_IN && param!!.getValue() == null && !param.getNull()) throw ApplicationException("attribute value of tag ProcParam is required, when attribute type has value \"in\"")
        if (!param.getNull() && param!!.getValue() == null && param!!.getDirection() !== ProcParamBean.DIRECTION_OUT) throw ApplicationException("required attribute value is empty")
        var parent: Tag = getParent()
        while (parent != null && parent !is StoredProc) {
            parent = parent.getParent()
        }
        if (parent is StoredProc) {
            (parent as StoredProc)!!.addProcParam(param)
        } else {
            throw ApplicationException("Wrong Context, tag ProcParam must be inside a StoredProc tag")
        }
        return SKIP_BODY
    }
}