package com.puravida.modules.menu.web.controller;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.menu.application.dto.CreateDishRequest;
import com.puravida.modules.menu.application.dto.DishResponse;
import com.puravida.modules.menu.application.port.in.CreateDishPort;
import com.puravida.modules.menu.application.port.in.DeleteDishPort;
import com.puravida.modules.menu.application.port.in.GetActiveDishesPort;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/admin/dishes")
public class AdminDishController {

    private final CreateDishPort createDishPort;
    private final GetActiveDishesPort getActiveDishesPort;
    private final DeleteDishPort deleteDishPort;
    private final AuthenticateBearerTokenPort authenticateBearerTokenPort;

    public AdminDishController(
            CreateDishPort createDishPort,
            GetActiveDishesPort getActiveDishesPort,
            DeleteDishPort deleteDishPort,
            AuthenticateBearerTokenPort authenticateBearerTokenPort
    ) {
        this.createDishPort = createDishPort;
        this.getActiveDishesPort = getActiveDishesPort;
        this.deleteDishPort = deleteDishPort;
        this.authenticateBearerTokenPort = authenticateBearerTokenPort;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DishResponse> create(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody CreateDishRequest request
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(createDishPort.create(request, authenticatedUser));
    }

    @GetMapping
    public ApiResponse<List<DishResponse>> getActive(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(getActiveDishesPort.getActive(authenticatedUser));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<DishResponse> delete(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("id") Integer dishId
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(deleteDishPort.delete(dishId, authenticatedUser));
    }
}
