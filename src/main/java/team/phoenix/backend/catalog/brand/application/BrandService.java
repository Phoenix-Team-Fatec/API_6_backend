package team.phoenix.backend.catalog.brand.application;

import java.util.List;
import java.util.Optional;

import team.phoenix.backend.domain.model.Brand;

public interface BrandService {

    List<Brand> listBrands(Integer codigo, String nome, String descricao);

    Optional<Brand> getBrandById(String id);

    Brand createBrand(Brand brand);

    Brand updateBrand(String id, Brand updatedBrand);

    void deleteBrand(String id);
}
