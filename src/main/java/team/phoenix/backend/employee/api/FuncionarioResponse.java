package team.phoenix.backend.employee.api;

import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import team.phoenix.backend.domain.model.Funcionario;

/**
 * DTO de resposta para dados de funcionário
 * Consolida informações de hr_records e funcionarios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FuncionarioResponse {
    
    private String id;
    private String matricula;
    private LocalDate dataRef;
    private Integer codMarca;
    private String descrMarca;
    private Integer codLoja;
    private String descrLoja;
    private LocalDate dataAdmiss;
    private LocalDate dataDemiss;
    private Integer codCargo;
    private String descriCargo;
    private boolean ativo;
    private Date criadoEm;
    private Date atualizadoEm;

    /**
     * Converte entidade Funcionario para FuncionarioResponse
     */
    public static FuncionarioResponse fromFuncionario(Funcionario funcionario) {
        return FuncionarioResponse.builder()
            .id(funcionario.getId().toString())
            .matricula(funcionario.getMatricula())
            .dataRef(funcionario.getDataRef())
            .codMarca(funcionario.getCodMarca())
            .descrMarca(funcionario.getDescrMarca())
            .codLoja(funcionario.getCodLoja())
            .descrLoja(funcionario.getDescrLoja())
            .dataAdmiss(funcionario.getDataAdmiss())
            .dataDemiss(funcionario.getDataDemiss())
            .codCargo(funcionario.getCodCargo())
            .descriCargo(funcionario.getDescriCargo())
            .ativo(funcionario.isAtivo())
            .criadoEm(funcionario.getCriadoEm())
            .atualizadoEm(funcionario.getAtualizadoEm())
            .build();
    }
}
