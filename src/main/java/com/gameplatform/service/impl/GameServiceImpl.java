package com.gameplatform.service.impl;

import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.GameDTO;
import com.gameplatform.model.dto.GameSearchDTO;
import com.gameplatform.model.entity.Game;
import com.gameplatform.model.entity.UserGame;
import com.gameplatform.model.entity.User;
import com.gameplatform.repository.GameRepository;
import com.gameplatform.repository.UserGameRepository;
import com.gameplatform.repository.UserRepository;
import com.gameplatform.service.GameService;
import com.gameplatform.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    private final CacheService cacheService;

    @Override
    @Transactional(readOnly = true)
    public GameDTO getGameById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new BusinessException("游戏不存在"));
        return convertToDTO(game);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameDTO> searchGames(GameSearchDTO searchDTO, Pageable pageable) {
        return gameRepository.findBySearchCriteria(
                searchDTO.getTitle(),
                searchDTO.getCategories(),
                searchDTO.getMinRating(),
                pageable
        ).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return gameRepository.findAllCategories();
    }

    @Override
    @Transactional
    public void rateGame(Long gameId, Long userId, Double rating) {
        if (rating < 0 || rating > 5) {
            throw new BusinessException("评分必须在0-5之间");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("游戏不存在"));

        UserGame userGame = userGameRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new BusinessException("您还没有这款游戏"));

        userGame.setUserRating(rating);
        userGameRepository.save(userGame);

        // 更新游戏总评分
        updateGameRating(game);

        // 清除缓存
        cacheService.evictCache("game:" + gameId);
    }

    @Override
    @Transactional
    public void updatePopularGames() {
        List<Game> games = gameRepository.findAll();
        for (Game game : games) {
            int popularity = calculateGamePopularity(game);
            game.setPopularity(popularity);
        }
        gameRepository.saveAll(games);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportUserGames(Long userId) {
        List<UserGame> userGames = userGameRepository.findByUserId(userId);
        if (userGames.isEmpty()) {
            throw new BusinessException("没有找到游戏记录");
        }

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
    @Transactional(readOnly = true)
    public Page<GameDTO> getUserGames(Long userId, Pageable pageable) {
        return userGameRepository.findByUserId(userId, pageable)
                .map(userGame -> convertToDTO(userGame.getGame()));
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
        userGameRepository.save(userGame);

        // 更新游戏人气值
        updateGamePopularity(game);
    }

    @Override
    @Transactional
    public void removeGameFromUser(Long gameId, Long userId) {
        UserGame userGame = userGameRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new BusinessException("未拥有该游戏"));

        userGameRepository.delete(userGame);

        // 更新游戏人气值
        updateGamePopularity(userGame.getGame());
    }

    private void updateGameRating(Game game) {
        List<UserGame> userGames = userGameRepository.findByGameId(game.getId());
        double avgRating = userGames.stream()
                .filter(ug -> ug.getUserRating() != null)
                .mapToDouble(UserGame::getUserRating)
                .average()
                .orElse(0.0);

        game.setRating(avgRating);
        game.setRatingCount((int) userGames.stream()
                .filter(ug -> ug.getUserRating() != null)
                .count());

        gameRepository.save(game);
    }

    private void updateGamePopularity(Game game) {
        int popularity = calculateGamePopularity(game);
        game.setPopularity(popularity);
        gameRepository.save(game);
    }

    private int calculateGamePopularity(Game game) {
        // 计算游戏人气值的逻辑
        long userCount = userGameRepository.countByGameId(game.getId());
        double avgRating = game.getRating();
        long ratingCount = game.getRatingCount();

        // 简单的人气计算公式：用户数 * (1 + 平均评分/5) * (1 + 评分数/100)
        return (int) (userCount * (1 + avgRating/5) * (1 + ratingCount/100.0));
    }

    private GameDTO convertToDTO(Game game) {
        GameDTO dto = new GameDTO();
        BeanUtils.copyProperties(game, dto);
        return dto;
    }
}