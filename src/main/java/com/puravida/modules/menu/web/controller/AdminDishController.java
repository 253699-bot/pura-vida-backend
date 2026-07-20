package com.puravida.modules.menu.web.controller;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.menu.application.dto.CreateDishRequest;
import com.puravida.modules.menu.application.dto.DishResponse;
import com.puravida.modules.menu.application.port.in.CreateDishPort;
import com.puravida.modules.menu.application.port.in.DeleteDishPort;
import com.puravida.modules.menu.application.port.in.GetActiveDishesPort;
import com.puravida.modules.menu.application.port.in.UpdateDishImagePort;
import com.puravida.modules.menu.application.port.in.UpdateDishPort;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/admin/dishes")
public class AdminDishController {

    private final CreateDishPort createDishPort;
    private final GetActiveDishesPort getActiveDishesPort;
    private final DeleteDishPort deleteDishPort;
    private final UpdateDishPort updateDishPort;
    private final UpdateDishImagePort updateDishImagePort;
    private final AuthenticateBearerTokenPort authenticateBearerTokenPort;

    public AdminDishController(
            CreateDishPort createDishPort,
            GetActiveDishesPort getActiveDishesPort,
            DeleteDishPort deleteDishPort,
            UpdateDishPort updateDishPort,
            UpdateDishImagePort updateDishImagePort,
            AuthenticateBearerTokenPort authenticateBearerTokenPort
    ) {
        this.createDishPort = createDishPort;
        this.getActiveDishesPort = getActiveDishesPort;
        this.deleteDishPort = deleteDishPort;
        this.updateDishPort = updateDishPort;
        this.updateDishImagePort = updateDishImagePort;
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

    @PutMapping("/{id}")
    public ApiResponse<DishResponse> update(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("id") Integer dishId,
            @Valid @RequestBody CreateDishRequest request
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(updateDishPort.update(dishId, request, authenticatedUser));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DishResponse> updateImage(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("id") Integer dishId,
            @RequestPart("file") MultipartFile file
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        try {
            return ApiResponse.ok(updateDishImagePort.update(
                    dishId, file.getBytes(), file.getContentType(), authenticatedUser
            ));
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo leer la imagen del platillo.", exception);
        }
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