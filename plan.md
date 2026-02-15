# Social Media Backend - Development Plan

## Project Overview
A Spring Boot-based social media backend with JWT authentication, user profiles, posts, likes, comments, follows, and notifications.

---

## Current Implementation Status ✅

### Authentication & Authorization
- [x] User signup (`POST /api/auth/signup`)
- [x] User login (`POST /api/auth/login`)
- [x] JWT access token generation
- [x] Refresh token with HTTP-only cookies
- [x] Token refresh endpoint (`POST /api/auth/refresh`)
- [x] Logout endpoint (`POST /api/auth/logout`)
- [x] Session management in database
- [x] Spring Security configuration
- [x] Custom authentication entry point

### User & Profile Management
- [x] User entity (id, name, email, password, role, emailVerified, timestamps)
- [x] Profile entity (id, user, username, bio, profilePhotoUrl, isComplete)
- [x] Profile creation (`POST /api/users/create-profile`)
- [x] Username uniqueness validation
- [x] Profile-to-user one-to-one relationship

### Database Optimization
- [x] Indexes on Session table:
  - `idx_session_refresh_token` on `refreshToken` column
  - `idx_session_user_id` on `user_id` foreign key
- [x] Unique constraints on User.email and Profile.username

### Exception Handling
- [x] Global exception handler
- [x] Custom exceptions:
  - EmailAlreadyExistsException
  - InvalidCredentialsException
  - ProfileAlreadyExistsException
  - UsernameAlreadyExistsException

---

## Account Creation Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    1. SIGNUP PHASE                          │
│                 POST /api/auth/signup                       │
│    Body: { name, email, password }                         │
│    Response: UserDTO (no tokens)                            │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│                    2. LOGIN PHASE                           │
│                 POST /api/auth/login                        │
│    Body: { email, password }                                │
│    Response: { accessToken, user: { profileComplete } }     │
│    Cookie: refreshToken (httpOnly)                          │
└─────────────────────────────────────────────────────────────┘
                         ↓
        ┌────────────────┴────────────────┐
        │  Check: profileComplete?        │
        └────────────────┬────────────────┘
                         │
           ┌─────────────┴─────────────┐
           │                           │
        false                        true
           │                           │
           ▼                           ▼
┌──────────────────────────┐   ┌─────────────────┐
│  3. PROFILE CREATION     │   │  4. MAIN APP    │
│  POST /api/users/        │   │    Access       │
│        create-profile    │   │                 │
│  Headers: Bearer token   │   └─────────────────┘
│  Body: { username, bio,  │
│          profilePhotoUrl }│
└──────────────────────────┘
           │
           │ isComplete = true
           ▼
    ┌─────────────────┐
    │  4. MAIN APP    │
    │    Access       │
    └─────────────────┘
```

---

## Upcoming Development Roadmap

### Phase 1: User Profile Management 👤

#### API 1: Get Current User Profile
- **Endpoint:** `GET /api/users/profile`
- **Auth:** Required (Bearer token)
- **Response:** ProfileDTO with user information

**Implementation Steps:**
1. No new DTO needed (reuse `ProfileDTO`)
2. Add method to `ProfileService`: `ProfileDTO getCurrentUserProfile()`
3. Service implementation:
   - Extract user from SecurityContext
   - Find profile by user
   - Map to DTO
4. Add endpoint to `UserController`
5. Exception: `ProfileNotFoundException`

---

#### API 2: Update Profile
- **Endpoint:** `PUT /api/users/profile`
- **Auth:** Required
- **Request Body:** UpdateProfileRequest
- **Response:** Updated ProfileDTO

**Implementation Steps:**
1. Create `UpdateProfileRequest.java`:
   ```java
   - String username (optional, @Size)
   - String bio (optional, @Size)
   - String profilePhotoUrl (optional)
   ```
2. Add method to `ProfileService`: `ProfileDTO updateProfile(UpdateProfileRequest)`
3. Service implementation:
   - Get current user
   - Find existing profile
   - Validate username uniqueness (if changed)
   - Update fields (only non-null values)
   - Save and return DTO
4. Add `@PutMapping("/profile")` to controller
5. Add `@Valid` validation

---

#### API 3: Get Public Profile by Username
- **Endpoint:** `GET /api/users/{username}`
- **Auth:** Optional (show different data if authenticated)
- **Response:** PublicProfileDTO

**Implementation Steps:**
1. Create `PublicProfileDTO.java`:
   ```java
   - UUID id
   - String username
   - String bio
   - String profilePhotoUrl
   - Integer followersCount
   - Integer followingCount
   - Integer postsCount
   - Boolean isFollowedByCurrentUser (if authenticated)
   ```
2. Add to `ProfileRepository`:
   ```java
   Optional<Profile> findByUsername(String username);
   ```
3. Service & Implementation
4. Controller with `@PathVariable String username`

---

### Phase 2: Post Management 📝

#### Database Schema: Post Entity

**Create `Post.java`:**
```java
@Entity
@Table(name = "posts", indexes = {
    @Index(name = "idx_post_user_id", columnList = "user_id"),
    @Index(name = "idx_post_created_at", columnList = "createdAt")
})
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Integer likesCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer commentsCount = 0;

    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

#### API 4: Create Post
- **Endpoint:** `POST /api/posts`
- **Auth:** Required
- **Request Body:** CreatePostRequest
- **Response:** PostDTO (201 Created)

**Implementation Steps:**
1. Create `CreatePostRequest.java`:
   ```java
   @NotBlank(message = "Content is required")
   @Size(max = 2000)
   private String content;

   @Pattern(regexp = "https?://.*", message = "Invalid URL")
   private String imageUrl;
   ```

2. Create `PostDTO.java`:
   ```java
   - UUID id
   - String content
   - String imageUrl
   - UserDTO author (basic user info)
   - Integer likesCount
   - Integer commentsCount
   - Boolean isLikedByCurrentUser
   - LocalDateTime createdAt
   - LocalDateTime updatedAt
   ```

3. Create `PostRepository.java` extends `JpaRepository<Post, UUID>`

4. Create `PostService.java` interface
   ```java
   PostDTO createPost(CreatePostRequest request);
   ```

5. Create `PostServiceImpl.java`:
   - Get current user from security context
   - Map request to Post entity
   - Set user relationship
   - Save post
   - Map to PostDTO

6. Create `PostController.java`:
   ```java
   @RestController
   @RequestMapping("/api/posts")
   ```

7. Add validation with `@Valid`

---

#### API 5: Get User Feed (Paginated)
- **Endpoint:** `GET /api/posts/feed?page=0&size=10`
- **Auth:** Required
- **Response:** PagedResponse<PostDTO>

**Implementation Steps:**
1. Create generic `PagedResponse.java`:
   ```java
   public class PagedResponse<T> {
       private List<T> content;
       private int page;
       private int size;
       private long totalElements;
       private int totalPages;
       private boolean last;
   }
   ```

2. Add to `PostRepository`:
   ```java
   @Query("SELECT p FROM Post p WHERE p.user.id IN :userIds ORDER BY p.createdAt DESC")
   Page<Post> findFeedPosts(@Param("userIds") List<UUID> userIds, Pageable pageable);
   ```

3. Service implementation:
   - Get current user
   - Get list of followed user IDs + current user ID
   - Query posts with Pageable
   - For each post, check if liked by current user
   - Map to PagedResponse<PostDTO>

4. Controller with `Pageable` parameter

---

#### API 6: Get User's Posts
- **Endpoint:** `GET /api/posts/user/{userId}?page=0&size=10`
- **Auth:** Optional
- **Response:** PagedResponse<PostDTO>

**Implementation Steps:**
1. Add to `PostRepository`:
   ```java
   Page<Post> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
   ```
2. Service & Controller

---

#### API 7: Get Single Post
- **Endpoint:** `GET /api/posts/{postId}`
- **Auth:** Optional
- **Response:** PostDTO

**Implementation Steps:**
1. Service: Find by ID, check if liked by current user (if authenticated)
2. Controller
3. Exception: `PostNotFoundException`

---

#### API 8: Update Post
- **Endpoint:** `PUT /api/posts/{postId}`
- **Auth:** Required (must be post author)
- **Request Body:** UpdatePostRequest
- **Response:** Updated PostDTO

**Implementation Steps:**
1. Create `UpdatePostRequest.java`:
   ```java
   private String content;
   private String imageUrl;
   ```
2. Service implementation:
   - Find post by ID
   - Verify current user is the author
   - Update non-null fields
   - Save
3. Exceptions: `PostNotFoundException`, `UnauthorizedAccessException`

---

#### API 9: Delete Post
- **Endpoint:** `DELETE /api/posts/{postId}`
- **Auth:** Required (must be post author)
- **Response:** 204 No Content

**Implementation Steps:**
1. Service:
   - Find post
   - Verify ownership
   - Delete (cascade will handle likes/comments if configured)
2. Controller returns `ResponseEntity.noContent()`

---

### Phase 3: Like System ❤️

#### Database Schema: Like Entity

**Create `Like.java`:**
```java
@Entity
@Table(name = "likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}),
    indexes = {
        @Index(name = "idx_like_post_id", columnList = "post_id"),
        @Index(name = "idx_like_user_id", columnList = "user_id")
    }
)
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

---

#### API 10: Like a Post
- **Endpoint:** `POST /api/posts/{postId}/like`
- **Auth:** Required
- **Response:** 204 No Content

**Implementation Steps:**
1. Create `LikeRepository.java`:
   ```java
   boolean existsByUserAndPost(User user, Post post);
   Optional<Like> findByUserAndPost(User user, Post post);
   void deleteByUserAndPost(User user, Post post);
   ```

2. Service implementation:
   - Get current user
   - Find post
   - Check if already liked (throw exception if yes)
   - Create Like entity
   - Increment post.likesCount
   - Save like and post
   - Use `@Transactional`

3. Exception: `AlreadyLikedException`, `PostNotFoundException`

---

#### API 11: Unlike a Post
- **Endpoint:** `DELETE /api/posts/{postId}/like`
- **Auth:** Required
- **Response:** 204 No Content

**Implementation Steps:**
1. Service:
   - Find like by user and post
   - Delete like
   - Decrement post.likesCount
   - Save post
2. Exception: `LikeNotFoundException`

---

#### API 12: Get Post Likes (Paginated)
- **Endpoint:** `GET /api/posts/{postId}/likes?page=0&size=20`
- **Auth:** Optional
- **Response:** PagedResponse<LikeDTO>

**Implementation Steps:**
1. Create `LikeDTO.java`:
   ```java
   - UUID id
   - UserDTO user
   - LocalDateTime createdAt
   ```
2. Add to repository:
   ```java
   Page<Like> findByPostOrderByCreatedAtDesc(Post post, Pageable pageable);
   ```
3. Service & Controller

---

### Phase 4: Comment System 💬

#### Database Schema: Comment Entity

**Create `Comment.java`:**
```java
@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_comment_post_id", columnList = "post_id"),
    @Index(name = "idx_comment_user_id", columnList = "user_id"),
    @Index(name = "idx_comment_created_at", columnList = "createdAt")
})
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

#### API 13: Add Comment
- **Endpoint:** `POST /api/posts/{postId}/comments`
- **Auth:** Required
- **Request Body:** CreateCommentRequest
- **Response:** CommentDTO (201 Created)

**Implementation Steps:**
1. Create `CreateCommentRequest.java`:
   ```java
   @NotBlank(message = "Content is required")
   @Size(max = 1000)
   private String content;
   ```

2. Create `CommentDTO.java`:
   ```java
   - UUID id
   - String content
   - UserDTO author
   - LocalDateTime createdAt
   - LocalDateTime updatedAt
   - Boolean isAuthor (true if current user is comment author)
   ```

3. Create `CommentRepository.java`

4. Service implementation:
   - Get current user
   - Find post
   - Create comment
   - Increment post.commentsCount
   - Save comment and post
   - Use `@Transactional`

5. Controller

---

#### API 14: Get Post Comments (Paginated)
- **Endpoint:** `GET /api/posts/{postId}/comments?page=0&size=20`
- **Auth:** Optional
- **Response:** PagedResponse<CommentDTO>

**Implementation Steps:**
1. Add to repository:
   ```java
   Page<Comment> findByPostOrderByCreatedAtDesc(Post post, Pageable pageable);
   ```
2. Service: Map to DTOs with `isAuthor` flag
3. Controller

---

#### API 15: Update Comment
- **Endpoint:** `PUT /api/comments/{commentId}`
- **Auth:** Required (must be comment author)
- **Request Body:** UpdateCommentRequest
- **Response:** Updated CommentDTO

**Implementation Steps:**
1. Create `UpdateCommentRequest.java`
2. Service: Find, verify ownership, update
3. Exception: `CommentNotFoundException`, `UnauthorizedAccessException`

---

#### API 16: Delete Comment
- **Endpoint:** `DELETE /api/comments/{commentId}`
- **Auth:** Required (must be comment author or post owner)
- **Response:** 204 No Content

**Implementation Steps:**
1. Service:
   - Find comment
   - Verify user is author OR post owner
   - Delete comment
   - Decrement post.commentsCount

---

### Phase 5: Follow System 👥

#### Database Schema: Follow Entity

**Create `Follow.java`:**
```java
@Entity
@Table(name = "follows",
    uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}),
    indexes = {
        @Index(name = "idx_follow_follower_id", columnList = "follower_id"),
        @Index(name = "idx_follow_following_id", columnList = "following_id")
    }
)
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;  // Person who follows

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;  // Person being followed

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

**Update User Entity:**
```java
// Add these fields to User.java:
@Column(nullable = false)
@Builder.Default
private Integer followersCount = 0;

@Column(nullable = false)
@Builder.Default
private Integer followingCount = 0;

@Column(nullable = false)
@Builder.Default
private Integer postsCount = 0;
```

---

#### API 17: Follow a User
- **Endpoint:** `POST /api/users/{userId}/follow`
- **Auth:** Required
- **Response:** 204 No Content

**Implementation Steps:**
1. Create `FollowRepository.java`:
   ```java
   boolean existsByFollowerAndFollowing(User follower, User following);
   Optional<Follow> findByFollowerAndFollowing(User follower, User following);
   void deleteByFollowerAndFollowing(User follower, User following);
   Page<Follow> findByFollowerOrderByCreatedAtDesc(User follower, Pageable pageable);
   Page<Follow> findByFollowingOrderByCreatedAtDesc(User following, Pageable pageable);
   ```

2. Service implementation:
   - Get current user (follower)
   - Find target user (following)
   - Check if already following
   - Check if trying to follow self
   - Create Follow entity
   - Increment follower.followingCount
   - Increment following.followersCount
   - Save all (use `@Transactional`)

3. Exceptions: `AlreadyFollowingException`, `CannotFollowSelfException`, `UserNotFoundException`

---

#### API 18: Unfollow a User
- **Endpoint:** `DELETE /api/users/{userId}/follow`
- **Auth:** Required
- **Response:** 204 No Content

**Implementation Steps:**
1. Service:
   - Find follow relationship
   - Delete
   - Decrement counts
   - Save users
2. Exception: `NotFollowingException`

---

#### API 19: Get User's Followers
- **Endpoint:** `GET /api/users/{userId}/followers?page=0&size=20`
- **Auth:** Optional
- **Response:** PagedResponse<FollowDTO>

**Implementation Steps:**
1. Create `FollowDTO.java`:
   ```java
   - UUID id
   - UserDTO user (the follower)
   - LocalDateTime createdAt
   - Boolean isFollowedByCurrentUser (if authenticated)
   ```
2. Service & Controller

---

#### API 20: Get User's Following
- **Endpoint:** `GET /api/users/{userId}/following?page=0&size=20`
- **Auth:** Optional
- **Response:** PagedResponse<FollowDTO>

---

### Phase 6: Search & Discovery 🔍

#### API 21: Search Users
- **Endpoint:** `GET /api/search/users?q=john&page=0&size=10`
- **Auth:** Optional
- **Response:** PagedResponse<PublicProfileDTO>

**Implementation Steps:**
1. Add to `UserRepository`:
   ```java
   @Query("SELECT u FROM User u WHERE " +
          "LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
          "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
   Page<User> searchUsers(@Param("query") String query, Pageable pageable);
   ```

2. Or search in `ProfileRepository`:
   ```java
   @Query("SELECT p FROM Profile p WHERE " +
          "LOWER(p.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
          "LOWER(p.bio) LIKE LOWER(CONCAT('%', :query, '%'))")
   Page<Profile> searchProfiles(@Param("query") String query, Pageable pageable);
   ```

3. Create `SearchController.java`
4. Service & Controller

---

#### API 22: Search Posts
- **Endpoint:** `GET /api/search/posts?q=keyword&page=0&size=10`
- **Auth:** Optional
- **Response:** PagedResponse<PostDTO>

**Implementation Steps:**
1. Add to `PostRepository`:
   ```java
   @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))")
   Page<Post> searchPosts(@Param("query") String query, Pageable pageable);
   ```
2. Service & Controller

---

#### API 23: Get Suggested Users (People to Follow)
- **Endpoint:** `GET /api/users/suggestions?limit=10`
- **Auth:** Required
- **Response:** List<PublicProfileDTO>

**Implementation Steps:**
1. Algorithm: Users with most followers that current user doesn't follow
2. Add to `UserRepository`:
   ```java
   @Query("SELECT u FROM User u WHERE u.id NOT IN " +
          "(SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId) " +
          "AND u.id != :userId ORDER BY u.followersCount DESC")
   List<User> findSuggestedUsers(@Param("userId") UUID userId, Pageable pageable);
   ```

---

### Phase 7: Notifications 🔔

#### Database Schema: Notification Entity

**Create `Notification.java`:**
```java
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_recipient_read_created",
           columnList = "recipient_id, isRead, createdAt")
})
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;  // Who triggered the notification

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;  // LIKE, COMMENT, FOLLOW

    // Optional references
    private UUID postId;
    private UUID commentId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

**Create `NotificationType.java` enum:**
```java
public enum NotificationType {
    LIKE,     // "John liked your post"
    COMMENT,  // "Jane commented on your post"
    FOLLOW    // "Mike started following you"
}
```

---

#### API 24: Get User Notifications
- **Endpoint:** `GET /api/notifications?page=0&size=20&unreadOnly=false`
- **Auth:** Required
- **Response:** PagedResponse<NotificationDTO>

**Implementation Steps:**
1. Create `NotificationDTO.java`:
   ```java
   - UUID id
   - UserDTO actor
   - NotificationType type
   - String message
   - UUID postId (optional)
   - UUID commentId (optional)
   - Boolean isRead
   - LocalDateTime createdAt
   ```

2. Create `NotificationRepository.java`:
   ```java
   Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);
   Page<Notification> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(User recipient, Pageable pageable);
   long countByRecipientAndIsReadFalse(User recipient);
   ```

3. Service & Controller

---

#### API 25: Mark Notification as Read
- **Endpoint:** `PUT /api/notifications/{notificationId}/read`
- **Auth:** Required
- **Response:** 204 No Content

---

#### API 26: Mark All as Read
- **Endpoint:** `PUT /api/notifications/read-all`
- **Auth:** Required
- **Response:** 204 No Content

**Implementation Steps:**
1. Add to repository:
   ```java
   @Modifying
   @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false")
   int markAllAsRead(@Param("recipient") User recipient);
   ```

---

#### API 27: Get Unread Count
- **Endpoint:** `GET /api/notifications/unread-count`
- **Auth:** Required
- **Response:** `{ "count": 5 }`

---

#### Notification Creation (Background)

**Create `NotificationService.java` with async methods:**

```java
@Async
public void createLikeNotification(User actor, Post post) {
    if (post.getUser().getId().equals(actor.getId())) return; // Don't notify self

    Notification notification = Notification.builder()
        .recipient(post.getUser())
        .actor(actor)
        .type(NotificationType.LIKE)
        .postId(post.getId())
        .message(actor.getName() + " liked your post")
        .build();
    notificationRepository.save(notification);
}

@Async
public void createCommentNotification(User actor, Post post, Comment comment) {
    // Similar implementation
}

@Async
public void createFollowNotification(User actor, User target) {
    // Similar implementation
}
```

**Enable async in main application:**
```java
@SpringBootApplication
@EnableAsync
public class SocialMediaBackend1Application {
    // ...
}
```

**Call from services:**
```java
// In LikeService after creating like:
notificationService.createLikeNotification(currentUser, post);

// In CommentService after creating comment:
notificationService.createCommentNotification(currentUser, post, comment);

// In FollowService after creating follow:
notificationService.createFollowNotification(currentUser, targetUser);
```

---

## Development Patterns

### Standard Flow for Each API

```
1. ENTITY (if needed)
   └─ @Entity class with JPA annotations
   └─ Relationships (@ManyToOne, @OneToMany, etc.)
   └─ Indexes (@Index on frequently queried columns)
   └─ Lifecycle hooks (@PrePersist, @PreUpdate)
   └─ Lombok annotations (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)

2. REPOSITORY
   └─ Interface extending JpaRepository<Entity, UUID>
   └─ Custom query methods (findByXxx, existsByXxx)
   └─ @Query for complex queries

3. DTOs
   └─ Request DTO (validation: @NotBlank, @Size, @Email, @Pattern)
   └─ Response DTO (what frontend needs)
   └─ Mapper configuration (ModelMapper or manual mapping)

4. EXCEPTION (if needed)
   └─ Custom exception extending RuntimeException
   └─ Add @ExceptionHandler in GlobalExceptionHandler
   └─ Return appropriate HTTP status

5. SERVICE INTERFACE
   └─ Method signatures with clear return types
   └─ JavaDoc comments

6. SERVICE IMPLEMENTATION
   └─ @Service annotation
   └─ @RequiredArgsConstructor for dependency injection
   └─ @Transactional where needed
   └─ Business logic
   └─ Entity to DTO mapping

7. CONTROLLER
   └─ @RestController
   └─ @RequestMapping for base path
   └─ HTTP method annotations (@GetMapping, @PostMapping, etc.)
   └─ @Valid for request validation
   └─ ResponseEntity with appropriate status codes

8. TESTING (recommended)
   └─ Unit tests for service layer
   └─ Integration tests for controllers
   └─ Test data builders
```

---

## Database Indexes Strategy

### Why Indexes Matter
- **Without index:** Full table scan (O(n)) — slow on large datasets
- **With index:** B-tree lookup (O(log n)) — ~700x faster on 10K rows

### Indexing Rules
1. **Primary keys** — Auto-indexed by JPA
2. **Foreign keys** — Explicitly index all FK columns
3. **Unique constraints** — Auto-indexed
4. **WHERE clause columns** — Index columns in `findBy` queries
5. **ORDER BY columns** — Index for sorting performance
6. **Composite indexes** — For multi-column queries (order matters!)

### Current Indexes
```sql
-- Session table
CREATE INDEX idx_session_refresh_token ON session(refreshToken);
CREATE INDEX idx_session_user_id ON session(user_id);

-- Future indexes to add:
-- Post table
CREATE INDEX idx_post_user_id ON posts(user_id);
CREATE INDEX idx_post_created_at ON posts(createdAt);

-- Like table
CREATE INDEX idx_like_post_id ON likes(post_id);
CREATE INDEX idx_like_user_id ON likes(user_id);
CREATE UNIQUE INDEX uk_like_user_post ON likes(user_id, post_id);

-- Comment table
CREATE INDEX idx_comment_post_id ON comments(post_id);
CREATE INDEX idx_comment_user_id ON comments(user_id);

-- Follow table
CREATE INDEX idx_follow_follower_id ON follows(follower_id);
CREATE INDEX idx_follow_following_id ON follows(following_id);
CREATE UNIQUE INDEX uk_follow_follower_following ON follows(follower_id, following_id);

-- Notification table
CREATE INDEX idx_notification_recipient_read_created ON notifications(recipient_id, isRead, createdAt);
```

---

## Priority Implementation Order

### 🚀 Must Have (MVP)
1. **Profile Management** (Get, Update) — Users need to view/edit profiles
2. **Post CRUD** — Core social media feature
3. **Follow System** — Build social graph
4. **Feed Algorithm** — Show posts from followed users
5. **Like System** — Basic engagement

### 🎯 Should Have (V1.0)
6. **Comment System** — Deep engagement
7. **Search** — User & post discovery
8. **Notifications** — User retention

### 💡 Nice to Have (V2.0)
9. **Hashtags** — Content discovery
10. **Media Upload** — Direct image/video upload (not just URLs)
11. **Stories** — 24-hour ephemeral content
12. **Direct Messaging** — Private conversations
13. **Saved Posts** — Bookmark functionality
14. **Report/Block** — Moderation features

---

## Security Considerations

### Authentication
- ✅ JWT access tokens (short-lived: 15 mins)
- ✅ Refresh tokens (long-lived: 7 days, httpOnly cookies)
- ✅ Password hashing with BCrypt
- ⚠️ TODO: Email verification
- ⚠️ TODO: Password reset flow
- ⚠️ TODO: Rate limiting (prevent brute force)

### Authorization
- ✅ Role-based access (USER, ADMIN)
- ✅ Owner verification (users can only edit own content)
- ⚠️ TODO: Soft delete (don't permanently delete user data)
- ⚠️ TODO: Account privacy settings (public/private profiles)

### Data Validation
- ✅ Bean validation (@NotBlank, @Size, @Email)
- ⚠️ TODO: SQL injection protection (use parameterized queries)
- ⚠️ TODO: XSS protection (sanitize user input)
- ⚠️ TODO: File upload validation (size, type, virus scan)

### CORS
- ⚠️ TODO: Configure allowed origins for production
- ⚠️ TODO: Restrict HTTP methods

---

## Performance Optimization

### Database
- ✅ Indexes on frequently queried columns
- ⚠️ TODO: Query optimization (N+1 problem — use JOIN FETCH)
- ⚠️ TODO: Database connection pooling (HikariCP config)
- ⚠️ TODO: Read replicas for scaling

### Caching
- ⚠️ TODO: Redis for session storage
- ⚠️ TODO: Cache user profiles (frequently accessed)
- ⚠️ TODO: Cache feed posts (with TTL)

### API Performance
- ✅ Pagination for list endpoints
- ⚠️ TODO: Lazy loading for relationships
- ⚠️ TODO: DTOs to avoid exposing full entities
- ⚠️ TODO: Async processing for notifications

---

## Testing Strategy

### Unit Tests
- Service layer (business logic)
- Mock repositories with Mockito
- Test edge cases and error handling

### Integration Tests
- Controller endpoints (MockMvc)
- Database interactions (TestContainers)
- Security configurations

### Test Coverage Goals
- Services: 80%+
- Controllers: 70%+
- Critical flows (auth, payments): 95%+

---

## Deployment Checklist

### Environment Configuration
- [ ] Separate dev/staging/prod properties
- [ ] Environment variables for secrets (DB password, JWT secret)
- [ ] Logging configuration (different levels per env)

### Database
- [ ] Migration scripts (Flyway or Liquibase)
- [ ] Backup strategy
- [ ] Index creation scripts

### Security
- [ ] HTTPS only in production
- [ ] Secure cookies (secure=true, sameSite=strict)
- [ ] CORS configuration for production domain
- [ ] Rate limiting

### Monitoring
- [ ] Health check endpoint (`/actuator/health`)
- [ ] Metrics (Prometheus + Grafana)
- [ ] Error tracking (Sentry)
- [ ] Logging aggregation (ELK stack)

---

## Tech Stack

### Backend
- **Framework:** Spring Boot 3.x
- **Language:** Java 17+
- **Database:** PostgreSQL (or MySQL)
- **ORM:** JPA/Hibernate
- **Security:** Spring Security + JWT
- **Validation:** Hibernate Validator
- **Mapping:** ModelMapper
- **Build Tool:** Maven

### Future Additions
- **Caching:** Redis
- **Message Queue:** RabbitMQ / Kafka (for async tasks)
- **File Storage:** AWS S3 / MinIO
- **Email Service:** SendGrid / AWS SES
- **API Documentation:** Swagger/OpenAPI

---

## API Documentation Template

### Endpoint: [METHOD] /api/resource

**Description:** Brief description of what this endpoint does.

**Authentication:** Required/Optional

**Authorization:** Who can access (USER, ADMIN, Owner)

**Request:**
```json
{
  "field": "value"
}
```

**Response (Success):**
```json
{
  "field": "value"
}
```

**Response (Error):**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/resource"
}
```

**Status Codes:**
- `200 OK` — Success
- `201 Created` — Resource created
- `204 No Content` — Success with no response body
- `400 Bad Request` — Validation error
- `401 Unauthorized` — Not authenticated
- `403 Forbidden` — Not authorized
- `404 Not Found` — Resource doesn't exist
- `409 Conflict` — Resource already exists
- `500 Internal Server Error` — Server error

---

## Next Steps

1. ✅ Review this plan
2. ⬜ Set up database (PostgreSQL recommended)
3. ⬜ Configure application.properties
4. ⬜ Start with Phase 1: Profile Management APIs
5. ⬜ Test each API with Postman/Insomnia
6. ⬜ Build frontend integration
7. ⬜ Deploy to staging
8. ⬜ Production deployment

---

**Last Updated:** 2024
**Project Status:** In Development
**Current Phase:** Profile Management APIs
