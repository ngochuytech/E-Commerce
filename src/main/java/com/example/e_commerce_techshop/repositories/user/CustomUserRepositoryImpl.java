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

    @Override
    public Page<User> findAllShippers(String name, String email, String phone, String status, Pageable pageable) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Lọc theo role SHIPPER
        criteriaList.add(Criteria.where("roles").in("SHIPPER"));

        // Filter theo name
        if (name != null && !name.trim().isEmpty()) {
            criteriaList.add(Criteria.where("fullName").regex(".*" + name + ".*", "i"));
        }

        // Filter theo email
        if (email != null && !email.trim().isEmpty()) {
            criteriaList.add(Criteria.where("email").regex(".*" + email + ".*", "i"));
        }

        // Filter theo phone
        if (phone != null && !phone.trim().isEmpty()) {
            criteriaList.add(Criteria.where("phone").regex(".*" + phone + ".*", "i"));
        }

        // Filter theo status (active/banned)
        if (status != null && !status.trim().isEmpty()) {
            if ("active".equalsIgnoreCase(status)) {
                criteriaList.add(Criteria.where("isActive").is(true));
            } else if ("banned".equalsIgnoreCase(status)) {
                criteriaList.add(Criteria.where("isActive").is(false));
            }
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, User.class);
        query.with(pageable);
        List<User> shippers = mongoTemplate.find(query, User.class);
        return new PageImpl<>(shippers, pageable, total);
    }
    
}