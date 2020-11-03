package com.sl.es.service;

import com.alibaba.fastjson.JSON;
import com.sl.es.entity.Content;
import com.sl.es.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ContentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

//    1.解析数据放入到es 索引中
    public boolean ParseContent(String keyword) throws Exception {
        List<Content> contents = new HtmlParseUtil().parseJD(keyword);//查询到的所有数据合集

//        把查询到的数据放入es，使用批量插入
        BulkRequest bulkRequest=new BulkRequest();
        bulkRequest.timeout("2m");

        for (int i = 0; i <contents.size() ; i++) {
//            System.out.println(JSON.toJSONString(contents.get(i)));
            bulkRequest.add(
                    new IndexRequest("jd_goods")
                    .source(JSON.toJSONString(contents.get(i)), XContentType.JSON)
            );
        }

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        return  !bulk.hasFailures(); //Ture就是没有失败---如果不加！ 那么ture就是有失败
    }

//    2.获取这些数据 实现搜索功能
    public List<Map<String,Object>> SearchPage(String keyword,int pageNo,int pageSize) throws IOException {
        if (pageNo<=1){
            pageNo=1;
        }

//        条件搜索
        SearchRequest searchRequest=new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder=new SearchSourceBuilder();//用来执行查询

//        分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

//        精准匹配
        TermQueryBuilder termQueryBuilder= QueryBuilders.termQuery("title",keyword);//构造查询条件
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

//        执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

//        解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit documentFields: searchResponse.getHits().getHits()) {
            list.add(documentFields.getSourceAsMap());

        }
        return list;
    }


    //3. 新增高亮功能
    public List<Map<String,Object>> searchPageHighlightBuilder(String keyword,int pageNo,int pageSize) throws Exception {


        if (pageNo <= 1) {
            pageNo = 1;
        }
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);
        //精准匹配
//        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
//        sourceBuilder.query(termQueryBuilder);
//        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //match匹配 可以支持中文搜索
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title", keyword);
        sourceBuilder.query(matchQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));//超时
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title"); //高亮的字段
        highlightBuilder.requireFieldMatch(false);//如果一句里面有多个关键词高亮，则只显示第一个
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //解析结果
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {

            Map<String, HighlightField> highlightFields = hit.getHighlightFields(); //获取到高亮字段
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap(); //原来的结果！要在结果里面将高亮置换一下
            //解析高亮的字段 将原来的字段换为我们高亮的字段即可
            if (title != null) {
                Text[] fragments = title.fragments();
                String n_title = "";
                for (Text text : fragments) {
                    n_title += text;
                }
                sourceAsMap.put("title", n_title);//高亮字段替换掉原来的内容即可！
            }
            list.add(sourceAsMap);
        }
        return list;
    }

}
