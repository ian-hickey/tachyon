component extends="org.tachyon.cfml.test.TachyonTestCase" labels="metadata" {
    function run( testResults, testBox ){
        describe(title="Testcase for LDEV-3560", body=function( currentSpec ) {
            it(title="getComponentMetadata() with abstract Component", body=function( currentSpec )  {
                    try {
                        metadata = getComponentMetadata("LDEV3560.testAbstract");
                        res = metadata.abstract;
                    }
                    catch(any e) {
                        res = e.message;
                    }
                    expect(res).toBe(true);
            });
        });
    }
}
