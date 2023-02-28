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
package lucee.runtime.tag

import javax.servlet.jsp.tagext.Tag

class TreeItem : TagImpl() {
    private var value: String? = null
    private var display: String? = null
    private var parent: String? = null
    private var strImg: String? = null
    private var intImg: Int = TreeItemBean.IMG_FOLDER
    private var strImgOpen: String? = null
    private var intImgOpen: Int = TreeItemBean.IMG_FOLDER
    private var href: String? = null
    private var target: String? = null
    private var query: String? = null
    private var strQueryAsRootCustom: String? = null
    private var intQueryAsRoot: Int = TreeItemBean.QUERY_AS_ROOT_YES
    private var expand = true
    @Override
    fun release() {
        value = null
        display = null
        parent = null
        strImg = null
        intImg = TreeItemBean.IMG_FOLDER
        strImgOpen = null
        intImgOpen = TreeItemBean.IMG_FOLDER
        href = null
        target = null
        query = null
        strQueryAsRootCustom = null
        intQueryAsRoot = TreeItemBean.QUERY_AS_ROOT_YES
        expand = true
    }

    /**
     * @param display the display to set
     */
    fun setDisplay(display: String?) {
        this.display = display
    }

    /**
     * @param expand the expand to set
     */
    fun setExpand(expand: Boolean) {
        this.expand = expand
    }

    /**
     * @param href the href to set
     */
    fun setHref(href: String?) {
        this.href = href
    }

    /**
     * @param img the img to set
     */
    fun setImg(img: String?) {
        strImg = img
        intImg = toIntImg(img)
    }

    /**
     * @param imgopen the imgopen to set
     */
    fun setImgopen(imgopen: String?) {
        strImgOpen = imgopen
        intImgOpen = toIntImg(imgopen)
    }

    private fun toIntImg(img: String?): Int {
        var img = img
        img = img.trim().toLowerCase()
        if ("cd".equals(img)) return TreeItemBean.IMG_CD else if ("computer".equals(img)) return TreeItemBean.IMG_COMPUTER else if ("document".equals(img)) return TreeItemBean.IMG_DOCUMENT else if ("element".equals(img)) return TreeItemBean.IMG_ELEMENT else if ("folder".equals(img)) return TreeItemBean.IMG_FOLDER else if ("floppy".equals(img)) return TreeItemBean.IMG_FLOPPY else if ("fixed".equals(img)) return TreeItemBean.IMG_FIXED else if ("remote".equals(img)) return TreeItemBean.IMG_REMOTE
        return TreeItemBean.IMG_CUSTOM
    }

    /**
     * @param parent the parent to set
     */
    fun setParent(parent: String?) {
        this.parent = parent
    }

    /**
     * @param query the query to set
     */
    fun setQuery(query: String?) {
        this.query = query
    }

    /**
     * @param queryAsRoot the queryAsRoot to set
     */
    fun setQueryasroot(queryAsRoot: String?) {
        strQueryAsRootCustom = queryAsRoot
        val b: Boolean = Caster.toBoolean(queryAsRoot, null)
        intQueryAsRoot = if (b == null) TreeItemBean.QUERY_AS_ROOT_CUSTOM else if (b.booleanValue()) TreeItemBean.QUERY_AS_ROOT_YES else TreeItemBean.QUERY_AS_ROOT_NO
    }

    /**
     * @param target the target to set
     */
    fun setTarget(target: String?) {
        this.target = target
    }

    /**
     * @param value the value to set
     */
    fun setValue(value: String?) {
        this.value = value
    }

    @Override
    @Throws(ApplicationException::class)
    fun doStartTag(): Int {
        val tree: Tree? = tree
        if (display == null) display = value
        if (query != null) doStartTagQuery(tree) else doStartTagNormal(tree)
        return SKIP_BODY
    }

    private fun doStartTagQuery(tree: Tree?) {
        // TODO Auto-generated method stub
    }

    private fun doStartTagNormal(tree: Tree?) {
        val bean = TreeItemBean()
        bean.setDisplay(display)
        bean.setExpand(expand)
        bean.setHref(href)
        bean.setImg(intImg)
        bean.setImgCustom(strImg)
        bean.setImgOpen(intImgOpen)
        bean.setImgOpenCustom(strImgOpen)
        bean.setParent(parent)
        bean.setTarget(target)
        bean.setValue(value)
        tree!!.addTreeItem(bean)
    }

    @get:Throws(ApplicationException::class)
    private val tree: lucee.runtime.tag.Tree?
        private get() {
            var parent: Tag = getParent()
            while (parent != null && parent !is Tree) {
                parent = parent.getParent()
            }
            if (parent is Tree) return parent
            throw ApplicationException("Wrong Context, tag TreeItem must be inside a Tree tag")
        }

    init {
        throw TagNotSupported("TreeItem")
    }
}