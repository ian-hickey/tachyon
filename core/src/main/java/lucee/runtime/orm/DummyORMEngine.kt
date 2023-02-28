/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.orm

import lucee.runtime.PageContext

class DummyORMEngine : ORMEngine {
    @get:Override
    val label: String?
        get() = "No ORM Engine Installed"

    @get:Override
    val mode: Int
        get() = ORMEngine.MODE_STRICT

    @Override
    @Throws(PageException::class)
    fun createSession(pc: PageContext?): ORMSession? {
        throw notInstalledEL()
    }

    @Override
    @Throws(PageException::class)
    fun init(pc: PageContext?) {
    }

    @Override
    fun getConfiguration(pc: PageContext?): ORMConfiguration? {
        throw notInstalledEL()
    }

    @Override
    @Throws(PageException::class)
    fun reload(pc: PageContext?, force: Boolean): Boolean {
        throw notInstalledEL()
    }

    private fun notInstalled(pc: PageContext?): PageException? {
        return ApplicationException("No ORM Engine installed!", "Check out the Extension Store in the Lucee Administrator for \"ORM\".")
    }

    private fun notInstalledEL(): PageRuntimeException? {
        return PageRuntimeException(notInstalled(null))
    }

    companion object {
        private val HIBERNATE: String? = "FAD1E8CB-4F45-4184-86359145767C29DE"
        private const val tryToInstall = true
    }
}