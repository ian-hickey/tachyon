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
package tachyon.transformer.bytecode.util

import org.objectweb.asm.ClassReader

class ClassRenamer internal constructor(cv: ClassVisitor?, newName: String?) : ClassVisitor(ASM4, cv), Opcodes {
    private val newName: String?
    private var oldName: String? = null
    private var doNothing = false
    @Override
    fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<String?>?) {
        oldName = name
        doNothing = oldName!!.equals(newName)
        // print.e("vist:" + (oldName + ":" + newName));
        cv.visit(version, ACC_PUBLIC, newName, signature, superName, interfaces)
    }

    @Override
    fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<String?>?): MethodVisitor? {
        var mv: MethodVisitor? = cv.visitMethod(access, name, fixDesc(desc), fixSignature(signature), exceptions)
        if (mv != null && access and ACC_ABSTRACT === 0) {
            mv = MethodRenamer(mv)
        }
        return mv
    }

    internal inner class MethodRenamer(mv: MethodVisitor?) : MethodVisitor(ASM4, mv) {
        @Override
        fun visitTypeInsn(i: Int, s: String?) {
            var s = s
            if (!doNothing && oldName!!.equals(s)) {
                s = newName
            }
            mv.visitTypeInsn(i, s)
        }

        @Override
        fun visitFieldInsn(opcode: Int, owner: String?, name: String?, desc: String?) {
            if (!doNothing && oldName!!.equals(owner)) {
                mv.visitFieldInsn(opcode, newName, name, fixDesc(desc))
            } else {
                mv.visitFieldInsn(opcode, owner, name, fixDesc(desc))
            }
        }

        @Override
        fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?) {
            if (!doNothing && oldName!!.equals(owner)) {
                mv.visitMethodInsn(opcode, newName, name, fixDesc(desc))
            } else {
                mv.visitMethodInsn(opcode, owner, name, fixDesc(desc))
            }
        }
    }

    private fun fixDesc(desc: String?): String? {
        // print.e("fixDesc:"+desc);
        return _fix(desc)
    }

    private fun fixSignature(signature: String?): String? {
        // print.e("fixSignature:"+signature);
        return _fix(signature)
    }

    private fun _fix(str: String?): String? {
        var str = str
        if (!doNothing && !StringUtil.isEmpty(str)) {
            if (str.indexOf(oldName) !== -1) {
                str = StringUtil.replace(str, oldName, newName, false)
            }
        }
        return str
    }

    companion object {
        fun rename(src: ByteArray?, newName: String?): ByteArray? {
            val cr = ClassReader(src)
            val cw: ClassWriter = ASMUtil.getClassWriter()
            val ca = ClassRenamer(cw, newName)
            cr.accept(ca, 0)
            return if (ca.doNothing) null else cw.toByteArray()
        }
    }

    init {
        var newName = newName
        newName = ListUtil.trim(newName, "\\/")
        this.newName = newName.replace('.', '/')
    }
}