package team.phoenix.backend.importer;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import team.phoenix.backend.domain.model.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

// Serviço que lê dados de planilhas Excel e converte para objetos de domínio
@Service
public class DataImportService {

    static final List<String> RH_FILES = List.of(
        "docs/dom-rock/BASE RH/BASE RH_JUL25.xlsx",
        "docs/dom-rock/BASE RH/BASE RH_AGO25.xlsx",
        "docs/dom-rock/BASE RH/BASE RH_DEZ25.xlsx"
    );
    static final List<String> VENDAS_FILES = List.of(
        "docs/dom-rock/BASE_VENDAS/BASE_VENDAS_JUL25.xlsx",
        "docs/dom-rock/BASE_VENDAS/BASE_VENDAS_AGO25.xlsx",
        "docs/dom-rock/BASE_VENDAS/BASE_VENDAS_DEZ25.xlsx"
    );
    static final String COMMISSION_FILE = "docs/dom-rock/BASE_COMMISS_FINAL.xlsx";

    // Lê taxas de comissão do arquivo Excel
    // Retorna: lista de CommissionRate
    public List<CommissionRate> readCommissionRates() throws IOException {
        List<CommissionRate> result = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(COMMISSION_FILE))) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                result.add(CommissionRate.builder()
                    .codMarca(parseInt(row.getCell(0))).descrMarca(parseString(row.getCell(1)))
                    .codCargo(parseInt(row.getCell(2))).descriCargo(parseString(row.getCell(3)))
                    .pctComiss(parseDouble(row.getCell(4))).build());
            }
        }
        return result;
    }

    // Lê registros de RH (funcionários) de arquivos Excel
    // Retorna: lista de HrRecord
    public List<HrRecord> readHrRecords() throws IOException {
        List<HrRecord> result = new ArrayList<>();
        for (String path : RH_FILES) {
            try (Workbook wb = new XSSFWorkbook(new FileInputStream(path))) {
                Sheet sheet = wb.getSheetAt(0);
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    result.add(HrRecord.builder()
                        .dataRef(parseDate(row.getCell(0)))
                        .codMarca(parseInt(row.getCell(1))).descrMarca(parseString(row.getCell(2)))
                        .codLoja(parseInt(row.getCell(3))).descrLoja(parseString(row.getCell(4)))
                        .matricula(parseString(row.getCell(5)))
                        .dataAdmiss(parseDate(row.getCell(6))).dataDemiss(parseDate(row.getCell(7)))
                        .codCargo(parseInt(row.getCell(8))).descriCargo(parseString(row.getCell(9)))
                        .build());
                }
            }
        }
        return result;
    }

    // Lê registros de vendas de arquivos Excel
    // Retorna: lista de SalesRecord
    public List<SalesRecord> readSalesRecords() throws IOException {
        List<SalesRecord> result = new ArrayList<>();
        for (String path : VENDAS_FILES) {
            try (Workbook wb = new XSSFWorkbook(new FileInputStream(path))) {
                Sheet sheet = wb.getSheetAt(0);
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    result.add(SalesRecord.builder()
                        .dateRef(parseDate(row.getCell(0)))
                        .codMarca(parseInt(row.getCell(1))).descrMarca(parseString(row.getCell(2)))
                        .codLoja(parseInt(row.getCell(3))).descrLoja(parseString(row.getCell(4)))
                        .matricula(parseString(row.getCell(5))).vlrVenda(parseDouble(row.getCell(6)))
                        .build());
                }
            }
        }
        return result;
    }

    // Converte célula Excel em LocalDate
    // Parâm cell: célula a converter
    // Retorna: LocalDate ou null se célula vazia/inválida
    private LocalDate parseDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell))
            return cell.getLocalDateTimeCellValue().toLocalDate();
        return null;
    }

    // Converte célula Excel em Integer
    // Parâm cell: célula a converter
    // Retorna: Integer ou null se célula vazia
    private Integer parseInt(Cell cell) {
        if (cell == null) return null;
        return (int) cell.getNumericCellValue();
    }

    // Converte célula Excel em Double
    // Parâm cell: célula a converter
    // Retorna: Double ou null se célula vazia
    private Double parseDouble(Cell cell) {
        if (cell == null) return null;
        return cell.getNumericCellValue();
    }

    private String parseString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> cell.toString().trim();
        };
    }
}
