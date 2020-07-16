package mil.nga.keycloak.keys.loader;

import org.jboss.logging.Logger;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.keys.PublicKeyLoader;
import org.keycloak.keys.PublicKeyStorageProvider;
import org.keycloak.keys.PublicKeyStorageUtils;
import org.keycloak.keys.loader.HardcodedPublicKeyLoader;
import org.keycloak.keys.loader.PublicKeyStorageManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.security.PublicKey;

/**
 * This class just exists to allow instantiation of the LoginGovIdentityProviderPublicKeyLoader.
 * The purpose is to fix a bug caused by the login.gov jwks certs endpoint where they do
 * not set the JWKS Spec https://tools.ietf.org/html/draft-ietf-jose-json-web-key-41#section-4.2
 * 4.2 OPTIONAL "use"="sig"
 *
 * This causes the login.gov jwks endpoint to fail subsequent checks and token fails with a
 * validation exception.
 *
 * Modified from original:   org.keycloak.keys.loader.PublicKeyStorageManager
 */
public class LoginGovPublicKeyStorageManager extends PublicKeyStorageManager {
    private static final Logger logger = Logger.getLogger(LoginGovPublicKeyStorageManager.class);

    public static PublicKey getIdentityProviderPublicKey(KeycloakSession session, RealmModel realm, OIDCIdentityProviderConfig idpConfig, JWSInput input) {
        boolean keyIdSetInConfiguration = idpConfig.getPublicKeySignatureVerifierKeyId() != null
                && ! idpConfig.getPublicKeySignatureVerifierKeyId().trim().isEmpty();

        String kid = input.getHeader().getKeyId();

        PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);

        String modelKey = PublicKeyStorageUtils.getIdpModelCacheKey(realm.getId(), idpConfig.getInternalId());
        PublicKeyLoader loader;
        if (idpConfig.isUseJwksUrl()) {
            loader = new LoginGovOIDCIdentityProviderPublicKeyLoader(session, idpConfig);
        } else {
            String pem = idpConfig.getPublicKeySignatureVerifier();

            if (pem == null || pem.trim().isEmpty()) {
                logger.warnf("No public key saved on identityProvider %s", idpConfig.getAlias());
                return null;
            }

            loader = new HardcodedPublicKeyLoader(
                    keyIdSetInConfiguration
                            ? idpConfig.getPublicKeySignatureVerifierKeyId().trim()
                            : kid, pem);
        }

        return (PublicKey)keyStorage.getPublicKey(modelKey, kid, loader).getPublicKey();
    }
}
