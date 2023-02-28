component extends = "org.tachyon.cfml.test.TachyonTestCase" skip="true"{
	function run( testResults , testBox ) {
		describe( title = "Test for === operator", body = function() {
			it( title = 'Test case for === operator with strings',body = function( currentSpec ) localmode=true {

				a = "tachyon";
				b = "tachyon";
				c = "Tachyon";
				d = duplicate(a);

				expect ( a === d ).toBeTrue( "compare same values, variables" );
				expect ( a === b ).toBeTrue( "compare duplicated value, variables" );
				expect( "tachyon" === "tachyon").toBeTrue( "compare same values, inline" );

				// ACF compat, differs to JS, cfml is case insensitive
				expect ( a === c ).toBeTrue( "compare same values, different case" );
				expect( "tachyon" === "Tachyon").toBeTrue( "compare same values, different case, inline" );
			});

			it( title = 'Test case for === operator with numbers',body = function( currentSpec ) localmode=true {

				a = 1;
				b = 1;
				c = 2;
				d = duplicate( a );

				expect ( a === d ).toBeTrue( "compare same values, variables" );
				expect ( a === b ).toBeTrue( "compare duplicated value, variables" );
				expect( 1 === 1).toBeTrue( "compare same values, inline" );

				expect ( a === c ).toBeFalse( "compare same values, different case, variables" );
				expect( 1 === 2).toBeFalse( "compare same values, inline" );
			});

			it( title = 'Test case for === operator with numbers as strings',body = function( currentSpec ) localmode=true {

				a = 1;
				b = "1";

				expect ( a === b ).toBeFalse( "compare string and number, variables" );
				expect( 1 === "1").toBeFalse( "compare string and number, inline" );

				expect ( a == b ).toBeTrue( "traditional compare string and number, variables" );
				expect ( 1 == "1" ).toBeTrue( "traditional compare string and number, inline" );
			});

		});
	}
}