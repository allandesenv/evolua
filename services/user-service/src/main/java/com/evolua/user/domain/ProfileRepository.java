package com.evolua.user.domain; import java.util.List; public interface ProfileRepository { Profile save(Profile item); List<Profile> findAllByUserId(String userId); }
