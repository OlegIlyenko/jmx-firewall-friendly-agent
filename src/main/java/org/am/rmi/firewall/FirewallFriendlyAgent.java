package org.am.rmi.firewall;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;


/**
 * <p>This agent will start an RMI Connector Server using only
 * port "example.rmi.agent.port".
 * <p/>
 * <p>Idea take from: http://blogs.sun.com/jmxetc/entry/connecting_through_firewall_using_jmx
 */
public class FirewallFriendlyAgent {

	public static final String DEFAULT_PORT = "62277";
	public static final String PORT_SYS_PROPERTY = "org.am.rmi.port";
	public static final String PREFIX = "Firewall Friendly Agent: ";
	
	private FirewallFriendlyAgent() {}
	         
	public static void premain(String agentArgs)
		throws IOException {

		// Ensure cryptographically strong random number generator used
		// to choose the object number - see java.rmi.server.ObjID
		System.setProperty("java.rmi.server.randomIDs", "true");

		// Start an RMI registry on port specified by example.rmi.agent.port
		final int port = Integer.parseInt(System.getProperty(PORT_SYS_PROPERTY, DEFAULT_PORT));
		System.out.println(PREFIX + "Create RMI registry on port " + port);
		LocateRegistry.createRegistry(port);

		// Retrieve the PlatformMBeanServer.
		//
		System.out.println(PREFIX + "Get the platform's MBean server");
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		// Environment map.
		//
		System.out.println(PREFIX + "Initialize the environment map");
		HashMap<String, Object> env = new HashMap<String, Object>();

		// This where we would enable security - left out of this
		// for the sake of the example....
		//

		// Create an RMI connector server.
		//
		// As specified in the JMXServiceURL the RMIServer stub will be
		// registered in the RMI registry running in the local host on
		// port 3000 with the name "jmxrmi". This is the same name the
		// out-of-the-box management agent uses to register the RMIServer
		// stub too.
		//
		// The port specified in "service:jmx:rmi://"+hostname+":"+port
		// is the second port, where RMI connection objects will be exported.
		// Here we use the same port as that we choose for the RMI registry.
		// The port for the RMI registry is specified in the second part
		// of the URL, in "rmi://"+hostname+":"+port

		String userProvidedHostname = System.getProperty("java.rmi.server.hostname");
		String hostname = null;
		if (userProvidedHostname == null || "".equals(userProvidedHostname)){
			hostname = InetAddress.getLocalHost().getHostName();
		} else {
			hostname = userProvidedHostname;
		}
		System.out.println(PREFIX + "Create an RMI connector server. Url for access: " + getServiceUrl(port, hostname));
		JMXServiceURL url = new JMXServiceURL(getServiceUrl(port, hostname));

		// Now create the server from the JMXServiceURL
		JMXConnectorServer cs =
			JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);

		// Start the RMI connector server.
		System.out.println(PREFIX + "Start the RMI connector server on port " + port);
		cs.start();
	}

	private static String getServiceUrl(final int port, final String hostname) {
		return "service:jmx:rmi://" + hostname + ":" + port + "/jndi/rmi://" + hostname + ":" + port + "/jmxrmi";
	}

}
