package io.jzheaux.springsecurity.resolutions;


import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class Scratch {

    public static void main(String[] args) throws IOException {


        WebClient webClient = WebClient.create("https://sso-int.daimler.com/idp/userinfo.openid");

        Mono<String> monoRes = webClient.get()
                .headers(
                        httpHeaders -> {
                            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                            httpHeaders.setBearerAuth("0001hGuu41uLiIYq86YboowFlop8");
                        }
                )
                .retrieve()
                .bodyToMono(String.class);

        String stringRes = monoRes.block();

        System.out.println(stringRes);

    }


}
