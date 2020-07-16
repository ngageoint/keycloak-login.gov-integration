package mil.nga.keycloak.social;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.representations.OIDCConfigurationRepresentation;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class LoginGovIdentityProviderFactory
        extends AbstractIdentityProviderFactory<LoginGovIdentityProvider>
        implements SocialIdentityProviderFactory<LoginGovIdentityProvider> {

    public static final String PROVIDER_ID = "login.gov";

    @Override
    public String getName() {
        return "Login.gov";
    }

    @Override
    public LoginGovIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new LoginGovIdentityProvider(session, new LoginGovIdentityProviderConfig(model));
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }


    @Override
    public Map<String, String> parseConfig(KeycloakSession session, InputStream inputStream) {
        OIDCConfigurationRepresentation rep;
        try {
            rep = JsonSerialization.readValue(inputStream, OIDCConfigurationRepresentation.class);
        } catch (IOException e) {
            throw new RuntimeException("failed to load openid connect metadata", e);
        }
        OIDCIdentityProviderConfig config = new OIDCIdentityProviderConfig(new IdentityProviderModel());
        config.setIssuer(rep.getIssuer());
        config.setLogoutUrl(rep.getLogoutEndpoint());
        config.setAuthorizationUrl(rep.getAuthorizationEndpoint());
        config.setTokenUrl(rep.getTokenEndpoint());
        config.setUserInfoUrl(rep.getUserinfoEndpoint());
        if (rep.getJwksUri() != null) {
            config.setValidateSignature(true);
            config.setUseJwksUrl(true);
            config.setJwksUrl(rep.getJwksUri());
        }
        return config.getConfig();
    }

    @Override
    public OIDCIdentityProviderConfig createConfig() {
        return new OIDCIdentityProviderConfig(new IdentityProviderModel());
    }
}
