package com.dyrnq.bdcm;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.noear.solon.annotation.BindProps;
import org.noear.solon.annotation.Configuration;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@BindProps(prefix = "repo")
public class RepoProps implements Serializable {
    private String type;
    private String externalUrl;
    private RepoLocal local = new RepoLocal();
    private RepoS3 s3 = new RepoS3();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RepoS3 implements Serializable {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RepoLocal implements Serializable {
        private String path;
        private String listen;
        private String additionalPrefixMapping;
    }
}
