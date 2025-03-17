package com.dyrnq.bdcm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomeDir {
    static Logger logger = LoggerFactory.getLogger(HomeDir.class);

    private String homeAbsolutePath;
    private String tmpAbsolutePath;

}
