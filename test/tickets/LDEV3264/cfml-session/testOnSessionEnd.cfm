<cfscript>
	param name="url.dumpEndedSessions" default="false";
	if ( url.dumpEndedSessions ){
		param name="url.check";
		echo( structKeyExists(server.LDEV3264_endedSessions, url.check ) );
	} else {
		session.tachyonRocks=true;
		echo( session.sessionid );
	}
</cfscript>
