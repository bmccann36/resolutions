package io.jzheaux.springsecurity.resolutions.grantedAuthorities;

import org.springframework.security.core.GrantedAuthority;

/**
 * TRIauthority is just a wrapper implementation of GrantedAuthority
 * by implementing the class we can use all the DSL of spring security with minimal boilerplate for setting up user authorization
 */
public class TRIauthority implements GrantedAuthority {
    private String authority;

    public TRIauthority(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return this.authority;
    }
}
