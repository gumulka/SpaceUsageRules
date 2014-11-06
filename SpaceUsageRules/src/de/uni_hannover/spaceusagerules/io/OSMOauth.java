package de.uni_hannover.spaceusagerules.io;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

public class OSMOauth extends DefaultApi10a {
	private static final String AUTHORIZATION_URL = "https://www.openstreetmap.org/oauth/authorize?oauth_token=%s";

	@Override
	public String getAccessTokenEndpoint() {
		return "https://www.openstreetmap.org/oauth/access_token";
	}

	@Override
	public String getRequestTokenEndpoint() {
		return "https://www.openstreetmap.org/oauth/request_token";
	}
	@Override
	public String getAuthorizationUrl(Token requestToken) {
		return String.format(AUTHORIZATION_URL, requestToken.getToken());
	}
}
