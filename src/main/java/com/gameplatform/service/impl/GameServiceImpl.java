package com.gameplatform.service.impl;
import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.GameDTO;
import com.gameplatform.model.entity.Game;
import com.gameplatform.model.entity.UserGame;
import com.gameplatform.model.entity.User;
import com.gameplatform.repository.GameRepository;
import com.gameplatform.repository.UserGameRepository;
import com.gameplatform.repository.UserRepository;
import com.gameplatform.service.GameService;
import com.gameplatform.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:13
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final UserGameRepository userGameRepository;
    private final FileUtil fileUtil;

    @Override
    @Transactional(readOnly = true)
    public GameDTO getGameById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new BusinessException("游戏不存在"));
        return convertToDTO(game);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameDTO> searchGames(GameDTO searchDTO, Pageable pageable) {
        Page<Game> games;

        if (searchDTO.getCategories() != null && !searchDTO.getCategories().isEmpty()) {
            games = gameRepository.findByCategoriesIn(searchDTO.getCategories(), pageable);
        } else if (searchDTO.getTitle() != null && !searchDTO.getTitle().isEmpty()) {
            games = gameRepository.findByTitleContainingIgnoreCase(searchDTO.getTitle(), pageable);
        } else {
            games = gameRepository.findAll(pageable);
        }

        return games.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public GameDTO createGame(GameDTO gameDTO, MultipartFile coverImage) {
        // 验证游戏标题是否唯一
        if (gameRepository.existsByTitle(gameDTO.getTitle())) {
            throw new BusinessException("游戏标题已存在");
        }

        Game game = new Game();
        BeanUtils.copyProperties(gameDTO, game, "id", "coverImage", "screenshots");

        // 处理封面图片
        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                String imagePath = fileUtil.saveFile(coverImage, "games/covers");
                game.setCoverImage(imagePath);
            } catch (IOException e) {
                throw new BusinessException("封面图片上传失败");
            }
        }

        // 初始化评分和人气
        game.setRating(0.0);
        game.setRatingCount(0);
        game.setPopularity(0);

        return convertToDTO(gameRepository.save(game));
    }

    @Override
    @Transactional
    public void rateGame(Long gameId, Long userId, Double rating) {
        if (rating < 0 || rating > 5) {
            throw new BusinessException("评分必须在0-5之间");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("游戏不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        UserGame userGame = userGameRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new BusinessException("您还没有这款游戏"));

        // 更新用户评分
        userGame.setUserRating(rating);
        userGameRepository.save(userGame);

        // 更新游戏总评分
        updateGameRating(game);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportUserGames(Long userId) {
        List<UserGame> userGames = userGameRepository.findByUserId(userId);
        if (userGames.isEmpty()) {
            throw new BusinessException("没有找到游戏记录");
        }

        // 生成CSV格式的数据
        StringBuilder csv = new StringBuilder();
        csv.append("游戏名称,购买时间,游戏时长,最后游玩时间,评分\n");

        for (UserGame ug : userGames) {
            csv.append(String.format("%s,%s,%d,%s,%.1f\n",
                    ug.getGame().getTitle(),
                    ug.getPurchasedAt(),
                    ug.getPlayTime(),
                    ug.getLastPlayedAt(),
                    ug.getUserRating() != null ? ug.getUserRating() : 0.0
            ));
        }

        return csv.toString().getBytes();
    }

    @Override
    @Transactional
    public void addGameToUser(Long gameId, Long userId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("游戏不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (userGameRepository.existsByGameIdAndUserId(gameId, userId)) {
            throw new BusinessException("已拥有该游戏");
        }

        UserGame userGame = new UserGame();
        userGame.setGame(game);
        userGame.setUser(user);
        userGame.setPurchasedAt(LocalDateTime.now());
        userGame.setPlayTime(0);

        userGameRepository.save(userGame);

        // 更新游戏人气
        game.setPopularity(game.getPopularity() + 1);
        gameRepository.save(game);
    }

    @Override
    @Transactional
    public void updateGameScreenshots(Long gameId, List<MultipartFile> screenshots) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("游戏不存在"));

        List<String> screenshotPaths = new ArrayList<>();
        for (MultipartFile screenshot : screenshots) {
            try {
                String path = fileUtil.saveFile(screenshot, "games/screenshots");
                screenshotPaths.add(path);
            } catch (IOException e) {
                throw new BusinessException("截图上传失败");
            }
        }

        // 删除旧的截图
        if (game.getScreenshots() != null) {
            for (String oldPath : game.getScreenshots()) {
                fileUtil.deleteFile(oldPath);
            }
        }

        game.setScreenshots(new HashSet<>(screenshotPaths));
        gameRepository.save(game);
    }

    private void updateGameRating(Game game) {
        List<UserGame> userGames = userGameRepository.findByGameId(game.getId());
        OptionalDouble avgRating = userGames.stream()
                .filter(ug -> ug.getUserRating() != null)
                .mapToDouble(UserGame::getUserRating)
                .average();

        game.setRating(avgRating.orElse(0.0));
        game.setRatingCount((int) userGames.stream()
                .filter(ug -> ug.getUserRating() != null)
                .count());

        gameRepository.save(game);
    }

    private GameDTO convertToDTO(Game game) {
        GameDTO dto = new GameDTO();
        BeanUtils.copyProperties(game, dto);

        if (game.getCoverImage() != null) {
            dto.setCoverImage(fileUtil.getFileUrl(game.getCoverImage()));
        }

        if (game.getScreenshots() != null) {
            dto.setScreenshots(game.getScreenshots().stream()
                    .map(fileUtil::getFileUrl)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }
}