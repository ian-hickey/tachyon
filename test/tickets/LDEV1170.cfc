component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run(){
		describe( title="Test cases for LDEV-1170", body=function(){
			it(title="Checking generate3DesKey hidden ACF function", body=function(){
				expect(generate3DesKey('Test')).toBe("VGVzdA==");
			});
		});
	}
}