package mil.nga.keycloak.keys.loader;

import org.jboss.logging.Logger;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.keys.loader.OIDCIdentityProviderPublicKeyLoader;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.JWKSHttpUtils;
import org.keycloak.util.JWKSUtils;

import java.util.Collections;
import java.util.Map;

/**
 * This class just exists to allow instantiation of the LoginGovIdentityProviderPublicKeyLoader.
 * The purpose is to fix a bug caused by the login.gov jwks certs endpoint where they do
 * not set the JWKS Spec https://tools.ietf.org/html/draft-ietf-jose-json-web-key-41#section-4.2
 * 4.2 OPTIONAL "use"="sig"
 *
 * This causes the login.gov jwks endpoint to fail subsequent checks and token fails with a
 * validation exception.
 *
 * Modified from Original:  org.keycloak.keys.loader.OIDCIdentityProviderPublicKeyLoader
 */
public class LoginGovOIDCIdentityProviderPublicKeyLoader extends OIDCIdentityProviderPublicKeyLoader {
    private static final Logger logger = Logger.getLogger(OIDCIdentityProviderPublicKeyLoader.class);

    private final KeycloakSession session;
    private final OIDCIdentityProviderConfig config;

    public LoginGovOIDCIdentityProviderPublicKeyLoader(KeycloakSession session, OIDCIdentityProviderConfig config) {
        super(session, config);
        this.config = config;
        this.session = session;
    }

    @Override
    public Map<String, KeyWrapper> loadKeys() throws Exception {
        if (config.isUseJwksUrl()) {
            String jwksUrl = config.getJwksUrl();
            JSONWebKeySet jwks = JWKSHttpUtils.sendJwksRequest(session, jwksUrl);

            //Patch to function with login.gov -- force a default "use" = "sig" for null "use"
            for (JWK jwk : jwks.getKeys()) {
                if(jwk.getPublicKeyUse() == null){
                    jwk.setPublicKeyUse(JWK.Use.SIG.asString());
                }
            }
            //

            return JWKSUtils.getKeyWrappersForUse(jwks, JWK.Use.SIG);
        } else {
            try {
                KeyWrapper publicKey = getSavedPublicKey();
                if (publicKey == null) {
                    return Collections.emptyMap();
                }
                return Collections.singletonMap(publicKey.getKid(), publicKey);
            } catch (Exception e) {
                logger.warnf(e, "Unable to retrieve publicKey for verify signature of identityProvider '%s' . Error details: %s", config.getAlias(), e.getMessage());
                return Collections.emptyMap();
            }
        }
    }
}
