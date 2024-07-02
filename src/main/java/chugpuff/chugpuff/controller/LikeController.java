package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/like")
public class LikeController {
    @Autowired
    private LikeService likeService;


    @PostMapping("/{boardNo}")
    public void toggleLike(@PathVariable int boardNo, @RequestParam String userId) {
        likeService.toggleLike(boardNo, userId);
    }
}
