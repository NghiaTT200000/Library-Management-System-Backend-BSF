package com.library_management.library_management_artifact.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Book management endpoints")
public class BookController {
    @GetMapping
    public List<String> getAll() {
        ArrayList<String> exampleList = new ArrayList<>();
        exampleList.add("Wuthering Heights");
        return exampleList;
    }
}
