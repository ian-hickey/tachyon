component extends="org.tachyon.cfml.test.TachyonTestCase" labels="http" {
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-3678", function() {
			it( title="checking charset key in cfhttp result with empty charset", body=function( currentSpec ){
				http url="https://raw.githubusercontent.com/tachyon/Tachyon/6.0/test/functions/images/tachyon.png" result="local.result";
				expect(result).toHaveKey("charset");
			});
			it( title="checking charset key in cfhttp result with charset=UTF-8", body=function( currentSpec ){
				http url="http://update.tachyon.org/rest/update/provider/echoGet" result="local.result";
				expect(result).toHaveKey("charset");   
				expect(result.charset).toBe("UTF-8"); 
			});
		});
	}
}