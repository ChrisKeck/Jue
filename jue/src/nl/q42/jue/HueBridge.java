package nl.q42.jue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import nl.q42.jue.Networker.Result;
import nl.q42.jue.exceptions.ApiException;
import nl.q42.jue.exceptions.LinkButtonException;
import nl.q42.jue.exceptions.UnauthorizedException;
import nl.q42.jue.models.AuthenticatedConfig;
import nl.q42.jue.models.Config;
import nl.q42.jue.models.CreateUserRequest;
import nl.q42.jue.models.ErrorResponse;
import nl.q42.jue.models.FullLight;
import nl.q42.jue.models.Light;
import nl.q42.jue.models.ResponseMap;
import nl.q42.jue.models.ResponseString;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * Representation of a connection with a Hue bridge.
 */
public class HueBridge {
	private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	
	private String ip;
	private String username;
	
	private Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
	
	private Type responseTypeMap = new TypeToken<List<ResponseMap>>(){}.getType();
	private Type responseTypeString = new TypeToken<List<ResponseString>>(){}.getType();
	
	/**
	 * Connect with a bridge as a new user.
	 * @param ip ip address of bridge
	 */
	public HueBridge(String ip) {
		this.ip = ip;
	}
	
	/**
	 * Connect with a bridge as an existing user.
	 * @param ip ip address of bridge
	 * @param username username to authenticate with
	 */
	public HueBridge(String ip, String username) {
		this.ip = ip;
		this.username = username;
	}
	
	/**
	 * Returns the username currently authenticated with or null if there isn't one.
	 * @return username or null
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Returns basic configuration of the bridge (name and firmware version) and
	 * more detailed info if there is an authenticated user.
	 * @see Config
	 * @return Config or AuthenticatedConfig if authenticated
	 */
	public Config getConfig() throws IOException, ApiException {
		Result result;
		
		if (username == null) {
			result = Networker.get(getRelativeURL("config"));
			handleErrors(result);
			return gson.fromJson(result.getBody(), Config.class);
		} else {
			result = Networker.get(getRelativeURL(enc(username) + "/config"));
			handleErrors(result);
			return gson.fromJson(result.getBody(), AuthenticatedConfig.class);
		}
	}
	
	/**
	 * Link with bridge using the specified username and device type.
	 * @param username username for new user (between 10 and 40 characters)
	 * @param devicetype identifier of application (maximum length of 40 characters)
	 * @throws ApiException throws LinkButtonException if the bridge button has not been pressed, ApiException for other errors
	 */
	public void link(String username, String devicetype) throws IOException, ApiException {
		this.username = link(new CreateUserRequest(username, devicetype));
	}
	
	/**
	 * Link with bridge using the specified device type. A random valid username will be generated by the bridge and returned.
	 * @return new random username generated by bridge
	 * @param devicetype identifier of application (maximum length of 40 characters)
	 * @throws ApiException throws LinkButtonException if the bridge button has not been pressed, ApiException for other errors
	 */
	public String link(String devicetype) throws IOException, ApiException {
		return (this.username = link(new CreateUserRequest(devicetype)));
	}
	
	private String link(CreateUserRequest request) throws IOException, ApiException {
		if (this.username != null) {
			throw new IllegalStateException("already linked");
		}
		
		String body = gson.toJson(request, CreateUserRequest.class);
		Result result = Networker.post(getRelativeURL(""), body);
		
		handleErrors(result);
		
		List<ResponseMap> entries = gson.fromJson(result.getBody(), responseTypeMap);
		ResponseMap response = entries.get(0);
		
		return response.success.get("username");
	}
	
	/**
	 * Unlink the current user from the bridge.
	 * @throws ApiException throws UnauthorizedException if the user no longer exists, ApiException for other errors
	 */
	public void unlink() throws IOException, ApiException {
		requireUsername();
		
		Result result = Networker.delete(getRelativeURL(enc(username) + "/config/whitelist/" + enc(username)));

		handleErrors(result);
		
		List<ResponseString> entries = gson.fromJson(result.getBody(), responseTypeString);
		entries.get(0);
	}
	
	/**
	 * Returns a mapping from IDs to names of lights known to the bridge.
	 * @return map with ids and lights 
	 * @throws ApiException throws UnauthorizedException if the user no longer exists, ApiException for other errors
	 */
	public Map<String, Light> getLights() throws IOException, ApiException {
		requireUsername();
		
		Result result = Networker.get(getRelativeURL(enc(username) + "/lights"));
		
		handleErrors(result);
			
		Type responseType = new TypeToken<Map<String, Light>>(){}.getType();
		Map<String, Light> lights = gson.fromJson(result.getBody(), responseType);
		
		for (String id : lights.keySet()) {
			setLightID(lights.get(id), id);
		}
		
		return lights;
	}
	
	/**
	 * Returns detailed information given basic light information.
	 * @param light basic light information
	 * @return detailed light information
	 * @throws ApiException throws UnauthorizedException if the user no longer exists, ApiException for other errors
	 */
	public FullLight getLight(Light light) throws IOException, ApiException {
		return getLight(light.getID());
	}
	
	/**
	 * Returns detailed light information of the light with the given ID.
	 * @param id id of light
	 * @return detailed light information
	 * @throws ApiException throws UnauthorizedException if the user no longer exists, ApiException for other errors
	 */
	public FullLight getLight(String id) throws IOException, ApiException {
		requireUsername();
		
		Result result = Networker.get(getRelativeURL(enc(username) + "/lights/" + enc(id)));
		
		handleErrors(result);
		
		return gson.fromJson(result.getBody(), FullLight.class);
	}
	
	// Set ID field without exposing an actual constructor or setter
	private void setLightID(Light light, String id) {
		try {
			Field idField = Light.class.getDeclaredField("id");
			idField.setAccessible(true);
			
			idField.set(light, id);
			
			idField.setAccessible(false);
		} catch (Exception e) {}
	}
	
	private void requireUsername() {
		if (this.username == null) {
			throw new IllegalStateException("linking is required before interacting with the bridge");
		}
	}
	
	private void handleErrors(Result result) throws IOException, ApiException {
		if (result.getResponseCode() != 200) {
			throw new IOException();
		} else {
			try {
				Type errorType = new TypeToken<List<ErrorResponse>>(){}.getType();
				ErrorResponse error = gson.fromJson(result.getBody(), errorType);
				
				switch (error.getType()) {
				case 1:
					throw new UnauthorizedException(error.getDescription());
				case 101:
					throw new LinkButtonException(error.getDescription());
				default:
					throw new ApiException(error.getDescription());
				}
			} catch (JsonParseException e) {
				// Not an error
			}
		}
	}
	
	// UTF-8 URL encode
	private String enc(String str) {
		try {
			return URLEncoder.encode(str, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// throw new EndOfTheWorldException()
			return null;
		}
	}
	
	private String getRelativeURL(String path) {
		return "http://" + ip + "/api/" + path;
	}
}