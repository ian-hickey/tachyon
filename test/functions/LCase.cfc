component extends="org.tachyon.cfml.test.TachyonTestCase"	{
	public void function testUCase(){
		expect( LCase( "A B C" ) ) .toBe( "a b c" );
	}
}