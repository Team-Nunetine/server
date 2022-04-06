package server.sookdak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.sookdak.domain.Board;
import server.sookdak.domain.Post;
import server.sookdak.domain.PostImage;
import server.sookdak.domain.User;
import server.sookdak.dto.req.PostSaveRequestDto;
import server.sookdak.dto.res.PostListResponseDto;
import server.sookdak.exception.CustomException;
import server.sookdak.repository.BoardRepository;
import server.sookdak.repository.PostImageRepository;
import server.sookdak.repository.PostRepository;
import server.sookdak.repository.UserRepository;
import server.sookdak.util.SecurityUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static server.sookdak.constants.ExceptionCode.*;
import static server.sookdak.dto.res.PostListResponseDto.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public void savePost(PostSaveRequestDto postSaveRequestDto, Long boardId, List<String> imageURLs) {
        String userEmail = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(BOARD_NOT_FOUND));

        Post post = Post.createPost(user, board, postSaveRequestDto.getContent(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));

        for (String imageURL : imageURLs) {
            PostImage postImage = PostImage.createPostImage(post, imageURL);
            postImageRepository.save(postImage);
        }

        postRepository.save(post);
    }

    public PostListResponseDto getPostList(Long boardId, String order) {
        String userEmail = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(BOARD_NOT_FOUND));

        List<PostList> posts = new ArrayList<>();
        if (order.equals("latest")) {
            posts = postRepository.findAllByBoardOrderByCreatedAtDesc(board).stream()
                    .map(post -> new PostList(post, postImageRepository.existsByPost(post)))
                    .collect(Collectors.toList());
        } else if (order.equals("popularity")) {
            posts = postRepository.findAllByBoardOrderByLikedDescCreatedAtDesc(board).stream()
                    .map(post -> new PostList(post, postImageRepository.existsByPost(post)))
                    .collect(Collectors.toList());
        } else {
            throw new CustomException(WRONG_TYPE_ORDER);
        }

        return PostListResponseDto.of(posts);
    }
}
