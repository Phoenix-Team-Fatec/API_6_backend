package team.phoenix.backend.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import team.phoenix.backend.domain.model.Position;
import team.phoenix.backend.domain.repository.PositionRepository;

@ExtendWith(MockitoExtension.class)
class PositionImporterTest {

    @TempDir Path tempDir;
    @Mock PositionRepository positionRepository;

    @Test void importFromFile_skipsDuplicatesFromSheetAndDatabase() throws Exception {
        Path file = tempDir.resolve("BASE_COMMISS_FINAL.xlsx");
        writeWorkbook(file);
        var importer = new PositionImporter(positionRepository);
        var savedPositions = new AtomicReference<List<Position>>();

        when(positionRepository.findByCodigo(10)).thenReturn(Optional.empty());
        when(positionRepository.findByCodigo(20))
            .thenReturn(Optional.of(Position.builder().codigo(20).nome("GERENTE").descricao("GERENTE").build()));
        when(positionRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Position> positions = invocation.getArgument(0);
            savedPositions.set(positions);
            return positions;
        });

        int imported = importer.importFromFile(file.toString());

        assertThat(imported).isEqualTo(1);
        verify(positionRepository).saveAll(anyList());
        assertThat(savedPositions.get())
            .extracting(Position::getCodigo, Position::getNome, Position::getDescricao)
            .containsExactly(org.assertj.core.groups.Tuple.tuple(10, "VENDEDOR", "VENDEDOR"));
    }

    private void writeWorkbook(Path path) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Comissoes");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Cod_Marca");
            header.createCell(1).setCellValue("Descr_Marca");
            header.createCell(2).setCellValue("Cod_Cargo");
            header.createCell(3).setCellValue("Descri_Cargo");

            Row seller = sheet.createRow(1);
            seller.createCell(2).setCellValue(10);
            seller.createCell(3).setCellValue("VENDEDOR");

            Row duplicateSeller = sheet.createRow(2);
            duplicateSeller.createCell(2).setCellValue(10);
            duplicateSeller.createCell(3).setCellValue("VENDEDOR DUPLICADO");

            Row existingManager = sheet.createRow(3);
            existingManager.createCell(2).setCellValue(20);
            existingManager.createCell(3).setCellValue("GERENTE");

            try (FileOutputStream out = new FileOutputStream(path.toFile())) {
                workbook.write(out);
            }
        }
    }
}
