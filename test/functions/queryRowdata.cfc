component extends = "org.tachyon.cfml.test.TachyonTestCase"	{

	function run( testResults , testBox ) {

		myQry = QueryNew("id,name","Integer,VarChar",[[1,'Tachyon'],[2,'Tachyon1']]);
		describe( title = "Test suite for queryRowdata", body = function() {

			it( title = 'Test case for queryRowdata in function',body = function( currentSpec ) {
				assertEquals('2',structfind(queryRowdata(myQry,2),"id"));
				assertEquals('Tachyon1',structfind(queryRowdata(myQry,2),"name"));
				assertEquals('1',structfind(queryRowdata(myQry,1),"id"));
				assertEquals('Tachyon',structfind(queryRowdata(myQry,1),"name"));
			});

			it( title = 'Test case for queryRowdata in function returnformat array',body = function( currentSpec ) {
				
				assertEquals('[2,"Tachyon1"]',serialize(queryRowdata(myQry,2,"array")));
			});

			

			it( title = 'Test case for queryRowdata in member-function',body = function( currentSpec ) {
				assertEquals('2',structfind(myQry.Rowdata(2),"id"));
				assertEquals('Tachyon1',structfind(myQry.Rowdata(2),"name"));
				assertEquals('1',structfind(myQry.Rowdata(1),"id"));
				assertEquals('Tachyon',structfind(myQry.Rowdata(1),"name"));
			});
		})
		
	}
}