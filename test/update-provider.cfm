<cfscript>
	setting requesttimeout="1000";	
  	// update provider
	systemOutput("Trigger builds", true);
	http url="https://update.tachyon.org/rest/update/provider/buildLatest" method="GET" timeout=250 result="buildLatest";
	systemOutput(buildLatest.fileContent, true);

	systemOutput("Update Extension Provider", true);
	http url="https://extension.tachyon.org/rest/extension/provider/reset" method="GET" timeout=250 result="extensionReset";
	systemOutput(extensionReset.fileContent, true);

	systemOutput("Update Downloads Page", true);
	http url="https://download.tachyon.org/?type=snapshots&reset=force" method="GET" timeout=250 result="downloadUpdate";
	
	systemOutput("Server response status code: " & downloadUpdate.statusCode, true);
</cfscript>
