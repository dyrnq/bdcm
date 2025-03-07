package com.dyrnq.bdcm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeDir {
    static Logger logger = LoggerFactory.getLogger(HomeDir.class);

    private String homeAbsolutePath;
    private String tmpAbsolutePath;

    private String acmeHome;

    private String acmeShDir;
    private String acmeSh;


    public HomeDir() {
        super();
    }

    public HomeDir(String homeAbsolutePath,
                   String tmpAbsolutePath,
                   String acmeShDir,
                   String acmeSh,
                   String acmeHome) {
        this.homeAbsolutePath = homeAbsolutePath;
        this.tmpAbsolutePath = tmpAbsolutePath;
        this.acmeSh = acmeSh;
        this.acmeShDir = acmeShDir;
        this.acmeHome = acmeHome;
    }

    public String getHomeAbsolutePath() {
        return homeAbsolutePath;
    }

    public String getTmpAbsolutePath() {
        return tmpAbsolutePath;
    }

    public String getAcmeHome() {
        return acmeHome;
    }

    public String getAcmeshDir() {
        return acmeShDir;
    }

    public String getAcmeSh() {
        return acmeSh;
    }

//    public void setAcmeSh(String acmeSh) {
//        this.acmeSh = acmeSh;
//    }
}
