component extends = "org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults, testBox ){
		describe( "test case for LDEV-2605", function() {
			it(title = "Elvisoperator without parenthesis", body = function( currentSpec ){
				server.system.properties.APP_ENV = "development"
				if( server.system.properties.APP_ENV ?: "unknown" == "development" ){
					test = "test is OK without parenthesis";
				}
				expect(test).tobe("test is OK without parenthesis");
			});

			it(title = "Elvisoperator with parenthesis", body = function( currentSpec ){
				server.system.properties.APP_ENV = "tachyon"
				if( (server.system.properties.APP_ENV ?: "unknown") == "tachyon" ){
					test = "test is OK with parenthesis";
				}
				expect(test).tobe("test is OK with parenthesis");
			});
		});
	}
}