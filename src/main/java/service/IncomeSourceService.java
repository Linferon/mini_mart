package service;

import dao.impl.IncomeSourceDao;
import exception.nsee.SourceNotFoundException;
import model.IncomeSource;
import util.LoggerUtil;

import java.util.List;
import java.util.function.Supplier;

public class IncomeSourceService {
    private final IncomeSourceDao sourceDao = new IncomeSourceDao();
    private static IncomeSourceService instance;

    private IncomeSourceService() {}

    public static synchronized IncomeSourceService getInstance() {
        if (instance == null) {
            instance = new IncomeSourceService();
        }
        return instance;
    }
    
    public List<IncomeSource> getAllIncomeSources() {
        return findAndValidate(sourceDao::findAll, "Источники дохода не найдены");
    }

    public IncomeSource getIncomeSourceById(Long id) {
        return sourceDao.findById(id)
                .orElseThrow(() -> new SourceNotFoundException("Источник дохода с ID " + id + " не найден"));
    }

    private List<IncomeSource> findAndValidate(Supplier<List<IncomeSource>> supplier, String errorMessage) {
        List<IncomeSource> sources = supplier.get();
        
        if (sources.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new SourceNotFoundException(errorMessage);
        }
        
        LoggerUtil.info("Получено источников дохода: " + sources.size());
        return sources;
    }
}