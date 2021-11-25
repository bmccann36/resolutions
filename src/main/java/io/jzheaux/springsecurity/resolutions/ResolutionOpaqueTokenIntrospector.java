package io.jzheaux.springsecurity.resolutions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * we implement our own token introspector using the delegate pattern
 */
public class ResolutionOpaqueTokenIntrospector implements OpaqueTokenIntrospector {
    private final OpaqueTokenIntrospector delegate;
    private final UserRepository users;

    public ResolutionOpaqueTokenIntrospector(
            OpaqueTokenIntrospector delegate, UserRepository users) {
        this.delegate = delegate;
        this.users = users;
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        // let the out of the box implementation do the bulk of the work
        OAuth2AuthenticatedPrincipal principal = this.delegate.introspect(token);

        System.err.println("PRICINPAL");
        // basically just doing custom type mapping
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writeValueAsString(principal));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String name = principal.getName();
        Map<String, Object> attributes =
                new HashMap<>(principal.getAttributes());
        Collection<GrantedAuthority> authorities =
                new ArrayList<>(principal.getAuthorities());

        UUID userId = UUID.fromString(principal.getAttribute("user_id"));
        attributes.put("user_id", userId);

        // this piece is for tying a user identity to an authorization token which isn't used for identifying a user necessarily
        User user = users.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("no user"));
        if ("premium".equals(user.getSubscription())) {
            if (authorities.stream().map(GrantedAuthority::getAuthority)
                    .anyMatch(authority -> "SCOPE_resolution:write".equals(authority))) {
                authorities.add(new SimpleGrantedAuthority("resolution:share"));
            }
        }

        // in the end we need to return the same thing that the default method returns
        OAuth2AuthenticatedPrincipal delegate =
                new DefaultOAuth2AuthenticatedPrincipal(name, attributes, authorities);

        // this makes it so the @AuthenticatedPrincipal annotation can cast the result to a User inside the 'Share' method
        return new BridgeUser(user, delegate);
    }

    private static class BridgeUser extends User implements OAuth2AuthenticatedPrincipal {
        private final OAuth2AuthenticatedPrincipal delegate;

        public BridgeUser(User user, OAuth2AuthenticatedPrincipal delegate) {
            super(user);
            this.delegate = delegate;
        }

        @Override
        @Nullable
        public <A> A getAttribute(String name) {
            return delegate.getAttribute(name);
        }

        @Override
        public Map<String, Object> getAttributes() {
            return delegate.getAttributes();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return delegate.getAuthorities();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }
    }
}
