package ai.mcpdirect.backend.dao.mapper;

import ai.mcpdirect.backend.dao.entity.AIPortSystemProperty;

import java.util.List;

public interface AIPortSystemPropertyMapper<T extends AIPortSystemProperty> {
    List<T> getValidSystemProperties();
    List<T> getValidSystemPropertiesFrom(long from);
    List<T> getSystemProperties(String key, boolean archived);
    T getSystemProperty(String key,long archived);
    void addSystemProperty(T property);
    void updateSystemProperty(T property);
    void updateSystemPropertyArchived(String key,long archived,long lastArchived);

}