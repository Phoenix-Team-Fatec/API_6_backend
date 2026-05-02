package team.phoenix.backend.catalog.position.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import team.phoenix.backend.domain.model.Position;
import team.phoenix.backend.domain.repository.PositionRepository;

@ExtendWith(MockitoExtension.class)
class PositionServiceTest {

    @Mock PositionRepository positionRepository;
    @InjectMocks PositionServiceImpl service;

    @Test void listPositions_noFilter_returnsAll() {
        var p1 = Position.builder().id("1").codigo(10).nome("VENDEDOR").descricao("Vendas").build();
        var p2 = Position.builder().id("2").codigo(20).nome("GERENTE").descricao("Gestão").build();
        when(positionRepository.findAll()).thenReturn(List.of(p1, p2));

        assertThat(service.listPositions(null, null, null)).containsExactly(p1, p2);
    }

    @Test void listPositions_filterByCodigo_returnsOne() {
        var p = Position.builder().id("1").codigo(10).nome("VENDEDOR").descricao("Vendas").build();
        when(positionRepository.findByCodigo(10)).thenReturn(Optional.of(p));

        assertThat(service.listPositions(10, null, null)).containsExactly(p);
    }

    @Test void listPositions_filterByNome_returnsFiltered() {
        var p = Position.builder().id("1").codigo(10).nome("VENDEDOR").descricao("Vendas").build();
        when(positionRepository.findByNomeContainingIgnoreCase("vendedor")).thenReturn(List.of(p));

        assertThat(service.listPositions(null, "vendedor", null)).containsExactly(p);
    }

    @Test void createPosition_withValidData_setsAuditAndSaves() {
        var input = Position.builder().codigo(10).nome(" VENDEDOR ").descricao(" Vendas ").build();
        when(positionRepository.findByCodigo(10)).thenReturn(Optional.empty());
        when(positionRepository.save(input)).thenReturn(input);

        var created = service.createPosition(input);

        assertThat(created.getNome()).isEqualTo("VENDEDOR");
        assertThat(created.getDescricao()).isEqualTo("Vendas");
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isNull();
        verify(positionRepository).save(input);
    }

    @Test void createPosition_withDuplicatedCodigo_throwsConflict() {
        when(positionRepository.findByCodigo(10))
            .thenReturn(Optional.of(Position.builder().id("1").codigo(10).nome("VENDEDOR").descricao("Vendas").build()));

        assertThatThrownBy(() -> service.createPosition(Position.builder().codigo(10).nome("VENDEDOR").descricao("Vendas").build()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cargo com código já existente: 10");
    }

    @Test void updatePosition_notFound_throwsNotFound() {
        when(positionRepository.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePosition("999", Position.builder().nome("NOVO").descricao("Nova descrição").build()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cargo não encontrado: 999");
    }

    @Test void updatePosition_updatesOnlySentFields() {
        var current = Position.builder()
            .id("1")
            .codigo(10)
            .nome("VENDEDOR")
            .descricao("Vendas")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
        when(positionRepository.findById("1")).thenReturn(Optional.of(current));
        when(positionRepository.findByCodigo(20)).thenReturn(Optional.empty());
        when(positionRepository.save(current)).thenReturn(current);

        var updated = service.updatePosition("1", Position.builder().codigo(20).nome(" GERENTE ").descricao(" Gestão ").build());

        assertThat(updated.getCodigo()).isEqualTo(20);
        assertThat(updated.getNome()).isEqualTo("GERENTE");
        assertThat(updated.getDescricao()).isEqualTo("Gestão");
        assertThat(updated.getUpdatedAt()).isNotNull();
        verify(positionRepository).save(current);
    }

    @Test void deletePosition_notFound_throwsNotFound() {
        when(positionRepository.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletePosition("999"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cargo não encontrado: 999");
        verify(positionRepository, never()).deleteById("999");
    }
}
