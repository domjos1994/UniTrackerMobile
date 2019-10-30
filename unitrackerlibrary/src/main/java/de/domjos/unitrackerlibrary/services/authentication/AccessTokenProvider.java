package de.domjos.unitrackerlibrary.services.authentication;

public interface AccessTokenProvider {

    String token();
    String refreshToken();
    String authorization();
}
