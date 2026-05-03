package team.phoenix.backend.employee.application;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import team.phoenix.backend.audit.application.AuditLogService;
import team.phoenix.backend.audit.domain.AuditAction;
import team.phoenix.backend.audit.domain.AuditResourceType;
import team.phoenix.backend.domain.model.Funcionario;
import team.phoenix.backend.domain.model.HrRecord;
import team.phoenix.backend.domain.repository.FuncionarioRepository;
import team.phoenix.backend.domain.repository.HrRecordRepository;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock FuncionarioRepository funcionarioRepository;
    @Mock HrRecordRepository hrRecordRepository;
    @Mock AuditLogService auditLogService;
    @InjectMocks FuncionarioServiceImpl service;

    @Test
    void softDelete_whenActive_savesAndRecordsAuditLog() {
        var id = new ObjectId();
        var funcionario = Funcionario.builder()
            .id(id)
            .matricula("MATRIC-1")
            .ativo(true)
            .build();
        when(funcionarioRepository.findById(id)).thenReturn(Optional.of(funcionario));

        service.softDelete(id.toHexString());

        verify(funcionarioRepository).save(funcionario);
        verify(auditLogService).record(argThat(entry ->
            entry.action() == AuditAction.SOFT_DELETE
                && entry.resourceType() == AuditResourceType.FUNCIONARIO
                && entry.resourceId().equals(id.toHexString())
        ));
    }

    @Test
    void reactivate_whenInactive_savesAndRecordsAuditLog() {
        var id = new ObjectId();
        var funcionario = Funcionario.builder()
            .id(id)
            .matricula("MATRIC-1")
            .ativo(false)
            .build();
        when(funcionarioRepository.findById(id)).thenReturn(Optional.of(funcionario));

        service.reactivate(id.toHexString());

        verify(funcionarioRepository).save(funcionario);
        verify(auditLogService).record(argThat(entry ->
            entry.action() == AuditAction.RESTORE
                && entry.resourceType() == AuditResourceType.FUNCIONARIO
                && entry.resourceId().equals(id.toHexString())
        ));
    }

    @Test
    void consolidateFromHrRecords_savesEmployeesAndRecordsAuditLog() {
        var hrRecord = HrRecord.builder()
            .matricula("MATRIC-1")
            .dataRef(LocalDate.of(2025, 7, 1))
            .codMarca(10)
            .codLoja(1)
            .codCargo(100)
            .build();
        when(hrRecordRepository.findLatestByEachMatricula()).thenReturn(List.of(hrRecord));
        when(funcionarioRepository.findByMatricula("MATRIC-1")).thenReturn(Optional.empty());

        service.consolidateFromHrRecords();

        verify(funcionarioRepository).saveAll(argThat(funcionarios -> funcionarios.iterator().hasNext()));
        verify(auditLogService).record(argThat(entry ->
            entry.action() == AuditAction.CONSOLIDATE
                && entry.resourceType() == AuditResourceType.FUNCIONARIO
                && entry.resourceId().equals("hr_records")
        ));
    }
}
