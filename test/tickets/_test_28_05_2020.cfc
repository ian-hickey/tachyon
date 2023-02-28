component extends = "org.tachyon.cfml.test.TachyonTestCase" {

	function beforeAll(){
		variables.uri = createURI("test_28_05_2020");
	}

	function run( testResults, testBox ){
		describe( "testing testcase", function(){
			it( title = "Checking testcase for LDEV-2882", body = function( currentSpec ){
			 	local.result = _InternalRequest(
					template : "#uri#\test.cfm"
				)
				expect(trim(result.filecontent)).toBe("tachyon_test");
			}); 
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}