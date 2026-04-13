package team.phoenix.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import team.phoenix.backend.domain.model.Funcionario;
import team.phoenix.backend.domain.repository.FuncionarioRepository;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock
    FuncionarioRepository funcionarioRepository;

    @InjectMocks
    FuncionarioServiceImpl funcionarioService;

    @Test
    void listActive_returnsOnlyRecordsFromActiveQuery() {
        var funcionario = Funcionario.builder()
            .id(new ObjectId())
            .matricula("123")
            .deletedAt(null)
            .build();

        when(funcionarioRepository.findByDeletedAtIsNull()).thenReturn(List.of(funcionario));

        var result = funcionarioService.listActive();

        assertThat(result).containsExactly(funcionario);
        verify(funcionarioRepository).findByDeletedAtIsNull();
    }

    @Test
    void softDelete_whenFound_setsDeletedAtAndSaves() {
        var id = new ObjectId();
        var funcionario = Funcionario.builder()
            .id(id)
            .matricula("123")
            .build();

        when(funcionarioRepository.findById(id)).thenReturn(Optional.of(funcionario));

        funcionarioService.softDelete(id.toHexString());

        assertThat(funcionario.getDeletedAt()).isNotNull();
        verify(funcionarioRepository).save(funcionario);
    }

    @Test
    void softDelete_whenAlreadyDeleted_doesNotSaveAgain() {
        var id = new ObjectId();
        var funcionario = Funcionario.builder()
            .id(id)
            .matricula("123")
            .deletedAt(new Date())
            .build();

        when(funcionarioRepository.findById(id)).thenReturn(Optional.of(funcionario));

        funcionarioService.softDelete(id.toHexString());

        verify(funcionarioRepository, never()).save(funcionario);
    }

    @Test
    void softDelete_whenNotFound_throwsIllegalArgumentException() {
        var id = new ObjectId();
        when(funcionarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> funcionarioService.softDelete(id.toHexString()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Funcionario not found");
    }
}