package i2am.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesFactory {
	private final static Class clazz = (new Object() {/**/}).getClass().getEnclosingClass();
	private final static Logger logger = LoggerFactory.getLogger(clazz);
	
	// singleton
	private volatile static PropertiesFactory instance;
	public static PropertiesFactory getInstance() {
		if(instance == null) {
			synchronized(PropertiesFactory.class) {
				if(instance == null) {
					instance = new PropertiesFactory();
				}
			} 
		}
		return instance;
	} 
	
	private Properties props;
 
	// singleton
	private PropertiesFactory() {
	}

	public Properties getObject() {
		if(props == null) {
			props = new Properties();
			try {
				props.load(PropertiesFactory.class.getResourceAsStream("/metadb.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return props;
	}
}
