component extends="org.tachyon.cfml.test.TachyonTestCase" {

	function run( testResults , testBox ) {
		struct1 = {
		  	user_name: "Tachyon",
		  	company_name: "Tachyon",
  			company_url: "Tachyon.org"
		};

		struct2 = {
			userName: "Tachyon",
			companyname: "Tachyon",
  			companyurl: "Tachyon.org"
		};

		struct1.each(function(key,value){
			struct1["{{" & key & "}}"] = value;
			struct1.delete(key);
		});

		struct2.each(function(key,value){
			struct2["{{" & key & "}}"] = value;
			struct2.delete(key);
		});
		describe( "test suite for LDEV2681", function() {
			it(title = "structkey with '_' in structeach function", body = function( currentSpec ) {
				expect("true").toBe(structkeyexists(struct1,"{{user_name}}"));
				expect("true").toBe(structkeyexists(struct1,"{{company_name}}"));
				expect("true").toBe(structkeyexists(struct1,"{{company_url}}"));
			});

			it(title = "structkey without '_' in structeach function", body = function( currentSpec ) {
				expect("true").toBe(structkeyexists(struct2,"{{username}}"));
				expect("true").toBe(structkeyexists(struct2,"{{companyname}}"));
				expect("true").toBe(structkeyexists(struct2,"{{companyurl}}"));
			});
		});
	} 
}
