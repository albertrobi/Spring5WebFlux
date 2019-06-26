package com.example.webfluxdemo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.stereotype.Repository;

import com.example.webfluxdemo.model.Tweet;

import reactor.core.publisher.Flux;

/**
 * Created by rajeevkumarsingh on 08/09/17.
 */
@Repository
public interface TweetRepository extends ReactiveMongoRepository<Tweet, String> {
	
	 @Tailable
	 Flux<Tweet> findByTextStartingWith(String text);
	 
}
