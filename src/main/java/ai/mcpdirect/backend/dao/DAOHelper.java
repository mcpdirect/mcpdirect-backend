package ai.mcpdirect.backend.dao;

import ai.mcpdirect.backend.dao.entity.AIPortSystemProperty;
import ai.mcpdirect.backend.dao.mapper.AIPortSystemPropertyMapper;
import appnet.hstp.engine.util.JSON;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class DAOHelper<P extends AIPortSystemProperty,M extends AIPortSystemPropertyMapper<P>> implements ApplicationContextAware {
    protected SqlSessionFactory sqlSessionFactory;
    public SqlSessionFactory getSqlSessionFactory(){
        return sqlSessionFactory;
    }

    public String getSqlSessionFactoryBeanName(){
        return "sqlSessionFactory";
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        sqlSessionFactory = (SqlSessionFactory) applicationContext.getBean(getSqlSessionFactoryBeanName());
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(this::loadSystemProperties,0,60, TimeUnit.SECONDS);
    }

    public interface SqlExecutor<T>{
        T executeSql(SqlSession sqlSession) throws Exception;
    }

    public <T> T executeSql(SqlExecutor<T> executor) throws Exception {
        SqlSession sqlSession =  sqlSessionFactory.openSession(ExecutorType.BATCH);
        T t;
        try {
            t = executor.executeSql(sqlSession);
            sqlSession.commit();
        }catch (Exception e){
            sqlSession.rollback(true);
            throw e;
        }finally {
            sqlSession.close();
        }
        return t;
    }

    private M propertyMapper;

    public M getSystemPropertyMapper() {
        return propertyMapper;
    }

    @Autowired
    public void setSystemPropertyMapper(M mapper) {
        this.propertyMapper = mapper;
    }

    private long lastUpdated;
    public void loadSystemProperties(){
        try{
            for (AIPortSystemProperty property : getSystemPropertyMapper().getValidSystemPropertiesFrom(lastUpdated)) {
                praProperties.put(property.key,property);
                if(property.created>lastUpdated){
                    lastUpdated = property.created;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected final ConcurrentHashMap<String, AIPortSystemProperty> praProperties = new ConcurrentHashMap<>();

    public AIPortSystemProperty getSystemProperty(String key){
        return praProperties.get(key);
    }

    public interface PropertyValueConvertor<T>{
        T convert(Object o) throws Exception;
    }
    private final ConcurrentHashMap<String,Object> properties = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T getSystemProperty(String key, PropertyValueConvertor<T> convertor, T defaultValue){
        T i = null;
        try {
            i = (T)properties.get(key);
        }catch (Throwable e){
            properties.remove(key);
        }
        if (i == null) {
            try {
                i = convertor.convert(getSystemProperty(key).value);
                properties.put(key, i);
            }catch (Exception ignore){}
        }
        return i!=null?i:defaultValue;
    }
    public int getIntSystemProperty(String key,int defaultValue){
        return getSystemProperty(key,o-> Integer.parseInt(getSystemProperty(key).value),defaultValue);
    }
    public long getLongSystemProperty(String key,long defaultValue){
        return getSystemProperty(key,o-> Long.parseLong(getSystemProperty(key).value),defaultValue);
    }
    public String getStringSystemProperty(String key){
        return getSystemProperty(key).value;
    }
    public <T> T getObjectSystemProperty(String key,Class<T> type){
        return getSystemProperty(key, o-> JSON.fromJson(getSystemProperty(key).value,type),null);
    }

    public void setSystemProperty(P req){
        P property = propertyMapper.getSystemProperty(req.key, req.archived);
        if(property!=null){
            if(req.archived>0) {
                req = property;
                req.archived = 0;
                property = propertyMapper.getSystemProperty(req.key, 0);
            }
            property.archived = System.currentTimeMillis();
            propertyMapper.addSystemProperty(property);
            propertyMapper.updateSystemProperty(req);
        }else if(req.archived==0){
            propertyMapper.addSystemProperty(req);
        }
    }
}