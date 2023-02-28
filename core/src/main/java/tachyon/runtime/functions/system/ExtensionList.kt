package tachyon.runtime.functions.system

import tachyon.commons.lang.StringUtil

class ExtensionList : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 0) return call(pc)
        return if (args.size == 1) call(pc, Caster.toBoolean(args[0])) else throw FunctionException(pc, "ExtensionList", 0, 1, args.size)
    }

    companion object {
        private const val serialVersionUID = 3853910569001016577L
        @Throws(PageException::class)
        fun call(pc: PageContext?): Query? {
            return call(pc, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, imageObject: Boolean): Query? {
            val config: ConfigPro = pc.getConfig() as ConfigPro
            var bif: BIF? = null
            var bifLoaded = false
            val qry: Query = RHExtension.toQuery(config, (pc.getConfig() as ConfigWebPro).getServerRHExtensions(), null)
            RHExtension.toQuery(config, (pc.getConfig() as ConfigWebPro).getRHExtensions(), qry)
            if (imageObject) {
                try {
                    for (i in 1..qry.getRecordcount()) {
                        val image: Object = qry.getAt("image", i, null)
                        if (!StringUtil.isEmpty(image, true)) {
                            // image stuff is in a OPTIONAL extension
                            if (!bifLoaded) {
                                bif = getBIF(pc)
                                bifLoaded = true
                            }
                            if (bif != null) {
                                val res: Object = bif.invoke(pc, arrayOf<Object?>(image))
                                if (res != null) {
                                    qry.setAt(KeyConstants._image, i, res)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                }
            }
            return qry
        }

        private fun getBIF(pc: PageContext?): BIF? {
            val eng: CFMLEngine = CFMLEngineFactory.getInstance()
            return try {
                eng.getClassUtil().loadBIF(pc, "org.tachyon.extension.image.functions.ImageRead")
            } catch (e: Exception) {
                null
            }
        }
    }
}