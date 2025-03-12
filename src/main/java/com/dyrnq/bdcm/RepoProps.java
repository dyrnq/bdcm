package com.dyrnq.bdcm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.noear.solon.annotation.BindProps;
import org.noear.solon.annotation.Configuration;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@BindProps(prefix="repo")
public class RepoProps {
    private String type;
    private RepoLocal local = new RepoLocal();
    private RepoS3 s3 = new RepoS3();
}
