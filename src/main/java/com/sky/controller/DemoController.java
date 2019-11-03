package com.sky.controller;

import com.sky.tools.MapFormatHelper;
import com.sky.tools.SelectHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DemoController {
    @Autowired
    private SelectHelper selectHelper;

    @RequestMapping("/home")
    public Object home() throws Exception {
        List<Map<String, Object>> maps = selectHelper.doQuery("select * from Cs_Parameter");
        for (Map<String, Object> map : maps) {
            System.out.println(MapFormatHelper.getIntValue(map,"id"));
        }
        return maps;
    }
}
