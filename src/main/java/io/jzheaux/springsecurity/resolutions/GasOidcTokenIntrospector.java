package io.jzheaux.springsecurity.resolutions;

import io.jzheaux.springsecurity.resolutions.grantedAuthorities.TRIauthority;
import io.jzheaux.springsecurity.resolutions.model.UserInfoResDTO;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class GasOidcTokenIntrospector implements OpaqueTokenIntrospector {

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {

        UserInfoResDTO usrInfo = null;
        try {
            usrInfo = getUserInfo(token);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println(usrInfo);
        // map user info response onto OAuth2AuthenticatedPrincipal
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", usrInfo.getSub());
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        // check to see if this user has authorization to access the TRI app
        // for now there is just basic start authorization, in the future there may be a couple of entitlements
        if (
                usrInfo.getBasicStartAuthorization().equalsIgnoreCase("true") &&
                        usrInfo.getAppId().equalsIgnoreCase("TRI")
        ) {
            authorities.add(new TRIauthority("basic_start_authorization"));
        }
        attributes.put("sub", usrInfo.getSub());

        // in the end we need to return the same thing that the default method returns
        return new DefaultOAuth2AuthenticatedPrincipal(usrInfo.getSub(), attributes, authorities);
    }

    private UserInfoResDTO getUserInfo(String authToken) throws IOException {
        WebClient webClient = WebClient.create("https://sso-int.daimler.com");

        Mono<UserInfoResDTO> monoRes = webClient.get()
                .uri("idp/userinfo.openid")
                .headers(
                        httpHeaders -> {
                            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                            httpHeaders.setBearerAuth(authToken);
                        }
                )
                .retrieve()
                .bodyToMono(UserInfoResDTO.class);

        return monoRes.block();
    }

}
