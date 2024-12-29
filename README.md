
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
│  ├─ eslint.config.mjs
│  ├─ package-lock.json
│  ├─ package.json
│  ├─ public
│  │  └─ index.html
│  └─ src
│     ├─ .eslintrc.js
│     ├─ api
│     │  ├─ auth.js
│     │  ├─ comment.js
│     │  ├─ event.js
│     │  ├─ game.js
│     │  ├─ notification.js
│     │  ├─ post.js
│     │  ├─ report.js
│     │  └─ settings.js
│     ├─ App.vue
│     ├─ assets
│     │  ├─ images
│     │  └─ styles
│     ├─ components
│     │  ├─ common
│     │  │  ├─ Footer.vue
│     │  │  ├─ Header.vue
│     │  │  └─ Sidebar.vue
│     │  ├─ event
│     │  ├─ game
│     │  ├─ NotificationBell.vue
│     │  ├─ NotificationList.vue
│     │  └─ post
│     ├─ layouts
│     │  ├─ AdminLayout.vue
│     │  └─ MainLayout.vue
│     ├─ main.js
│     ├─ router
│     │  ├─ error.js
│     │  ├─ index.js
│     │  └─ permission.js
│     ├─ store
│     │  ├─ index.js
│     │  └─ modules
│     │     ├─ auth.js
│     │     ├─ event.js
│     │     ├─ game.js
│     │     ├─ notification.js
│     │     ├─ permission.js
│     │     └─ post.js
│     ├─ utils
│     │  ├─ auth.js
│     │  ├─ request.js
│     │  └─ websocket.js
│     └─ views
│        ├─ admin
│        │  ├─ ContentManagement.vue
│        │  ├─ PostManagement.vue
│        │  ├─ ReportManagement.vue
│        │  └─ UserManagement.vue
│        ├─ auth
│        │  ├─ Login.vue
│        │  └─ Register.vue
│        ├─ community
│        │  ├─ PostDetail.vue
│        │  ├─ PostEdit.vue
│        │  └─ PostList.vue
│        ├─ dashboard
│        │  └─ index.vue
│        ├─ error
│        │  ├─ 403.vue
│        │  ├─ 404.vue
│        │  └─ 500.vue
│        ├─ event
│        │  ├─ EventDetail.vue
│        │  ├─ EventForm.vue
│        │  ├─ EventList.vue
│        │  └─ RegistrationManagement.vue
│        ├─ game
│        │  ├─ GameDetail.vue
│        │  └─ GameList.vue
│        ├─ Home.vue
│        ├─ profile
│        │  ├─ index.vue
│        │  └─ UserProfile.vue
│        └─ settings
│           └─ index.vue
├─ jsconfig.json
├─ mvnw
├─ mvnw.cmd
├─ package-lock.json
├─ pom.xml
├─ README.md
├─ src
│  ├─ main
│  │  ├─ java
│  │  │  └─ com
│  │  │     └─ gameplatform
│  │  │        ├─ config
│  │  │        │  ├─ CorsConfig.java
│  │  │        │  ├─ SecurityConfig.java
│  │  │        │  ├─ SwaggerConfig.java
│  │  │        │  └─ WebSocketConfig.java
│  │  │        ├─ controller
│  │  │        │  ├─ AdminController.java
│  │  │        │  ├─ AuthController.java
│  │  │        │  ├─ CommentController.java
│  │  │        │  ├─ EventController.java
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
│  │  │        │  │  ├─ PostDTO.java
│  │  │        │  │  ├─ RegisterRequestDTO.java
│  │  │        │  │  ├─ ReportDTO.java
│  │  │        │  │  ├─ StatisticsDTO.java
│  │  │        │  │  ├─ UnreadCountDTO.java
│  │  │        │  │  ├─ UserDTO.java
│  │  │        │  │  └─ UserGameDTO.java
│  │  │        │  └─ entity
│  │  │        │     ├─ Comment.java
│  │  │        │     ├─ CommentLike.java
│  │  │        │     ├─ DailyStatistics.java
│  │  │        │     ├─ Event.java
│  │  │        │     ├─ EventRegistration.java
│  │  │        │     ├─ Game.java
│  │  │        │     ├─ Notification.java
│  │  │        │     ├─ Post.java
│  │  │        │     ├─ PostLike.java
│  │  │        │     ├─ Report.java
│  │  │        │     ├─ Setting.java
│  │  │        │     ├─ User.java
│  │  │        │     ├─ UserGame.java
│  │  │        │     └─ UserSetting.java
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
│  │  │        │  ├─ JwtAuthenticationFilter.java
│  │  │        │  └─ JwtAuthenticationProvider.java
│  │  │        ├─ service
│  │  │        │  ├─ CommentService.java
│  │  │        │  ├─ EmailService.java
│  │  │        │  ├─ EventService.java
│  │  │        │  ├─ GameService.java
│  │  │        │  ├─ impl
│  │  │        │  │  ├─ CommentServiceImpl.java
│  │  │        │  │  ├─ EventServiceImpl.java
│  │  │        │  │  ├─ GameServiceImpl.java
│  │  │        │  │  ├─ NotificationServiceImpl.java
│  │  │        │  │  ├─ PostServiceImpl.java
│  │  │        │  │  ├─ SettingsServiceImpl.java
│  │  │        │  │  ├─ StatisticsServiceImpl.java
│  │  │        │  │  └─ UserServiceImpl.java
│  │  │        │  ├─ NotificationService.java
│  │  │        │  ├─ PostService.java
│  │  │        │  ├─ SettingsService.java
│  │  │        │  ├─ StatisticsService.java
│  │  │        │  ├─ UserService.java
│  │  │        │  └─ WebSocketService.java
│  │  │        └─ util
│  │  │           ├─ DateUtils.java
│  │  │           ├─ FileUtil.java
│  │  │           ├─ IpUtils.java
│  │  │           ├─ JwtUtil.java
│  │  │           └─ PageUtils.java
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