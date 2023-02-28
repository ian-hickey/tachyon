component extends = "org.tachyon.cfml.test.TachyonTestCase"	{

	function run( testResults , testBox ) {
		myQry = QueryNew("id,name","Integer,VarChar",[[1,'Tachyon'],[2,'Tachyon1']]);
		describe( title = "Test suite for queryGetRow", body = function() {

			it( title = 'Test case for queryGetRow in function',body = function( currentSpec ) {
				assertEquals('2',structfind(queryGetRow(myQry,2),"id"));
				assertEquals('Tachyon1',structfind(queryGetRow(myQry,2),"name"));
				assertEquals('1',structfind(queryGetRow(myQry,1),"id"));
				assertEquals('Tachyon',structfind(queryGetRow(myQry,1),"name"));
			});

			it( title = 'Test case for queryGetRow in member-function',body = function( currentSpec ) {
				assertEquals('2',structfind(myQry.GetRow(2),"id"));
				assertEquals('Tachyon1',structfind(myQry.GetRow(2),"name"));
				assertEquals('1',structfind(myQry.GetRow(1),"id"));
				assertEquals('Tachyon',structfind(myQry.GetRow(1),"name"));
			});

		})
	}
}