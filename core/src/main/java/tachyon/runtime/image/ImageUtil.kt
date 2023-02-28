package tachyon.runtime.image

import java.awt.image.BufferedImage

object ImageUtil {
    private fun getImageClass(): Class? {
        return try {
            val config: Config = ThreadLocalPageContext.getConfig()
            val id: Identification? = if (config == null) null else config.getIdentification()
            ClassUtil.loadClassByBundle("org.tachyon.extension.image.Image", "image.extension", null as Version?, id, null)
        } catch (e: Exception) {
            null
        }
    }

    private fun getImageUtilClass(): Class? {
        return try {
            val config: Config = ThreadLocalPageContext.getConfig()
            val id: Identification? = if (config == null) null else config.getIdentification()
            ClassUtil.loadClassByBundle("org.tachyon.extension.image.ImageUtil", "image.extension", null as Version?, id, null)
        } catch (e: Exception) {
            null
        }
    }

    private fun toImage(pc: PageContext?, obj: Object?, checkForVariables: Boolean, defaultValue: Object?): Object? {
        return try {
            toImage(pc, obj, checkForVariables)
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Throws(PageException::class)
    private fun toImage(pc: PageContext?, obj: Object?, checkForVariables: Boolean): Object? {
        try {
            val clazz: Class? = getImageClass()
            if (clazz != null) {
                val m: Method = clazz.getMethod("toImage", arrayOf<Class?>(PageContext::class.java, Object::class.java, Boolean::class.javaPrimitiveType))
                return m.invoke(null, arrayOf(pc, obj, checkForVariables))
            }
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        throw ApplicationException("Cannot convert Object to an Image, you need to install the Image Extension to do so.")
    }

    @Throws(PageException::class)
    fun getImageBytes(o: Object?, format: String?): ByteArray? {
        return try {
            val m: Method = o.getClass().getMethod("getImageBytes", arrayOf<Class?>(String::class.java, Boolean::class.javaPrimitiveType))
            m.invoke(o, arrayOf(format, false))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    fun getImageBytes(bi: BufferedImage?): ByteArray? {
        try {
            val clazz: Class? = getImageClass()
            if (clazz != null) {
                val c: Constructor = clazz.getConstructor(arrayOf<Class?>(BufferedImage::class.java))
                val o: Object = c.newInstance(arrayOf<Object?>(bi))
                return getImageBytes(o, "png")
            }
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        throw ApplicationException("Cannot convert BufferedImage to a byte array, you need to install the Image Extension to do so.")
    }

    @Throws(PageException::class)
    fun toBufferedImage(file: Resource?, format: String?): BufferedImage? {
        try {
            val clazz: Class? = getImageUtilClass()
            if (clazz != null) {
                val m: Method = clazz.getMethod("toBufferedImage", arrayOf<Class?>(Resource::class.java, String::class.java))
                return m.invoke(null, arrayOf(file, format)) as BufferedImage
            }
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        throw ApplicationException("Cannot convert Object to a BufferedImage, you need to install the Image Extension to do so.")
    }

    /*
	 * public static Type getImageType() { return Type.getType("Lorg/tachyon/extension/image/Image;"); }
	 */
    fun isCastableToImage(pc: PageContext?, obj: Object?): Boolean {
        try {
            val clazz: Class? = getImageClass()
            if (clazz != null) {
                val m: Method = clazz.getMethod("isCastableToImage", arrayOf<Class?>(PageContext::class.java, Object::class.java))
                return m.invoke(null, arrayOf<Object?>(pc, obj))
            }
        } catch (e: Exception) {
        }
        return false
    }

    fun isImage(obj: Object?): Boolean {
        try {
            val clazz: Class? = getImageClass()
            if (clazz != null) {
                val m: Method = clazz.getMethod("isImage", arrayOf<Class?>(Object::class.java))
                return m.invoke(null, arrayOf<Object?>(obj))
            }
        } catch (e: Exception) {
        }
        return false
    }
}