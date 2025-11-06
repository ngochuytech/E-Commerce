package com.example.e_commerce_techshop.repositories.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.example.e_commerce_techshop.models.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<User> findAllByFilters(String userName, String userEmail, String userPhone, Pageable pageable) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if(userName != null && !userName.trim().isEmpty()) {
            criteriaList.add(Criteria.where("fullName").regex(".*" + userName + ".*", "i"));
        }
        if(userEmail != null && !userEmail.trim().isEmpty()) {
            criteriaList.add(Criteria.where("email").regex(".*" + userEmail + ".*", "i"));
        }
        if(userPhone != null && !userPhone.trim().isEmpty()) {
            criteriaList.add(Criteria.where("phone").regex(".*" + userPhone + ".*", "i"));
        }
        if(!criteriaList.isEmpty()){
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        long total = mongoTemplate.count(query, User.class);
        query.with(pageable);
        List<User> users = mongoTemplate.find(query, User.class);
        return new PageImpl<>(users, pageable, total);
    }
    
}
