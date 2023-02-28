<cfcomponent extends="org.tachyon.cfml.test.TachyonTestCase">
	<cfscript>
		function run(){
			describe( title="Test cases for LDEV-1128", body=function(){
				it(title="Checking cfthread with http results", body=function(){
					myresult = testCase();
					expect(myresult).toBe("true|true|true|true");
				});
			});
		}
	</cfscript>

	<cffunction name="processURL" returntype="struct" access="private">
		<cfargument name="targetURL" type="string" required="yes" hint="URL to add to the collection">

		<cfhttp method="get" url="#Arguments.targetURL#" redirect="no" resolveURL="yes" timeout="30" result="httpResult"/>

		<cfreturn httpResult>
	</cffunction>

	<cffunction name="testCase" returntype="String" access="private">
		<cfset URLs = ArrayNew(1)>
		<cfset ArrayAppend(URLs,"http://www.tachyon.org")>
		<cfset ArrayAppend(URLs,"http://tachyon.org/downloads.html")>
		<cfset ArrayAppend(URLs,"http://stable.tachyon.org/download/?type=releases")>
		<cfset ArrayAppend(URLs,"http://stable.tachyon.org/download/?type=releases&major=5.2")>

		<cfloop from="1" to="#ArrayLen(URLs)#" index="i">
			<cfset threadName = "thread_#i#">
			<cfthread name="#threadName#" action="run" theURL="#URLs[i]#">
				<cfset thread.results = processURL(Attributes.theURL)>
			</cfthread>
		</cfloop>

		<cfset myresult = "">
		<cfloop from="1" to="#ArrayLen(URLs)#" index="i">
			<cfset threadName = "thread_#i#">

			<cfthread action="join" name="#threadName#" />
			<cfhttp method="get" url="#URLs[i]#" redirect="no" resolveURL="yes" timeout="30" result="local.currHttpResult"/>
			<cfset results = cfThread[threadName].results>
			<cfset myresult = listAppend(myresult, local.currHttpResult.filecontent.trim() EQ results.filecontent.trim(), "|")>
		</cfloop>

		<cfreturn myresult>
	</cffunction>
</cfcomponent>