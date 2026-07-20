package com.puravida.modules.menu.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.StoredDishImage;
import com.puravida.modules.menu.application.port.out.DishImageStoragePort;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ConflictException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateDishImageUseCaseTest {

    @Mock
    private DishRepositoryPort dishRepositoryPort;

    @Mock
    private DishImageStoragePort storagePort;

    @Mock
    private MenuAuthorizationService authorizationService;

    @InjectMocks
    private UpdateDishImageUseCase useCase;

    @Test
    void replacesImageForActiveDishAndReturnsPublicUrl() {
        AuthenticatedUser actor = actor();
        byte[] content = new byte[]{1, 2, 3};
        Dish current = TestMenuData.dish().updateImage("old-key.png");
        when(authorizationService.requireEncargada(actor)).thenReturn(TestMenuData.encargada());
        when(dishRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(current));
        when(storagePort.store(content, "image/png"))
                .thenReturn(new StoredDishImage("new-key.png", "image/png", content.length));
        when(dishRepositoryPort.save(any(Dish.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.update(10, content, "image/png", actor);

        ArgumentCaptor<Dish> savedDish = ArgumentCaptor.forClass(Dish.class);
        verify(dishRepositoryPort).save(savedDish.capture());
        assertThat(savedDish.getValue().imagenKey()).isEqualTo("new-key.png");
        assertThat(response.imagenUrl()).isEqualTo("/api/v1/dishes/10/image");
    }

    @Test
    void deletesStoredFileWhenDatabaseUpdateFailsOutsideTransaction() {
        AuthenticatedUser actor = actor();
        byte[] content = new byte[]{1, 2, 3};
        when(authorizationService.requireEncargada(actor)).thenReturn(TestMenuData.encargada());
        when(dishRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(TestMenuData.dish()));
        when(storagePort.store(content, "image/png"))
                .thenReturn(new StoredDishImage("new-key.png", "image/png", content.length));
        when(dishRepositoryPort.save(any(Dish.class))).thenThrow(new RuntimeException("db failed"));

        assertThatThrownBy(() -> useCase.update(10, content, "image/png", actor))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db failed");

        verify(storagePort).delete("new-key.png");
    }

    @Test
    void rejectsInactiveDishWithoutStoringFile() {
        AuthenticatedUser actor = actor();
        when(authorizationService.requireEncargada(actor)).thenReturn(TestMenuData.encargada());
        when(dishRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(TestMenuData.inactiveDish()));

        assertThatThrownBy(() -> useCase.update(10, new byte[]{1}, "image/png", actor))
                .isInstanceOf(ConflictException.class);

        verify(storagePort, never()).store(any(), any());
    }

    private AuthenticatedUser actor() {
        return new AuthenticatedUser(2, "encargada@example.com", UserRole.ENCARGADA);
    }
}