package com.evolua.social.domain; import java.util.List; public interface PostRepository { Post save(Post item); List<Post> findAllByUserId(String userId); }
