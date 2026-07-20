package com.puravida.modules.menu.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.puravida.modules.menu.application.dto.DishImageContent;
import com.puravida.modules.menu.application.port.out.DishImageStoragePort;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.shared.domain.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetDishImageUseCaseTest {

    @Mock
    private DishRepositoryPort dishRepositoryPort;

    @Mock
    private DishImageStoragePort storagePort;

    @InjectMocks
    private GetDishImageUseCase useCase;

    @Test
    void loadsConfiguredDishImage() {
        var dish = TestMenuData.dish().updateImage("key.png");
        var image = new DishImageContent(new byte[]{1, 2}, "image/png", "etag");
        when(dishRepositoryPort.findById(10)).thenReturn(Optional.of(dish));
        when(storagePort.load("key.png")).thenReturn(image);

        assertThat(useCase.get(10)).isSameAs(image);
    }

    @Test
    void returnsNotFoundWhenDishHasNoImage() {
        when(dishRepositoryPort.findById(10)).thenReturn(Optional.of(TestMenuData.dish()));

        assertThatThrownBy(() -> useCase.get(10))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("La imagen del platillo no esta disponible.");
    }
}