component extends = "org.tachyon.cfml.test.TachyonTestCase" {
	function run( testResults, testBox ) {
		describe( "Test case for structCount()", function() {
			it( title = "Checking structCount()", body = function( currentSpec ) {
				world = { "save":"water","clean":"wastes" };
				expect(structcount(world)).toBe(2);
			});

			it( title = "Checking struct.Count() function", body = function( currentSpec ){
				legend = {"save":"energy","forget":"sadness","feel":"happy"};
				expect(legend.count()).toBe(3);
			});
		});
	}
}