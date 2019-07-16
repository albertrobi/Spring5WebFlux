package com.example.webfluxdemo.controller;

import com.example.webfluxdemo.exception.TweetNotFoundException;
import com.example.webfluxdemo.model.Tweet;
import com.example.webfluxdemo.payload.ErrorResponse;
import com.example.webfluxdemo.repository.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import javax.validation.Valid;

/**
 * Created by rajeevkumarsingh on 08/09/17.
 */
@RestController
public class TweetController {

    @Autowired
    private TweetRepository tweetRepository;

    @GetMapping("/tweets")
    public Flux<Tweet> getAllTweets() {
        return tweetRepository.findAll();
    }

    @PostMapping("/tweets")
    public Mono<Tweet> createTweets(@Valid @RequestBody Tweet tweet) {
        return tweetRepository.save(tweet);
    }

    @GetMapping("/tweets/{id}")
    public Mono<ResponseEntity<Tweet>> getTweetById(@PathVariable(value = "id") String tweetId) {
        return tweetRepository.findById(tweetId)
                .map(savedTweet -> ResponseEntity.ok(savedTweet))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/tweets/{id}")
    public Mono<ResponseEntity<Tweet>> updateTweet(@PathVariable(value = "id") String tweetId,
                                                   @Valid @RequestBody Tweet tweet) {
        return tweetRepository.findById(tweetId)
                .flatMap(existingTweet -> {
                    existingTweet.setText(tweet.getText());
                    return tweetRepository.save(existingTweet);
                })
                .map(updateTweet -> new ResponseEntity<>(updateTweet, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/tweets/{id}")
    public Mono<ResponseEntity<Void>> deleteTweet(@PathVariable(value = "id") String tweetId) {

        return tweetRepository.findById(tweetId)
                .flatMap(existingTweet ->
                        tweetRepository.delete(existingTweet)
                            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
                )
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 
    // The nice thing about Server-Sent Events is that they have a built in reconnection feature, when the client loses 
    // the connection he tries to reconnect to the server automatically. WebSocket does not have such a built in functionality.
    
    //The second is for our application to push tweet information whenever it is added, we need to use 
    //MongoDB with the capped collection.

    //The reason is that by default, for each query, when the user has exhausted the data for that query,
    //MongoDB closes the cursor. Cursor in MongoDB you can understand that it is a pointer in a result set of a query.
    //MongoDB’s close cursor makes it impossible to continue receiving data from our query.

    //To solve this problem, we will use the tailable cursor with MongoDB’s capped collection. 
    //Capped collections are fixed-size collections, meaning we can only add a certain number of documents. 
    //When the capped collection is full, the new document will override the oldest document. 
    //For the tailable cursor, if you used the tail -f command in Linux, you can imagine that tailable cusor 
    //will keep retrieving document after the user has retrieved the result for the query.
    
    //Also, please note that tailable cursors may become dead, or invalid if the query initially returns no match. 
    //In other words, even if new persisted documents match the filter query, the subscriber will not be able to receive them.
    //This is a known limitation of MongoDB tailable cursors. We must ensure that there are matching documents 
    //in the capped collection, before creating a tailable cursor.
    
    @CrossOrigin(allowedHeaders = "*",origins = "*")
    // Tweets are Sent to the client as Server Sent Events
    @GetMapping(value = "/stream/tweets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> streamAllTweets() {
        return tweetRepository.findAll();
    }
    
    @CrossOrigin(allowedHeaders = "*",origins = "*")
    // Tweets are Sent to the client as Server Sent Events
    @GetMapping(value = "/stream/tweets/{tweetText}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> streamTweetByText(@PathVariable(value = "tweetText") String tweetText) {
        return tweetRepository.findByTextStartingWith(tweetText);
    }




    /*
        Exception Handling Examples (These can be put into a @ControllerAdvice to handle exceptions globally)
    */

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity handleDuplicateKeyException(DuplicateKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("A Tweet with the same text already exists"));
    }

    @ExceptionHandler(TweetNotFoundException.class)
    public ResponseEntity handleTweetNotFoundException(TweetNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

}
