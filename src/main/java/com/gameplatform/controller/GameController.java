package com.gameplatform.controller;import com.gameplatform.model.dto.GameDTO;
import com.gameplatform.model.dto.GameSearchDTO;
import com.gameplatform.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:08
 * @description TODO
 */
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;

    @GetMapping
    public ResponseEntity<Page<GameDTO>> searchGames(GameSearchDTO searchDTO, Pageable pageable) {
        return ResponseEntity.ok(gameService.searchGames(searchDTO, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameDTO> getGame(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getGameById(id));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(gameService.getAllCategories());
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<?> rateGame(
            @PathVariable Long id,
            @RequestParam Double rating,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 假设UserDetails中包含用户ID
        Long userId = Long.parseLong(userDetails.getUsername());
        gameService.rateGame(id, userId, rating);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportGames(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        byte[] content = gameService.exportUserGames(userId);
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=\"games.csv\"")
                .body(content);
    }
}