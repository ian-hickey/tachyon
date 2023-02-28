<cfscript>
	test1=createObject(type:'java',class:'org.tachyon.mockup.osgi.Test',bundlename:"tachyon.mockup",bundleversion:"1.0.0.0");
	meta1=getMetaData(test1);
	//bi1=bundleInfo(test1);// load the classic way, when no bundle info is provided
	
	test2=createObject(type:'java',class:'org.tachyon.mockup.osgi.Test');
	
	meta2=getMetaData(test2);
	bi2=bundleInfo(test2);

	

	sct={
		"bundle1":{"name":bi2.name,"version":bi2.version},
		"bundle2":{"name":bi2.name,"version":bi2.version}
	};

	echo(serializeJson(sct));
</cfscript>