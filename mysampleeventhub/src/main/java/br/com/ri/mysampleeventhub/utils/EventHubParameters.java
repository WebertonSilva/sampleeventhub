package br.com.ri.mysampleeventhub.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventHubParameters {
	
	Properties appProperties = new Properties();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EventHubParameters.class);
	
	public EventHubParameters() {
		
		try {
			this.appProperties.load(new FileInputStream("src\\main\\resources\\application.properties"));
		} catch (IOException e) {
			LOGGER.error("Arquivo de propriedade não encontrado", e);
		}
	}

	public String getConsumerGroupName() {
		return this.appProperties.getProperty("consumerGroupName");
	}

	public String getNamespaceName() {
		return this.appProperties.getProperty("namespaceName");
	}

	public String getEventHubName() {
		return this.appProperties.getProperty("eventHubName");
	}

	public String getSasKeyName() {
		return this.appProperties.getProperty("sasKeyName");
	}

	public String getSasKey() {
		return this.appProperties.getProperty("sasKey");
	}

	public String getStorageConnectionString() {
		return this.appProperties.getProperty("storageConnectionString");
	}

	public String getStorageContainerName() {
		return this.appProperties.getProperty("storageContainerName");
	}
	
	public String getDirTmpBlobFile() {
		return this.appProperties.getProperty("dirTmpBlobFile");
	}
	
}
