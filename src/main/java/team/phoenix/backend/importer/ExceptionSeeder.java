package team.phoenix.backend.importer;

import org.springframework.stereotype.Component;
import team.phoenix.backend.domain.model.*;
import java.time.LocalDate;
import java.util.*;

@Component
public class ExceptionSeeder {

    public List<MonthlyException> buildAll() {
        List<MonthlyException> all = new ArrayList<>();
        all.addAll(july2025());
        all.addAll(august2025());
        all.addAll(december2025());
        return all;
    }

    private List<MonthlyException> july2025() {
        return List.of(
            absence("2025-07", "MATRIC-58",  LocalDate.of(2025,7,10), LocalDate.of(2025,7,25)),
            absence("2025-07", "MATRIC-124", LocalDate.of(2025,7,21), LocalDate.of(2025,7,25)),
            absence("2025-07", "MATRIC-400", LocalDate.of(2025,7,15), LocalDate.of(2025,7,17)),
            absence("2025-07", "MATRIC-485", LocalDate.of(2025,7,10), LocalDate.of(2025,7,29)),
            vacation("2025-07", "MATRIC-549", LocalDate.of(2025,7,10), LocalDate.of(2025,7,25)),
            vacation("2025-07", "MATRIC-183", LocalDate.of(2025,7,10), LocalDate.of(2025,7,25))
        );
    }

    private List<MonthlyException> august2025() {
        List<MonthlyException> list = new ArrayList<>(List.of(
            absence("2025-08", "MATRIC-113", LocalDate.of(2025,8,4),  LocalDate.of(2025,8,11)),
            absence("2025-08", "MATRIC-126", LocalDate.of(2025,8,18), LocalDate.of(2025,9,8)),
            absence("2025-08", "MATRIC-137", LocalDate.of(2025,8,26), LocalDate.of(2025,9,5)),
            absence("2025-08", "MATRIC-115", LocalDate.of(2025,8,1),  LocalDate.of(2025,8,22)),
            vacation("2025-08", "MATRIC-103", LocalDate.of(2025,8,4),  LocalDate.of(2025,8,17)),
            vacation("2025-08", "MATRIC-127", LocalDate.of(2025,8,4),  LocalDate.of(2025,8,29)),
            MonthlyException.builder().yearMonth("2025-08").type(ExceptionType.RATE_OVERRIDE)
                .codMarca(10).codCargo(300).overrideRate(0.0175)
                .rateType(RateType.ABSOLUTE).appliesToManagers(false).build()
        ));
        for (String m : List.of("MATRIC-134", "MATRIC-135", "MATRIC-14", "MATRIC-141",
                                "MATRIC-143", "MATRIC-144", "MATRIC-147", "MATRIC-148")) {
            list.add(MonthlyException.builder().yearMonth("2025-08")
                .type(ExceptionType.BONUS_FIXED).matricula(m).amount(500.0).build());
        }
        return list;
    }

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
            absence("2025-12", "MATRIC-188", LocalDate.of(2025,12,3),  LocalDate.of(2025,12,10)),
            absence("2025-12", "MATRIC-5",   LocalDate.of(2025,11,10), LocalDate.of(2025,12,12)),
            absence("2025-12", "MATRIC-71",  LocalDate.of(2025,12,1),  LocalDate.of(2025,12,31)),
            vacation("2025-12", "MATRIC-318", LocalDate.of(2025,12,15), LocalDate.of(2026,1,2)),
            vacation("2025-12", "MATRIC-52",  LocalDate.of(2025,11,24), LocalDate.of(2025,12,13)),
            rateAdditive("2025-12", 40, 0.01),
            rateAdditive("2025-12", 50, 0.01),
            rateAdditive("2025-12", 60, 0.01),
            rateAdditive("2025-12", 30, 0.005),
            MonthlyException.builder().yearMonth("2025-12").type(ExceptionType.SALES_BONUS_TIER)
                .codMarca(10).appliesToManagers(false).bonusTiers(salesTiers).build(),
            MonthlyException.builder().yearMonth("2025-12").type(ExceptionType.SALES_BONUS_TIER)
                .codMarca(20).appliesToManagers(false).bonusTiers(salesTiers).build(),
            MonthlyException.builder().yearMonth("2025-12").type(ExceptionType.STORE_BONUS_TIER)
                .appliesToManagers(true).bonusTiers(storeTiers).build()
        ));
    }

    private MonthlyException absence(String ym, String mat, LocalDate s, LocalDate e) {
        return MonthlyException.builder().yearMonth(ym).type(ExceptionType.ABSENCE)
            .matricula(mat).startDate(s).endDate(e).build();
    }

    private MonthlyException vacation(String ym, String mat, LocalDate s, LocalDate e) {
        return MonthlyException.builder().yearMonth(ym).type(ExceptionType.VACATION)
            .matricula(mat).startDate(s).endDate(e).build();
    }

    private MonthlyException rateAdditive(String ym, Integer codMarca, double delta) {
        return MonthlyException.builder().yearMonth(ym).type(ExceptionType.RATE_OVERRIDE)
            .codMarca(codMarca).overrideRate(delta).rateType(RateType.ADDITIVE)
            .appliesToManagers(false).build();
    }
}
