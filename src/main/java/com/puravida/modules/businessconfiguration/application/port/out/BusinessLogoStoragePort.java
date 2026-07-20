package com.puravida.modules.businessconfiguration.application.port.out;

import com.puravida.modules.businessconfiguration.application.dto.BusinessLogoContent;
import com.puravida.modules.businessconfiguration.application.dto.StoredBusinessLogo;

public interface BusinessLogoStoragePort {
    StoredBusinessLogo store(byte[] content, String declaredMediaType);
    BusinessLogoContent load(String key);
    void delete(String key);
}
