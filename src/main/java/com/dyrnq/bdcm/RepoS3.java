package com.dyrnq.bdcm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepoS3 implements Serializable {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
}
