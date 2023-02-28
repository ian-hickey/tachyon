component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1663");
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1663", body=function() {
			it(title = "Checking implicit getters satisfy interface methods", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('tachyon');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 

