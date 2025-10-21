package org.example.ootoutfitoftoday.domain.dashboard.service.query.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardQueryServiceImpl implements AdminDashboardQueryService {
}
