component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1564");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1564", function() {
			it( title='Checking ', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe("tachyon");
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}