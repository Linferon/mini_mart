package service.impl;

import dao.impl.IncomeSourceDao;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.SourceNotFoundException;
import model.IncomeSource;
import service.IncomeSourceService;
import service.UserService;
import util.LoggerUtil;

import java.util.List;
import java.util.function.Supplier;

public class IncomeSourceServiceImpl implements IncomeSourceService {
    private final IncomeSourceDao sourceDao = new IncomeSourceDao();
    private final UserService userService = new UserServiceImpl();

    private static final String ROLE_DIRECTOR = "Директор";
    private static final String ROLE_ACCOUNTANT = "Бухгалтер";

    @Override
    public List<IncomeSource> getAllIncomeSources() {
        checkManagementPermission();
        return findAndValidate(sourceDao::findAll, "Источники дохода не найдены");
    }

    @Override
    public IncomeSource getIncomeSourceById(Long id) {
        checkManagementPermission();
        return sourceDao.findById(id)
                .orElseThrow(() -> new SourceNotFoundException("Источник дохода с ID " + id + " не найден"));
    }

    @Override
    public IncomeSource getIncomeSourceByName(String name) {
        checkManagementPermission();
        return sourceDao.findByName(name)
                .orElseThrow(() -> new SourceNotFoundException("Источник дохода с названием '" + name + "' не найден"));
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

    private void checkAuthentication() {
        if (!userService.isAuthenticated()) {
            throw new AuthenticationException("Пользователь не авторизован");
        }
    }

    private void checkManagementPermission() {
        checkAuthentication();
        
        if (!userService.hasRole(ROLE_DIRECTOR, ROLE_ACCOUNTANT)) {
            throw new AuthorizationException("Только директор или бухгалтер может управлять источниками дохода");
        }
    }
}