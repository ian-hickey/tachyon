<cfcomponent extends="org.tachyon.cfml.test.TachyonTestCase">
	<cfscript>
		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-929", function() {
				it("Access cfproperty default value, without accessors='true' ", function( currentSpec ) {
					var uri=createURI("LDEV0929/App1/test.cfm");
					var result = _InternalRequest(
						template:uri
					);
					expect(result.filecontent.trim()).toBe("TachyonTest");
				});
				
				it("Access cfproperty default value, with accessors='true' ", function( currentSpec ) {
					var uri=createURI("LDEV0929/App2/test.cfm");
					var result = _InternalRequest(
						template:uri
					);
					expect(result.filecontent.trim()).toBe("TachyonTest");
				});
			});
		}
		// private function//
		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}
	</cfscript>
</cfcomponent>