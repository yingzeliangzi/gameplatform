
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
│  ├─ package-lock.json
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
│  │  │  ├─ notification.js
│  │  │  ├─ post.js
│  │  │  ├─ report.js
│  │  │  └─ settings.js
│  │  ├─ App.vue
│  │  ├─ assets
│  │  │  ├─ images
│  │  │  │  ├─ 403.svg
│  │  │  │  ├─ 404.svg
│  │  │  │  └─ 500.svg
│  │  │  └─ styles
│  │  │     └─ index.scss
│  │  ├─ components
│  │  │  ├─ comment
│  │  │  │  ├─ CommentEditior.vue
│  │  │  │  ├─ CommentFilters.vue
│  │  │  │  ├─ CommentItem.vue
│  │  │  │  └─ CommentList.vue
│  │  │  ├─ common
│  │  │  │  ├─ Footer.vue
│  │  │  │  ├─ Header.vue
│  │  │  │  ├─ ImageUpload.vue
│  │  │  │  └─ Sidebar.vue
│  │  │  ├─ event
│  │  │  ├─ game
│  │  │  │  └─ GameCategories.vue
│  │  │  ├─ NotificationBell.vue
│  │  │  ├─ NotificationList.vue
│  │  │  └─ post
│  │  ├─ layouts
│  │  │  ├─ AdminLayout.vue
│  │  │  ├─ Layout.vue
│  │  │  └─ MainLayout.vue
│  │  ├─ main.js
│  │  ├─ permission.js
│  │  ├─ router
│  │  │  ├─ error.js
│  │  │  ├─ index.js
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
│  │  │     ├─ post.js
│  │  │     ├─ statics.js
│  │  │     └─ tagsView.js
│  │  ├─ utils
│  │  │  ├─ auth.js
│  │  │  ├─ crypto.js
│  │  │  ├─ form-rules.js
│  │  │  ├─ index.js
│  │  │  ├─ request.js
│  │  │  ├─ validate.js
│  │  │  └─ websocket.js
│  │  └─ views
│  │     ├─ admin
│  │     │  ├─ ContentManagement.vue
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
│  │     │  └─ UserProfile.vue
│  │     └─ settings
│  │        └─ index.vue
│  └─ vue.config.js
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