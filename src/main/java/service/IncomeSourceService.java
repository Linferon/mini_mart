package service;

import dao.impl.IncomeSourceDao;
import exception.nsee.SourceNotFoundException;
import model.IncomeSource;

import java.util.List;

import static util.EntityUtil.findAndValidate;

public class IncomeSourceService {
    private final IncomeSourceDao sourceDao;
    private static IncomeSourceService instance;

    private IncomeSourceService() {
        sourceDao = new IncomeSourceDao();
    }

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
}