package team.phoenix.backend.importer;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import team.phoenix.backend.domain.model.*;
import team.phoenix.backend.service.PseudoCodeGenerator;
import team.phoenix.backend.service.TextualRuleGenerator;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

// Servico que le dados de planilhas Excel e converte para objetos de dominio
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

    // Le taxas de comissao do arquivo Excel
    // Retorna: lista de CommissionRate
    public List<CommissionRate> readCommissionRates() throws IOException {
        List<CommissionRate> result = new ArrayList<>();
        LocalDate defaultData = LocalDate.now().withDayOfMonth(1);

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(COMMISSION_FILE))) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                CommissionRate rate = CommissionRate.builder()
                    .codMarca(parseInt(row.getCell(0)))
                    .descrMarca(parseString(row.getCell(1)))
                    .codCargo(parseInt(row.getCell(2)))
                    .descriCargo(parseString(row.getCell(3)))
                    .pctComiss(parseDouble(row.getCell(4)))
                    .data(defaultData)
                    .versao(1)
                    .isVigente(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(null)
                    .deletedAt(null)
                    .versoesAnteriores(new ArrayList<>())
                    .build();

                rate.setTextoOriginal(TextualRuleGenerator.generate(rate));
                rate.setExplicacao(PseudoCodeGenerator.generate(rate));
                result.add(rate);
            }
        }
        return result;
    }

    // Le registros de RH (funcionarios) de arquivos Excel
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

    // Cria funcionarios a partir dos registros de RH
    // Retorna: lista de Funcionario com dados basicos (sem comissao por enquanto)
    public List<Funcionario> readFuncionarios() throws IOException {
        List<HrRecord> hrRecords = readHrRecords();
        List<Funcionario> result = new ArrayList<>();
        
        for (HrRecord hr : hrRecords) {
            Cargo cargo = Cargo.builder()
                .codCargo(String.valueOf(hr.getCodCargo()))
                .descricaoDoCargo(hr.getDescriCargo())
                .build();
            
            Funcionario func = Funcionario.builder()
                .yearMonth(convertLocalDateToDate(hr.getDataRef()))
                .matricula(hr.getMatricula())
                .type("CLT")
                .startDate(hr.getDataAdmiss() != null ? convertLocalDateToDate(hr.getDataAdmiss()) : null)
                .endDate(hr.getDataDemiss() != null ? convertLocalDateToDate(hr.getDataDemiss()) : null)
                .appliesToManagers(false)
                .cargo(cargo)
                .comissao(null)
                .deletedAt(null)
                .build();
            
            result.add(func);
        }
        return result;
    }

    // Converte LocalDate para java.util.Date
    // Param localDate: data em LocalDate
    // Retorna: java.util.Date ou null se input null
    private java.util.Date convertLocalDateToDate(LocalDate localDate) {
        if (localDate == null) return null;
        return java.sql.Date.valueOf(localDate);
    }

    // Le registros de vendas de arquivos Excel
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

    // Converte celula Excel em LocalDate
    // Param cell: celula a converter
    // Retorna: LocalDate ou null se celula vazia/invalida
    private LocalDate parseDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell))
            return cell.getLocalDateTimeCellValue().toLocalDate();
        return null;
    }

    // Converte celula Excel em Integer
    // Param cell: celula a converter
    // Retorna: Integer ou null se celula vazia
    private Integer parseInt(Cell cell) {
        if (cell == null) return null;
        return (int) cell.getNumericCellValue();
    }

    // Converte celula Excel em Double
    // Param cell: celula a converter
    // Retorna: Double ou null se celula vazia
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
