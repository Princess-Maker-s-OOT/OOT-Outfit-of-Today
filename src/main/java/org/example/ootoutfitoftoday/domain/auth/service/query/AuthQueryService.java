package org.example.ootoutfitoftoday.domain.auth.service.query;

import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.dto.request.DeviceInfoResponse;

import java.util.List;

public interface AuthQueryService {

    List<DeviceInfoResponse> getDeviceList(AuthUser authUser, String currentDeviceId);
}
