package team.phoenix.backend.service;

import java.util.List;

import team.phoenix.backend.domain.model.Funcionario;

public interface FuncionarioService {
    List<Funcionario> listActive();

    void softDelete(String id);
}