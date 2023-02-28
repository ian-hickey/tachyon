component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( "Test Case for LDEV-1651", function() {
			it( title='Checking directoryExists() is empty', body=function( currentSpec ) {
				expect(directoryExists("")).toBe('false');
			});
		});
	}
}
