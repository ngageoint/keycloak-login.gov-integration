package mil.nga.keycloak.social;

import org.jboss.logging.Logger;
import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.util.Base64Url;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.crypto.RSAProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.sessions.AuthenticationSessionModel;
import mil.nga.keycloak.keys.loader.LoginGovPublicKeyStorageManager;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.security.PublicKey;

public class LoginGovIdentityProvider
        extends OIDCIdentityProvider
        implements SocialIdentityProvider<OIDCIdentityProviderConfig> {

    public static final String EMAIL_SCOPE = "email";
    public static final String OPENID_SCOPE = "openid";
    public static final String DEFAULT_SCOPE = OPENID_SCOPE;

    private static final Logger log = Logger.getLogger(LoginGovIdentityProvider.class);

    public LoginGovIdentityProvider(KeycloakSession session, LoginGovIdentityProviderConfig config) {
        super(session, config);
        String defaultScope = config.getDefaultScope();

        if (defaultScope ==  null || defaultScope.trim().isEmpty()) {
            config.setDefaultScope(OPENID_SCOPE + " " + EMAIL_SCOPE);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

    @Override
    protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
        UriBuilder uriBuilder = super.createAuthorizationUrl(request);

        LoginGovIdentityProviderConfig config = (LoginGovIdentityProviderConfig)getConfig();
        uriBuilder.queryParam("acr_values", new Object[] { config.getAcrValues() });
        logger.debugv("Login.gov Authorization Url: {0}", uriBuilder.toString());
        return uriBuilder;
    }

    /**
     * Verify the token is signed by the IDP with the JWK exposed by the wellknown endpoints.
     * Overriden from parent because of an issue with login.gov / Keycloak interpretation of the JWK Spec 4.2.
     * https://tools.ietf.org/html/draft-ietf-jose-json-web-key-41#section-4.2
     *
     * Login.gov does NOT set the "use" field, which should be set to "sig".  Keycloak does not default the
     * "use" field and therefore will not validate with JWK endpoint provided by login.gov.
     *
     * This implementation defaults the JWK by setting the null "use" fields to "sig"
     * allowing validation with login.gov.
     *
     * @param jws - signed token
     * @return true if validation is successful, false otherwise
     */
    @Override
    protected boolean verify(JWSInput jws) {
        if (!getConfig().isValidateSignature()) return true;

        try {
            PublicKey publicKey = LoginGovPublicKeyStorageManager.getIdentityProviderPublicKey(session, session.getContext().getRealm(), getConfig(), jws);

            return publicKey != null && RSAProvider.verify(jws, publicKey);
        } catch (Exception e) {
            logger.debug("Failed to verify token", e);
            return false;
        }
    }

    @Override
    protected BrokeredIdentityContext extractIdentity(AccessTokenResponse tokenResponse, String accessToken, JsonWebToken idToken) throws IOException {
        BrokeredIdentityContext identityContext = super.extractIdentity(tokenResponse, accessToken, idToken);

        final String email = identityContext.getEmail();
        if(email == null || email.isEmpty()) {
            throw new IdentityBrokerException("Unable to determine user email address.");
        }
        identityContext.setEmail(email.toLowerCase());

        return identityContext;
    }
}
