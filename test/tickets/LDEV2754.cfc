component extends = "org.tachyon.cfml.test.TachyonTestCase" labels="query" {

	function beforeAll(){
		variables.uri = createURI("LDEV2754");
	}

	function run( testResults, testBox ) {
		describe( "Test case for LDEV2754", function(){
			it(title = "Using (?) mark with DB",skip=true, body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 1 }
				)
				expect(result.filecontent).tobe("juwait");
			});

			it(title = "Using (') with DB ", skip=true,body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 2 }
				)
				expect(result.filecontent).tobe("juwait");
			});

			it(title = "Using (') with QoQ", skip=true,body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 3 }
				)
				expect(result.filecontent).tobe("tachyon");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}