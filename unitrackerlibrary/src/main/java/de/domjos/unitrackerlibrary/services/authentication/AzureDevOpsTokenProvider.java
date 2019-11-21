package de.domjos.unitrackerlibrary.services.authentication;

import de.domjos.unitrackerlibrary.services.engine.Authentication;

public final class AzureDevOpsTokenProvider implements AccessTokenProvider {
    private Authentication authentication;
    private String token;

    public AzureDevOpsTokenProvider(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public String token() {
        return this.refreshToken();
    }

    @Override
    public String refreshToken() {
        return this.token;
    }

    @Override
    public String authorization() {
        return "Bearer " + this.token;
    }
}
