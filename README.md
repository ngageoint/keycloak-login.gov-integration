### About
The software enables users of Keycloak (a modern open source identity and access management solution) to easily integrate with login.gov as an identity provider.

### Pull Requests

This software was developed at the National Geospatial-Intelligence Agency (NGA) in collaboration with Arrona Tech, LLC under subcontract to Alion, Inc. The government has "unlimited rights" and is releasing this software to increase the impact of government investments by providing developers with the opportunity to take things in new directions. The software use, modification, and distribution rights are stipulated within the Apache 2.0 license.

If you'd like to contribute to this project, please make a pull request. We'll review the pull request and discuss the changes. All pull request contributions to this project will be released under the Apache license.

Keycloak is open sourced under the Apache 2.0 license. 

https://github.com/keycloak/keycloak
# Keycloak-Login_gov-IdentityProvider

Keycloak Login.gov Identity Provider Implementation

In addition to configuring the generic OIDC properties you will need to set the 
`Authentication Context Class (acr_value)` to either:
 * LOA1 :  Self-Asserted - (NIST 800-63-3) Identity Assurance Level l (IAL) (default)
 * LOA3 :  Proofed  - (NIST 800-63-3) Identity Assurance Level 2 (IAL)