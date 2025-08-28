package ai.mcpdirect.backend.dao;

import ai.mcpdirect.backend.dao.entity.AIPortSystemProperty;
import ai.mcpdirect.backend.dao.mapper.account.AccountMapper;
import ai.mcpdirect.backend.dao.mapper.account.AccountSystemPropertyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountDataHelper extends DAOHelper<AIPortSystemProperty, AccountSystemPropertyMapper>{
    protected static AccountDataHelper INSTANCE;
    public static AccountDataHelper getInstance(){
        return INSTANCE;
    }

    public AccountDataHelper(){
        INSTANCE = this;
    }

    // private AccountSystemPropertyMapper propertyMapper;

    // public AccountSystemPropertyMapper getAccountSystemPropertyMapper() {
    //     return propertyMapper;
    // }

    // @Autowired
    // public void setAccountPropertyMapper(AccountSystemPropertyMapper mapper) {
    //     this.propertyMapper = mapper;
    // }

    // @Override
    // public void loadSystemProperties(){
    //     for (AIPortSystemProperty property : propertyMapper.getSystemProperties()) {
    //         praProperties.put(property.key,property);
    //     }
    // }
    private AccountMapper mapper;

    public AccountMapper getAccountMapper() {
        return mapper;
    }

    @Autowired
    public void setAccountMapper(AccountMapper mapper) {
        this.mapper = mapper;
    }
}
