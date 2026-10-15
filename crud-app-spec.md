# Book Management System Specification

## Overview
A web-based Book Management system built with Spring Boot and Thymeleaf to manage a collection of library books.

## Functional Requirements
* REQ-1: Users can add a new book with a title, author, and ISBN number.
* REQ-2: Users can view a complete list of all stored books in a structured table.
* REQ-3: Users can edit and update existing book details.
* REQ-4: Users can delete a book from the collection.

## Data Model
* `id` (Long) - Unique primary identifier
* `title` (String) - Required, 2–100 characters
* `author` (String) - Required, 2–50 characters
* `isbn` (String) - Required