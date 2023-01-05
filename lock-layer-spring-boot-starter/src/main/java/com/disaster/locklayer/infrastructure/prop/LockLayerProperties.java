package com.disaster.locklayer.infrastructure.prop;

import com.disaster.locklayer.infrastructure.constant.Constants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Lock layer config.
 *
 * @author disaster
 * @version 1.0
 */
@ConfigurationProperties(prefix = Constants.CONFIG_PREFIX)
@Data
public class LockLayerProperties {
    private boolean enable = false;

}
