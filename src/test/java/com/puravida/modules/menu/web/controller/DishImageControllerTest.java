package com.puravida.modules.menu.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.puravida.modules.menu.application.dto.DishImageContent;
import com.puravida.modules.menu.application.port.in.GetDishImagePort;
import com.puravida.shared.domain.exception.NotFoundException;
import com.puravida.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = DishImageController.class, properties = "debug=false")
@Import(GlobalExceptionHandler.class)
class DishImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetDishImagePort getDishImagePort;

    @Test
    void returnsPublicDishImage() throws Exception {
        byte[] content = new byte[]{1, 2, 3};
        when(getDishImagePort.get(12)).thenReturn(new DishImageContent(content, "image/png", "abc123"));

        mockMvc.perform(get("/api/v1/dishes/12/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string(HttpHeaders.ETAG, "\"abc123\""))
                .andExpect(content().bytes(content));
    }

    @Test
    void returnsNotFoundWhenImageDoesNotExist() throws Exception {
        when(getDishImagePort.get(12)).thenThrow(new NotFoundException("La imagen del platillo no esta disponible."));

        mockMvc.perform(get("/api/v1/dishes/12/image"))
                .andExpect(status().isNotFound());
    }
}