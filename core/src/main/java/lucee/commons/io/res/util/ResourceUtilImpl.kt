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
package lucee.commons.io.res.util

import java.io.IOException

class ResourceUtilImpl private constructor() : lucee.runtime.util.ResourceUtil {
    @Override
    @Throws(IOException::class)
    fun checkCopyToOK(source: Resource, target: Resource) {
        ResourceUtil.checkCopyToOK(source, target)
    }

    @Override
    @Throws(IOException::class)
    fun checkCreateDirectoryOK(resource: Resource, createParentWhenNotExists: Boolean) {
        ResourceUtil.checkCreateDirectoryOK(resource, createParentWhenNotExists)
    }

    @Override
    @Throws(IOException::class)
    fun checkCreateFileOK(resource: Resource, createParentWhenNotExists: Boolean) {
        ResourceUtil.checkCreateFileOK(resource, createParentWhenNotExists)
    }

    @Override
    @Throws(IOException::class)
    fun checkGetInputStreamOK(resource: Resource) {
        ResourceUtil.checkGetInputStreamOK(resource)
    }

    @Override
    @Throws(IOException::class)
    fun checkGetOutputStreamOK(resource: Resource) {
        ResourceUtil.checkGetOutputStreamOK(resource)
    }

    @Override
    @Throws(IOException::class)
    fun checkMoveToOK(source: Resource, target: Resource) {
        ResourceUtil.checkMoveToOK(source, target)
    }

    @Override
    @Throws(IOException::class)
    fun checkRemoveOK(resource: Resource) {
        ResourceUtil.checkRemoveOK(resource)
    }

    @Override
    @Throws(IOException::class)
    fun copyRecursive(src: Resource, trg: Resource) {
        ResourceUtil.copyRecursive(src, trg)
    }

    @Override
    @Throws(IOException::class)
    fun copyRecursive(src: Resource, trg: Resource, filter: ResourceFilter?) {
        ResourceUtil.copyRecursive(src, trg, filter)
    }

    @Override
    fun createResource(res: Resource, level: Short, type: Short): Resource? {
        return ResourceUtil.createResource(res, level, type)
    }

    @Override
    fun getExtension(res: Resource?): String {
        return ResourceUtil.getExtension(res, null)
    }

    @Override
    fun getExtension(res: Resource?, defaultValue: String?): String {
        return ResourceUtil.getExtension(res, defaultValue)
    }

    @Override
    fun getExtension(strFile: String?): String {
        return ResourceUtil.getExtension(strFile, null)
    }

    @Override
    fun getExtension(strFile: String?, defaultValue: String?): String {
        return ResourceUtil.getExtension(strFile, defaultValue)
    }

    @Override
    fun getMimeType(res: Resource?, defaultValue: String?): String {
        return ResourceUtil.getMimeType(res, defaultValue)
    }

    @Override
    fun getMimeType(barr: ByteArray?, defaultValue: String?): String {
        return IOUtil.getMimeType(barr, defaultValue)
    }

    @Override
    fun getPathToChild(file: Resource?, dir: Resource?): String? {
        return ResourceUtil.getPathToChild(file, dir)
    }

    @Override
    fun isChildOf(file: Resource?, dir: Resource?): Boolean {
        return ResourceUtil.isChildOf(file, dir)
    }

    @Override
    fun isEmpty(res: Resource): Boolean {
        return ResourceUtil.isEmpty(res)
    }

    @Override
    fun isEmptyDirectory(res: Resource): Boolean {
        return ResourceUtil.isEmptyDirectory(res, null)
    }

    @Override
    fun isEmptyFile(res: Resource): Boolean {
        return ResourceUtil.isEmptyFile(res)
    }

    @Override
    fun merge(parent: String?, child: String?): String? {
        return ResourceUtil.merge(parent, child)
    }

    @Override
    @Throws(IOException::class)
    fun moveTo(src: Resource, dest: Resource) {
        ResourceUtil.moveTo(src, dest, true)
    }

    @Override
    @Throws(IOException::class)
    fun removeChildren(res: Resource?) {
        ResourceUtil.removeChildren(res)
    }

    @Override
    @Throws(IOException::class)
    fun removeChildren(res: Resource?, filter: ResourceNameFilter?) {
        ResourceUtil.removeChildren(res, filter)
    }

    @Override
    @Throws(IOException::class)
    fun removeChildren(res: Resource?, filter: ResourceFilter?) {
        ResourceUtil.removeChildren(res, filter)
    }

    @Override
    fun removeScheme(scheme: String, path: String): String {
        return ResourceUtil.removeScheme(scheme, path)
    }

    @Override
    @Throws(IOException::class)
    fun setAttribute(res: Resource, attributes: String) {
        ResourceUtil.setAttribute(res, attributes)
    }

    // FUTURE public Resource toResourceExisting(PageContext pc, String path, Resource defaultValue)
    @Override
    @Throws(PageException::class)
    fun toResourceExisting(pc: PageContext?, path: String?): Resource {
        return ResourceUtil.toResourceExisting(pc, path)
    }

    @Override
    @Throws(PageException::class)
    fun toResourceExistingParent(pc: PageContext, destination: String): Resource? {
        return ResourceUtil.toResourceExistingParent(pc, destination)
    }

    @Override
    fun toResourceNotExisting(pc: PageContext?, destination: String?): Resource {
        return ResourceUtil.toResourceNotExisting(pc, destination)
    }

    @Override
    fun translatePath(path: String?, slashAdBegin: Boolean, slashAddEnd: Boolean): String? {
        return ResourceUtil.translatePath(path, slashAdBegin, slashAddEnd)
    }

    @Override
    fun translatePathName(path: String?): Array<String?> {
        return ResourceUtil.translatePathName(path)
    }

    @Override
    @Throws(IOException::class)
    fun toString(r: Resource?, charset: String?): String {
        return IOUtil.toString(r, charset)
    }

    @Override
    @Throws(IOException::class)
    fun toString(r: Resource?, charset: Charset?): String {
        return IOUtil.toString(r, charset)
    }

    @Override
    fun contractPath(pc: PageContext?, path: String?): String {
        return ContractPath.call(pc, path)
    }

    @get:Override
    val homeDirectory: Resource
        get() = SystemUtil.getHomeDirectory()

    @get:Override
    val systemDirectory: Resource
        get() = SystemUtil.getSystemDirectory()

    @get:Override
    val tempDirectory: Resource
        get() = SystemUtil.getTempDirectory()

    @Override
    fun parsePlaceHolder(path: String?): String {
        return SystemUtil.parsePlaceHolder(path)
    }

    @Override
    fun getExtensionResourceFilter(extension: String?, allowDir: Boolean): ResourceFilter {
        return ExtensionResourceFilter(extension, allowDir)
    }

    @Override
    fun getExtensionResourceFilter(extensions: Array<String?>?, allowDir: Boolean): ResourceFilter {
        return ExtensionResourceFilter(extensions, allowDir)
    }

    @Override
    fun getContentType(res: Resource): lucee.commons.io.res.ContentType {
        return ResourceUtil.getContentType(res)
    }

    fun getContentType(res: Resource, defaultValue: lucee.commons.io.res.ContentType): lucee.commons.io.res.ContentType {
        return ResourceUtil.getContentType(res, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toResourceExistingParent(pc: PageContext, destination: String, allowRealpath: Boolean): Resource? {
        return ResourceUtil.toResourceExistingParent(pc, destination, allowRealpath)
    }

    @Override
    fun toResourceNotExisting(pc: PageContext, destination: String, allowRealpath: Boolean, checkComponentMappings: Boolean): Resource? {
        return ResourceUtil.toResourceNotExisting(pc, destination, allowRealpath, checkComponentMappings)
    }

    @Override
    fun isUNCPath(path: String): Boolean {
        return ResourceUtil.isUNCPath(path)
    }

    @Override
    fun toExactResource(res: Resource): Resource {
        return ResourceUtil.toExactResource(res)
    }

    @Override
    fun prettifyPath(path: String?): String? {
        return ResourceUtil.prettifyPath(path)
    }

    @Override
    fun getCanonicalPathSilent(res: Resource?): String {
        return ResourceUtil.getCanonicalPathEL(res)
    }

    @Override
    fun getCanonicalResourceSilent(res: Resource): Resource {
        return ResourceUtil.getCanonicalResourceEL(res)
    }

    @Override
    fun createNewResourceSilent(res: Resource): Boolean {
        return ResourceUtil.createNewResourceEL(res)
    }

    @Override
    @Throws(IOException::class)
    fun touch(res: Resource?) {
        ResourceUtil.touch(res)
    }

    @Override
    @Throws(IOException::class)
    fun clear(res: Resource) {
        ResourceUtil.clear(res)
    }

    @Override
    fun changeExtension(file: Resource, newExtension: String): Resource {
        return ResourceUtil.changeExtension(file, newExtension)
    }

    @Override
    fun deleteContent(src: Resource, filter: ResourceFilter?) {
        ResourceUtil.deleteContent(src, filter)
    }

    @Override
    @Throws(IOException::class)
    fun copy(src: Resource, trg: Resource) {
        ResourceUtil.copy(src, trg)
    }

    @Override
    fun removeChildrenSilent(res: Resource?, filter: ResourceNameFilter?) {
        ResourceUtil.removeChildrenEL(res, filter)
    }

    @Override
    fun removeChildrenSilent(res: Resource?, filter: ResourceFilter?) {
        ResourceUtil.removeChildrenEL(res, filter)
    }

    @Override
    fun removeChildrenSilent(res: Resource?) {
        ResourceUtil.removeChildrenEL(res)
    }

    @Override
    fun removeSilent(res: Resource, force: Boolean) {
        ResourceUtil.removeEL(res, force)
    }

    @Override
    fun createFileSilent(res: Resource, force: Boolean) {
        ResourceUtil.createFileEL(res, force)
    }

    @Override
    fun createDirectorySilent(res: Resource, force: Boolean) {
        ResourceUtil.createDirectoryEL(res, force)
    }

    @Override
    fun getRealSize(res: Resource, filter: ResourceFilter?): Long {
        return ResourceUtil.getRealSize(res, filter)
    }

    @Override
    fun getChildCount(res: Resource, filter: ResourceFilter?): Int {
        return ResourceUtil.getChildCount(res, filter)
    }

    @Override
    fun isEmptyDirectory(res: Resource, filter: ResourceFilter?): Boolean {
        return ResourceUtil.isEmptyDirectory(res, filter)
    }

    @Override
    @Throws(IOException::class)
    fun deleteEmptyFolders(res: Resource) {
        ResourceUtil.deleteEmptyFolders(res)
    }

    @Override
    fun getResource(pc: PageContext?, ps: PageSource, defaultValue: Resource): Resource {
        return ResourceUtil.getResource(pc, ps, defaultValue)
    }

    @Override
    fun directrySize(dir: Resource?, filter: ResourceFilter?): Int {
        return ResourceUtil.directrySize(dir, filter)
    }

    @Override
    fun directrySize(dir: Resource?, filter: ResourceNameFilter?): Int {
        return ResourceUtil.directrySize(dir, filter)
    }

    @Override
    fun names(resources: Array<Resource>): Array<String?> {
        return ResourceUtil.names(resources)
    }

    @Override
    fun merge(srcs: Array<Resource?>?, vararg trgs: Resource?): Array<Resource?> {
        return ResourceUtil.merge(srcs, trgs)
    }

    @Override
    @Throws(IOException::class)
    fun removeEmptyFolders(dir: Resource) {
        ResourceUtil.removeEmptyFolders(dir, null)
    }

    @Override
    fun listRecursive(res: Resource?, filter: ResourceFilter?): List<Resource> {
        return ResourceUtil.listRecursive(res, filter)
    }

    @Override
    fun getSeparator(rp: ResourceProvider): Char {
        return ResourceUtil.getSeparator(rp)
    }

    @get:Override
    val fileResourceProvider: ResourceProvider
        get() = ResourcesImpl.getFileResourceProvider()

    companion object {
        val instance = ResourceUtilImpl()
    }
}