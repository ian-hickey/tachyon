component extends="org.tachyon.cfml.test.TachyonTestCase" labels="extensions"{
	function run( testResults , testBox ) {
		describe( "test case for extensionList()", function() {

			it(title = "Checking extensionList() returns a query", body = function( currentSpec ) {
				var exts = ExtensionList();
				expect( exts ).toBeQuery();
			});

		});	
	}
}