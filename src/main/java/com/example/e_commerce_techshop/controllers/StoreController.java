package com.example.e_commerce_techshop.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.StoreResponse;
import com.example.e_commerce_techshop.services.store.IStoreService;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/stores")
@RequiredArgsConstructor
@Tag(name = "Store Browsing", description = "Public APIs for browsing and discovering stores")
public class StoreController {
    private final IStoreService storeService;

    @GetMapping("/{storeId}")
    @Operation(summary = "Get store by ID", 
               description = "Retrieve detailed store information including description, location, and contact details")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Store found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StoreResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Store not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> getStoreById(
            @Parameter(description = "Store ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String storeId) {
        try {
            StoreResponse storeResponse = storeService.getStoreById(storeId);
            return ResponseEntity.ok(ApiResponse.ok(storeResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get all stores", 
               description = "Retrieve list of all approved and active stores")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Stores retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = StoreResponse.class))
            )
        )
    })
    public ResponseEntity<?> getAllStores() {
        List<StoreResponse> stores = storeService.getAllStores();
        return ResponseEntity.ok(ApiResponse.ok(stores));
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get stores by owner", 
               description = "Retrieve all stores owned by a specific user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Owner stores retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = StoreResponse.class))
            )
        )
    })
    public ResponseEntity<?> getStoresByOwner(
            @Parameter(description = "Store owner/user ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String ownerId) {
        List<Store> stores = storeService.getStoresByOwner(ownerId);
        List<StoreResponse> storeResponses = stores.stream()
                .map(StoreResponse::fromStore)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(storeResponses));
    }
}
