component extends = "org.tachyon.cfml.test.TachyonTestCase" skip=true{

	function run ( testResults , testBox ) {
		describe("This testcase for LDEV-3091",function(){
			
			it(title="cfsavecontent tag with variable", body =function( currentSpec ){
				cfsavecontent ( variable="content" ) {
					writeOutput("G'day World")
				}
				expect(content).toBe("G'day World");
			});

			it(title="script savecontent with variable", body =function( currentSpec ){
				savecontent variable="content" {
					writeOutput("G'day World")
				}
				expect(content).toBe("G'day World");
			});

			it(title="save savecontent for return value", body =function( currentSpec ){
				try {
					greeting = savecontent {
						writeOutput("G'day World")
					}
					variables.content = greeting;
				}
				catch (any e) {
					variables.content = e.message;
				}
				expect(content).toBe("G'day World");
			});
		});
	}
}