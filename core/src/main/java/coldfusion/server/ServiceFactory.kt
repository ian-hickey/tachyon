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
package coldfusion.server

import lucee.runtime.services.DataSourceServiceImpl

object ServiceFactory {
    fun clear() {}
    @Throws(ServiceException::class)
    fun getSecurityService(): SecurityService? {
        throw missingService("SecurityService")
    }

    @Throws(ServiceException::class)
    fun getLoggingService(): LoggingService? {
        throw missingService("LoggingService")
    }

    @Throws(ServiceException::class)
    fun getSchedulerService(): SchedulerService? {
        throw missingService("SchedulerService")
    }

    fun getDataSourceService(): DataSourceService? {
        return DataSourceServiceImpl()
    }

    @Throws(ServiceException::class)
    fun getMailSpoolService(): MailSpoolService? {
        throw missingService("MailSpoolService")
    }

    @Throws(ServiceException::class)
    fun getVerityService(): VerityService? {
        throw missingService("VerityService")
    }

    @Throws(ServiceException::class)
    fun getDebuggingService(): DebuggingService? {
        throw missingService("DebuggingService")
    }

    @Throws(ServiceException::class)
    fun getRuntimeService(): RuntimeService? {
        throw missingService("RuntimeService")
    }

    @Throws(ServiceException::class)
    fun getCronService(): CronService? {
        throw missingService("CronService")
    }

    @Throws(ServiceException::class)
    fun getClientScopeService(): ClientScopeService? {
        throw missingService("ClientScopeService")
    }

    @Throws(ServiceException::class)
    fun getMetricsService(): MetricsService? {
        throw missingService("MetricsService")
    }

    @Throws(ServiceException::class)
    fun getXmlRpcService(): XmlRpcService? {
        throw missingService("XmlRpcService")
    }

    @Throws(ServiceException::class)
    fun getGraphingService(): GraphingService? {
        throw missingService("GraphingService")
    }

    @Throws(ServiceException::class)
    fun getArchiveDeployService(): ArchiveDeployService? {
        throw missingService("ArchiveDeployService")
    }

    @Throws(ServiceException::class)
    fun getRegistryService(): RegistryService? {
        throw missingService("RegistryService")
    }

    @Throws(ServiceException::class)
    fun getLicenseService(): LicenseService? {
        throw missingService("LicenseService")
    }

    @Throws(ServiceException::class)
    fun getDocumentService(): DocumentService? {
        throw missingService("DocumentService")
    }

    @Throws(ServiceException::class)
    fun getEventProcessorService(): EventGatewayService? {
        throw missingService("DocumentService")
    }

    @Throws(ServiceException::class)
    fun getWatchService(): WatchService? {
        throw missingService("WatchService")
    }

    private fun missingService(service: String?): ServiceException? {
        // TODO Auto-generated method stub
        return ServiceException("the service [$service] is currently missing. At the moment you can use cfadmin tag instead")
    }

    fun setSecurityService(service: SecurityService?) {}
    fun setSchedulerService(service: SchedulerService?) {}
    fun setLoggingService(service: LoggingService?) {}
    fun setDataSourceService(service: DataSourceService?) {}
    fun setMailSpoolService(service: MailSpoolService?) {}
    fun setVerityService(service: VerityService?) {}
    fun setDebuggingService(service: DebuggingService?) {}
    fun setRuntimeService(service: RuntimeService?) {}
    fun setCronService(service: CronService?) {}
    fun setClientScopeService(service: ClientScopeService?) {}
    fun setMetricsService(service: MetricsService?) {}
    fun setXmlRpcService(service: XmlRpcService?) {}
    fun setGraphingService(service: GraphingService?) {}
    fun setArchiveDeployService(service: ArchiveDeployService?) {}
    fun setRegistryService(service: RegistryService?) {}
    fun setLicenseService(service: LicenseService?) {}
    fun setDocumentService(service: DocumentService?) {}
    fun setEventProcessorService(service: EventGatewayService?) {}
    fun setWatchService(service: WatchService?) {}
}