package com.gmail.api.config;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.google.api.client.util.Value;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.http.CoreHttpProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class OutlookConfiguration {

    @Value("@{outlook.client-id")
    private String clientId;
    @Value("@{outlook.client-secret")
    private String clientSecret;
    @Value("@{outlook.redirect-uri")
    private String redirectUri;
    @Value("${outlook.authorization-uri}")
    private String authorizationUri;
    @Value("${outlook.token-uri}")
    private String tokenUri;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return registrationId -> outlookClientRegistration();
    }

    @Bean
    public GraphServiceClient<CoreHttpProvider> graphServiceClient(ClientRegistrationRepository clientRegistrationRepository) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("outlook");

        TokenCredential tokenCredential = new ClientSecretCredentialBuilder()
                .clientId(clientRegistration.getClientId())
                .clientSecret(clientRegistration.getClientSecret())
                .authorityHost(authorizationUri)
                .build();

        TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(tokenCredential);

        return GraphServiceClient
                .builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }

    private ClientRegistration outlookClientRegistration() {
        return ClientRegistration.withRegistrationId("outlook")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .scope("https://outlook.office.com/mail.read")
                .authorizationUri(authorizationUri)
                .tokenUri("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                .userInfoUri("https://graph.microsoft.com/v1.0/me")
                .clientName("Outlook")
                .build();
    }


}
