## Design and implementation of a game service community platform based on SPRINGBOOT and VUE

### Functional modules
1. User registration and login Users register new accounts and log in to existing accounts, log in using social media accounts, and change personal information (profile editing, avatar uploading, etc.) 
2. Game library management Synchronize the user's game library, view and search all games, filter by category, popularity, rating, etc., display the game details page (game introduction, screenshots, price, etc.), and export the user's game ownership details in the form of a CSV file 
3. Community communication Rate games, post and reply to others' posts, and report posts 
4. Activities and competitions Administrators publish and manage community activities and game competitions; users sign up for activities and competitions, and view activity details and schedules 
5. Notification system Site system notifications (game discount reminders, event notifications, community notifications, etc.) 
6. Permission management (for administrators only) Set user permissions, manage user roles and access control 
7. Content management (for administrators only) Manage game information and community content (posts, comments, etc.), view and handle user reports

### Requirements
1. Development language: Java;
2. FE: Vue; 
3. BE: SpringBoot; 
4. Project building tool: Maven; 
5. Database: MySQL; 

### Treemap
```
gameplatform
├─ .mvn
│  └─ wrapper
│     └─ maven-wrapper.properties
├─ application-dev.yml
├─ application-prod.yml
├─ application.yml
├─ game-community-frontend
│  ├─ .env
│  ├─ .env.development
│  ├─ .env.production
│  ├─ .env.staging
│  ├─ eslint.config.mjs
│  ├─ jsconfig.json
│  ├─ package.json
│  ├─ public
│  │  └─ index.html
│  ├─ src
│  │  ├─ .eslintrc.js
│  │  ├─ api
│  │  │  ├─ auth.js
│  │  │  ├─ comment.js
│  │  │  ├─ event.js
│  │  │  ├─ game.js
│  │  │  ├─ home.js
│  │  │  ├─ index.js
│  │  │  ├─ notification.js
│  │  │  ├─ post.js
│  │  │  ├─ report.js
│  │  │  ├─ settings.js
│  │  │  └─ user.js
│  │  ├─ App.vue
│  │  ├─ assets
│  │  │  ├─ images
│  │  │  │  ├─ 403.svg
│  │  │  │  ├─ 404.svg
│  │  │  │  └─ 500.svg
│  │  │  └─ styles
│  │  │     └─ index.scss
│  │  ├─ components
│  │  │  ├─ AppMain.vue
│  │  │  ├─ comments
│  │  │  │  ├─ CommentEditor.vue
│  │  │  │  ├─ CommentFilters.vue
│  │  │  │  ├─ CommentItem.vue
│  │  │  │  └─ CommentList.vue
│  │  │  ├─ common
│  │  │  │  ├─ BaseList.vue
│  │  │  │  └─ ImageUpload.vue
│  │  │  ├─ event
│  │  │  │  ├─ EventCard.vue
│  │  │  │  └─ EventCardSimple.vue
│  │  │  ├─ Footer.vue
│  │  │  ├─ game
│  │  │  │  ├─ GameCard.vue
│  │  │  │  └─ GameCategories.vue
│  │  │  ├─ Header.vue
│  │  │  ├─ layout
│  │  │  │  └─ PageContainer.vue
│  │  │  ├─ Navbar.vue
│  │  │  ├─ notification
│  │  │  │  ├─ NotificationItem.vue
│  │  │  │  └─ NotificationList.vue
│  │  │  ├─ NotificationBell.vue
│  │  │  ├─ NotificationList.vue
│  │  │  ├─ post
│  │  │  │  └─ PostCard.vue
│  │  │  ├─ sidebar
│  │  │  │  ├─ index.vue
│  │  │  │  └─ SidebarItem.vue
│  │  │  ├─ Sidebar.vue
│  │  │  └─ upload
│  │  │     ├─ AvatarUpload.vue
│  │  │     └─ UploadComponent.vue
│  │  ├─ hooks
│  │  │  └─ useFormatTime.js
│  │  ├─ layout
│  │  │  └─ index.vue
│  │  ├─ layouts
│  │  │  ├─ AdminLayout.vue
│  │  │  ├─ BasicLayout.vue
│  │  │  ├─ Layout.vue
│  │  │  └─ MainLayout.vue
│  │  ├─ main.js
│  │  ├─ permission.js
│  │  ├─ router
│  │  │  ├─ error.js
│  │  │  ├─ index.js
│  │  │  ├─ modules
│  │  │  │  └─ routes.js
│  │  │  └─ permission.js
│  │  ├─ store
│  │  │  ├─ index.js
│  │  │  └─ modules
│  │  │     ├─ app.js
│  │  │     ├─ auth.js
│  │  │     ├─ comment.js
│  │  │     ├─ event.js
│  │  │     ├─ game.js
│  │  │     ├─ notification.js
│  │  │     ├─ permission.js
│  │  │     ├─ post.js
│  │  │     ├─ statics.js
│  │  │     ├─ tagsView.js
│  │  │     └─ user.js
│  │  ├─ utils
│  │  │  ├─ auth.js
│  │  │  ├─ crypto.js
│  │  │  ├─ form-rules.js
│  │  │  ├─ index.js
│  │  │  ├─ request.js
│  │  │  ├─ time.js
│  │  │  ├─ validate.js
│  │  │  └─ websocket.js
│  │  └─ views
│  │     ├─ admin
│  │     │  ├─ ContentManagement.vue
│  │     │  ├─ Dashboard.vue
│  │     │  ├─ PostManagement.vue
│  │     │  ├─ ReportManagement.vue
│  │     │  └─ UserManagement.vue
│  │     ├─ auth
│  │     │  ├─ Login.vue
│  │     │  └─ Register.vue
│  │     ├─ community
│  │     │  ├─ Community.vue
│  │     │  ├─ PostDetail.vue
│  │     │  ├─ PostEdit.vue
│  │     │  └─ PostList.vue
│  │     ├─ dashboard
│  │     │  └─ index.vue
│  │     ├─ error
│  │     │  ├─ 403.vue
│  │     │  ├─ 404.vue
│  │     │  └─ 500.vue
│  │     ├─ event
│  │     │  ├─ EventDetail.vue
│  │     │  ├─ EventForm.vue
│  │     │  ├─ EventList.vue
│  │     │  └─ RegistrationManagement.vue
│  │     ├─ game
│  │     │  ├─ GameDetail.vue
│  │     │  └─ GameList.vue
│  │     ├─ Home.vue
│  │     ├─ profile
│  │     │  ├─ index.vue
│  │     │  ├─ Profile.vue
│  │     │  └─ UserProfile.vue
│  │     └─ settings
│  │        ├─ index.vue
│  │        └─ Settings.vue
│  └─ vue.config.js
├─ jsconfig.json
├─ mvnw
├─ mvnw.cmd
├─ package-lock.json
├─ package.json
├─ pom.xml
├─ README.md
├─ src
│  ├─ main
│  │  ├─ java
│  │  │  └─ com
│  │  │     └─ gameplatform
│  │  │        ├─ annotation
│  │  │        │  └─ RequirePermission.java
│  │  │        ├─ aspect
│  │  │        │  └─ PermissionAspect.java
│  │  │        ├─ common
│  │  │        │  └─ Result.java
│  │  │        ├─ config
│  │  │        │  ├─ ActiveMQConfig.java
│  │  │        │  ├─ CorsConfig.java
│  │  │        │  ├─ properties
│  │  │        │  │  └─ NotificationProperties.java
│  │  │        │  ├─ RedisConfig.java
│  │  │        │  ├─ RedisHealthIndicator.java
│  │  │        │  ├─ SchedulingConfig.java
│  │  │        │  ├─ SecurityConfig.java
│  │  │        │  ├─ SwaggerConfig.java
│  │  │        │  ├─ ThymeleafConfig.java
│  │  │        │  ├─ WebSocketConfig.java
│  │  │        │  └─ WebSocketEventListener.java
│  │  │        ├─ controller
│  │  │        │  ├─ AdminController.java
│  │  │        │  ├─ AuthController.java
│  │  │        │  ├─ CommentController.java
│  │  │        │  ├─ EventController.java
│  │  │        │  ├─ FileController.java
│  │  │        │  ├─ GameController.java
│  │  │        │  ├─ NotificationController.java
│  │  │        │  ├─ PostController.java
│  │  │        │  └─ UserController.java
│  │  │        ├─ exception
│  │  │        │  ├─ AuthenticationException.java
│  │  │        │  ├─ BusinessException.java
│  │  │        │  ├─ ErrorCodes.java
│  │  │        │  ├─ ErrorResponse.java
│  │  │        │  ├─ FileOperationException.java
│  │  │        │  ├─ GlobalExceptionHandler.java
│  │  │        │  ├─ InvalidOperationException.java
│  │  │        │  ├─ ResourceAlreadyExistsException.java
│  │  │        │  ├─ ResourceNotFoundException.java
│  │  │        │  └─ ValidationException.java
│  │  │        ├─ GameplatformApplication.java
│  │  │        ├─ model
│  │  │        │  ├─ dto
│  │  │        │  │  ├─ CommentDTO.java
│  │  │        │  │  ├─ EventDTO.java
│  │  │        │  │  ├─ EventListItemDTO.java
│  │  │        │  │  ├─ EventRegistrationDTO.java
│  │  │        │  │  ├─ GameDTO.java
│  │  │        │  │  ├─ GameSearchDTO.java
│  │  │        │  │  ├─ LoginRequestDTO.java
│  │  │        │  │  ├─ LoginResponseDTO.java
│  │  │        │  │  ├─ NotificationDTO.java
│  │  │        │  │  ├─ NotificationSettingsDTO.java
│  │  │        │  │  ├─ PasswordUpdateDTO.java
│  │  │        │  │  ├─ PostDTO.java
│  │  │        │  │  ├─ RegisterRequestDTO.java
│  │  │        │  │  ├─ ReportDTO.java
│  │  │        │  │  ├─ StatisticsDTO.java
│  │  │        │  │  ├─ UnreadCountDTO.java
│  │  │        │  │  ├─ UserDTO.java
│  │  │        │  │  ├─ UserGameDTO.java
│  │  │        │  │  └─ UserProfileDTO.java
│  │  │        │  ├─ entity
│  │  │        │  │  ├─ Comment.java
│  │  │        │  │  ├─ CommentLike.java
│  │  │        │  │  ├─ DailyStatistics.java
│  │  │        │  │  ├─ Event.java
│  │  │        │  │  ├─ EventRegistration.java
│  │  │        │  │  ├─ Game.java
│  │  │        │  │  ├─ Notification.java
│  │  │        │  │  ├─ Post.java
│  │  │        │  │  ├─ PostLike.java
│  │  │        │  │  ├─ Report.java
│  │  │        │  │  ├─ Setting.java
│  │  │        │  │  ├─ User.java
│  │  │        │  │  ├─ UserGame.java
│  │  │        │  │  └─ UserSetting.java
│  │  │        │  └─ message
│  │  │        │     └─ NotificationMessage.java
│  │  │        ├─ repository
│  │  │        │  ├─ CommentLikeRepository.java
│  │  │        │  ├─ CommentRepository.java
│  │  │        │  ├─ DailyStatisticsRepository.java
│  │  │        │  ├─ EventRegistrationRepository.java
│  │  │        │  ├─ EventRepository.java
│  │  │        │  ├─ GameRepository.java
│  │  │        │  ├─ NotificationRepository.java
│  │  │        │  ├─ PostLikeRepository.java
│  │  │        │  ├─ PostRepository.java
│  │  │        │  ├─ ReportRepository.java
│  │  │        │  ├─ SettingRepository.java
│  │  │        │  ├─ UserGameRepository.java
│  │  │        │  ├─ UserRepository.java
│  │  │        │  └─ UserSettingRepository.java
│  │  │        ├─ security
│  │  │        │  ├─ CustomAccessDeniedHandler.java
│  │  │        │  ├─ CustomAuthenticationEntryPoint.java
│  │  │        │  ├─ JwtAuthenticationFilter.java
│  │  │        │  ├─ JwtAuthenticationProvider.java
│  │  │        │  └─ WebSocketAuthenticationInterceptor.java
│  │  │        ├─ service
│  │  │        │  ├─ BaseEventService.java
│  │  │        │  ├─ CacheService.java
│  │  │        │  ├─ CommentService.java
│  │  │        │  ├─ EmailService.java
│  │  │        │  ├─ EventService.java
│  │  │        │  ├─ GameService.java
│  │  │        │  ├─ impl
│  │  │        │  │  ├─ CacheServiceImpl.java
│  │  │        │  │  ├─ CommentServiceImpl.java
│  │  │        │  │  ├─ EmailServiceImpl.java
│  │  │        │  │  ├─ EventServiceImpl.java
│  │  │        │  │  ├─ GameServiceImpl.java
│  │  │        │  │  ├─ NotificationServiceImpl.java
│  │  │        │  │  ├─ PostServiceImpl.java
│  │  │        │  │  ├─ SettingsServiceImpl.java
│  │  │        │  │  ├─ StatisticsServiceImpl.java
│  │  │        │  │  └─ UserServiceImpl.java
│  │  │        │  ├─ message
│  │  │        │  │  └─ NotificationListener.java
│  │  │        │  ├─ NotificationService.java
│  │  │        │  ├─ PostService.java
│  │  │        │  ├─ scheduled
│  │  │        │  │  └─ ScheduledTasks.java
│  │  │        │  ├─ SettingsService.java
│  │  │        │  ├─ StatisticsService.java
│  │  │        │  ├─ UserService.java
│  │  │        │  └─ WebSocketService.java
│  │  │        └─ util
│  │  │           ├─ DateUtils.java
│  │  │           ├─ FileUtil.java
│  │  │           ├─ IpUtils.java
│  │  │           ├─ JwtUtil.java
│  │  │           ├─ PageUtils.java
│  │  │           └─ VerificationCodeUtil.java
│  │  └─ resources
│  │     ├─ application.properties
│  │     ├─ static
│  │     └─ templates
│  └─ test
│     └─ java
│        └─ com
│           └─ gameplatform
│              └─ GameplatformApplicationTests.java
└─ vue.config.js

```