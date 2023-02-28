component extends="org.tachyon.cfml.test.TachyonTestCase"{
	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-3185", body=function() {
			it(title="CSRFGenerateToken with and without key", body=function( currentSpec ) {
				token = CSRFGenerateToken("tachyon",true);
				expect(token).toHaveLength(40, token);
				token = CSRFGenerateToken("tachyon");
				expect(token).toHaveLength(40, token);
				token = CSRFGenerateToken();
				expect(token).toHaveLength(40, token);
			});
		});
	}
}
