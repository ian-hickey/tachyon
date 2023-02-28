/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.orm

import tachyon.runtime.Component

interface ORMSession {
    /**
     * flush all elements in all sessions (for all datasources)
     *
     * @param pc Page Context
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun flushAll(pc: PageContext?)

    /**
     * flush all elements in the default sessions
     *
     * @param pc Page Context
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun flush(pc: PageContext?)

    /**
     * flush all elements in a specific sessions defined by datasource name
     *
     * @param pc Page Context
     * @param datasource Datasource name
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun flush(pc: PageContext?, datasource: String?)

    /**
     * delete elememt from datasource
     *
     * @param pc Page Context
     * @param obj Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun delete(pc: PageContext?, obj: Object?)

    /**
     * insert entity into datasource, even the entry already exist
     *
     * @param pc Page Context
     * @param obj Object
     * @param forceInsert force insert
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun save(pc: PageContext?, obj: Object?, forceInsert: Boolean)

    /**
     * Reloads data for an entity that is already loaded. This method refetches data from the database
     * and repopulates the entity with the refreshed data.
     *
     * @param pc Page Context
     * @param obj Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun reload(pc: PageContext?, obj: Object?)

    /**
     * creates an entity matching the given name
     *
     * @param pc Page Context
     * @param entityName entity name
     * @return component
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun create(pc: PageContext?, entityName: String?): Component?

    /**
     * Attaches the specified entity to the current ORM session. It copies the state of the given object
     * onto the persistent object with the same identifier and returns the persistent object. If there
     * is no persistent instance currently associated with the session, it is loaded. The given instance
     * is not associated with the session. User have to use the returned object from this session.
     *
     * @param pc Page Context
     * @param obj Object
     * @return component
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun merge(pc: PageContext?, obj: Object?): Component?

    /**
     * clear all elements in the default sessions
     *
     * @param pc Page Context
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun clear(pc: PageContext?)

    /**
     * clear all elements in a specific sessions defined by datasource name
     *
     * @param pc Page Context
     * @param dataSource Datasource Name
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun clear(pc: PageContext?, dataSource: String?)

    /**
     * load and return an Object that match given filter, if there is more than one Object matching the
     * filter, only the first Object is returned
     *
     * @param pc Page Context
     * @param name name
     * @param filter filter
     * @return Returns an object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun load(pc: PageContext?, name: String?, filter: Struct?): Component?

    @Throws(PageException::class)
    fun toQuery(pc: PageContext?, obj: Object?, name: String?): Query?

    /**
     * load and return an Object that match given id, if there is more than one Object matching the id,
     * only the first Object is returned
     *
     * @param pc Page Context
     * @param name name
     * @param id id
     * @return Returns an object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun load(pc: PageContext?, name: String?, id: String?): Component? // FUTURE deprecate
    // public Component load(PageContext pc, String name, Object id) throws PageException; // FUTURE ADD
    /**
     * load and return an Array of Objects matching given filter
     *
     * @param pc Page Context
     * @param name name
     * @param filter filter
     * @return array of objects
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun loadAsArray(pc: PageContext?, name: String?, filter: Struct?): Array?

    /**
     * load and return an Array of Objects matching given filter
     *
     * @param pc Page Context
     * @param name name
     * @param filter filter
     * @param options options
     * @return array of objects
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun loadAsArray(pc: PageContext?, name: String?, filter: Struct?, options: Struct?): Array?

    /**
     * @param pc Page Context
     * @param name name
     * @param filter filter
     * @param options options
     * @param order order
     * @return array of objects
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun loadAsArray(pc: PageContext?, name: String?, filter: Struct?, options: Struct?, order: String?): Array?

    /**
     * load and return an Array of Objects matching given id
     *
     * @param pc Page Context
     * @param name name
     * @param id id
     * @return array
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun loadAsArray(pc: PageContext?, name: String?, id: String?): Array?

    /**
     * @param pc Page Context
     * @param name name
     * @param id id
     * @param order order
     * @return array
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun loadAsArray(pc: PageContext?, name: String?, id: String?, order: String?): Array?

    /**
     * load and return an Array of Objects matching given sampleEntity
     *
     * @param pc Page Context
     * @param obj object
     * @return array
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun loadByExampleAsArray(pc: PageContext?, obj: Object?): Array?

    /**
     * load and return an Object that match given sampleEntity, if there is more than one Object matching
     * the id, only the first Object is returned
     *
     * @param pc Page Context
     * @param obj object
     * @return Component
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun loadByExample(pc: PageContext?, obj: Object?): Component?

    @Throws(PageException::class)
    fun evictCollection(pc: PageContext?, entity: String?, collection: String?)

    @Throws(PageException::class)
    fun evictCollection(pc: PageContext?, entity: String?, collection: String?, id: String?)

    @Throws(PageException::class)
    fun evictEntity(pc: PageContext?, entity: String?)

    @Throws(PageException::class)
    fun evictEntity(pc: PageContext?, entity: String?, id: String?)

    @Throws(PageException::class)
    fun evictQueries(pc: PageContext?)

    @Throws(PageException::class)
    fun evictQueries(pc: PageContext?, cacheName: String?)

    @Throws(PageException::class)
    fun evictQueries(pc: PageContext?, cacheName: String?, datasource: String?)

    @Throws(PageException::class)
    fun executeQuery(pc: PageContext?, dataSourceName: String?, hql: String?, params: Array?, unique: Boolean, queryOptions: Struct?): Object?

    @Throws(PageException::class)
    fun executeQuery(pc: PageContext?, dataSourceName: String?, hql: String?, params: Struct?, unique: Boolean, queryOptions: Struct?): Object?

    /**
     * close all elements in all sessions
     *
     * @param pc Page Context
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun closeAll(pc: PageContext?)

    /**
     * close all elements in the default sessions
     *
     * @param pc Page Context
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun close(pc: PageContext?)

    /**
     * close all elements in a specific sessions defined by datasource name
     *
     * @param pc Page Context
     * @param datasource Datsource Name
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun close(pc: PageContext?, datasource: String?)

    /**
     * is session valid or not
     *
     * @param ds datasource
     * @return is session valid
     */
    fun isValid(ds: DataSource?): Boolean
    val isValid: Boolean

    /**
     * engine from session
     *
     * @return engine
     */
    val engine: tachyon.runtime.orm.ORMEngine?

    @Throws(PageException::class)
    fun getRawSession(dataSourceName: String?): Object?

    @Throws(PageException::class)
    fun getRawSessionFactory(dataSourceName: String?): Object?

    @Throws(PageException::class)
    fun getTransaction(dataSourceName: String?, autoManage: Boolean): ORMTransaction?
    val entityNames: Array<String?>?
    val dataSources: Array<Any?>?
}