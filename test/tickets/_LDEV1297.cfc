component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1297", body=function() {
			it(title="checking string.hash member function", body = function( currentSpec ) {
				var str = "tachyon";
				var result = str.hash();
				expect(result).toBe('8A567C484832DBCFE5B311CDAEDB5625');
			});
		});
	}
}