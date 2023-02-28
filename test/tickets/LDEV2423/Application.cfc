component {
	this.name = "tachyontest";
	this.datasources["tachyondb"] = server.getDatasource("mssql");
	this.datasource = "tachyondb";
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}
