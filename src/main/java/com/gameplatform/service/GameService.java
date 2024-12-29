package com.gameplatform.service;import com.gameplatform.model.dto.GameDTO;
import com.gameplatform.model.dto.GameSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:11
 * @description TODO
 */
public interface GameService {
    GameDTO getGameById(Long id);

    Page<GameDTO> searchGames(GameSearchDTO searchDTO, Pageable pageable);

    List<String> getAllCategories();

    void rateGame(Long gameId, Long userId, Double rating);

    byte[] exportUserGames(Long userId);

    Page<GameDTO> getUserGames(Long userId, Pageable pageable);

    void addGameToUser(Long gameId, Long userId);

    void removeGameFromUser(Long gameId, Long userId);
}
