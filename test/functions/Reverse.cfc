component extends="org.tachyon.cfml.test.TachyonTestCase" {

	public void function testReverse() {

		expect ( reverse( "abcd" ) ).toBe( 'dcba' );
		expect ( reverse( "a b c d" ) ).toBe( 'd c b a' );

		expect ( [1,2,3].reverse().toJson() ).toBe( "[3,2,1]" );

	}

}