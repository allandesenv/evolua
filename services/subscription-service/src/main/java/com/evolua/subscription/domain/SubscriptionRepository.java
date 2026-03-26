package com.evolua.subscription.domain; import java.util.List; public interface SubscriptionRepository { Subscription save(Subscription item); List<Subscription> findAllByUserId(String userId); }
