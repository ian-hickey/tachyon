/**
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
package lucee.transformer.bytecode.util

import java.io.IOException

class MethodCleaner internal constructor(cv: ClassVisitor?, private val methodName: String?, args: Array<Class?>?, rtn: Class?, msg: String?) : ClassVisitor(ASM4, cv), Opcodes {
    // private Class[] arguments;
    private val strArgs: String?
    private val rtn: Class?
    private val msg: String?
    fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<String?>?) {
        cv.visit(version, access, name, signature, superName, interfaces)
    }

    fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<String?>?): MethodVisitor? {
        if (name!!.equals(methodName) && desc!!.equals(strArgs)) {
            val mv: MethodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
            mv.visitCode()
            if (msg == null) empty(mv) else exception(mv)
            mv.visitEnd()
            return mv
        }
        return super.visitMethod(access, name, desc, signature, exceptions)
    }

    private fun exception(mv: MethodVisitor?) {
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException")
        mv.visitInsn(DUP)
        mv.visitLdcInsn(msg)
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V")
        mv.visitInsn(ATHROW)
        mv.visitMaxs(3, 1)
    }

    private fun empty(mv: MethodVisitor?) {
        // void
        if (rtn === Void.TYPE) {
            mv.visitInsn(RETURN)
        } else if (rtn === Int::class.javaPrimitiveType) {
            mv.visitInsn(ICONST_0)
            mv.visitInsn(IRETURN)
            mv.visitMaxs(1, 1)
        } else if (rtn === Double::class.javaPrimitiveType) {
            mv.visitInsn(DCONST_0)
            mv.visitInsn(DRETURN)
            mv.visitMaxs(2, 1)
        } else if (rtn === Float::class.javaPrimitiveType) {
            mv.visitInsn(FCONST_0)
            mv.visitInsn(FRETURN)
            mv.visitMaxs(1, 1)
        } else if (rtn === Long::class.javaPrimitiveType) {
            mv.visitInsn(LCONST_0)
            mv.visitInsn(LRETURN)
            mv.visitMaxs(2, 1)
        } else {
            mv.visitInsn(ACONST_NULL)
            mv.visitInsn(ARETURN)
            mv.visitMaxs(1, 1)
        }
    }

    companion object {
        fun modifie(src: ByteArray?, methodName: String?, args: Array<Class?>?, rtn: Class?, msg: String?): ByteArray? {
            val cr = ClassReader(src)
            val cw: ClassWriter = ASMUtil.getClassWriter()
            val ca: ClassVisitor = MethodCleaner(cw, methodName, args, rtn, msg)
            cr.accept(ca, 0)
            return cw.toByteArray()
        }

        @Throws(IOException::class, ExpressionException::class)
        fun modifie(path: String?, methodName: String?, argNames: Array<String?>?, rtnName: String?, msg: String?) {
            val res: Resource = ResourceUtil.toResourceExisting(ThreadLocalPageContext.getConfig(), path)
            val args: Array<Class?> = arrayOfNulls<Class?>(argNames!!.size)
            for (i in argNames.indices) {
                args[i] = ClassUtil.loadClass(argNames!![i])
            }
            val rtn: Class = ClassUtil.loadClass(rtnName)
            val result: ByteArray = modifie(IOUtil.toBytes(res), methodName, args, rtn, msg)
            IOUtil.write(res, result)
        }
    }

    init {
        // this.arguments = arguments;
        val sb = StringBuilder("(")
        for (i in args.indices) {
            sb.append(Type.getDescriptor(args!![i]))
        }
        sb.append(")")
        sb.append(Type.getDescriptor(rtn))
        strArgs = sb.toString()
        this.rtn = rtn
        this.msg = if (StringUtil.isEmpty(msg)) null else msg
    }
}