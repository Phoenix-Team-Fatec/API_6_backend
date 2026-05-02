package team.phoenix.backend.catalog.brand.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import team.phoenix.backend.domain.model.Brand;
import team.phoenix.backend.domain.repository.BrandRepository;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    public List<Brand> listBrands(Integer codigo, String nome, String descricao) {
        if (codigo != null) {
            return brandRepository.findByCodigo(codigo)
                .filter(b -> matchesNome(b, nome))
                .filter(b -> matchesDescricao(b, descricao))
                .map(List::of)
                .orElse(List.of());
        }
        if (nome != null && !nome.isBlank() && descricao != null && !descricao.isBlank()) {
            return brandRepository.findAll().stream()
                .filter(b -> matchesNome(b, nome))
                .filter(b -> matchesDescricao(b, descricao))
                .toList();
        }
        if (nome != null && !nome.isBlank()) {
            return brandRepository.findByNomeContainingIgnoreCase(nome);
        }
        if (descricao != null && !descricao.isBlank()) {
            return brandRepository.findByDescricaoContainingIgnoreCase(descricao);
        }
        return brandRepository.findAll();
    }

    @Override
    public Optional<Brand> getBrandById(String id) {
        return brandRepository.findById(id);
    }

    @Override
    public Brand createBrand(Brand brand) {
        validateRequiredFields(brand.getCodigo(), brand.getNome(), brand.getDescricao());

        if (brandRepository.findByCodigo(brand.getCodigo()).isPresent()) {
            throw new IllegalStateException("Marca com código já existente: " + brand.getCodigo());
        }

        brand.setNome(brand.getNome().trim());
        brand.setDescricao(brand.getDescricao().trim());
        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(null);

        return brandRepository.save(brand);
    }

    @Override
    public Brand updateBrand(String id, Brand updatedBrand) {
        Optional<Brand> existing = brandRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Marca não encontrada: " + id);
        }

        Brand current = existing.get();

        if (updatedBrand.getCodigo() != null) {
            Optional<Brand> byCodigo = brandRepository.findByCodigo(updatedBrand.getCodigo());
            if (byCodigo.isPresent() && !byCodigo.get().getId().equals(id)) {
                throw new IllegalStateException("Marca com código já existente: " + updatedBrand.getCodigo());
            }
            current.setCodigo(updatedBrand.getCodigo());
        }

        if (updatedBrand.getDescricao() != null) {
            if (updatedBrand.getDescricao().isBlank()) {
                throw new IllegalArgumentException("Descrição da marca é obrigatória");
            }
            current.setDescricao(updatedBrand.getDescricao().trim());
        }

        if (updatedBrand.getNome() != null) {
            if (updatedBrand.getNome().isBlank()) {
                throw new IllegalArgumentException("Nome da marca é obrigatório");
            }
            current.setNome(updatedBrand.getNome().trim());
        }

        current.setUpdatedAt(LocalDateTime.now());

        return brandRepository.save(current);
    }

    @Override
    public void deleteBrand(String id) {
        if (brandRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Marca não encontrada: " + id);
        }
        brandRepository.deleteById(id);
    }

    private void validateRequiredFields(Integer codigo, String nome, String descricao) {
        if (codigo == null) {
            throw new IllegalArgumentException("Código da marca é obrigatório");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da marca é obrigatório");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição da marca é obrigatória");
        }
    }

    private boolean matchesNome(Brand brand, String nome) {
        if (nome == null || nome.isBlank()) {
            return true;
        }
        return brand.getNome() != null && brand.getNome().toLowerCase().contains(nome.toLowerCase());
    }

    private boolean matchesDescricao(Brand brand, String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return true;
        }
        return brand.getDescricao() != null && brand.getDescricao().toLowerCase().contains(descricao.toLowerCase());
    }
}
