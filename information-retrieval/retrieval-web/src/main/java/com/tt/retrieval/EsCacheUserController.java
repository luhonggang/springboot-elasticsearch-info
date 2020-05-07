//package com.tt.retrieval;
//
//import com.tt.retrieval.entity.EsCacheUser;
//import com.tt.retrieval.entity.EsCacheUserResVo;
//import com.tt.retrieval.service.EsRetrievalService;
//import org.elasticsearch.index.query.*;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
//import org.springframework.data.elasticsearch.core.query.SearchQuery;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
///**
// * 参考文档：
// * https://docs.spring.io/spring-data/elasticsearch/docs/2.1.22.RELEASE/reference/html/
// * @author LuHongGang
// * @version 1.0
// * @date 2020/4/28 14:50
// */
//@RestController
//@RequestMapping(value = "/es-cache")
//public class EsCacheUserController {
//
//    private final EsRetrievalService esRetrievalService;
//
//    public EsCacheUserController(EsRetrievalService esRetrievalService) {
//        this.esRetrievalService = esRetrievalService;
//    }
//
//    /**
//     * 新增数据到ES 且存在重复的记录会被更新
//     * @param esCacheUser 数据
//     * @return  用户数据
//     */
//    @PostMapping(value = "user-save-post")
//    public EsCacheUser userSavePost(@RequestBody EsCacheUser esCacheUser){
//        System.out.println(" 执行的结果 ： " + esRetrievalService.save(esCacheUser));
//        return esCacheUser;
//        // 保存并更新 若当前用户ID 和es 存储的用户信息的ID 重复即 此处做覆盖处理
//        // save方法是细粒度的 字段层面的值更新
//        /**
//         *  即 ES中存的数据如下:
//         *  [
//         *   {
//         *     "id": 1,
//         *     "userCacheName": "李四",
//         *     "userPhone": "1111111111111"
//         *   },
//         *   {
//         *     "id": 2,
//         *     "userCacheName": "李四",
//         *     "userPhone": "1111111111111"
//         *   },
//         *   {
//         *     "id": 3,
//         *     "userCacheName": "李1",
//         *     "userPhone": "1111111111111"
//         *   }
//         * ]
//         * 新增的请求数据如:
//         * {
//         * 	"id":3,
//         * 	"userCacheName":"李3",
//         * 	"userPhone":"1111111111111"
//         * }
//         * 执行后的结果为:
//         * [
//         *   {
//         *     "id": 1,
//         *     "userCacheName": "李四",
//         *     "userPhone": "1111111111111"
//         *   },
//         *   {
//         *     "id": 2,
//         *     "userCacheName": "李四",
//         *     "userPhone": "1111111111111"
//         *   },
//         *   {
//         *     "id": 3,
//         *     "userCacheName": "李3",
//         *     "userPhone": "1111111111111"
//         *   }
//         * ]
//         */
//    }
//
//    /**
//     * 新增数据到ES
//     * @param esCacheUserResVo 集合数据
//     * @return   List<EsCacheUser>
//     */
//    @PostMapping(value = "user-save-list")
//    public EsCacheUserResVo userSaveList(@RequestBody  EsCacheUserResVo esCacheUserResVo) {
//        esRetrievalService.saveAll(esCacheUserResVo.getEsCacheUserList());
//       return esCacheUserResVo;
//    }
//
//    /**
//     * PUT 数据到ES
//     * @param id 用户id
//     * @param userCacheName 用户姓名
//     * @param userPhone 用户手机号码
//     * @return String data
//     */
//    @GetMapping(value = "user-save")
//    public String userSave(@RequestParam("id") Long id,
//                           @RequestParam("userCacheName") String userCacheName,
//                           @RequestParam("userPhone") String userPhone){
//        EsCacheUser cacheUser =  EsCacheUser.builder().id(id).userCacheName(userCacheName).userPhone(userPhone).build();
//        return esRetrievalService.save(cacheUser).toString();
//    }
//
//    /**
//     * 查询出所用的用户数据
//     * @return List<T></T>
//     */
//    @GetMapping(value = "search-info")
//    public List<EsCacheUser> searchInfo(){
//        Iterable<EsCacheUser> its = esRetrievalService.findAll();
//        List<EsCacheUser> resEsUserList = new ArrayList<>();
//        its.forEach(resEsUserList::add);
//        return resEsUserList;
//    }
//
//    /**
//     * 条件查询出数据
//     * @return List<T></T>
//     */
//    @GetMapping(value = "search-info-condition")
//    public Page<EsCacheUser>  searchInfoCondition(){
////        单个条件匹配 搜索用户名为李四的人
////        QueryBuilder queryBuilder = QueryBuilders.matchQuery("userCacheName","李四");
////        模糊查询 ?匹配单个字符，*匹配多个字符 搜索用户名字中含有李字的数据
////        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery("userCacheName","*李*");
////
////        复合查询
//        WildcardQueryBuilder queryBuilder1 = QueryBuilders.wildcardQuery(
//                "userCacheName", "*米*");//搜索名字中含有米的文档
//        WildcardQueryBuilder queryBuilder2 = QueryBuilders.wildcardQuery(
//                "userPhone", "*7*");//搜索手机号码中含有7的文档
//
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        //userCacheName中必须含有米,userPhone中必须含有7,相当于 and 只要userCacheName中必须含有米会输出且只要userPhone中含有7 也会被输出
//        // or->使用boolQueryBuilder.should(queryBuilder1);
//        boolQueryBuilder.must(queryBuilder1);
//        boolQueryBuilder.must(queryBuilder2);
//        Page<EsCacheUser> its = (Page<EsCacheUser>) esRetrievalService.search(queryBuilder1);
//        /*
//        [
//          {
//            "id": 2,
//            "userCacheName": "李四",
//            "userPhone": "1111111111111"
//          },
//          {
//            "id": 3,
//            "userCacheName": "李3",
//            "userPhone": "000000000000"
//          },
//          {
//            "id": 4,
//            "userCacheName": "李4",
//            "userPhone": "44444444"
//          },
//          {
//            "id": 5,
//            "userCacheName": "李5",
//            "userPhone": "5555555555555"
//          },
//          {
//            "id": 6,
//            "userCacheName": "李6",
//            "userPhone": "6666666666"
//          }
//        ]
//        * */
//        // 多条件检索 如上数据 匹配 userCacheName和userPhone里面带数字4 的记录 并且以分页形式返回
////        QueryBuilder matchAllQueryBuilder = QueryBuilders.multiMatchQuery("4","userCacheName","userPhone");
////        Page<EsCacheUser> its = (Page<EsCacheUser>) esRetrievalService.search(matchAllQueryBuilder);
////        its.forEach(System.out::println);
//        System.out.println("返回的内容content : " + its.getContent());
////        esRetrievalService.existsById()
////        SearchQuery searchQuery = new NativeSearchQuery(filter);
//        return its;
//
//    }
//
//    /**
//     * 支持分词过滤 分页查询
//     * @param userCacheName 用户名
//     * @param pageIndex 分页起始页
//     * @param pageSize  分页显示条数
//     * @return  分页集合数据
//     */
//    @GetMapping(value = "search-info-page")
//    public Page<EsCacheUser>  searchInfoPage(
//            @RequestParam(value = "userCacheName") String userCacheName, @RequestParam(value = "pageIndex") int pageIndex, @RequestParam(value = "pageSize") int pageSize){
//        BoolQueryBuilder filter = QueryBuilders.boolQuery();
//        // 分词查询 用户名称
//        String queryName = "userCacheName";
//        MultiMatchQueryBuilder multiMatchQueryBuilder =
//                QueryBuilders.multiMatchQuery(userCacheName.toLowerCase(),queryName);
//        // NativeSearchQueryBuilder：Spring提供的一个查询条件构建器，帮助构建json格式的请求体
//        filter.should(multiMatchQueryBuilder);
//        // 可以继续过滤  如-> filter.mustNot(QueryBuilders.termQuery(ES_SEARCH_OPER_FLAG, "D"));
//        // 适合自定义查询
//        SearchQuery searchQuery = new NativeSearchQuery(filter);
//
////        SearchQuery searchQuery = new NativeSearchQueryBuilder()
////                .withQuery(matchAllQuery())
////                .withFilter(boolFilter().must(termFilter("id", documentId)))
////                .build();
//        // 设置分页查询条件
//        searchQuery.setPageable(PageRequest.of(pageIndex,pageSize));
//        Page<EsCacheUser>  page = esRetrievalService.search(searchQuery);
//        System.out.println("查询出的内容 ： " + page.getContent());
//        return page;
//
//    }
//
//
//    /**
//     * 依据ID 查询用户信息
//     * @param id 用户ID
//     * @return Optional<EsCacheUser>
//     */
//    @GetMapping(value = "search-by-id")
//    public Optional<EsCacheUser> searchById(@RequestParam("id") Long id){
//        return esRetrievalService.findById(id);
//    }
//
//    /**
//     * 删除指定的ID 的记录
//     * @param id id
//     */
//    @DeleteMapping(value = "delete-by-id")
//    public void deleteById(Long id){
//        esRetrievalService.deleteById(id);
//    }
//
//    /**
//     *  统计总的记录条数
//     * @return 指定索引->库下 数据的总记录
//     */
//    @GetMapping(value = "count-total")
//    public Long countTotal(){
//        return esRetrievalService.count();
//    }
//
//
//
////    @Resource
////    EsCacheDslExecutorService dslExecutorService;
////
////    /**
////     *  统计总的记录条数
////     * @return 指定索引->库下 数据的总记录
////     */
////    @GetMapping(value = "query-all")
////    public void querryAll(@RequestBody EsCacheUser user){
//////        Predicate predicate = user.getuserCacheName().equalsIgnoreCase("李四");
//////        dslExecutorService.findAll(predicate);
////
////    }
//
//}
