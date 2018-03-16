CREATE TABLE TBL_ANOMALY_DETECTION (
	cluster_name	VARCHAR(32) NULL,
	host_name		VARCHAR(32) NOT NULL,
	log_key			VARCHAR(64)	NOT NULL,
	log_value		DOUBLE(16, 8)		NULL,
	upper_bound		DOUBLE(16, 8)		NULL,
	lower_bound		DOUBLE(16, 8)		NULL,
	is_anomaly		BOOLEAN		NOT NULL DEFAULT 0,
	logging_time	DATETIME	NOT NULL
)
;