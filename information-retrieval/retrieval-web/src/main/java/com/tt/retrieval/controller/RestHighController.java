package com.tt.retrieval.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tt.retrieval.common.StringUtils;
import com.tt.retrieval.common.dto.BaseResponseDto;
import com.tt.retrieval.common.dto.EsInfoDocDto;
import com.tt.retrieval.common.dto.PageBeanDto;
import com.tt.retrieval.common.GlobalIndexEnum;
import com.tt.retrieval.common.vo.ConditionVo;
import com.tt.retrieval.common.vo.EsInfoDocVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * 搭建搜索引擎服务，可用ES实现。
 * 具备搜索源的 分词、排序（时间顺序、关联度顺序）、全文检索功能
 * @author LuHongGang
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/high")
public class RestHighController {

    @Resource
    RestHighLevelClient restHighLevelClient;

    /**
     * INDEX API use case
     *
     * @return BaseResponseDto<EsInfoDoc>
     * @throws IOException
     */
    @GetMapping("/test")
    public BaseResponseDto<EsInfoDocVo> high() throws IOException {
        EsInfoDocVo esInfoDocVo = EsInfoDocVo.builder()
                .infoId(StringUtils.getUUID())
                .infoTitle("我们该正确的认知到自己")
                .content("我们都应该相信自己会变的很牛皮的,牛逼的人物")
                .comment("你是真的很不错")
                .userCacheName("小米米")
                .admire("100").build();
//        String json = JSON.toJSONString(esInfoDoc);
        Map<String, Object> source = new HashMap<String, Object>();
        source.put("infoId", esInfoDocVo.getInfoId());
        source.put("infoTitle", esInfoDocVo.getInfoTitle());
        source.put("content", esInfoDocVo.getContent());
        source.put("comment", esInfoDocVo.getComment());
        source.put("userCacheName", esInfoDocVo.getUserCacheName());
        source.put("admire", esInfoDocVo.getAdmire());
        IndexRequest request = new IndexRequest(GlobalIndexEnum.GLOBAL_INDEX_INFO.getValues(),
                GlobalIndexEnum.GLOBAL_TYPE_INFO.getValues(), UUID.randomUUID().toString())
                // source 类似于我们需要存储的数据
                .source(source)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        System.out.println(" 注入的对象信息restHighLevelClient ：=" + restHighLevelClient);
        StopWatch watch = new StopWatch("es-http-request");
        watch.start("es-doc-begin");
        IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        watch.stop();
        log.info("耗时信息:{}",  watch.prettyPrint());
        RestStatus status = response.status();
        if (status.equals(RestStatus.OK) || status.equals(RestStatus.CREATED)) {
            System.out.println("返回的结果 response: " + response.toString());
        } else {
            System.out.println("返回的结果 response: 失败 " + response.toString());
        }
        BaseResponseDto dto = BaseResponseDto.builder()
                .code(response.status().getStatus())
                .data(esInfoDocVo)
                .message(RestStatus.fromCode(status.getStatus()).toString())
                .build();
        return dto;
    }

    /**
     * run --> ok
     * 创建索引并新增数据领域类型
     *
     * @param esInfoDocVo 实体数据
     * @return BaseResponseDto
     */
    @PostMapping("/create-index-and-doc")
    public BaseResponseDto createIndexAndDoc(@RequestBody EsInfoDocVo esInfoDocVo) throws IOException {
        // 此处esInfoDoc参数仅为了 好构造存储数据的模型而传递
        Map<String, Object> source = new HashMap<>(20);
        source.put("infoId", esInfoDocVo.getInfoId());
        source.put("infoTitle", esInfoDocVo.getInfoTitle());
        source.put("content", esInfoDocVo.getContent());
        source.put("comment", esInfoDocVo.getComment());
        source.put("userCacheName", esInfoDocVo.getUserCacheName());
        source.put("admire", esInfoDocVo.getAdmire());
        source.put("createTime", esInfoDocVo.getCreateTime());

        // 创建索引
        CreateIndexRequest request = new CreateIndexRequest(GlobalIndexEnum.GLOBAL_INDEX_INFO.getValues());
        // /创建的每个索引都可以有与之关联的特定主分片和副本
        request.settings(Settings.builder()
                .put("index.number_of_shards", 5)
                .put("index.number_of_replicas", 2));
        // 为索引创建别名
        request.alias(new Alias(GlobalIndexEnum.GLOBAL_INDEX_ALIAS.getValues()));
        // 要存储的数据格式 JSON.toJSONString(source),XContentType.JSON 有异常
//        request.mapping(source);
        request.source(source);

        //同步执行
//        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
//        System.out.println(" 初始化执行的结果 : " + JSON.toJSONString(createIndexResponse));

//        //返回的CreateIndexResponse允许检索有关执行的操作的信息，如下所示：
//        boolean acknowledged = createIndexResponse.isAcknowledged();//指示是否所有节点都已确认请求
//        boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();//指示是否在超时之前为索引中的每个分片启动了必需的分片副本数
//        BaseResponseDto dto = BaseResponseDto.builder()
//                .code(acknowledged ? "200":"500")
//                .data(esInfoDoc)
//                .message(acknowledged ?(shardsAcknowledged ? "数据存储成功" :"副本数备份异常") : "节点请求确认异常")
//                .build();
//        #################### 异步执行如下示例 ####################
        //异步方法不会阻塞并立即返回。
//        ActionListener<CreateIndexResponse> listener = new ActionListener<CreateIndexResponse>() {
//            @Override
//            public void onResponse(CreateIndexResponse createIndexResponse) {
//                //如果执行成功，则调用onResponse方法;
//            }
//            @Override
//            public void onFailure(Exception e) {
//                //如果失败，则调用onFailure方法。
//            }
//        };

        // 异步执行
        restHighLevelClient.indices().createAsync(request, RequestOptions.DEFAULT,
                new ActionListener<CreateIndexResponse>() {
                    @Override
                    public void onResponse(CreateIndexResponse createIndexResponse) {
                        // 成功执行调用
                        System.out.println(" 成功创建索引信息 ");
                        //返回的CreateIndexResponse允许检索有关执行的操作的信息，如下所示：
                        boolean acknowledged = createIndexResponse.isAcknowledged();//指示是否所有节点都已确认请求
                        boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();//指示是否在超时之前为索引中的每个分片启动了必需的分片副本数

                    }
                    @Override
                    public void onFailure(Exception e) {
                        // 执行失败调用
                        System.out.println(" 索引创建失败 ");
                    }
                });

        return BaseResponseDto.builder()
                .code(true ? 200 : 500)
                .data(esInfoDocVo)
                .message(true ? (true ? "数据存储成功" : "副本数备份异常") : "节点请求确认异常")
                .build();
    }

    /**
     * run --> ok
     * 索引是否存在判断
     *
     * @return boolean
     * @throws IOException
     */
    @GetMapping("/exists-index")
    public boolean existsIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest(GlobalIndexEnum.GLOBAL_INDEX_INFO.getValues());
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        return exists;
    }


    /**
     * run --> ok
     * 数据存储
     *
     * @return BaseResponseDto
     * @throws IOException IOException
     */
    @PostMapping("/save-info")
    public BaseResponseDto saveInfo(@RequestBody EsInfoDocVo esInfoDocVo) throws IOException {
        IndexRequest indexRequest = new IndexRequest(GlobalIndexEnum.GLOBAL_INDEX_INFO.getValues(),
                GlobalIndexEnum.GLOBAL_TYPE_INFO.getValues(), UUID.randomUUID().toString())
                // source 类似于我们需要存储的数据
                .source(JSONObject.toJSONString(esInfoDocVo), XContentType.JSON)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        ActionListener<IndexResponse> actionListener = new ActionListener<IndexResponse>() {

            @SneakyThrows
            @Override
            public void onResponse(IndexResponse indexResponse) {
                // 执行成功后会回调该方法
                System.out.println(" 新增成功 ");
//                try {
//                    if(Objects.nonNull(restHighLevelClient)){
//                        restHighLevelClient.close();
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                }finally {
//                    if(Objects.nonNull(restHighLevelClient)){
//                        restHighLevelClient.close();
//                    }
//                }
            }

            @Override
            public void onFailure(Exception e) {
                // 执行失败回调该方法
                System.out.println(" 新增失败 ");
            }
        };

        restHighLevelClient.indexAsync(indexRequest, RequestOptions.DEFAULT, actionListener);

        // 关闭
//        restHighLevelClient.close();
        return BaseResponseDto.builder()
                .code(RestStatus.OK.getStatus())
                .message("新增成功")
                .build();
    }

    /**
     * run --> ok
     * 删除指定的"_id": "1803a3a1-38a7-49d8-a6f8-98e74f237703" 的数据
     *
     * @param esId es中字段_id的值对应的数据删除
     * @return BaseResponseDto
     * @throws IOException IOException
     */
    @DeleteMapping("/delete-info")
    public BaseResponseDto deleteInfo(@RequestParam("esId") String esId) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(GlobalIndexEnum.GLOBAL_INDEX_INFO.getValues(),
                GlobalIndexEnum.GLOBAL_TYPE_INFO.getValues(), esId)
                // source 类似于我们需要存储的数据
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);

        // 关闭
        restHighLevelClient.close();
        return BaseResponseDto.builder()
                .code(RestStatus.OK.getStatus())
                .message("删除成功")
                .build();
    }


    /**
     * https://www.cnblogs.com/keatsCoder/p/11341835.html
     * 条件查询 满足分词高亮 全文搜索
     *
     * @param conditionVo 条件参数
     * @return BaseResponseDto
     * @throws IOException Exception
     */
    @PostMapping(value = "/search-info",produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseDto<PageBeanDto<List<EsInfoDocDto>>>  searchInfo(@RequestBody ConditionVo conditionVo) throws IOException {
        Integer pageNo = conditionVo.getPageIndex();
        Integer pageSize = conditionVo.getPageSize();
        SearchRequest searchRequest = new SearchRequest(GlobalIndexEnum.GLOBAL_INDEX_INFO.getValues());
        // 设置过滤条件
        BoolQueryBuilder filter = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 多匹配条件 可根据字段进行搜索,must表示符合条件的,相反的must not表示不符合条件的
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("content", conditionVo.getInfoVo().getContent());
        // 范围查询 新建range条件
//        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("fields_timestamp");
//        rangeQueryBuilder.gte("2019-03-21T08:24:37.873Z"); //开始时间
//        rangeQueryBuilder.lte("2019-03-21T08:24:37.873Z"); //结束时间
//        boolBuilder.must(rangeQueryBuilder);
//        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("id", id);

        // curl 'http://localhost:9201/index_info/_analyze?pretty=true' -d '{"userCacheName":"春天"}'
        // 用户名称分词matchPhraseQuery
        String userName = "userCacheName";
        MultiMatchQueryBuilder multiMatchQueryBuilder =
                QueryBuilders.multiMatchQuery(conditionVo.getInfoVo().getUserCacheName().toLowerCase(), userName);

        // 多字段组合查询 --> QueryBuilders.multiMatchQuery("Spring开发框架", "name", "description").minimumShouldMatch("70%");
        //设置高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder().field(userName).requireFieldMatch(false);
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);

        // 中文分词 可据boost 设置权重且可指定分词的搜索方式operator(Operator.AND)
        MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("userCacheName", conditionVo.getInfoVo().getContent()).analyzer("ik_smart");

        filter.must(matchQueryBuilder);
        filter.should(matchQuery);
        filter.should(multiMatchQueryBuilder);
        // 设置查询,可以是任何类型的QueryBuilder
        sourceBuilder.query(filter);
        // 设置确定结果要从哪个索引开始搜索的from选项,默认为0
        sourceBuilder.from(pageNo);
        // 设置确定搜素命中返回数的size选项，默认为10
        sourceBuilder.size(pageSize);
        // 按需字段排序
        // 设置排序规则 注意 --> *** 排序字段必须建立keyword字段 ***
//        sourceBuilder.sort("admire", SortOrder.DESC); // 第一排序规则 ERROR
//        sourceBuilder.sort("_id", SortOrder.ASC); // 第二排序规则

//        sourceBuilder.sort(new SortBuilder())
        // 设置一个可选的超时,控制允许搜索的时间
//        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        System.out.println(" 输出构造的条件体 ： " + sourceBuilder.toString());
        // 条件封装到请求
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        // 获取搜索结果,SearchHits提供有关所有匹配的全局信息,例如总命中数或最高分数：
        SearchHits hits = searchResponse.getHits();
        // 匹配到总的记录数量
        long totalHits = hits.getTotalHits().value;
        // 得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        List<EsInfoDocDto> esInfoDocDtoList = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            System.out.println("hit.getSourceAsString() ： " + hit.getSourceAsString());
            // 构造词条高亮显示
            // 文档的主键
//            String id = hit.getId();
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = String.valueOf(sourceAsMap.get(userName));

            // 获取高亮查询的内容。如果存在，则替换原来的name
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null) {
                HighlightField nameField = highlightFields.get(userName);
                if (nameField != null) {
                    Text[] fragments = nameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text str : fragments) {
                        stringBuffer.append(str.string());
                    }

                    name = stringBuffer.toString().replace("\\","");
                    sourceAsMap.put(userName,name);
//                    EsInfoDocDto esInfoDocDto = JSONObject.parseObject(name,EsInfoDocDto.class);
//                    esInfoDocDtoList.add(esInfoDocDto);

                }
            }
            System.out.println(" 转换前的实体信息 sourceAsMap ： " + sourceAsMap);
            JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
            JSONObject json = JSON.parseObject(JSON.toJSONString(sourceAsMap, SerializerFeature.WriteDateUseDateFormat));
            System.out.println(" 转换后的实体信息 json ： " + json);
            EsInfoDocDto docDto = EsInfoDocDto.builder()
                    .admire(json.getString("admire"))
                    .infoTitle(json.getString("infoTitle"))
                    .infoId(json.getString("infoId"))
                    .comment(json.getString("comment"))
                    .comment(json.getString("content"))
                    .createTime(json.getString("createTime"))
                    .userCacheName(json.getString("userCacheName"))
                    .build();
            System.out.println(" 转换后的实体信息 docDto ： " + json.getString("userCacheName"));
            esInfoDocDtoList.add(docDto);
        }
        Integer totalRows = Integer.valueOf(totalHits+"");
        // 计算总的页数
        int totalPage = totalRows % pageSize == 0 ? totalRows / pageSize : (totalRows / pageSize + 1);
        PageBeanDto<List<EsInfoDocDto>> dto = new PageBeanDto<>();
                dto.setTotalPage(totalPage);
                dto.setPageNo(pageNo <=0 ? 1 : pageNo);
                dto.setPageSize(pageSize);
                dto.setRows(totalRows);
                dto.setLists(esInfoDocDtoList);

        return (BaseResponseDto) BaseResponseDto.builder()
                .code(RestStatus.OK.getStatus())
                .message("查询成功")
                .data(dto)
                .build();
    }

    /**
     * https://www.deathearth.com/734.html
     * 分词高亮及设置权重查询
     * @param conditionVo 条件参数
     * @return BaseResponseDto
     * @throws IOException Exception
     */
    @PostMapping(value = "/search",produces = MediaType.APPLICATION_JSON_UTF8_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseDto search(@RequestBody ConditionVo conditionVo) throws IOException {
        Integer pageNo = conditionVo.getPageIndex();
        Integer pageSize = conditionVo.getPageSize();
        String userName = "userCacheName";
        SearchRequest searchRequest = new SearchRequest(GlobalIndexEnum.GLOBAL_INDEX_INFO.getValues());
        // 设置过滤条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        String key = conditionVo.getInfoVo().getUserCacheName();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 匹配所有文档
        boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        boolQueryBuilder.must(QueryBuilders.matchPhraseQuery(userName,key));

        // 关键词全文搜索筛选条件
        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery(key);
        boolQueryBuilder.must(queryBuilder);
        //设置高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder().field(userName).requireFieldMatch(false);
        highlightBuilder.preTags("<span style=\"color:green\">");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);

        // 设置查询,可以是任何类型的 QueryBuilder
        sourceBuilder.query(boolQueryBuilder);
        // 设置确定结果要从哪个索引开始搜索的from选项,默认为0
        sourceBuilder.from(pageNo);
        // 设置确定搜素命中返回数的size选项，默认为10
        sourceBuilder.size(pageSize);
        // 设置一个可选的超时,控制允许搜索的时间
        log.info(" ########## 输出构造的条件体 ########## :{}",sourceBuilder.toString());
        // 条件封装到请求
        searchRequest.source(sourceBuilder);
        long begin = System.currentTimeMillis();
        StopWatch watch = new StopWatch("es-local");
        watch.start("rest-http-01");
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        watch.prettyPrint();
        watch.stop();
        log.info("耗时：{}秒",(System.currentTimeMillis()-begin)/1000);
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
                        log.info("高亮内容 ： {}",str);
                        stringBuffer.append(str.string());
                    }

                    name = stringBuffer.toString().replace("\\","");
                    sourceAsMap.put(userName,name);
                }
            }
            System.out.println(" 转换前的实体信息 sourceAsMap ： " + sourceAsMap);
//            JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
            JSONObject json = JSON.parseObject(JSON.toJSONString(sourceAsMap, SerializerFeature.WriteDateUseDateFormat));
            // 将高亮内容json数据转指定的Java对象接收
            EsInfoDocDto docDto = JSONObject.toJavaObject(json,EsInfoDocDto.class);
            log.info(" 转换后的实体信息 docDto ： {}",docDto);
            esInfoDocDtoList.add(docDto);
        }
        Integer totalRows = Integer.valueOf(totalHits+"");
        // 计算总的页数
        int totalPage = totalRows % pageSize == 0 ? totalRows / pageSize : (totalRows / pageSize + 1);
        PageBeanDto<List<EsInfoDocDto>> dto = new PageBeanDto<>();
        dto.setTotalPage(totalPage);
        dto.setPageNo(pageNo <=0 ? 1 : pageNo);
        dto.setPageSize(pageSize);
        dto.setRows(totalRows);
        dto.setLists(esInfoDocDtoList);

        return  BaseResponseDto.builder()
                .code(RestStatus.OK.getStatus())
                .message("查询成功")
                .data(dto)
                .build();
    }


    public static void main(String[] args) {
        // update --当id不存在时将会抛出异常
        // upsert--id不存在时就插入
//        UpdateRequest request = new UpdateRequest(index, type, "1").doc(jsonMap).upsert(jsonMap);
//        UpdateResponse response = restHighLevelClient.update(request);
        System.out.println("" +GlobalIndexEnum.GLOBAL_INDEX_ALIAS.getValues());
    }
}
