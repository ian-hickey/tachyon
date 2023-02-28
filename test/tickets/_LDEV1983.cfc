component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1983", function() {
			it(title = "Calling duplicate() on an array that ends with null values truncates those nulls", body = function( currentSpec ) {
				arr = [javacast('null', ''),javacast('null', ''),javacast('null', '')];
				expect(arrayisEmpty(duplicate(arr))).toBe(False);
			}); 
		});
	}
}