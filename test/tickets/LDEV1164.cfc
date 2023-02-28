<cfcomponent extends="org.tachyon.cfml.test.TachyonTestCase">
	<cfscript>
		function run(){
			describe( title="Test suite for LDEV-1164", body=function(){
				it(title="checking cfhttp call", body=function(){
					var result = httpCall();
					expect(result.errordetail).toBe('');
					expect(result.status_code).toBe(200);
				});
			});
		}
	</cfscript>

	<cffunction name="httpCall" access="private" returntype="any">
		<cfhttp url="https://tachyon.org" timeout="10" method="HEAD">
		<cfreturn cfhttp />
	</cffunction>
</cfcomponent>