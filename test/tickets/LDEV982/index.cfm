<cfscript>
	echo(application.configured);
	sleep(2000);
	admin
		action="purgeExpiredSessions"
		type="server"
		password="#url.SERVERADMINPASSWORD#";
	echo(application.configured);
</cfscript>