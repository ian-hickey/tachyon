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
package tachyon.runtime.op

import java.util.Date

/**
 * oimplementation of interface Operation
 */
class OperationImpl : Operation {
    @Override
    fun compare(left: Boolean, right: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), if (left) Boolean.TRUE else Boolean.FALSE, if (right) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    fun compare(left: Boolean, right: Date?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), if (left) Boolean.TRUE else Boolean.FALSE, right)
    }

    @Override
    fun compare(left: Boolean, right: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), if (left) Boolean.TRUE else Boolean.FALSE, Double.valueOf(right))
    }

    @Override
    @Throws(PageException::class)
    fun compare(left: Boolean, right: Object?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), if (left) Boolean.TRUE else Boolean.FALSE, right)
    }

    @Override
    fun compare(left: Boolean, right: String?): Int {
        return try {
            OpUtil.compare(ThreadLocalPageContext.get(), if (left) Boolean.TRUE else Boolean.FALSE, right)
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        }
    }

    @Override
    fun compare(left: Date?, right: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), left, if (right) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    fun compare(left: Date?, right: Date?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), left, right)
    }

    @Override
    fun compare(left: Date?, right: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), left, Double.valueOf(right))
    }

    @Override
    @Throws(PageException::class)
    fun compare(left: Date?, right: Object?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), left, right)
    }

    @Override
    @Throws(PageException::class)
    fun compare(left: Date?, right: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), left, right)
    }

    @Override
    fun compare(left: Double, right: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(left), if (right) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    fun compare(left: Double, right: Date?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(left), right)
    }

    @Override
    fun compare(left: Double, right: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(left), Double.valueOf(right))
    }

    @Override
    @Throws(PageException::class)
    fun compare(left: Double, right: Object?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(left), right)
    }

    @Override
    fun compare(left: Double, right: String?): Int { // FUTURE add throws PageException also to other below
        return try {
            OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(left), right)
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    fun compare(left: Object?, right: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), left, if (right) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    fun compare(left: Object?, right: Date?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), left, right)
    }

    @Override
    @Throws(PageException::class)
    fun compare(left: Object?, right: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), left, Double.valueOf(right))
    }

    @Override
    @Throws(PageException::class)
    fun compare(left: Object?, right: Object?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), left, right)
    }

    @Override
    @Throws(PageException::class)
    fun compare(left: Object?, right: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), left, right)
    }

    @Override
    fun compare(left: String?, right: Boolean): Int {
        return try {
            OpUtil.compare(ThreadLocalPageContext.get(), left, if (right) Boolean.TRUE else Boolean.FALSE)
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    fun compare(left: String?, right: Date?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), left, right)
    }

    @Override
    fun compare(left: String?, right: Double): Int {
        return try {
            OpUtil.compare(ThreadLocalPageContext.get(), left, Double.valueOf(right))
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    fun compare(left: String?, right: Object?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), left, right)
    }

    @Override
    fun compare(left: String?, right: String?): Int {
        return try {
            OpUtil.compare(ThreadLocalPageContext.get(), left, right)
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        }
    }

    @Override
    fun concat(left: String?, right: String?): String? {
        return left.concat(right)
    }

    @Override
    @Throws(PageException::class)
    fun ct(left: Object?, right: Object?): Boolean {
        return OpUtil.ct(ThreadLocalPageContext.get(), left, right)
    }

    @Override
    fun divide(left: Double, right: Double): Double {
        return OpUtil.divide(ThreadLocalPageContext.get(), Double.valueOf(left), Double.valueOf(right))
    }

    @Override
    @Throws(PageException::class)
    fun equals(left: Object?, right: Object?, caseSensitive: Boolean): Boolean {
        return OpUtil.equals(ThreadLocalPageContext.get(), left, right, caseSensitive)
    }

    @Override
    @Throws(PageException::class)
    fun eqv(left: Object?, right: Object?): Boolean {
        return OpUtil.eqv(ThreadLocalPageContext.get(), left, right)
    }

    @Override
    @Throws(PageException::class)
    fun exponent(left: Object?, right: Object?): Double {
        return OpUtil.exponent(ThreadLocalPageContext.get(), left, right)
    }

    @Override
    @Throws(PageException::class)
    fun imp(left: Object?, right: Object?): Boolean {
        return OpUtil.imp(ThreadLocalPageContext.get(), left, right)
    }

    @Override
    fun minus(left: Double, right: Double): Double {
        return OpUtil.minus(ThreadLocalPageContext.get(), Double.valueOf(left), Double.valueOf(right))
    }

    @Override
    fun modulus(left: Double, right: Double): Double {
        return OpUtil.modulus(ThreadLocalPageContext.get(), Double.valueOf(left), Double.valueOf(right))
    }

    @Override
    fun multiply(left: Double, right: Double): Double {
        return OpUtil.multiply(ThreadLocalPageContext.get(), Double.valueOf(left), Double.valueOf(right))
    }

    @Override
    @Throws(PageException::class)
    fun nct(left: Object?, right: Object?): Boolean {
        return OpUtil.nct(ThreadLocalPageContext.get(), left, right)
    }

    @Override
    fun plus(left: Double, right: Double): Double {
        return OpUtil.plus(ThreadLocalPageContext.get(), Double.valueOf(left), Double.valueOf(right))
    }

    @Override
    fun equalsComplexEL(left: Object?, right: Object?, caseSensitive: Boolean, checkOnlyPublicAppearance: Boolean): Boolean {
        return OpUtil.equalsComplexEL(ThreadLocalPageContext.get(), left, right, caseSensitive, checkOnlyPublicAppearance)
    }

    @Override
    @Throws(PageException::class)
    fun equalsComplex(left: Object?, right: Object?, caseSensitive: Boolean, checkOnlyPublicAppearance: Boolean): Boolean {
        return OpUtil.equalsComplex(ThreadLocalPageContext.get(), left, right, caseSensitive, checkOnlyPublicAppearance)
    }

    companion object {
        private var singelton: OperationImpl? = null
        val instance: Operation?
            get() {
                if (singelton == null) singelton = OperationImpl()
                return singelton
            }
    }
}