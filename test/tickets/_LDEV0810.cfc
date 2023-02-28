component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-810", function() {
			it(title="checking reEscape function in tachyon", body = function( currentSpec ) {
				uri=createURI("LDEV0810/test.cfm");
				result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("tachyon\?\[\]\^");
			});
		});
	}
	// private function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
