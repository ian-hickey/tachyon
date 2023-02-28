component extends="org.tachyon.cfml.test.TachyonTestCase"	{

	public void function testLJustify(){
		expect ( "abc".lJustify( 10 ) ).toBe("abc       ");
		expect ( LJustify( "abc", 10 ) ).toBe("abc       ");
	}

}