package de.domjos.unitrackerlibrary.services.authentication;

public interface AccessTokenProvider {

    String authType();
    String token();
    String refreshToken();
}
