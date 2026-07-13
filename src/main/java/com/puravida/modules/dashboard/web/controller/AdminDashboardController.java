package com.puravida.modules.dashboard.web.controller;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.dashboard.application.dto.DashboardSummaryResponse;
import com.puravida.modules.dashboard.application.dto.TodayDashboardResponse;
import com.puravida.modules.dashboard.application.dto.TopDishesResponse;
import com.puravida.modules.dashboard.application.port.in.GetDashboardSummaryPort;
import com.puravida.modules.dashboard.application.port.in.GetTodayDashboardPort;
import com.puravida.modules.dashboard.application.port.in.GetTopDishesPort;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/admin/dashboard")
public class AdminDashboardController {

    private final GetTodayDashboardPort getTodayDashboardPort;
    private final GetDashboardSummaryPort getDashboardSummaryPort;
    private final GetTopDishesPort getTopDishesPort;
    private final AuthenticateBearerTokenPort authenticateBearerTokenPort;

    public AdminDashboardController(
            GetTodayDashboardPort getTodayDashboardPort,
            GetDashboardSummaryPort getDashboardSummaryPort,
            GetTopDishesPort getTopDishesPort,
            AuthenticateBearerTokenPort authenticateBearerTokenPort
    ) {
        this.getTodayDashboardPort = getTodayDashboardPort;
        this.getDashboardSummaryPort = getDashboardSummaryPort;
        this.getTopDishesPort = getTopDishesPort;
        this.authenticateBearerTokenPort = authenticateBearerTokenPort;
    }

    @GetMapping("/today")
    public ApiResponse<TodayDashboardResponse> getToday(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(getTodayDashboardPort.getToday(authenticatedUser));
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(getDashboardSummaryPort.getSummary(from, to, authenticatedUser));
    }

    @GetMapping("/top-dishes")
    public ApiResponse<TopDishesResponse> getTopDishes(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(getTopDishesPort.getTopDishes(from, to, authenticatedUser));
    }
}
