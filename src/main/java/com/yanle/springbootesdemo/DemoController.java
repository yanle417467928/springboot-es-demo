package com.yanle.springbootesdemo;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
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

    @DeleteMapping
    @PostMapping(value = "/delete/book/novel")
    public ResponseEntity delete(@RequestParam(name = "id") String id) {
        DeleteResponse result = client.prepareDelete("book", "novel", id).get();
        return new ResponseEntity(result.getResult().toString(), HttpStatus.OK);
    }

    @PutMapping
    @PostMapping(value = "/update/book/novel")
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
}
