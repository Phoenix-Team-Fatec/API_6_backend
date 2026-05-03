package team.phoenix.backend.catalog.brand.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
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

import team.phoenix.backend.audit.application.AuditLogService;
import team.phoenix.backend.audit.domain.AuditAction;
import team.phoenix.backend.audit.domain.AuditResourceType;
import team.phoenix.backend.domain.model.Brand;
import team.phoenix.backend.domain.repository.BrandRepository;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock BrandRepository brandRepository;
    @Mock AuditLogService auditLogService;
    @InjectMocks BrandServiceImpl service;

    @Test void listBrands_noFilter_returnsAll() {
        var b1 = Brand.builder().id("1").codigo(10).nome("PRETO").descricao("Linha preta").build();
        var b2 = Brand.builder().id("2").codigo(20).nome("CINZA").descricao("Linha cinza").build();
        when(brandRepository.findAll()).thenReturn(List.of(b1, b2));

        assertThat(service.listBrands(null, null, null)).containsExactly(b1, b2);
    }

    @Test void listBrands_filterByCodigo_returnsOne() {
        var b = Brand.builder().id("1").codigo(10).nome("PRETO").descricao("Linha preta").build();
        when(brandRepository.findByCodigo(10)).thenReturn(Optional.of(b));

        assertThat(service.listBrands(10, null, null)).containsExactly(b);
    }

    @Test void listBrands_filterByNome_returnsFiltered() {
        var b = Brand.builder().id("1").codigo(10).nome("PRETO").descricao("Linha preta").build();
        when(brandRepository.findByNomeContainingIgnoreCase("pre")).thenReturn(List.of(b));

        assertThat(service.listBrands(null, "pre", null)).containsExactly(b);
    }

    @Test void createBrand_withValidData_setsAuditAndSaves() {
        var input = Brand.builder().id("1").codigo(10).nome(" PRETO ").descricao(" Linha preta ").build();
        when(brandRepository.findByCodigo(10)).thenReturn(Optional.empty());
        when(brandRepository.save(input)).thenReturn(input);

        var created = service.createBrand(input);

        assertThat(created.getNome()).isEqualTo("PRETO");
        assertThat(created.getDescricao()).isEqualTo("Linha preta");
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isNull();
        verify(brandRepository).save(input);
        verify(auditLogService).record(argThat(entry ->
            entry.action() == AuditAction.CREATE
                && entry.resourceType() == AuditResourceType.BRAND
                && entry.resourceId().equals("1")
        ));
    }

    @Test void createBrand_withDuplicatedCodigo_throwsConflict() {
        when(brandRepository.findByCodigo(10))
            .thenReturn(Optional.of(Brand.builder().id("1").codigo(10).nome("PRETO").descricao("Linha preta").build()));

        assertThatThrownBy(() -> service.createBrand(Brand.builder().codigo(10).nome("PRETO").descricao("Linha preta").build()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Marca com código já existente: 10");
    }

    @Test void updateBrand_notFound_throwsNotFound() {
        when(brandRepository.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateBrand("999", Brand.builder().nome("NOVA").descricao("Nova descrição").build()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Marca não encontrada: 999");
    }

    @Test void updateBrand_updatesOnlySentFields() {
        var current = Brand.builder()
            .id("1")
            .codigo(10)
            .nome("PRETO")
            .descricao("Linha preta")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
        when(brandRepository.findById("1")).thenReturn(Optional.of(current));
        when(brandRepository.findByCodigo(20)).thenReturn(Optional.empty());
        when(brandRepository.save(current)).thenReturn(current);

        var updated = service.updateBrand("1", Brand.builder().codigo(20).nome(" CINZA ").descricao(" Linha cinza ").build());

        assertThat(updated.getCodigo()).isEqualTo(20);
        assertThat(updated.getNome()).isEqualTo("CINZA");
        assertThat(updated.getDescricao()).isEqualTo("Linha cinza");
        assertThat(updated.getUpdatedAt()).isNotNull();
        verify(brandRepository).save(current);
        verify(auditLogService).record(argThat(entry ->
            entry.action() == AuditAction.UPDATE
                && entry.resourceType() == AuditResourceType.BRAND
                && entry.resourceId().equals("1")
        ));
    }

    @Test void deleteBrand_whenFound_deletesAndRecordsAuditLog() {
        var current = Brand.builder()
            .id("1")
            .codigo(10)
            .nome("PRETO")
            .descricao("Linha preta")
            .build();
        when(brandRepository.findById("1")).thenReturn(Optional.of(current));

        service.deleteBrand("1");

        verify(brandRepository).deleteById("1");
        verify(auditLogService).record(argThat(entry ->
            entry.action() == AuditAction.DELETE
                && entry.resourceType() == AuditResourceType.BRAND
                && entry.resourceId().equals("1")
        ));
    }

    @Test void deleteBrand_notFound_throwsNotFound() {
        when(brandRepository.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteBrand("999"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Marca não encontrada: 999");
        verify(brandRepository, never()).deleteById("999");
    }
}
