package com.evolua.content.domain; import java.util.List; public interface TrailRepository { Trail save(Trail item); List<Trail> findAllByUserId(String userId); }
