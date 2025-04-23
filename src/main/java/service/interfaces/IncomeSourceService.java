package service.interfaces;

import model.IncomeSource;

import java.util.List;

public interface IncomeSourceService {
    List<IncomeSource> getAllIncomeSources();

    IncomeSource getIncomeSourceById(Long id);

    IncomeSource getIncomeSourceByName(String name);
}
