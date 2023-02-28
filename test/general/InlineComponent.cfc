component extends="org.tachyon.cfml.test.TachyonTestCase" {
	function beforeAll(){
	}

	function afterAll(){
	}

	function run( testResults , testBox ) {
		describe( "test inline component", function() {
			it(title="test inline component", body=function() {
				inline=new component {   
					function subTest() {
						return "inline"; 
					}  
				};   
				expect(inline.subTest()).toBe("inline");
				
				var md=getComponentMetaData(inline);
				expect(md.inline).toBe(true);
				expect(md.sub).toBe(false);
			});
		});
	}
}
