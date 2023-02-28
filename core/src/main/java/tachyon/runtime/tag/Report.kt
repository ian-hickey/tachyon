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

import tachyon.runtime.exp.TagNotSupported

/**
 * Runs a predefined Crystal Reports report.
 *
 *
 *
 */
class Report : BodyTagImpl() {
    private var template: String? = null
    private var format: String? = null
    private var name: String? = null
    private var filename: String? = null
    private var query: String? = null
    private var overwrite = false
    private var encryption: String? = null
    private var ownerpassword: String? = null
    private var userpassword: String? = null
    private var permissions: String? = null
    private var datasource: String? = null
    private var type: String? = null
    private var timeout = 0.0
    private var password: String? = null
    private var orderby: String? = null
    private var report: String? = null
    private var username: String? = null
    private var formula: String? = null

    /**
     * set the value password
     *
     * @param password value to set
     */
    fun setPassword(password: String?) {
        this.password = password
    }

    /**
     * set the value orderby Orders results according to your specifications.
     *
     * @param orderby value to set
     */
    fun setOrderby(orderby: String?) {
        this.orderby = orderby
    }

    /**
     * set the value report
     *
     * @param report value to set
     */
    fun setReport(report: String?) {
        this.report = report
    }

    /**
     * set the value username
     *
     * @param username value to set
     */
    fun setUsername(username: String?) {
        this.username = username
    }

    /**
     * set the value formula Specifies one or more named formulas. Terminate each formula specification
     * with a semicolon.
     *
     * @param formula value to set
     */
    fun setFormula(formula: String?) {
        this.formula = formula
    }

    @Override
    fun doStartTag(): Int {
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        return SKIP_BODY
    }

    @Override
    fun release() {
        super.release()
        password = ""
        orderby = ""
        report = ""
        username = ""
        formula = ""
        template = ""
        format = ""
        name = ""
        filename = ""
        query = ""
        overwrite = false
        encryption = ""
        ownerpassword = ""
        userpassword = ""
        permissions = ""
        datasource = ""
        type = ""
        timeout = 0.0
    }

    fun addReportParam(param: ReportParamBean?) {
        // TODO Auto-generated method stub
    }

    /**
     * constructor for the tag class
     *
     * @throws TagNotSupported
     */
    init {
        // TODO implement tag
        throw TagNotSupported("report")
    }
}