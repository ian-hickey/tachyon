component extends = "org.tachyon.cfml.test.TachyonTestCase" {
	function run( testResults, testBox ) {
		describe( "Test case for structFind()", function() {
			it( title = "Checking structFind()", body = function( currentSpec ) {
				findAnimals = { cow : "moo", pig : "oink", cat : "meow" };
				expect(structFind(findAnimals,"pig")).toBe("oink");
			});

			it( title = "Checking struct.Find()", body = function( currentSpec ) {
				findAnimals = { cow : "moo", pig : "oink", cat : "meow" };
				expect(findAnimals.Find("snail","")).toBe("");
			});
		});
	}
}