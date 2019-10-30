package de.domjos.unitrackerlibrary.services.authentication;

import org.json.JSONArray;
import org.json.JSONObject;

import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.engine.JSONEngine;

public final class GithubTokenProvider implements AccessTokenProvider {
    private Authentication authentication;
    private String token;

    public GithubTokenProvider(Authentication authentication) {
        this.authentication = authentication;
        this.token = this.authentication.getAPIKey();
    }

    @Override
    public String token() {
        if(this.token.equals("")) {
            this.token = this.refreshToken();
        }
        return this.token;
    }

    @Override
    public String refreshToken() {
        try {
            JSONEngine jsonEngine = new JSONEngine(this.authentication);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("client_secret", "b5d664ec43c104fe21b8dbf7699fbb6d82118ad8");
            jsonObject.put("client_id", "fb4ad3bae97a0a91e6a4");
            jsonObject.put("note", "UniTrackerMobile Authentication");
            JSONArray jsonArray = new JSONArray();
            jsonArray.put("user");
            jsonArray.put("public_repo");
            jsonArray.put("delete_repo");
            jsonObject.put("scopes", jsonArray);

            jsonEngine.executeRequest(this.authentication.getServer() + "/authorizations", jsonObject.toString(), "POST");
            if(jsonEngine.getCurrentState() == 200 || jsonEngine.getCurrentState() == 201) {
                return new JSONObject(jsonEngine.getCurrentMessage()).getString("token");
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public String authorization() {
        return this.authentication.getUserName() + ":" + this.token;
    }
}
