<cfset error.message="">
<cfset error.detail="">
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">

<cfadmin 
	action="getCacheConnections"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="connections">
 
<!--- load available drivers --->
<cfset driverNames=structnew("linked")>
<cfset driverNames=ComponentListPackageAsStruct("tachyon-server.admin.cdriver",driverNames)>
<cfset driverNames=ComponentListPackageAsStruct("tachyon.admin.cdriver",driverNames)>
<cfset driverNames=ComponentListPackageAsStruct("cdriver",driverNames)>

<cfset drivers={}>
<cfloop collection="#driverNames#" index="n" item="fn">
	
	<cfif n NEQ "Cache" and n NEQ "Field" and n NEQ "Group">
		<cfset tmp = createObject("component",fn)>
		<!--- Workaround for EHCache Extension --->
		<cfset clazz=tmp.getClass()>
		<cfif "tachyon.extension.io.cache.eh.EHCache" EQ clazz or "tachyon.runtime.cache.eh.EHCache" EQ clazz>
			<cfset clazz="org.tachyon.extension.cache.eh.EHCache">
		</cfif>
		<cfset drivers[clazz]=tmp>
	</cfif>
</cfloop>
<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="access"
	secType="cache">

<cfswitch expression="#url.action2#">
	<cfcase value="list"><cfinclude template="services.cache.list.cfm"/></cfcase>
	<cfcase value="create"><cfinclude template="services.cache.create.cfm"/></cfcase>

</cfswitch>