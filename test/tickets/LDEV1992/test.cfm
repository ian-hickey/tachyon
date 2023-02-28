<cfscript>
entity = entityLoad( "users", {FirstName="tachyon"}, true );
ormclearsession();
EntityMerge( entity ); //merge entity 
writeOutput(ormgetsession().getStatistics().getEntityCount());

</cfscript>