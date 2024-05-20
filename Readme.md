# Online Collaborative Text Editor

## Overview

This project is part of the CMPS211 Advanced Programming Techniques course at Computer Engineering Cairo University. The objective is to design and implement an online real-time collaborative text editor, akin to Google Docs, enabling multiple users to edit the same document concurrently. The project aims to:

- Develop a basic online collaborative text editor.
- Apply concepts learned in the course.
- Enhance Java programming skills.

## Specifications

### User Management

- **User Registration**: Allow users to register and create an account.
- **User Authentication**: Allow users to log in to their account.

### Document Management

- **File Management**: Allow users to create, open, rename, and delete files.
- **Access Control**: Enable users to share documents with others and control sharing permissions (viewer or editor). Ensure that only authorized users can view/edit/rename documents and that only the owner can delete the file.
- **List Documents**: Display a list of documents owned by the user and documents shared with them.

### Real-time Collaborative Editing

- **Support File Editing**: Allow users to edit the document text and format (support bold and italic only).
- **Support Concurrent Edits**: Allow multiple users to edit the text file simultaneously. Implement an algorithm to handle concurrency issues and conflicts arising from multiple simultaneous edits.
- **Real-time Updates**: Display real-time edits made by other users and represent the movement of other users' cursors.

### UI

Implement a simple user interface for the following components:

- **Login**
- **Sign up**
- **File Management**:
  - List all documents owned by the user.
  - List all documents shared by others.
  - Create a document.
  - Provide options to delete, rename, share, and open documents (as per permissions).
- **Text Editor**

### Guides

The following guides illustrate how to run the project:

1. **Navigate to the FrontEnd directory:**

   ```bash
   cd FrontEnd
   ```

2. **Install packages**

   ```bash
   npm i
   ```

3. **Build the React project:**

   ```bash
   npm run build
   ```

4. **Copy the build files to the Spring Boot static directory:**

   ```bash
   cp -r build/* ../src/main/resources/static
   ```

5. **Navigate back to the project root directory:**

   ```bash
   cd ..
   ```

6. **Run the Spring Boot application:**

   ```bash
   mvn spring-boot:run
   ```
