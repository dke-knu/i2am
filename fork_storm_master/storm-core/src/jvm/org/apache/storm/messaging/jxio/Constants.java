package org.apache.storm.messaging.jxio;

import org.apache.storm.Config;
import org.apache.storm.utils.Utils;

import java.util.Map;

public class Constants {
    private static Map storm_conf;
	public static final int MSGPOOL_BUF_SIZE         = Utils.getInt(storm_conf.get(Config.STORM_MEESAGING_JXIO_MSGPOOL_BUFFER_SIZE));
	public static final int CLIENT_INPUT_BUF_COUNT   = 100;
	public static final int CLIENT_OUTPUT_BUF_COUNT  = 100;
	public static final int SERVER_INITIAL_BUF_COUNT = 500;
	public static final int SERVER_INC_BUF_COUNT     = 100;

    public static void setStorm_conf(Map storm_conf) {
        Constants.storm_conf = storm_conf;
    }
}
