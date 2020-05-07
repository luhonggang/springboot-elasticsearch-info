package com.tt.retrieval.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tt.retrieval.common.BaseException;
import com.tt.retrieval.common.GlobalIndexEnum;
import com.tt.retrieval.common.ResultCodeEnum;
import com.tt.retrieval.common.StringUtils;
import com.tt.retrieval.common.dto.BaseResponseDto;
import com.tt.retrieval.common.dto.EsInfoDocDto;
import com.tt.retrieval.common.dto.PageBeanDto;
import com.tt.retrieval.common.dto.ResultDto;
import com.tt.retrieval.common.vo.ConditionVo;
import com.tt.retrieval.common.vo.EsInfoDocVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class RestHighLevelClientService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 高亮搜索
     *
     * @param conditionVo 搜索条件
     * @return BaseResponseDto
     */
    public BaseResponseDto findContentWithHighlight(ConditionVo conditionVo) {
        Integer pageNo = conditionVo.getPageIndex();
        Integer pageSize = conditionVo.getPageSize();
        String userName = "userCacheName";
        SearchRequest searchRequest = new SearchRequest(GlobalIndexEnum.GLOBAL_INDEX_INFO.getValues());
        // 设置过滤条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 查询内容
        String condition = conditionVo.getInfoVo().getUserCacheName();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 匹配所有文档
        boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        boolQueryBuilder.must(QueryBuilders.matchPhraseQuery(userName, condition));

        // 关键词全文搜索筛选条件
        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery(condition);
        boolQueryBuilder.must(queryBuilder);
        //设置高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder().field(userName).requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);

        // 设置查询,可以是任何类型的 QueryBuilder
        sourceBuilder.query(boolQueryBuilder);
        // 设置确定结果要从哪个索引开始搜索的from选项,默认为0
        sourceBuilder.from(pageNo);
        // 设置确定搜素命中返回数的size选项，默认为10
        sourceBuilder.size(pageSize);
        // 设置一个可选的超时,控制允许搜索的时间
        log.info(" ########## 输出构造的条件体 ########## :{}", sourceBuilder.toString());
        // 条件封装到请求
        searchRequest.source(sourceBuilder);
        long begin = System.currentTimeMillis();
        StopWatch watch = new StopWatch("es-local");
        watch.start("rest-http-01");
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        watch.prettyPrint();
        watch.stop();
        log.info("耗时：{}秒", (System.currentTimeMillis() - begin) / 1000);
        // 获取搜索结果,SearchHits提供有关所有匹配的全局信息,例如总命中数或最高分数：
        SearchHits hits = searchResponse.getHits();
        // 匹配到总的记录数量
        long totalHits = hits.getTotalHits().value;
        // 得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        List<EsInfoDocDto> esInfoDocDtoList = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name;
            // 获取高亮查询的内容。如果存在，则替换原来的name
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null) {
                HighlightField nameField = highlightFields.get(userName);
                if (nameField != null) {
                    Text[] fragments = nameField.getFragments();
                    StringBuilder stringBuffer = new StringBuilder();
                    for (Text str : fragments) {
                        log.info("高亮内容 ： {}", str);
                        stringBuffer.append(str.string());
                    }

                    name = stringBuffer.toString().replace("\\", "");
                    sourceAsMap.put(userName, name);
                }
            }
            System.out.println(" 转换前的实体信息 sourceAsMap ： " + sourceAsMap);
//            JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
            JSONObject json = JSON.parseObject(JSON.toJSONString(sourceAsMap, SerializerFeature.WriteDateUseDateFormat));
            // 将高亮内容json数据转指定的Java对象接收
            EsInfoDocDto docDto = JSONObject.toJavaObject(json, EsInfoDocDto.class);
            log.info(" 转换后的实体信息 docDto ： {}", docDto);
            esInfoDocDtoList.add(docDto);
        }
        Integer totalRows = Integer.valueOf(totalHits + "");
        // 计算总的页数
        int totalPage = totalRows % pageSize == 0 ? totalRows / pageSize : (totalRows / pageSize + 1);
        PageBeanDto<List<EsInfoDocDto>> dto = new PageBeanDto<>();
        dto.setTotalPage(totalPage);
        dto.setPageNo(pageNo <= 0 ? 1 : pageNo);
        dto.setPageSize(pageSize);
        dto.setRows(totalRows);
        dto.setLists(esInfoDocDtoList);

        return BaseResponseDto.builder()
                .code(RestStatus.OK.getStatus())
                .message("查询成功")
                .data(dto)
                .build();
    }

    /**
     * 全文档匹配搜索关键词 且高亮显示 特定的关键词 而非任意文档匹配的关键词
     * MatchQuery 即全文检索，会对关键字进行分词后匹配词条 查询最大化搜索
     * operator：设置查询的结果取交集还是并集，并集用 or， 交集用 and
     *
     * @param conditionVo 条件
     * @return 返回实体
     */
    public BaseResponseDto searchAllInfo(ConditionVo conditionVo) {
        Integer pageNo = conditionVo.getPageIndex();
        Integer pageSize = conditionVo.getPageSize();
        String comment = "comment";
        // 内容
        String content = "content";
        // 标题
        String title = "infoTitle";
        SearchRequest searchRequest = new SearchRequest(GlobalIndexEnum.GLOBAL_INDEX_INFO.getValues());
        // 设置过滤条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 查询内容
        String condition = conditionVo.getInfoVo().getUserCacheName();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 匹配所有文档
        boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        // 从标题中进行检索
        boolQueryBuilder.must(QueryBuilders.matchQuery(title, condition).boost(2.0f));
        // 从内容中进行检索
        boolQueryBuilder.must(QueryBuilders.matchQuery(content, condition).boost(1.0f));
        // 从评论中进行检索
        boolQueryBuilder.must(QueryBuilders.matchQuery(comment, condition));
        // 关键词全文搜索筛选条件
//            boolQueryBuilder.must(QueryBuilders.queryStringQuery(condition).analyzer(title));
        //设置标题高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder().field(title).requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);

        // 设置查询,可以是任何类型的 QueryBuilder
        sourceBuilder.query(boolQueryBuilder);
        // 设置确定结果要从哪个索引开始搜索的from选项,默认为0
        sourceBuilder.from(pageNo);
        // 设置确定搜素命中返回数的size选项，默认为10
        sourceBuilder.size(pageSize);
        // 设置一个可选的超时,控制允许搜索的时间
        log.info(" ########## 输出构造的条件体 ########## :{}", sourceBuilder.toString());
        // 条件封装到请求
        searchRequest.source(sourceBuilder);
        long begin = System.currentTimeMillis();
        StopWatch watch = new StopWatch("es-all-search");
        watch.start("search-http-timeOut");
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        watch.stop();
        log.info(" 耗时毫秒数：{}", watch.prettyPrint());
        watch.start("handler-http-timeOut");
        // 获取搜索结果,SearchHits提供有关所有匹配的全局信息,例如总命中数或最高分数：
        SearchHits hits = searchResponse.getHits();
        // 匹配到总的记录数量
        long totalHits = hits.getTotalHits().value;
        // 得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        List<EsInfoDocDto> esInfoDocDtoList = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String titleContent;
            // 获取高亮查询的内容。如果存在，则替换原来的字段值为当前高亮处理后的内容
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null) {
                HighlightField nameField = highlightFields.get(title);
                if (nameField != null) {
                    Text[] fragments = nameField.getFragments();
                    StringBuilder stringBuffer = new StringBuilder();
                    for (Text str : fragments) {
                        log.info("高亮内容 ： {}", str);
                        stringBuffer.append(str.string());
                    }
                    titleContent = stringBuffer.toString();
                    sourceAsMap.put(title, titleContent);
                }
            }
            //            JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
            JSONObject json = JSON.parseObject(JSON.toJSONString(sourceAsMap, SerializerFeature.WriteDateUseDateFormat));
            // 将高亮内容json数据转指定的Java对象接收
            EsInfoDocDto docDto = JSONObject.toJavaObject(json, EsInfoDocDto.class);
            log.info(" 转换后的实体信息 docDto ： {}", docDto);
            esInfoDocDtoList.add(docDto);
        }
        Integer totalRows = Integer.valueOf(String.valueOf(totalHits));
        // 计算总的页数
        int totalPage = totalRows % pageSize == 0 ? totalRows / pageSize : (totalRows / pageSize + 1);
        PageBeanDto<List<EsInfoDocDto>> dto = new PageBeanDto<>();
        dto.setTotalPage(totalPage);
        dto.setPageNo(pageNo <= 0 ? 1 : pageNo);
        dto.setPageSize(pageSize);
        dto.setRows(totalRows);
        dto.setLists(esInfoDocDtoList);
        watch.stop();
        log.info(" 总的请求耗时毫秒数：{}", watch.prettyPrint());
        log.info(" 所有任务处理耗时详情：{}", JSON.toJSONString(watch.getTaskInfo()));
        return BaseResponseDto.builder()
                .code(RestStatus.OK.getStatus())
                .message("查询成功")
                .data(dto)
                .build();
    }

    /**
     * 精确查询 如搜索C股只会将C股或包含C股的数据拉取出
     * https://www.cnblogs.com/yjf512/p/4897294.html
     *
     * @param conditionVo 查询条件
     * @return BaseResponseDto
     */
    public BaseResponseDto searchPhraseQueryInfo(ConditionVo conditionVo) {
        Integer pageNo = conditionVo.getPageIndex();
        Integer pageSize = conditionVo.getPageSize();
//        String comment = "comment";
//        // 内容
//        String content = "content";
        // 标题
        String title = "infoTitle";
        SearchRequest searchRequest = new SearchRequest(GlobalIndexEnum.GLOBAL_INDEX_INFO.getValues());
        // 设置过滤条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 查询内容
        String condition = conditionVo.getInfoVo().getUserCacheName();
        // match_phrase 精确匹配所有同时包含如:"C股融资" 时使用 -> 做到完全的内容匹配
        MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery(title, condition);
        // 完全匹配可能比较严，我们会希望有个可调节因子，少匹配一个也满足，那就需要使用到slop
//        matchPhraseQueryBuilder.slop(100);
        //设置标题高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder().field(title).requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);

        //
        sourceBuilder.query(matchPhraseQueryBuilder);
        // 设置确定结果要从哪个索引开始搜索的from选项,默认为0
        sourceBuilder.from(pageNo);
        // 设置确定搜素命中返回数的size选项，默认为10
        sourceBuilder.size(pageSize);
        // 设置一个可选的超时,控制允许搜索的时间
        log.info(" ########## 输出构造的条件体 ########## :{}", sourceBuilder.toString());
        // 条件封装到请求
        searchRequest.source(sourceBuilder);
        long begin = System.currentTimeMillis();
        StopWatch watch = new StopWatch("es-term-query");
        watch.start("rest-http-timeOut");
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        watch.stop();
        log.info(" 耗时毫秒数：{}", watch.prettyPrint());
        watch.start("handler-http-timeOut");
        // 获取搜索结果,SearchHits提供有关所有匹配的全局信息,例如总命中数或最高分数：
        SearchHits hits = searchResponse.getHits();
        // 匹配到总的记录数量
        long totalHits = hits.getTotalHits().value;
        // 得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        List<EsInfoDocDto> esInfoDocDtoList = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String titleContent;
            // 获取高亮查询的内容。如果存在，则替换原来的字段值为当前高亮处理后的内容
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null) {
                HighlightField nameField = highlightFields.get(title);
                if (nameField != null) {
                    Text[] fragments = nameField.getFragments();
                    StringBuilder stringBuffer = new StringBuilder();
                    for (Text str : fragments) {
                        log.info("高亮内容 ： {}", str);
                        stringBuffer.append(str.string());
                    }
                    titleContent = stringBuffer.toString();
                    sourceAsMap.put(title, titleContent);
                }
            }
            //            JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
            JSONObject json = JSON.parseObject(JSON.toJSONString(sourceAsMap, SerializerFeature.WriteDateUseDateFormat));
            // 将高亮内容json数据转指定的Java对象接收
            EsInfoDocDto docDto = JSONObject.toJavaObject(json, EsInfoDocDto.class);
            log.info(" 转换后的实体信息 docDto ： {}", docDto);
            esInfoDocDtoList.add(docDto);
        }
        Integer totalRows = Integer.valueOf(String.valueOf(totalHits));
        // 计算总的页数
        int totalPage = totalRows % pageSize == 0 ? totalRows / pageSize : (totalRows / pageSize + 1);
        PageBeanDto<List<EsInfoDocDto>> dto = new PageBeanDto<>();
        dto.setTotalPage(totalPage);
        dto.setPageNo(pageNo <= 0 ? 1 : pageNo);
        dto.setPageSize(pageSize);
        dto.setRows(totalRows);
        dto.setLists(esInfoDocDtoList);
        watch.stop();
        log.info(" 总的请求耗时毫秒数：{}", watch.prettyPrint());
        log.info(" 所有任务处理耗时详情：{}", JSON.toJSONString(watch.getTaskInfo()));
        return BaseResponseDto.builder()
                .code(RestStatus.OK.getStatus())
                .message("查询成功")
                .data(dto)
                .build();
    }

    /**
     * 指定索引是否存在
     * @param indexName 索引名
     * @return          boolean
     * @throws IOException 异常
     */
    private boolean isExistIndex(String indexName) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
       return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }
    /**
     * 创建索引
     *
     * @param indexName 指定索引名
     * @param settings  es设置 如分片副本数设置
     * @param mapping   存储的数据结构 json字符串
     * @return ResultDto
     */
    public ResultDto createIndex(String indexName, Settings settings, String mapping) {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        Assert.notNull(indexName, ResultCodeEnum.ES_INDEX_ERROR.getMessage());
        Assert.notNull(settings, ResultCodeEnum.ES_SETTING_ERROR.getMessage());
        Assert.notNull(mapping, ResultCodeEnum.ES_MAPPING_ERROR.getMessage());
        request.settings(settings);
        request.source(mapping, XContentType.JSON);
        // 别名设置全局唯一
        request.alias(new Alias(GlobalIndexEnum.GLOBAL_INFO_ALIAS.getValues()));
        try {
            if (isExistIndex(indexName)) {
                return ResultDto.ok();
            } else {
                // 同步调用创建索引
                CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
                // 指示是否所有节点都已确认请求
                boolean acknowledged = createIndexResponse.isAcknowledged();
                // 指示是否在超时之前为索引中的每个分片启动了必需的分片副本数
                boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();
                if (acknowledged != shardsAcknowledged) {
                    return ResultDto.setResult(ResultCodeEnum.ES_ERROR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(" ############# 索引创建异常error ############# :{}", e.getMessage());
            throw new BaseException(ResultCodeEnum.SERVER_ERROR);
        }
        return ResultDto.ok();
    }


    /**
     * 删除索引
     *
     * @param indexNames
     * @return
     * @throws IOException
     */
    public AcknowledgedResponse deleteIndex(String... indexNames) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(indexNames);
        return restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
    }


    /**
     * 判断 index 是否存在
     *
     * @param indexName
     * @return
     * @throws IOException
     */
    public boolean indexExists(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 根据 id 删除指定索引中的文档
     *
     * @param indexName
     * @param id
     * @return
     * @throws IOException
     */
    public DeleteResponse deleteDoc(String indexName, String id) throws IOException {
        DeleteRequest request = new DeleteRequest(indexName, id);
        return restHighLevelClient.delete(request, RequestOptions.DEFAULT);
    }

    /**
     * 根据 id 更新指定索引中的文档
     *
     * @param indexName
     * @param id
     * @return
     * @throws IOException
     */
    public UpdateResponse updateDoc(String indexName, String id, String updateJson) throws IOException {
        UpdateRequest request = new UpdateRequest(indexName, id);
        request.doc(XContentType.JSON, updateJson);
        return restHighLevelClient.update(request, RequestOptions.DEFAULT);
    }

    /**
     * 根据 id 更新指定索引中的文档
     *
     * @param indexName 索引
     * @param id        主键ID
     */
    public ResultDto updateInfo(String indexName, String id, String json) {
        UpdateRequest request = new UpdateRequest(indexName, id);
        request.doc(json, XContentType.JSON);
        try {
            UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            log.info("################# 更新失败 ################# error:{}", e.getMessage());
        }
        return ResultDto.ok();
    }

    /**
     * 判断记录是都存在
     *
     * @param indexName 索引名
     * @param id        数据ID
     * @return boolean
     */
    public boolean existsTheDoc(String indexName, String id) {
        GetRequest getRequest = new GetRequest(indexName, id);
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        boolean exists = false;
        try {
            exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("是否存在当前记录 exists: " + exists);
        return exists;
    }

    /**
     * 根据某字段的 k-v 更新索引中的文档
     *
     * @param fieldName
     * @param value
     * @param indexName
     * @throws IOException
     */
    public void updateByQuery(String fieldName, String value, String... indexName) throws IOException {
        UpdateByQueryRequest request = new UpdateByQueryRequest(indexName);
        //单次处理文档数量
        request.setBatchSize(100)
                .setQuery(new TermQueryBuilder(fieldName, value))
                .setTimeout(TimeValue.timeValueMinutes(2));
        restHighLevelClient.updateByQuery(request, RequestOptions.DEFAULT);
    }

    /**
     * 添加文档 手动指定id
     *
     * @param indexName
     * @param id
     * @param source
     * @return
     * @throws IOException
     */
    public IndexResponse addDoc(String indexName, String id, String source) throws IOException {
        IndexRequest request = new IndexRequest(indexName);
        if (null != id) {
            request.id(id);
        }
        request.source(source, XContentType.JSON);
        return restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    /**
     * 添加文档 使用自动id
     *
     * @param indexName
     * @param source
     * @return
     * @throws IOException
     */
    public IndexResponse addDoc(String indexName, String source) throws IOException {
        return addDoc(indexName, null, source);
    }

    /**
     * 简单模糊匹配 默认分页为 0,10
     *
     * @param field
     * @param key
     * @param page
     * @param size
     * @param indexNames
     * @return
     * @throws IOException
     */
    public SearchResponse search(String field, String key, int page, int size, String... indexNames) throws IOException {
        SearchRequest request = new SearchRequest(indexNames);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(new MatchQueryBuilder(field, key))
                .from(page)
                .size(size);
        request.source(builder);
        return restHighLevelClient.search(request, RequestOptions.DEFAULT);
    }

    /**
     * term 查询 精准匹配
     *
     * @param field
     * @param key
     * @param page
     * @param size
     * @param indexNames
     * @return
     * @throws IOException
     */
    public SearchResponse termSearch(String field, String key, int page, int size, String... indexNames) throws IOException {
        SearchRequest request = new SearchRequest(indexNames);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.termsQuery(field, key))
                .from(page)
                .size(size);
        request.source(builder);
        return restHighLevelClient.search(request, RequestOptions.DEFAULT);
    }


    /**
     * 批量导入
     *
     * @param indexName
     * @param isAutoId  使用自动id 还是使用传入对象的id
     * @param source
     * @return
     * @throws IOException
     */
    public BulkResponse importAll(String indexName, boolean isAutoId, String source) throws IOException {
        if (0 == source.length()) {
            //todo 抛出异常 导入数据为空
        }
        BulkRequest request = new BulkRequest();

        JSONArray array = JSON.parseArray(source);

        //todo 识别json数组
        if (isAutoId) {
            for (Object s : array) {
                request.add(new IndexRequest(indexName).source(s, XContentType.JSON));
            }
        } else {
            for (Object s : array) {
                request.add(new IndexRequest(indexName).id(JSONObject.parseObject(s.toString()).getString("id")).source(s, XContentType.JSON));
            }
        }
        return restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
    }

    /**
     * 新增单条数据到ES
     *
     * @param indexName 索引名
     * @param id        唯一键ID
     * @param json      JSON格式字符串
     * @return ResultDto
     */
    public ResultDto createInfo(String indexName, String id, String json) {
        try {
            // 索引存在才去新增记录 TODO 后期可去除
            if (!isExistIndex(indexName)){
                throw new BaseException(ResultCodeEnum.INDEX_NOT_EXIST);
            } else {
                // 当前记录是否存在
                if(existsTheDoc(indexName,id)){
                    return ResultDto.ok();
                } else {
                    IndexRequest indexRequest = new IndexRequest(indexName, null, id)
                            //类似于我们需要存储的数据
                            .source(json, XContentType.JSON)
                            .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                    ActionListener<IndexResponse> actionListener = new ActionListener<IndexResponse>() {
                        @SneakyThrows
                        @Override
                        public void onResponse(IndexResponse indexResponse) {
                            // 执行成功后会回调该方法
                            log.info(" ###################### [createInfo方法异步新增成功] ###################### ");
                        }
                        @Override
                        public void onFailure(Exception e) {
                            // 执行失败回调该方法
                            e.printStackTrace();
                            log.info("[###################### [createInfo方法异步新增失败] ######################] error:{}", e.getMessage());
                        }
                    };
                    restHighLevelClient.indexAsync(indexRequest, RequestOptions.DEFAULT, actionListener);
                }
            }
        } catch (Exception e) {
            log.info(" [putStructuredObjectsToEs-->createInfo处理] 失败,error:{} ",e.getMessage());
            e.printStackTrace();
            throw new BaseException(ResultCodeEnum.ES_MAPPING_ERROR);
        }
        return ResultDto.ok();
    }

    /**
     * 分词使用测试
     *
     * @param vo 存储到ES中的数据结构
     * @return ResultDto
     */
    public ResultDto ikUse(EsInfoDocVo vo) {
        String ik_index = "ik_test_java";
        try {
//            XContentBuilder  mappings = JsonXContent.contentBuilder().startObject()
//                    .startObject("properties")
//                    .startObject("title")//默认使用standard分词器
//                    .field("type", "text")
//                    .startObject("fields")
//                    .startObject("title_ik_smart")
//                    .field("type", "text")
//                    .field("analyzer", "ik_smart")//使用ik_smart分词器
//                    .endObject()
//                    .startObject("title_ik_max_word")
//                    .field("type", "text")
//                    .field("analyzer", "ik_max_word")//使用ik_max_word分词器
//                    .endObject()
//                    .endObject()
//                    .endObject()
//                    .endObject().endObject();
            /* 索引重复 不能创建会抛错 */
            GetIndexRequest getIndexRequest = new GetIndexRequest(ik_index);
            boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            if (!exists) {
                // 不存在索引就去创建索引
                CreateIndexRequest request = new CreateIndexRequest(ik_index);
                request.settings(Settings.builder().put("index.number_of_shards", 3).put("index.number_of_replicas", 2));
                // 索引数据模型结构
                request.source(JSON.toJSONString(vo), XContentType.JSON);
                restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            }
            log.info(" ################### 分词索引创建成功开始新增分词数据到ES中 ################### ");

            IndexRequest indexRequest = new IndexRequest(ik_index, null, StringUtils.getUUID());
            // 索引下对应的具体的数据集合
            indexRequest.source(JSON.toJSONString(vo), XContentType.JSON);
            restHighLevelClient.indexAsync(indexRequest, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
                @Override
                public void onResponse(IndexResponse indexResponse) {
                    log.info(" ################### 索引分词数据新增成功 ################### ");
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    log.info(" ################### 索引分词数据新增失败 ################### error:{}", e.getMessage());
                }
            });
        } catch (IOException e) {
            log.info(" 分词自定义数据创建异常 ：{}", e.getMessage());
            e.printStackTrace();
        }
        return ResultDto.ok();
    }

    /**
     * 分词查询
     *
     * @param key 条件
     * @return ResultDto<List < EsInfoDocDto>>
     */
    public ResultDto searchIk(String key) {
        SearchRequest req = new SearchRequest("ik_test_java");
        // 使用dis_max直接取多个query中，分数最高的那一个query的分数即可
        DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
        SearchSourceBuilder ssb = new SearchSourceBuilder();
        // 设置content.keyword 后查询是完全匹配才有结果
        disMaxQueryBuilder.add(QueryBuilders.matchQuery("content", key).boost(2f));//查询field包含value的文档
        disMaxQueryBuilder.add(QueryBuilders.matchQuery("content.pinyin", key));
        ssb.query(disMaxQueryBuilder);
        req.source(ssb);
        List<EsInfoDocDto> esInfoDocDtoList = new ArrayList<>();
        try {
            SearchResponse resp = restHighLevelClient.search(req, RequestOptions.DEFAULT);
            SearchHit[] searchHits = resp.getHits().getHits();
            for (SearchHit hit : searchHits) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                JSONObject json = JSON.parseObject(JSON.toJSONString(sourceAsMap, SerializerFeature.WriteDateUseDateFormat));
                EsInfoDocDto docDto = JSONObject.toJavaObject(json, EsInfoDocDto.class);
                log.info(" 转换后的实体信息 docDto ： {}", docDto);
                esInfoDocDtoList.add(docDto);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResultDto.ok().setDataList(esInfoDocDtoList);
    }
}
