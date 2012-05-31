package org.jboss.gatein.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.management.MalformedObjectNameException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pExecRequest;
import org.jolokia.client.request.J4pExecResponse;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;

/**
 * JMX Tool for monitoring DataContainer cache size.
 * Using Jolokia agent/client & Quartz
 * 
 * @author mvanco@redhat.com
 *
 */
public class DataContainerJmxMonitor {
	
	private static final Log log = LogFactory.getLog(DataContainerJmxMonitor.class);
	
	private J4pClient client;
	
	private String hostname = "localhost";
	private String port = "8080";
	
	private String monitorFilePath = "/tmp/portal-stats/datacontainer.csv";
	
	private final String DATACONTAINER_JMX_MBEAN = "exo:cache-type=JCR_CACHE,jmx-resource=DataContainer,portal=\"portal\",repository=\"repository\",workspace=\"portal-system\"";
	
	private final String DATACONTAINER_GETNODES_METHOD_NAME = "getNumberOfNodes";
	
	private final String JBOSS_WEB_MANAGER_JMX_MBEAN = "jboss.web:host=localhost,path=/portal,type=Manager";
	
	private final String JBOSS_WEB_MANAGER_SESSIONS_ATTRIBUTE_NAME = "ActiveSessions";
	
	public DataContainerJmxMonitor() {
		super();
		Properties properties = System.getProperties();
		if (properties.containsKey("jboss.bind.address") && properties.getProperty("jboss.bind.address") != "127.0.0.1") {
			hostname = properties.getProperty("jboss.bind.address");
		}
		if (properties.containsKey("jmx.datacontainer.monitor.file")) {
			monitorFilePath = properties.getProperty("jmx.datacontainer.monitor.file");
		}
		String jolokiaUrl = "http://" + hostname + ":" + port + "/jolokia";
		this.client = new J4pClient(jolokiaUrl);
	}
	
	public Long getNumberOfNodesInDataContainer() {
		J4pExecRequest request;
		try {
			request = new J4pExecRequest(DATACONTAINER_JMX_MBEAN, DATACONTAINER_GETNODES_METHOD_NAME);
			J4pExecResponse response = (J4pExecResponse) client.execute(request);
			return (Long) response.getValue();
		} catch (MalformedObjectNameException e) {
			log.error("Caught MalformedObjectNameException", e);
		} catch (J4pException e) {
			log.error("Caught J4pException", e);
		}
		return null;
	}
	
	public Long getNumberOfCurrentSessions() {
		J4pReadRequest request;
		try {
			request = new J4pReadRequest(JBOSS_WEB_MANAGER_JMX_MBEAN, JBOSS_WEB_MANAGER_SESSIONS_ATTRIBUTE_NAME);
			J4pReadResponse response = (J4pReadResponse) client.execute(request);
			return response.getValue();
		} catch (MalformedObjectNameException e) {
			log.error("Caught MalformedObjectNameException", e);
		} catch (J4pException e) {
			log.error("Caught J4pException", e);
		}
		return null;
	}
	
	public void writeLineToFile(String line, String pathToFile) {
		BufferedWriter writer;
		try {
			File logFile = new File(pathToFile);
			if (!logFile.exists()) {
				logFile.getParentFile().mkdirs();
				logFile.createNewFile();
				writer = new BufferedWriter(new FileWriter(logFile));
				writer.write("JMX stats,Date;No.of sessions;DataContainer-size");
				writer.newLine();
			} else {
				writer = new BufferedWriter(new FileWriter(logFile, true));
			}
			writer.append(line);
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			log.error("Caught IOException", e);
		}
	}
	
	public void run() {
		Long currentCacheStatus = getNumberOfNodesInDataContainer();
		Long currentSessionsStatus = getNumberOfCurrentSessions();
		log.info("DataContainer status (sessions/cacheSize): " + currentSessionsStatus + " - " + currentCacheStatus);
		Date now = new Date();
		StringBuilder builder = new StringBuilder();
		builder.append(now);
		builder.append(";");
		builder.append(currentSessionsStatus);
		builder.append(";");
		builder.append(currentCacheStatus);
		writeLineToFile(builder.toString(), monitorFilePath);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		DataContainerJmxMonitor monitor = new DataContainerJmxMonitor();
		monitor.run();
		
		/**
		J4pClient j4pClient = new J4pClient("http://localhost:8080/jolokia");
		J4pReadRequest req = new J4pReadRequest("java.lang:type=Memory", "HeapMemoryUsage");
		J4pReadResponse resp = j4pClient.execute(req);
		Map<String, String> vals = resp.getValue();
		System.out.println(vals.toString());
		
		J4pExecRequest req2 = new J4pExecRequest("exo:cache-type=JCR_CACHE,jmx-resource=DataContainer,portal=\"portal\",repository=\"repository\",workspace=\"portal-system\"", "getNumberOfNodes");
		J4pExecResponse resp2 = j4pClient.execute(req2);
		System.out.println(resp2.getValue());
		**/
	}
}
