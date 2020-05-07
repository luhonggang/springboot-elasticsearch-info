package com.tt.retrieval.controller;

import com.alibaba.fastjson.JSON;
import com.tt.retrieval.common.GlobalIndexEnum;
import com.tt.retrieval.common.dto.BaseResponseDto;
import com.tt.retrieval.common.dto.EsInfoDocDto;
import com.tt.retrieval.common.dto.ResultDto;
import com.tt.retrieval.common.vo.ConditionVo;
import com.tt.retrieval.common.vo.EsInfoDocVo;
import com.tt.retrieval.common.vo.TimeLineBasicInfoVo;
import com.tt.retrieval.service.RestHighLevelClientService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/info")
public class EsController {

    @Resource
    RestHighLevelClientService restHighLevelClientService;


    /**
     * run --> success
     * 创建索引接口
     *
     * @param basicInfoVo ES中存储的数据结构
     * @return ResultDto
     */
    @PostMapping("create-index")
    public ResultDto createIndex(@RequestBody TimeLineBasicInfoVo basicInfoVo) {
        Settings settings = Settings.builder()
                .put("index.number_of_shards", 5)
                .put("index.number_of_replicas", 2).build();
        log.info(" 创建索引请求参数：{}", basicInfoVo);
        return restHighLevelClientService.createIndex(GlobalIndexEnum.GLOBAL_INDEX.getValues(), settings, JSON.toJSONString(basicInfoVo));
    }

    /**
     * run --> success
     * 新增数据到ES 接口
     *
     * @param basicInfoVo ES中存储的数据结构
     * @return ResultDto
     */
    @PostMapping("create-info")
    public ResultDto createInfo(@RequestBody TimeLineBasicInfoVo basicInfoVo) {
        log.info(" 数据存入ES请求参数：{}", basicInfoVo);
        return restHighLevelClientService.createInfo(GlobalIndexEnum.GLOBAL_INDEX.getValues(), basicInfoVo.getId(), JSON.toJSONString(basicInfoVo));
    }

    /**
     * 文档更新
     *
     * @param basicInfoVo 数据
     * @return ResultDto
     */
    @PostMapping("update-info")
    public ResultDto updateInfo(@RequestBody TimeLineBasicInfoVo basicInfoVo) {
        boolean exists = restHighLevelClientService.existsTheDoc(GlobalIndexEnum.GLOBAL_INDEX.getValues(), basicInfoVo.getId());
        if (exists) {
            log.info(" ####################### 存在该条记录 #######################");
        } else {
            log.info(" ####################### 不存在该记录 #######################");
        }
        return restHighLevelClientService.updateInfo(GlobalIndexEnum.GLOBAL_INDEX.getValues(), basicInfoVo.getId(), JSON.toJSONString(basicInfoVo));
    }


    /**
     * 分词 --> 索引创建 并新增记录
     * @param vo 数据
     * @return  ResultDto
     */
    @PostMapping("ik-use")
    public  ResultDto ikUse(@RequestBody EsInfoDocVo vo){
        return restHighLevelClientService.ikUse(vo);
    }


    /**
     * 分词 --> 查询测试是否真有效
     * @param key 查询条件
     * @return  ResultDto
     */
    @GetMapping("search-ik")
    public  ResultDto searchIk(@RequestParam("key") String key){
        return restHighLevelClientService.searchIk(key);
    }



    /**
     * 高亮搜索
     *
     * @param conditionVo 条件
     * @return 返回实体
     */
    @PostMapping("/search-info")
    public BaseResponseDto searchInfo(@RequestBody ConditionVo conditionVo) {
        return restHighLevelClientService.findContentWithHighlight(conditionVo);
    }

    /**
     * 全文档匹配搜索关键词 且高亮显示 特定的关键词 而非任意文档匹配的关键词
     *
     * @param conditionVo 条件
     * @return 返回实体
     */
    @PostMapping("/search-all-info")
    public BaseResponseDto searchAllInfo(@RequestBody ConditionVo conditionVo) {
        return restHighLevelClientService.searchAllInfo(conditionVo);
    }

    /**
     * 精确检索
     *
     * @param conditionVo 条件
     * @return 返回实体
     */
    @PostMapping("/search-phrase-query-info")
    public BaseResponseDto searchPhraseQueryInfo(@RequestBody ConditionVo conditionVo) {
        return restHighLevelClientService.searchPhraseQueryInfo(conditionVo);
    }


    @GetMapping("/es")
    public String testHigh(HttpServletResponse httpServletResponse) throws IOException {
        String source = "{\n" +
                "  \"name\" : \"耐苦无领运动半袖\",\n" +
                "  \"price\" : 300,\n" +
                "  \"num\" : 800,\n" +
                "  \"date\" : \"2019-07-28\"\n" +
                "}";

        IndexResponse response = restHighLevelClientService.addDoc("idx_doc", source);

        System.out.println(" 请求成功打印响应结果 : " + response.status());

        return response.toString();
    }
}
