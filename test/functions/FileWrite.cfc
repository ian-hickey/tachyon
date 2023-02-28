component extends="org.tachyon.cfml.test.TachyonTestCase" {
	
	public void function testFileWrite() {
		var f = getTempFile( getTempDirectory(), "fileWrite", "txt" );
		fileWrite( f, "123" );
		expect( fileRead( f ) ).toBe( "123" );
		fileDelete( f );
	}

}
