component extends="org.tachyon.cfml.test.TachyonTestCase"	{

	//public function beforeTests(){}

	//public function afterTests(){}

	//public function setUp(){}

	public void function testStructCopyServerOS(){
        //see https://bitbucket.org/tachyon/tachyon/issue/205/structcopy-does-not-copy-serveros
		assertEquals(StructIsEmpty(StructCopy(server.os)),false);
	}

	public void function testStructCopySimple() {
		var s1 = {x="one"};
		var s2 = StructCopy(s1);
		assertEquals(s1.x, s2.x);
	}

}
