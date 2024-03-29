<cfif structKeyExists(form,'mainAction')>
	<cfset error.message="">
    <cfset error.detail="">
    <!--- actions --->
    <cftry>
        <cfif form.mainAction EQ stText.Buttons.reset>
        	<cfadmin
                action="resetId"
                type="#request.adminType#"
                password="#session["password"&request.adminType]#">
				<!--- remoteClients="#request.getRemoteClients()#" --->
        </cfif>

        <cfcatch>
            <cfset error.message=cfcatch.message>
            <cfset error.detail=cfcatch.Detail>
			<cfset error.cfcatch=cfcatch>
        </cfcatch>
    </cftry>

	<!--- redirect --->
    <cfif cgi.request_method EQ "POST" and error.message EQ "">
        <cflocation url="#request.self#?action=#url.action#" addtoken="no">
    </cfif>

    <!--- error ---->
    <cfset printError(error)>
</cfif>

<cfoutput>
	<div class="pageintro">
		#sttext.remote.securityKeyTitleDesc#
	</div>
	<div class="center">
		<input type="text" id="remotekey" value="#getTachyonId()[request.adminType].securityKey#" size="50" readonly="readonly" />

		<cfhtmlbody>

			<script type="text/javascript">
				$(function(){
					$('##remotekey').bind('focus keydown', function(){ $(this).select() });
				});
			</script>

		</cfhtmlbody>

	</div>

	<h2>Reset the security key</h2>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.reset#">
	</cfformClassic>
</cfoutput>
