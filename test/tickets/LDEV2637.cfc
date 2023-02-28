component extends="org.tachyon.cfml.test.TachyonTestCase" {

	function run( testResults , testBox ) {
		myQuery = QueryNew("myCol1,   myCol2,myCol3", "varchar,varchar,varchar");
		QueryAddRow(myQuery, 1);

		describe( "test suite for LDEV2637", function() {
			it(title = "QuerySetCell column name without spaces", body = function( currentSpec ) {
				QuerySetCell(myQuery,"myCol2","Tachyon", 1);
				expect("Tachyon").toBe(myQuery.myCol2);
			});

			it(title = "QuerySetCell column name with spaces", body = function( currentSpec ) {
				QuerySetCell(myQuery," myCol2","Tachyon", 1);
				expect("Tachyon").toBe(myQuery.myCol2);
			});
		});
	}
}