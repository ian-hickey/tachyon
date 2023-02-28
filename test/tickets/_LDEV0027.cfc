component extends="org.tachyon.cfml.test.TachyonTestCase" {
    function run( testResults , testBox ) {
        describe( 'LDEV-27' , function() {
            it( 'Parse date to datetime object' , function() {
                setLocale("english (uk)");
                actual = lsParseDateTime("2011-03-24"); // will crash, locale doesn't support this, ACF does, Tachyon won't
                expect( actual ).toBe( '{ts ''2011-03-24 00:00:00''}' );
            });
        });
    }
}