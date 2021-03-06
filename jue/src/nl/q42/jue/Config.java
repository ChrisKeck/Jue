package nl.q42.jue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Detailed bridge info available if authenticated.
 */
public class Config {
	private String name;
	private String swversion;
	private String mac;
	private boolean dhcp;
	private String ipaddress;
	private String netmask;
	private String gateway;
	private String proxyaddress;
	private int proxyport;
	private Date UTC;
	private boolean linkbutton;
	private Map<String, User> whitelist;
	private SoftwareUpdate swupdate;
	
	Config() {}
	
	/**
	 * Returns the name.
	 * @return name of the bridge
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the version of the software.
	 * @return version of software on the bridge
	 */
	public String getSoftwareVersion() {
		return swversion;
	}
	
	/**
	 * Returns the MAC address.
	 * @return mac address of bridge
	 */
	public String getMACAddress() {
		return mac;
	}
	
	/**
	 * Returns if the current IP address was obtained with DHCP.
	 * @return true if the current IP address was obtained with DHCP, false otherwise.
	 */
	public boolean isDHCPEnabled() {
		return dhcp;
	}
	
	/**
	 * Returns the IP address.
	 * @return ip address of bridge
	 */
	public String getIPAddress() {
		return ipaddress;
	}
	
	/**
	 * Returns the network mask.
	 * @return network mask
	 */
	public String getNetworkMask() {
		return netmask;
	}
	
	/**
	 * Returns the IP address of the gateway. 
	 * @return ip address of gateway
	 */
	public String getGateway() {
		return gateway;
	}
	
	/**
	 * Returns the IP address of the proxy or null if there is none.
	 * @return ip address of proxy or null
	 */
	public String getProxyAddress() {
		return proxyaddress.equals("none") ? null : proxyaddress;
	}
	
	/**
	 * Returns the port of the proxy or null if there is none.
	 * @return port of proxy or null
	 */
	public Integer getProxyPort() {
		return proxyaddress.equals("none") ? null : proxyport;
	}
	
	/**
	 * Returns the time on the bridge.
	 * @return time on the bridge
	 */
	public Date getUTCTime() {
		return UTC;
	}
	
	/**
	 * Returns if the link button has been pressed within the last 30 seconds.
	 * @return true if the link button has been pressed within the last 30 seconds, false otherwise
	 */
	public boolean isLinkButtonPressed() {
		return linkbutton;
	}
	
	/**
	 * Returns the list of whitelisted users.
	 * @return list of whitelisted users
	 */
	public List<User> getWhitelist() {
		ArrayList<User> usersList = new ArrayList<User>();
		
		usersList.addAll(whitelist.values());
		
		return usersList;
	}
	
	/**
	 * Returns information about a bridge firmware update.
	 * @return bridge firmware update info
	 */
	public SoftwareUpdate getSoftwareUpdate() {
		return swupdate;
	}
}
