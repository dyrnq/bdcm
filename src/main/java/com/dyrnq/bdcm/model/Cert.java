package com.dyrnq.bdcm.model;

import org.noear.solon.core.handle.UploadedFile;
import org.noear.wood.annotation.Column;
import org.noear.wood.annotation.Exclude;
import org.noear.wood.annotation.PrimaryKey;
import org.noear.wood.annotation.Table;

@Table("cert")
public class Cert {
    @Column("id")
    @PrimaryKey
    private
    String id;

    @Column("domain")
    private
    String domain;

    @Column("cert")
    private
    String cert;

    @Column("ca_id")
    private
    String caId; //如果是自签名证书 关联数据库中ca表的id字段

    @Column("approach")
    private
    Integer approach;

    @Column("renew")
    private
    Integer renew;// 开启自动renew证书,1为开启


    @Column("supplier")
    private
    Integer supplier;


    @Column("encryption")
    private
    Integer encryption;

    @Column("subject")
    private
    String subject;

    @Column("challenge")
    private
    Integer challenge; // 使用 ACME 标准定义的验证方式来验证您对证书中域名的控制权。80=HTTP-01  53=DNS-01
    //https://letsencrypt.org/zh-cn/docs/challenge-types/

    //在关系型数据库中，字段名不应该使用数据库的保留字作为名称。_r 表示raw(data)
    @Column("key_r")
    private
    String key;

    @Column("not_after")
    private
    Long notAfter;

    @Column("not_before")
    private
    Long notBefore;

    @Column("aux")
    private
    String aux; //辅助字段

    @Column("inst_id")
    private
    String instId; //inst table id

    @Column("dnsapi")
    private
    String dnsapi; //对应 acme.sh --dns <dnsapi>

    @Exclude
    private UploadedFile certFile;
    @Exclude
    private UploadedFile keyFile;
    @Exclude
    private
    Integer issue;// 立即申请?
    @Exclude
    private
    String encryptionDisplay;

    @Exclude
    private
    String approachDisplay;

    @Exclude
    private
    String challengeDisplay;
    @Exclude
    private
    String supplierDisplay;

    public String getEncryptionDisplay() {
        return this.encryptionDisplay;
    }

    public void setEncryptionDisplay(String encryptionDisplay) {
        this.encryptionDisplay = encryptionDisplay;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public String getCaId() {
        return caId;
    }

    public void setCaId(String caId) {
        this.caId = caId;
    }

    public Integer getApproach() {
        return approach;
    }

    public void setApproach(Integer approach) {
        this.approach = approach;
    }

    public Integer getRenew() {
        return renew;
    }

    public void setRenew(Integer renew) {
        this.renew = renew;
    }

    public Integer getSupplier() {
        return supplier;
    }

    public void setSupplier(Integer supplier) {
        this.supplier = supplier;
    }

    public Integer getEncryption() {
        return encryption;
    }

    public void setEncryption(Integer encryption) {
        this.encryption = encryption;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getChallenge() {
        return challenge;
    }

    public void setChallenge(Integer challenge) {
        this.challenge = challenge;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Long notAfter) {
        this.notAfter = notAfter;
    }

    public String getAux() {
        return aux;
    }

    public void setAux(String aux) {
        this.aux = aux;
    }

    public String getInstId() {
        return instId;
    }

    public void setInstId(String instId) {
        this.instId = instId;
    }

    public UploadedFile getCertFile() {
        return certFile;
    }

    public void setCertFile(UploadedFile certFile) {
        this.certFile = certFile;
    }

    public UploadedFile getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(UploadedFile keyFile) {
        this.keyFile = keyFile;
    }


    public Long getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Long notBefore) {
        this.notBefore = notBefore;
    }

    public String getDnsapi() {
        return dnsapi;
    }

    public void setDnsapi(String dnsapi) {
        this.dnsapi = dnsapi;
    }

    public String getApproachDisplay() {
        return approachDisplay;
    }

    public void setApproachDisplay(String approachDisplay) {
        this.approachDisplay = approachDisplay;
    }

    public String getChallengeDisplay() {
        return challengeDisplay;
    }

    public void setChallengeDisplay(String challengeDisplay) {
        this.challengeDisplay = challengeDisplay;
    }

    public String getSupplierDisplay() {
        return supplierDisplay;
    }

    public void setSupplierDisplay(String supplierDisplay) {
        this.supplierDisplay = supplierDisplay;
    }

    public Integer getIssue() {
        return issue;
    }

    public void setIssue(Integer issue) {
        this.issue = issue;
    }
}
