component extends="org.tachyon.cfml.test.TachyonTestCase"	{

	function run( testResults , testBox ) {

		describe( 'Number in scientific notation' , function(){

			it( 'works for integers' , function() {

				actual = 1E2;

				expect( actual ).toBeNumeric();
				expect( actual ).toBe( 100 );

			});

			it( 'works for decimals' , function() {

				actual = 1.0E2;

				expect( actual ).toBeNumeric();
				expect( actual ).toBe( 100 );

			});

		});

	}
	
} 