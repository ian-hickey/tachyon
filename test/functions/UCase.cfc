component extends="org.tachyon.cfml.test.TachyonTestCase"	{

	public void function testUCase(){
		expect( UCase( "a b c" ) ).toBe( "A B C" );
	}

}