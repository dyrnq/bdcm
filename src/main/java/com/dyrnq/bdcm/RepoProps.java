package com.dyrnq.bdcm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.noear.solon.annotation.BindProps;
import org.noear.solon.annotation.Configuration;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@BindProps(prefix="repo")
public class RepoProps implements Serializable {
    private String type;
    private RepoLocal local = new RepoLocal();
    private RepoS3 s3 = new RepoS3();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static
    public class RepoS3 implements Serializable {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static
    public class RepoLocal implements Serializable {
        private String path;
        private String listen;
    }
}
