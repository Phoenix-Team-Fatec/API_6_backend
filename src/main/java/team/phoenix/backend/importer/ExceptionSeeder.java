package team.phoenix.backend.importer;

import org.springframework.stereotype.Component;
import team.phoenix.backend.domain.model.*;
import java.time.LocalDate;
import java.util.*;

// Componente que gera dados de excepções mensais (afastamentos, férias, bônus, sobrescritas de taxa)
@Component
public class ExceptionSeeder {

    // Constrói lista completa de excepções para todos os meses
    // Retorna: lista de MonthlyException
    public List<MonthlyException> buildAll() {
        List<MonthlyException> all = new ArrayList<>();
        all.addAll(july2025());
        all.addAll(august2025());
        all.addAll(december2025());
        return all;
    }

    // Constrói excepções para julho de 2025
    // Retorna: lista de exceções do mês
    private List<MonthlyException> july2025() {
        return List.of(
            absence(LocalDate.of(2025,7,1), "MATRIC-58",  LocalDate.of(2025,7,10), LocalDate.of(2025,7,25)),
            absence(LocalDate.of(2025,7,1), "MATRIC-124", LocalDate.of(2025,7,21), LocalDate.of(2025,7,25)),
            absence(LocalDate.of(2025,7,1), "MATRIC-400", LocalDate.of(2025,7,15), LocalDate.of(2025,7,17)),
            absence(LocalDate.of(2025,7,1), "MATRIC-485", LocalDate.of(2025,7,10), LocalDate.of(2025,7,29)),
            vacation(LocalDate.of(2025,7,1), "MATRIC-549", LocalDate.of(2025,7,10), LocalDate.of(2025,7,25)),
            vacation(LocalDate.of(2025,7,1), "MATRIC-183", LocalDate.of(2025,7,10), LocalDate.of(2025,7,25))
        );
    }

    // Constrói excepções para agosto de 2025
    // Retorna: lista de excepções do mês
    private List<MonthlyException> august2025() {
        List<MonthlyException> list = new ArrayList<>(List.of(
            absence(LocalDate.of(2025,8,1), "MATRIC-113", LocalDate.of(2025,8,4),  LocalDate.of(2025,8,11)),
            absence(LocalDate.of(2025,8,1), "MATRIC-126", LocalDate.of(2025,8,18), LocalDate.of(2025,9,8)),
            absence(LocalDate.of(2025,8,1), "MATRIC-137", LocalDate.of(2025,8,26), LocalDate.of(2025,9,5)),
            absence(LocalDate.of(2025,8,1), "MATRIC-115", LocalDate.of(2025,8,1),  LocalDate.of(2025,8,22)),
            vacation(LocalDate.of(2025,8,1), "MATRIC-103", LocalDate.of(2025,8,4),  LocalDate.of(2025,8,17)),
            vacation(LocalDate.of(2025,8,1), "MATRIC-127", LocalDate.of(2025,8,4),  LocalDate.of(2025,8,29)),
            MonthlyException.builder().yearMonth(LocalDate.of(2025,8,1)).type(ExceptionType.RATE_OVERRIDE)
                .codMarca(10).codCargo(300).overrideRate(0.0175)
                .rateType(RateType.ABSOLUTE).appliesToManagers(false).build()
        ));
        for (String m : List.of("MATRIC-134", "MATRIC-135", "MATRIC-14", "MATRIC-141",
                                "MATRIC-143", "MATRIC-144", "MATRIC-147", "MATRIC-148")) {
            list.add(MonthlyException.builder().yearMonth(LocalDate.of(2025,8,1))
                .type(ExceptionType.BONUS_FIXED).matricula(m).amount(500.0).build());
        }
        return list;
    }

    // Constrói excepções para dezembro de 2025
    // Retorna: lista de excepções do mês
    private List<MonthlyException> december2025() {
        var salesTiers = List.of(
            BonusTier.builder().minValue(40000.0).maxValue(50000.0).bonusAmount(3500.0).build(),
            BonusTier.builder().minValue(50000.01).maxValue(60000.0).bonusAmount(4000.0).build(),
            BonusTier.builder().minValue(60000.01).maxValue(null).bonusAmount(4500.0).build());
        var storeTiers = List.of(
            BonusTier.builder().minValue(120000.0).maxValue(140000.0).bonusAmount(5000.0).build(),
            BonusTier.builder().minValue(140000.01).maxValue(160000.0).bonusAmount(6000.0).build(),
            BonusTier.builder().minValue(160000.01).maxValue(null).bonusAmount(7000.0).build());
        return new ArrayList<>(List.of(
            absence(LocalDate.of(2025,12,1), "MATRIC-188", LocalDate.of(2025,12,3),  LocalDate.of(2025,12,10)),
            absence(LocalDate.of(2025,12,1), "MATRIC-5",   LocalDate.of(2025,11,10), LocalDate.of(2025,12,12)),
            absence(LocalDate.of(2025,12,1), "MATRIC-71",  LocalDate.of(2025,12,1),  LocalDate.of(2025,12,31)),
            vacation(LocalDate.of(2025,12,1), "MATRIC-318", LocalDate.of(2025,12,15), LocalDate.of(2026,1,2)),
            vacation(LocalDate.of(2025,12,1), "MATRIC-52",  LocalDate.of(2025,11,24), LocalDate.of(2025,12,13)),
            rateAdditive(LocalDate.of(2025,12,1), 40, 0.01),
            rateAdditive(LocalDate.of(2025,12,1), 50, 0.01),
            rateAdditive(LocalDate.of(2025,12,1), 60, 0.01),
            rateAdditive(LocalDate.of(2025,12,1), 30, 0.005),
            MonthlyException.builder().yearMonth(LocalDate.of(2025,12,1)).type(ExceptionType.SALES_BONUS_TIER)
                .codMarca(10).appliesToManagers(false).bonusTiers(salesTiers).build(),
            MonthlyException.builder().yearMonth(LocalDate.of(2025,12,1)).type(ExceptionType.SALES_BONUS_TIER)
                .codMarca(20).appliesToManagers(false).bonusTiers(salesTiers).build(),
            MonthlyException.builder().yearMonth(LocalDate.of(2025,12,1)).type(ExceptionType.STORE_BONUS_TIER)
                .appliesToManagers(true).bonusTiers(storeTiers).build()
        ));
    }

    // Cria excepção de afastamento
    // Parâm ym: ano-mês
    // Parâm mat: matrícula do funcionário
    // Parâm s: data de início
    // Parâm e: data de fim
    // Retorna: MonthlyException de afastamento
    private MonthlyException absence(LocalDate ym, String mat, LocalDate s, LocalDate e) {
        return MonthlyException.builder().yearMonth(ym).type(ExceptionType.ABSENCE)
            .matricula(mat).startDate(s).endDate(e).build();
    }

    // Cria excepção de férias
    // Parâm ym: ano-mês
    // Parâm mat: matrícula do funcionário
    // Parâm s: data de início
    // Parâm e: data de fim
    // Retorna: MonthlyException de férias
    private MonthlyException vacation(LocalDate ym, String mat, LocalDate s, LocalDate e) {
        return MonthlyException.builder().yearMonth(ym).type(ExceptionType.VACATION)
            .matricula(mat).startDate(s).endDate(e).build();
    }

    // Cria excepção de sobrescrita de taxa (aditivaa)
    // Parâm ym: ano-mês
    // Parâm codMarca: código da marca
    // Parâm delta: valor a acrescentar à taxa
    // Retorna: MonthlyException de sobrescrita (ADITIVA)
    private MonthlyException rateAdditive(LocalDate ym, Integer codMarca, double delta) {
        return MonthlyException.builder().yearMonth(ym).type(ExceptionType.RATE_OVERRIDE)
            .codMarca(codMarca).overrideRate(delta).rateType(RateType.ADDITIVE)
            .appliesToManagers(false).build();
    }
}
