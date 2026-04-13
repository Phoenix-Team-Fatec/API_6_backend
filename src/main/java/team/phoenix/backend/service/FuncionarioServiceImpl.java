package team.phoenix.backend.service;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import team.phoenix.backend.domain.repository.FuncionarioRepository;

@Service
@RequiredArgsConstructor
public class FuncionarioServiceImpl implements FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    @Override
    public List<team.phoenix.backend.domain.model.Funcionario> listActive() {
        return funcionarioRepository.findByDeletedAtIsNull();
    }

    @Override
    public void softDelete(String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Funcionario not found");
        }

        var funcionario = funcionarioRepository.findById(objectId)
            .orElseThrow(() -> new IllegalArgumentException("Funcionario not found"));

        if (funcionario.getDeletedAt() == null) {
            funcionario.setDeletedAt(new Date());
            funcionarioRepository.save(funcionario);
        }
    }
}