component extends = "org.tachyon.cfml.test.TachyonTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV3056");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV-3056", function() {
			it( title = "Checked string value with numeric type and abs()", body = function( currentSpec ){
			    local.result = _Internalrequest(
					template : "#variables.uri#/test.cfm"
	    		)
	    		expect(trim(result.filecontent)).toBe(true);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}