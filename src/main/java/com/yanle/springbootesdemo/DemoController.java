package com.yanle.springbootesdemo;

import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Le Yan
 * @Description todo
 * @date 2018/10/23 15:57
 */
@RestController
public class DemoController {

    @Autowired
    private TransportClient client;

    @GetMapping(value = "/get/book/novel")
    public ResponseEntity get(@RequestParam(name = "id", defaultValue = "") String id) {
        if (id.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        GetResponse result = this.client.prepareGet("book", "novel", id).get();
        if (!result.isExists()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(result.getSource(), HttpStatus.OK);
    }

    @PostMapping(value = "/add/book/novel")
    public ResponseEntity add(@RequestParam(name = "title") String title,
                              @RequestParam(name = "author") String author,
                              @RequestParam(name = "word_count") String wordCount,
                              @RequestParam(name = "publish_date")
                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                      Date publishDate) {
        try {
            XContentBuilder content = XContentFactory.jsonBuilder().startObject().field("title", title).field("author", author).field("word_count", wordCount).field("publish_date", publishDate.getTime()).endObject();
            IndexResponse result = client.prepareIndex("book", "novel").setSource(content).get();
            return new ResponseEntity(result.getId(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/delete/book/novel")
    public ResponseEntity delete(@RequestParam(name = "id") String id) {
        DeleteResponse result = client.prepareDelete("book", "novel", id).get();
        return new ResponseEntity(result.getResult().toString(), HttpStatus.OK);
    }

    @PutMapping(value = "/update/book/novel")
    public ResponseEntity update(@RequestParam(name = "id") String id,
                                 @RequestParam(name = "title", required = false) String title,
                                 @RequestParam(name = "author", required = false) String author,
                                 @RequestParam(name = "word_count", required = false) String wordCount,
                                 @RequestParam(name = "publish_date", required = false)
                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                         Date publishDate) {
        UpdateRequest updateRequest = new UpdateRequest("book", "novel", id);
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
            if (title != null) {
                builder.field("title", title);
            }
            if (author != null) {
                builder.field("author", author);
            }
            if (wordCount != null) {
                builder.field("word_count", wordCount);
            }
            if (publishDate != null) {
                builder.field("publish_date", publishDate);
            }
            builder.endObject();
            updateRequest.doc(builder);
            UpdateResponse result = this.client.update(updateRequest).get();
            return new ResponseEntity(result.getResult().toString(), HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/query/book/novel")
    public ResponseEntity query(@RequestParam(name = "title", required = false) String title,
                                @RequestParam(name = "author", required = false) String author,
                                @RequestParam(name = "gt_word_count", defaultValue = "0") int gtWordCount,
                                @RequestParam(name = "lt_word_count", required = false) Integer ltWordCount) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (author != null) {
            boolQuery.must(QueryBuilders.matchQuery("author", author));
        }
        if (title != null) {
            boolQuery.must(QueryBuilders.matchQuery("title", title));
        }
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("word_count").from(gtWordCount);
        if (ltWordCount != null && ltWordCount > 0) {
            rangeQuery.to(ltWordCount);
        }
        boolQuery.filter(rangeQuery);

        SearchRequestBuilder builder = this.client.prepareSearch("book").setTypes("novel").setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(boolQuery).setFrom(0).setSize(10);

        SearchResponse  response = builder.get();
        List<Map<String,Object>> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()){
            result.add(hit.getSourceAsMap());
        }
        return new ResponseEntity(result,HttpStatus.OK);
    }
}
