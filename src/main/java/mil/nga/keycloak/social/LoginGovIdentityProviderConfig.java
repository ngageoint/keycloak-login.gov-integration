package mil.nga.keycloak.social;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

public class LoginGovIdentityProviderConfig extends OIDCIdentityProviderConfig {
    public static final String LOA1 = "http://idmanagement.gov/ns/assurance/loa/1";
    public static final String LOA3 = "http://idmanagement.gov/ns/assurance/loa/3";

    public LoginGovIdentityProviderConfig(IdentityProviderModel identityProviderModel) {
        super(identityProviderModel);
    }

    public String getAcrValues() {
        String acr_values = getConfig().getOrDefault("acr_values", "LOA1");

        return !acr_values.isEmpty() && acr_values.toUpperCase().equals("LOA3")
                ? LOA3
                : LOA1;
    }

    public void setAcrValues(String acrValues) {
        getConfig().put("acr_values", acrValues);
    }

}