component {
	this.vfs.s3.accessKeyId = url.ACCESS_KEY_ID;
	this.vfs.s3.awsSecretKey = url.SECRET_KEY;
	this.vfs.s3.tachyon.accessKeyId = url.ACCESS_KEY_ID;
	this.vfs.s3.tachyon.awsSecretKey = url.SECRET_KEY;

	public function onRequestStart() {
		setting requesttimeout=10;
	}
} 