component extends = "org.tachyon.cfml.test.TachyonTestCase" {

	function run( testResults, textbox ) {

		describe("testcase for LDEV-3804", function(){

			it(title="duplicate shouldn't throw java.lang.NoSuchMethodException", body=function( currentSpec ){

				expect ( function(){
					duplicate( createObject( 'java', 'java.util.Collections' ).synchronizedMap({}) );
				}).notToThrow();
				
			});

		});

	}

}