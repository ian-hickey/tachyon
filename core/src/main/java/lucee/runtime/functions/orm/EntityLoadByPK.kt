package lucee.runtime.functions.orm

import lucee.runtime.PageContext

object EntityLoadByPK {
    @Throws(PageException::class)
    fun call(pc: PageContext?, name: String?, oID: Object?): Object? {
        val session: ORMSession = ORMUtil.getSession(pc)
        val id: String
        id = if (Decision.isBinary(oID)) Caster.toBase64(oID) else Caster.toString(oID)
        return session.load(pc, name, id)
        // FUTURE call instead load(..,..,OBJECT);
    }
}