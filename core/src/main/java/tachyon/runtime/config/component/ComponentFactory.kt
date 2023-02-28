/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.config.component

import tachyon.commons.io.res.Resource

object ComponentFactory {
    /**
     * this method deploy all components for org.tachyon.cfml
     *
     * @param dir components directory
     * @param doNew redeploy even the file exist, this is set to true when a new version is started
     */
    fun deploy(dir: Resource?, doNew: Boolean) {
        val path = "/resource/component/" + Constants.DEFAULT_PACKAGE.replace('.', '/').toString() + "/"
        delete(dir, "Base")
        deploy(dir, path, doNew, "HelperBase")
        deploy(dir, path, doNew, "Feed")
        deploy(dir, path, doNew, "Ftp")
        deploy(dir, path, doNew, "Http")
        deploy(dir, path, doNew, "Mail")
        deploy(dir, path, doNew, "Query")
        deploy(dir, path, doNew, "Result")
        deploy(dir, path, doNew, "Administrator")

        // orm
        run {
            val ormDir: Resource = dir.getRealResource("orm")
            val ormPath = path + "orm/"
            if (!ormDir.exists()) ormDir.mkdirs()
            deploy(ormDir, ormPath, doNew, "IEventHandler")
            deploy(ormDir, ormPath, doNew, "INamingStrategy")
        }
        // test
        run {
            val testDir: Resource = dir.getRealResource("test")
            val testPath = path + "test/"
            if (!testDir.exists()) testDir.mkdirs()
            deploy(testDir, testPath, doNew, "TachyonTestSuite")
            deploy(testDir, testPath, doNew, "TachyonTestSuiteRunner")
            deploy(testDir, testPath, doNew, "TachyonTestCase")
        }
    }

    private fun deploy(dir: Resource?, path: String?, doNew: Boolean, name: String?) {
        val f: Resource = dir.getRealResource(name.toString() + ".cfc")
        if (!f.exists() || doNew) ConfigFactory.createFileFromResourceEL("$path$name.cfc", f)
    }

    private fun delete(dir: Resource?, name: String?) {
        val f: Resource = dir.getRealResource(name.toString() + ".cfc")
        if (f.exists()) f.delete()
    }
}