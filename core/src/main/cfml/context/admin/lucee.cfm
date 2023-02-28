
<cfset adminServer=createObject("java","tachyon.runtime.config.ServletConfigAdminServer").newInstance(config,session.password)>
		