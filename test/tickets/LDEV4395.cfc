component extends="org.tachyon.cfml.test.TachyonTestCase"  labels="mysql" {
	
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4395", body=function() {
			it( title="checking transaction savepoint behaviour - fails", skip=true, body=function( currentSpec ) {
				transaction {
					transaction action="setSavepoint" savepoint="foo";
					doQuery();
					// this throws // tachyon.runtime.exp.DatabaseException: There are no savepoint with name [foo] set
					transaction action="rollback" savepoint="foo"; 
				}
			});

			it( title="checking transaction rollback savepoint behaviour", skip=isMySqlNotSupported(),body=function( currentSpec ) {
				transaction { 
					doQuery(); // doing an extra query here avoids the  error
					transaction action="setSavepoint" savepoint="foo";
					doQuery();
					transaction action="rollback" savepoint="foo";
				}
			});
		});
	}

	private void function doQuery(){
		queryExecute(
			sql: "select 1",
			options: {
				datasource: mySqlCredentials()
			}
		);
	}

	function isMySqlNotSupported() {
		return isEmpty(mySqlCredentials());
	}
	
	private struct function mySqlCredentials() {
		return server.getDatasource("mysql");
	}

}