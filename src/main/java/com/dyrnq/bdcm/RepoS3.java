package com.dyrnq.bdcm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepoS3 {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
}
