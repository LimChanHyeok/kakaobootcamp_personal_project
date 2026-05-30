package org.example.community.postlike.service;

import lombok.RequiredArgsConstructor;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;
import org.example.community.post.repository.PostRepository;
import org.example.community.postlike.dto.response.PostLikeResponse;
import org.example.community.postlike.repository.PostLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    /**
     * 게시글 존재 여부를 확인, 사용자가 이미 좋아요를 눌렀는지 확인
     * 좋아요를 안눌렀으면 post_like에 insert후 post에 like_count +1
     * 현재 like_count 조회후 응답
     */
    @Transactional
    public PostLikeResponse likePost(Long postId, Long loginUserId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        boolean alreadyLiked = postLikeRepository.existsByPostIdAndUserId(postId, loginUserId);

        /**
         * 좋아요를 누르지 않았을 때 해야됨
         */
        if (!alreadyLiked) {
            postLikeRepository.save(postId, loginUserId);
            postRepository.increaseLikeCount(postId);
        }

        int likeCount = postRepository.findLikeCountByPostId(postId);

        return new PostLikeResponse(
                postId,
                true,
                likeCount
        );
    }

    @Transactional
    public PostLikeResponse unlikePost(Long postId, Long loginUserId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        boolean alreadyLiked = postLikeRepository.existsByPostIdAndUserId(postId, loginUserId);

        /**
         * 이미 좋아요가 눌러져있을때만 해야됨
         */
        if (alreadyLiked) {
            postLikeRepository.delete(postId, loginUserId);
            postRepository.decreaseLikeCount(postId);
        }

        int likeCount = postRepository.findLikeCountByPostId(postId);

        return new PostLikeResponse(
                postId,
                false,
                likeCount
        );
    }
}