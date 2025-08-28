package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolApp;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

//@Mapper
public interface ToolAppMapper {

    String SELECT_FIELDS = "id, name, decription, summary, developer, version, rating";
    String TABLE_NAME = "aitool.tool_app";

    @Insert("INSERT INTO " + TABLE_NAME + " (id, name, decription, summary, developer, version, rating) " +
            "VALUES (#{id}, #{name}, #{description}, #{summary}, #{developer}, #{version}, #{rating})")
    int insertToolsApp(AIPortToolApp app);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE name = #{name}")
    AIPortToolApp selectToolsAppByName(@Param("name") String name);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME)
    List<AIPortToolApp> selectAllToolsApp();

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME +" WHERE id=#{id}")
    List<AIPortToolApp> selectToolsAppById(long id);

    @Update("UPDATE " + TABLE_NAME + " SET rating = #{rating} WHERE id = #{id}")
    int updateToolsAppRating(@Param("id") long id, @Param("rating") int rating);
}
