package io.jzheaux.springsecurity.resolutions.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserInfoResDTO {

    private String sub;
    @JsonProperty("authorization_group")
    private String authorizationGroup;
    @JsonProperty("app_id")
    private String appId;
    @JsonProperty("basic_start_authorization")
    private String basicStartAuthorization;

}
