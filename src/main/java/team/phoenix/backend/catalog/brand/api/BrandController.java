package team.phoenix.backend.catalog.brand.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import team.phoenix.backend.domain.model.Brand;
import team.phoenix.backend.catalog.brand.application.BrandService;

@RestController
@RequestMapping("/api/brands")
@CrossOrigin
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<List<BrandResponse>> listBrands(
            @RequestParam(required = false) Integer codigo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String descricao) {
        var brands = brandService.listBrands(codigo, nome, descricao)
            .stream().map(BrandResponse::from).toList();
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBrand(@PathVariable String id) {
        var brand = brandService.getBrandById(id);
        if (brand.isPresent()) {
            return ResponseEntity.ok(BrandResponse.from(brand.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> createBrand(@RequestBody CreateBrandRequest req) {
        try {
            var brand = Brand.builder()
                .codigo(req.codigo())
                .nome(req.nome())
                .descricao(req.descricao())
                .build();

            var created = brandService.createBrand(brand);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(BrandResponse.from(created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar marca: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBrand(@PathVariable String id, @RequestBody CreateBrandRequest req) {
        try {
            var updated = Brand.builder()
                .codigo(req.codigo())
                .nome(req.nome())
                .descricao(req.descricao())
                .build();

            var brand = brandService.updateBrand(id, updated);
            return ResponseEntity.ok(BrandResponse.from(brand));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar marca: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBrand(@PathVariable String id) {
        try {
            brandService.deleteBrand(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao deletar marca: " + e.getMessage());
        }
    }
}
