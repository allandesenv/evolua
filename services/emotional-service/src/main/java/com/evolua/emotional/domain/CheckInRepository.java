package com.evolua.emotional.domain; import java.util.List; public interface CheckInRepository { CheckIn save(CheckIn item); List<CheckIn> findAllByUserId(String userId); }
