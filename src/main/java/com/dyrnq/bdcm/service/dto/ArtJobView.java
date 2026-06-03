package com.dyrnq.bdcm.service.dto;

import com.dyrnq.bdcm.model.ArtJob;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.noear.wood.annotation.Column;

@EqualsAndHashCode(callSuper = true)
@Data()
public class ArtJobView extends ArtJob {
    @Column("art_name")
    private String artName;

    @Column("art_url")
    private String artUrl;
}
