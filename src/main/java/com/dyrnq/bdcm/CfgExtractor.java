package com.dyrnq.bdcm;

public class CfgExtractor {
    private final String tokenCookieName;

    public CfgExtractor(String tokenCookieName) {
        this.tokenCookieName = tokenCookieName;
    }

    public String tokenCookieName() {
        return this.tokenCookieName;
    }
}
