package com.puravida.modules.menu.web.controller;

import com.puravida.modules.menu.application.dto.DishImageContent;
import com.puravida.modules.menu.application.port.in.GetDishImagePort;
import com.puravida.shared.web.ApiPaths;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/dishes")
public class DishImageController {

    private final GetDishImagePort getDishImagePort;

    public DishImageController(GetDishImagePort getDishImagePort) {
        this.getDishImagePort = getDishImagePort;
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") Integer dishId) {
        DishImageContent image = getDishImagePort.get(dishId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.mediaType()))
                .contentLength(image.content().length)
                .eTag(Character.toString(34) + image.etag() + Character.toString(34))
                .body(image.content());
    }
}