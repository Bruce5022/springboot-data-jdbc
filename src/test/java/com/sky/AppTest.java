package com.sky;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.tools.MapFormatHelper;
import com.sky.tools.SelectHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Unit test for simple App.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class AppTest {

    private static ObjectMapper om = new ObjectMapper();
    @Autowired
    private SelectHelper selectHelper;
    @Autowired
    private DataSource dataSource;

    @Test
    public void testConn() throws SQLException {
        System.out.println(dataSource.getClass());
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("select * from Cs_Parameter");
        // 展开结果集数据库
        while (rs.next()) {
            // 通过字段检索
            int id = rs.getInt("id");
            String parameterCode = rs.getString("parameterCode");
            String parameterName = rs.getString("parameterName");

            // 输出数据
            System.out.println("id: " + id + ",parameterCode:" + parameterCode + ",parameterName" + parameterName);
        }
        // 完成后关闭
        rs.close();
        stmt.close();
        connection.close();
    }

    @Test
    public void testTools() throws Exception {
        List<Map<String, Object>> maps = selectHelper.doQuery("select * from Cs_Parameter");
        for (Map<String, Object> map : maps) {
            System.out.println(MapFormatHelper.getIntValue(map,"id"));
        }
        System.out.println(om.writeValueAsString(maps));
    }
}
